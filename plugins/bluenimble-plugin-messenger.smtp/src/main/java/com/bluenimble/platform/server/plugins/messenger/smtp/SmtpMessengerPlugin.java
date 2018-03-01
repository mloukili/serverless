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
package com.bluenimble.platform.server.plugins.messenger.smtp;

import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;

import com.bluenimble.platform.Feature;
import com.bluenimble.platform.Json;
import com.bluenimble.platform.Lang;
import com.bluenimble.platform.Recyclable;
import com.bluenimble.platform.api.ApiSpace;
import com.bluenimble.platform.json.JsonArray;
import com.bluenimble.platform.json.JsonObject;
import com.bluenimble.platform.messaging.Messenger;
import com.bluenimble.platform.messenger.impls.smtp.SmtpMessenger;
import com.bluenimble.platform.plugins.Plugin;
import com.bluenimble.platform.plugins.PluginRegistryException;
import com.bluenimble.platform.plugins.impls.AbstractPlugin;
import com.bluenimble.platform.server.ApiServer;
import com.bluenimble.platform.server.ApiServer.Event;
import com.bluenimble.platform.server.ServerFeature;

public class SmtpMessengerPlugin extends AbstractPlugin {

	private static final long serialVersionUID = 3203657740159783537L;
	
	private static final String Provider = "bnb-smtp";

	interface Spec {
		String Server 	= "server";
		
		String Auth 		= "auth";
			String User 	= "user";
			String Password = "password";
	}
	
	private JsonArray mimeTypes;
	
	private String 		feature;
	
	@Override
	public void init (final ApiServer server) throws Exception {
		
		Feature aFeature = Messenger.class.getAnnotation (Feature.class);
		if (aFeature == null || Lang.isNullOrEmpty (aFeature.name ())) {
			return;
		}
		feature = aFeature.name ();
		
		server.addFeature (new ServerFeature () {
			private static final long serialVersionUID = 3585173809402444745L;
			@Override
			public Class<?> type () {
				return Messenger.class;
			}
			@Override
			public Object get (ApiSpace space, String name) {
				return ((RecyclableMessenger)(space.getRecyclable (createKey (name)))).messenger ();
			}
			@Override
			public String provider () {
				return Provider;
			}
			@Override
			public Plugin implementor () {
				return SmtpMessengerPlugin.this;
			}
		});
	}

	@Override
	public void onEvent (Event event, Object target) throws PluginRegistryException {
		if (!ApiSpace.class.isAssignableFrom (target.getClass ())) {
			return;
		}
		
		switch (event) {
			case Create:
				createSessions ((ApiSpace)target);
				break;
			case AddFeature:
				// if it's Messenger and provider is 'smtp' create createSession
				createSessions ((ApiSpace)target);
				break;
			case DeleteFeature:
				// if it's Messenger and provider is 'smtp' shutdown session
				dropSessions ((ApiSpace)target);
				break;
			default:
				break;
		}
	}
	
	@SuppressWarnings("unchecked")
	private void createSessions (ApiSpace space) {
		// create sessions
		JsonObject msgFeature = Json.getObject (space.getFeatures (), feature);
		if (msgFeature == null || msgFeature.isEmpty ()) {
			return;
		}
		
		Iterator<String> keys = msgFeature.keys ();
		while (keys.hasNext ()) {
			String key = keys.next ();
			
			JsonObject feature = Json.getObject (msgFeature, key);
			
			if (!Provider.equalsIgnoreCase (Json.getString (feature, ApiSpace.Features.Provider))) {
				continue;
			}
			
			String sessionKey = createKey (key);
			if (space.containsRecyclable (sessionKey)) {
				continue;
			}
			
			JsonObject spec = Json.getObject (feature, ApiSpace.Features.Spec);
		
			if (mimeTypes != null && mimeTypes.count () > 0) {
				MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap (); 
				for (int i = 0; i < mimeTypes.count (); i++) {
					mc.addMailcap ((String)mimeTypes.get (i)); 
				}
			}
			
			JsonObject oServer = Json.getObject (spec, Spec.Server);
			if (oServer == null || oServer.isEmpty ()) {
				continue;
			}
			
			final JsonObject oAuth = Json.getObject (spec, Spec.Auth);
			if (oAuth == null || oAuth.isEmpty ()) {
				continue;
			}
			
			Properties props = new Properties ();
			props.putAll (oServer);
			
			final String user = Json.getString (oAuth, Spec.User);
			
			final Session session = Session.getInstance (
				props,
				new Authenticator () {
					protected PasswordAuthentication getPasswordAuthentication () {
						return new PasswordAuthentication (user, Json.getString (oAuth, Spec.Password));
					}
				}
			);
			
			space.addRecyclable (sessionKey, new RecyclableMessenger (new SmtpMessenger (user, session)));
		}
		
	}
	
	private void dropSessions (ApiSpace space) {
		
		JsonObject msgFeature = Json.getObject (space.getFeatures (), feature);
		
		Set<String> recyclables = space.getRecyclables ();
		for (String r : recyclables) {
			if (!r.startsWith (feature + Lang.DOT)) {
				continue;
			}
			String name = r.substring ((feature + Lang.DOT).length ());
			if (msgFeature == null || msgFeature.containsKey (name)) {
				// it's deleted
				RecyclableMessenger rm = (RecyclableMessenger)space.getRecyclable (r);
				// remove from recyclables
				space.removeRecyclable (r);
				// recycle
				rm.recycle ();
			}
		}
	}
	
	private String createKey (String name) {
		return feature + Lang.DOT + name;
	}

	public JsonArray getMimeTypes () {
		return mimeTypes;
	}
	public void setMimeTypes (JsonArray mimeTypes) {
		this.mimeTypes = mimeTypes;
	}
	
	class RecyclableMessenger implements Recyclable {
		private static final long serialVersionUID = 50882416501226306L;

		private SmtpMessenger messenger;
		
		public RecyclableMessenger (SmtpMessenger messenger) {
			this.messenger = messenger;
		}
		
		@Override
		public void recycle () {
			// nothing
		}

		public SmtpMessenger messenger () {
			return (SmtpMessenger) get ();
		}

		@Override
		public Object get () {
			return messenger;
		}

		@Override
		public void set (ApiSpace space, ClassLoader classLoader, Object... args) {
			
		}
		
	}
	
}