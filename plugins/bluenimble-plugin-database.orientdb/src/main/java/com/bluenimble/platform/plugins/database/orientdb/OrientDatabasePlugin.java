/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bluenimble.platform.plugins.database.orientdb;

import java.util.Iterator;
import java.util.Set;

import com.bluenimble.platform.Feature;
import com.bluenimble.platform.Json;
import com.bluenimble.platform.Lang;
import com.bluenimble.platform.Recyclable;
import com.bluenimble.platform.api.ApiSpace;
import com.bluenimble.platform.api.tracing.Tracer;
import com.bluenimble.platform.db.Database;
import com.bluenimble.platform.json.JsonObject;
import com.bluenimble.platform.plugins.Plugin;
import com.bluenimble.platform.plugins.PluginRegistryException;
import com.bluenimble.platform.plugins.database.orientdb.impls.OrientDatabase;
import com.bluenimble.platform.plugins.impls.AbstractPlugin;
import com.bluenimble.platform.server.ApiServer;
import com.bluenimble.platform.server.ApiServer.Event;
import com.bluenimble.platform.server.ServerFeature;
import com.orientechnologies.orient.core.Orient;
import com.orientechnologies.orient.core.db.OPartitionedDatabasePool;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;

public class OrientDatabasePlugin extends AbstractPlugin {

	private static final long serialVersionUID 		= -6219529665471192558L;
	
	private static final String 	Provider 		= "bnb-db";
	
	interface Spec {
		String Host 	= "host";
		String Port 	= "port";
		String Database = "database";
		
		String User 	= "user";
		String Password = "password";
	}
	
	interface Protocol {
		String Remote	= "remote:";
		String Local 	= "plocal:";
	}
	
	private String				feature;
	
	private int 				weight;
	
	@Override
	public void init (final ApiServer server) throws Exception {
		
		weight = server.weight ();
		
		Feature aFeature = Database.class.getAnnotation (Feature.class);
		if (aFeature == null || Lang.isNullOrEmpty (aFeature.name ())) {
			return;
		}
		feature = aFeature.name ();
		
		// add features
		server.addFeature (new ServerFeature () {
			private static final long serialVersionUID = 2626039344401539390L;
			@Override
			public Class<?> type () {
				return Database.class;
			}
			@Override
			public Object get (ApiSpace space, String name) {
				return new OrientDatabase (OrientDatabasePlugin.this.acquire (space, name), tracer ());
				
			}
			@Override
			public Plugin implementor () {
				return OrientDatabasePlugin.this;
			}
			@Override
			public String provider () {
				return Provider;
			}
		});
		
		if (Orient.instance () != null) {
			Orient.instance ().removeShutdownHook ();
		}
		
	}
	
	@Override
	public void onEvent (Event event, Object target) throws PluginRegistryException {
		if (!ApiSpace.class.isAssignableFrom (target.getClass ())) {
			return;
		}
		
		tracer ().log (Tracer.Level.Info, "onEvent {0}, target {1}", event, target.getClass ().getSimpleName ());
		
		ApiSpace space = (ApiSpace)target;
		
		switch (event) {
			case Create:
				createPools (space);
				break;
			case AddFeature:
				// if it's database and provider is 'platform or orientdb' create factory
				createPools (space);
				break;
			case DeleteFeature:
				// if it's database and provider is 'platform or orientdb' stop factory
				dropPools (space);
				break;
			default:
				break;
		}
	}
	
	private void createPools (ApiSpace space) {
		
		// create factories
		JsonObject dbFeature = Json.getObject (space.getFeatures (), feature);
		if (dbFeature == null || dbFeature.isEmpty ()) {
			return;
		}
		
		Iterator<String> keys = dbFeature.keys ();
		while (keys.hasNext ()) {
			String key = keys.next ();
			JsonObject source = Json.getObject (dbFeature, key);
			
			if (!Provider.equalsIgnoreCase (Json.getString (source, ApiSpace.Features.Provider))) {
				continue;
			}
			
			JsonObject spec = Json.getObject (source, ApiSpace.Features.Spec);
			
			if (spec == null) {
				continue;
			}
			
			createPool (key, space, spec);
		}
	}
	
	private void dropPools (ApiSpace space) {
		
		JsonObject dbFeature = Json.getObject (space.getFeatures (), feature);
		
		Set<String> recyclables = space.getRecyclables ();
		for (String r : recyclables) {
			if (!r.startsWith (feature + Lang.DOT)) {
				continue;
			}
			String name = r.substring ((feature + Lang.DOT).length ());
			if (dbFeature == null || dbFeature.containsKey (name)) {
				// it's deleted
				RecyclablePool rf = (RecyclablePool)space.getRecyclable (r);
				// remove from recyclables
				space.removeRecyclable (r);
				// recycle
				rf.recycle ();
			}
		}
		
	}
	
	private OPartitionedDatabasePool createPool (String name, ApiSpace space, JsonObject spec) {
		
		String factoryKey = createFactoryKey  (name, space);
		
		if (space.containsRecyclable (factoryKey)) {
			return null;
		}
		
		OPartitionedDatabasePool pool = new OPartitionedDatabasePool (
			createUrl (spec), 
			Json.getString (spec, Spec.User), 
			Json.getString (spec, Spec.Password),
			weight, 
			weight
		);
		
		space.addRecyclable (factoryKey, new RecyclablePool (pool));
		
		return pool;
		
	}
	
	private String createFactoryKey (String name, ApiSpace space) {
		return feature + Lang.DOT + space.getNamespace () + Lang.DOT + name;
	}

	private String createUrl (JsonObject database) {
		return Protocol.Remote + Json.getString (database, Spec.Host) + Lang.COLON + Json.getInteger (database, Spec.Port, 2424) + Lang.SLASH + Json.getString (database, Spec.Database);
	}
	
	public ODatabaseDocumentTx acquire (ApiSpace space, String name) {
		return ((RecyclablePool)space.getRecyclable (createFactoryKey (name, space))).pool ().acquire ();
	}
	
	class RecyclablePool implements Recyclable {
		private static final long serialVersionUID = 50882416501226306L;

		private OPartitionedDatabasePool pool;
		
		public RecyclablePool (OPartitionedDatabasePool pool) {
			this.pool = pool;
		}
		
		@Override
		public void recycle () {
			try {
				pool.close ();
			} catch (Exception ex) {
				// Ignore
			}
		}

		public OPartitionedDatabasePool pool () {
			return (OPartitionedDatabasePool)get ();
		}

		@Override
		public Object get () {
			return pool;
		}

		@Override
		public void set (ApiSpace arg0, ClassLoader arg1, Object... arg2) {
			
		}
		
	}
}