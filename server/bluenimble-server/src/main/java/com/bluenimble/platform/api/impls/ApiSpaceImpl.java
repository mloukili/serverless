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
package com.bluenimble.platform.api.impls;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.Thread.State;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.bluenimble.platform.ArchiveUtils;
import com.bluenimble.platform.Feature;
import com.bluenimble.platform.FileUtils;
import com.bluenimble.platform.IOUtils;
import com.bluenimble.platform.Json;
import com.bluenimble.platform.Lang;
import com.bluenimble.platform.Recyclable;
import com.bluenimble.platform.api.Api;
import com.bluenimble.platform.api.ApiAccessDeniedException;
import com.bluenimble.platform.api.ApiAsyncExecutionException;
import com.bluenimble.platform.api.ApiContext;
import com.bluenimble.platform.api.ApiManagementException;
import com.bluenimble.platform.api.ApiRequest;
import com.bluenimble.platform.api.ApiRequest.Scope;
import com.bluenimble.platform.api.ApiService;
import com.bluenimble.platform.api.ApiSpace;
import com.bluenimble.platform.api.ApiStatus;
import com.bluenimble.platform.api.ApiStreamSource;
import com.bluenimble.platform.api.DescribeOption;
import com.bluenimble.platform.api.security.ApiConsumer;
import com.bluenimble.platform.api.security.ApiRequestSignerException;
import com.bluenimble.platform.api.tracing.Tracer;
import com.bluenimble.platform.api.tracing.impls.NoTracing;
import com.bluenimble.platform.json.JsonArray;
import com.bluenimble.platform.json.JsonObject;
import com.bluenimble.platform.plugins.PluginRegistryException;
import com.bluenimble.platform.reflect.BeanUtils;
import com.bluenimble.platform.scripting.ScriptingEngine;
import com.bluenimble.platform.security.KeyPair;
import com.bluenimble.platform.security.SpaceKeyStore;
import com.bluenimble.platform.server.ApiServer;
import com.bluenimble.platform.server.ApiServer.Event;
import com.bluenimble.platform.server.FeatureNotFoundException;
import com.bluenimble.platform.server.StatusManager;
import com.bluenimble.platform.server.impls.fs.FileSystemApiServer;
import com.bluenimble.platform.server.utils.ConfigKeys;
import com.bluenimble.platform.server.utils.DescribeUtils;

public class ApiSpaceImpl extends AbstractApiSpace {

	private static final long serialVersionUID = 7811697228373510259L;
	
	public interface Spaces {
		String Sys 	= "sys";	 
	}
	
	interface Describe {
		String Requests 	= "requests";
		interface Worker {
			String Id 	= "id";
			String Name 	= "name";
			String Status	= "status";
			interface request {
				String Spi		= "spi";
				String Script 	= "script";
			}
			String Service	= "service";
		}
		String Status 		= "status";
		
		String Running 		= "running";	
		String Completed 	= "completed";	
		String Scheduled 	= "scheduled";	
	}
	
	private static final String StateAvailable = "available";
	
	FileSystemApiServer 	server;
	
	protected Map<String, Api> 		apis = new ConcurrentHashMap<String, Api> ();

	private Map<String, Recyclable> recyclables = new ConcurrentHashMap<String, Recyclable> ();
	
	private ScriptingEngine 		scriptingEngine;
	
	private ThreadGroup 			group;
	private ThreadPoolExecutor 		executor;
	
	StatusManager 					statusManager;
	
	private Tracer 					tracer 		= NoTracing.Instance;

	public ApiSpaceImpl (FileSystemApiServer server, JsonObject descriptor, File home) throws Exception {
		this.server 		= server;
		this.descriptor 	= descriptor;
		this.home 			= home;
		
		start ();
	}
	
	@Override
	public String getNamespace () {
		return Json.getString (descriptor, Spec.Namespace);
	}

	@Override
	public JsonObject instance (DescribeOption... opts) throws ApiAccessDeniedException {
		if (!Spaces.Sys.equals (getNamespace ())) {
			throw new ApiAccessDeniedException ("access denied");
		}
		return server.describe (opts);
	}

	@Override
	public KeyPair getRootKeys () throws ApiAccessDeniedException {
		if (!Spaces.Sys.equals (getNamespace ())) {
			throw new ApiAccessDeniedException ("access denied");
		}
		return server.getKeys ();
	}
/*	
	@Override
	public void update (String apiNs, JsonObject descriptor) throws ApiManagementException {
		ApiImpl api = (ApiImpl)api (apiNs);
		if (api == null) {
			throw new ApiManagementException ("Api " + apiNs + " not found");
		}
		
		// update descriptor
		JsonObject oldDescriptor = api.getDescriptor ();
		oldDescriptor.set (Api.Spec.Name, Json.getString (descriptor, Api.Spec.Name, api.getName ()));
		oldDescriptor.set (Api.Spec.Description, Json.getString (descriptor, Api.Spec.Description, api.getDescription ()));
		
		JsonObject oLogging = Json.getObject (descriptor, Api.Spec.Logging);
		if (oLogging != null) {
			oldDescriptor.set (Api.Spec.Logging, oLogging);
			api.createtracer ();
		}
		
		JsonObject oFeatures = Json.getObject (descriptor, Api.Spec.Features);
		if (oFeatures != null) {
			oldDescriptor.set (Api.Spec.Features, oFeatures);
		}
		
		// write descriptor
		api.updateDescriptor ();
		
		// remove old
		remove (apiNs);
		
		// add new
		register (api);		
	}
*/
	@Override
	public Api install (String spaceFolder, String apiFile) throws ApiManagementException {
		
		String externalSpacesFolder = Json.getString (server.getDescriptor (), ConfigKeys.Spaces);
		if (Lang.isNullOrEmpty (externalSpacesFolder)) {
			throw new ApiManagementException ("Node doesn't support external spaces");
		}
		
		File externalSpaces = new File (externalSpacesFolder);
		if (!externalSpaces.exists () || !externalSpaces.isDirectory ()) {
			throw new ApiManagementException ("External spaces folder not found");
		}
		
		File fSpaceFolder = new File (externalSpaces, spaceFolder);
		if (!fSpaceFolder.exists () || !fSpaceFolder.isDirectory ()) {
			throw new ApiManagementException ("Space folder " + spaceFolder + " not found");
		}
		
		File fApiFile = new File (fSpaceFolder, apiFile);
		if (!fApiFile.exists () || !fApiFile.isFile ()) {
			throw new ApiManagementException ("Api file " + apiFile + " not found");
		}
		
		ApiFileStreamSource is = new ApiFileStreamSource (fApiFile, ConfigKeys.ApiExt);
		try {
			return _install (is);
		} finally {
			IOUtils.closeQuietly (is.stream ());
		}
		
	}
	
	@Override
	public Api install (ApiStreamSource source /*, JsonObject descriptor */) throws ApiManagementException {
		
		boolean started = isStarted ();
		
		// if the space is new, start it before 
		if (!started) {
			
			// start executor (this is important is the space just created since it will be down)
			try {
				started = start ();
			} catch (Exception e) {
				throw new ApiManagementException (e.getMessage (), e);
			}
			
			// notify space creation if it's new
			if (started) {
				try {
					server.getPluginsRegistry ().onEvent (Event.Create, this);
				} catch (Exception ex) {
					throw new ApiManagementException (ex.getMessage (), ex);
				} 
			}
		}
		
		if (!started) {
			throw new ApiManagementException ("Can't install Api, couldn't start owner space");
		}
		
		return _install (source);
	}
	
	public Api _install (ApiStreamSource source) throws ApiManagementException {
		if (source == null) {
			throw new ApiManagementException ("missing api payload");
		}
		
		InputStream stream = source.stream ();
		if (stream == null) {
			throw new ApiManagementException ("missing api stream");
		}
		
		String name = source.name ();
		if (Lang.isNullOrEmpty (name)) {
			name = getNamespace () + Lang.UNDERSCORE + Lang.UUID (40);
		}
		
		tracer.log (Tracer.Level.Info, "install api {0} / {1}", getNamespace (), source.name ());
		
		File apiHome = new File (home, name);
		try {
			ArchiveUtils.decompress (stream, apiHome);
		} catch (Exception e) {
			throw new ApiManagementException (e.getMessage (), e);
		} finally {
			IOUtils.closeQuietly (stream);
		}
		
		Api api = install (apiHome);
		
		return api;
	}

	/*
	@Override
	public Api install (JsonObject descriptor) throws ApiManagementException {
		if (Lang.isNullOrEmpty (getNamespace ())) {
			throw new ApiManagementException (
				"Api " + Api.Spec.Namespace + " not found in descriptor "
			);
		}
		// create api home
		File apiHome = new File (home, getNamespace () + Lang.UNDERSCORE + Lang.UUID (40));
		
		return install (apiHome, descriptor);
	}
	*/
	
	@Override
	public void uninstall (String apiNs) throws ApiManagementException {
		if (Lang.isNullOrEmpty (apiNs)) {
			throw new ApiManagementException ("api namespace is null or empty");
		}
		ApiImpl api = (ApiImpl)api (apiNs.trim ());
		if (api == null) {
			throw new ApiManagementException ("api '" + apiNs + "' not found");
		}

		// notify before uninstall 
		try {
			server.getPluginsRegistry ().onEvent (Event.Uninstall, api);
		} catch (PluginRegistryException e) {
			throw new ApiManagementException (e.getMessage (), e);
		}
		
		// stop api
		if (!api.status ().equals (ApiStatus.Stopped)) {
			api.stop (false);
		}
		
		// remove status
		statusManager.delete (api);
		
		// clear memory
		api.clear ();
		
		try {
			FileUtils.delete (((ApiImpl)api).getHome ());
		} catch (IOException e) {
			throw new ApiManagementException (e.getMessage (), e);
		}
		
		remove (apiNs);
		
		tracer.log (Tracer.Level.Info, "\tApi [{0}] uninstalled", api.getNamespace ());
	}
	
	@Override
	public void pause (String apiNs) throws ApiManagementException {
		ApiImpl api = (ApiImpl)api (apiNs);
		if (!api.status ().equals (ApiStatus.Running)) {
			throw new ApiManagementException ("can't pause api " + apiNs + ". Status=" + api.status ());
		}
		api.setStatus (ApiStatus.Paused, true);
	}

	@Override
	public void resume (String apiNs) throws ApiManagementException {
		ApiImpl api = (ApiImpl)api (apiNs);
		if (!api.status ().equals (ApiStatus.Paused)) {
			throw new ApiManagementException ("can't resume api " + apiNs + ". Status=" + api.status ());
		}
		// update status
		api.setStatus (ApiStatus.Running, true);
	}

	@Override
	public void start (String apiNs) throws ApiManagementException {
		ApiImpl api = (ApiImpl)api (apiNs);
		if (api == null) {
			throw new ApiManagementException ("api " + apiNs + " not found");
		}		
		if (ApiStatus.Failed.equals (api.status ())) {
			throw new ApiManagementException ("can't start api " + apiNs + ". Status=" + api.status ());
		}
		if (ApiStatus.Running.equals (api.status ())) {
			return;
		}
		if (ApiStatus.Paused.equals (api.status ())) {
			api.setStatus (ApiStatus.Running, true);
			return;
		}

		// start api
		api.start (false);

		// notify plugins
		try {
			server.getPluginsRegistry ().onEvent (Event.Start, api);
		} catch (PluginRegistryException e) {
			throw new ApiManagementException (e.getMessage (), e);
		}
		
	}

	@Override
	public void stop (String apiNs) throws ApiManagementException {
		ApiImpl api = (ApiImpl)api (apiNs);
		if (api == null) {
			throw new ApiManagementException ("api " + apiNs + " not found");
		}		
		if (!ApiStatus.Running.equals (api.status ())) {
			throw new ApiManagementException ("can't stop api " + apiNs + ". Status=" + api.status ());
		}
		
		// notify plugins
		try {
			server.getPluginsRegistry ().onEvent (Event.Stop, api);
		} catch (PluginRegistryException e) {
			throw new ApiManagementException (e.getMessage (), e);
		}

		// stop api
		api.stop (true);

	}

	@Override
	public Api api (String api) {
		return apis.get (api);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T feature (Class<T> t, String name, ApiContext context) {
		
		if (context == null) {
			throw new FeatureNotFoundException ("context not available");
		}
		
		if (Lang.isNullOrEmpty (name)) {
			throw new FeatureNotFoundException ("provider can't be null or empty");
		}
		
		Feature aFeature = t.getAnnotation (Feature.class);
		if (aFeature == null || Lang.isNullOrEmpty (aFeature.name ())) {
			throw new FeatureNotFoundException ("feature " + t.getSimpleName () + " not registered in this instance");
		} 
		
		String recyclableKey = contextRecyclableKey (aFeature.name (), name);

		Recyclable feature = context.getRecyclable (recyclableKey);
		if (feature != null) {
			return (T)feature;
		}
		
		T f = (T)server.getFeature (this, t, name);
		
		if (f == null) {
			throw new FeatureNotFoundException ("feature " + name + " not found");
		}
		
		if (Recyclable.class.isAssignableFrom (f.getClass ())) {
			context.addRecyclable (recyclableKey, (Recyclable)f);
		}
		
		return f;		
	}

	@Override
	public ApiRequest request (ApiRequest parentRequest, ApiConsumer consumer, Endpoint endpoint) {
		
		ContainerApiRequest 	request 	= new ContainerApiRequest (
			parentRequest, 
			parentRequest.getSpace () + Lang.COLON + parentRequest.getApi () + Lang.SLASH + parentRequest.getService ().getName (), 
			endpoint
		);
		
		request.set (ContainerApiRequest.Consumer, consumer, Scope.Parameter);
		request.set (ContainerApiRequest.Caller, getNamespace (), Scope.Parameter);
		
		return request;
	}

	@Override
	public String sign (ApiRequest request, String utcTimestamp, String accessKey,
			String secretKey, boolean writeToRequest) throws ApiRequestSignerException {
		return server.getRequestSigner ().sign (request, utcTimestamp, accessKey, secretKey, writeToRequest);
	}
	
	@Override
	public Collection<ApiSpace> spaces () throws ApiAccessDeniedException {
		if (!Spaces.Sys.equals (getNamespace ())) {
			throw new ApiAccessDeniedException ("access denied");
		}
		return server.spaces ();
	}
	
	@Override
	public ApiSpace space (String spaceNs) throws ApiAccessDeniedException {
		if (!Spaces.Sys.equals (getNamespace ())) {
			throw new ApiAccessDeniedException ("access denied");
		}
		return server.space (spaceNs);
	}
	
	@Override
	public void refresh (JsonObject descriptor) throws ApiManagementException {
		if (Json.isNullOrEmpty (descriptor)) {
			return;
		}
		
		String spaceNs = Json.getString (descriptor, ApiSpace.Spec.Namespace);
		if (Lang.isNullOrEmpty (spaceNs)) {
			return;
		}
		
		try {
			((ApiSpaceImpl)space (spaceNs))._refresh (descriptor);
		} catch (Exception e) {
			throw new ApiManagementException (e.getMessage (), e);
		}
	}
	
	@Override
	public boolean isBlocked () {
		return Json.getBoolean (descriptor, ApiSpace.Spec.Blocked, false);
	}

	@Override
	public boolean isStarted () {
		return executor != null;
	}

	@Override
	public ApiSpace create (JsonObject oSpace) throws ApiManagementException {
		if (!Spaces.Sys.equals (getNamespace ())) {
			throw new ApiManagementException ("access denied");
		}
		return server.create (oSpace);
	}

	@Override
	public void list (Selector selector) {
		Iterator<Api> ip = apis.values ().iterator ();
		while (ip.hasNext ()) {
			boolean cancel = selector.select (ip.next ());
			if (cancel) {
				return;
			}
		}
	}
	
	@Override
	public String getName () {
		return Json.getString (descriptor, getNamespace ());
	}

	@Override
	public String getDescription () {
		return Json.getString (descriptor, Spec.Description);
	}

	@Override
	public JsonObject getSecrets (String name) throws ApiManagementException {
		return Json.getObject (Json.getObject (descriptor, Spec.secrets.class.getSimpleName ()), name);
	}

	@Override
	public JsonArray keys () {
		return Json.getArray (descriptor, Spec.Keys);
	}

	@Override
	public void addSecrets (String name, JsonObject secrets) throws ApiManagementException {
		JsonObject allSecrets = Json.getObject (descriptor, Spec.secrets.class.getSimpleName ());
		if (allSecrets == null) {
			allSecrets = new JsonObject ();
		}
		allSecrets.set (name, secrets);
		try {
			saveDescriptor ();
		} catch (IOException e) {
			throw new ApiManagementException (e.getMessage (), e);
		}
	}

	@Override
	public void deleteSecrets (String name) throws ApiManagementException {
		JsonObject allSecrets = Json.getObject (descriptor, Spec.secrets.class.getSimpleName ());
		if (allSecrets == null) {
			allSecrets = new JsonObject ();
		}
		allSecrets.remove (name);
		try {
			saveDescriptor ();
		} catch (IOException e) {
			throw new ApiManagementException (e.getMessage (), e);
		}
	}

	@Override
	public JsonObject getFeatures () {
		return Json.getObject (descriptor, Spec.Features);
	}
	
	@Override
	public Object getRuntime (String name) {
		JsonObject runtime = Json.getObject (descriptor, ApiSpace.Spec.runtime.class.getSimpleName ());
		if (runtime == null) {
			return null;
		}
		return runtime.get (name);
	}
	
	@Override
	public void addRecyclable (String key, Recyclable recyclable) {
		recyclables.put (key, recyclable);
	}

	@Override
	public Recyclable getRecyclable (String key) {
		return recyclables.get (key);
	}
	
	@Override
	public void removeRecyclable (String key) {
		recyclables.remove (key);
	}
	
	@Override
	public boolean containsRecyclable (String key) {
		return recyclables.containsKey (key);
	}
	
	@Override
	public Set<String> getRecyclables () {
		return recyclables.keySet ();
	}
	
	@Override
	public void addFeature (String name, String feature, String provider, JsonObject spec) throws ApiManagementException {
		feature = feature.toLowerCase ();
		provider = provider.toLowerCase ();
		
		tracer.log (Tracer.Level.Info, "add feature [{0}] -> [{1} / provider {2}] ", name, feature, provider);
		
		JsonObject oFeature = Json.getObject (getFeatures (), feature);
		if (oFeature == null) {
			oFeature = new JsonObject ();
			getFeatures ().set (feature, oFeature);
		}
		if (oFeature.containsKey (name)) {
			throw new ApiManagementException ("feature '" + feature + "/" + name + "/" + provider + "' already available for space " + getNamespace ());
		}
		
		JsonObject uFeature = new JsonObject ();
		
		uFeature.set (ApiSpace.Features.Provider, provider).set (ApiSpace.Features.Spec, spec);
		
		oFeature.set (name, uFeature);

		try {
			server.getPluginsRegistry ().onEvent (Event.AddFeature, this);
		} catch (PluginRegistryException e) {
			throw new ApiManagementException (e.getMessage (), e);
		}
		
		try {
			saveDescriptor ();
		} catch (IOException e) {
			throw new ApiManagementException (e.getMessage (), e);
		}
	}

	@Override
	public void deleteFeature (String name, String feature) throws ApiManagementException {
		JsonObject oFeature = Json.getObject (getFeatures (), feature);
		if (oFeature == null || !oFeature.containsKey (name)) {
			throw new ApiManagementException ("feature '" + feature + "/" + name + "' not available for space " + getNamespace ());
		}
		oFeature.remove (name);
		
		try {
			server.getPluginsRegistry ().onEvent (Event.DeleteFeature, this);
		} catch (PluginRegistryException e) {
			throw new ApiManagementException (e.getMessage (), e);
		}
		
		try {
			saveDescriptor ();
		} catch (IOException e) {
			throw new ApiManagementException (e.getMessage (), e);
		}
	}

	@Override
	public void alter (String spaceNs, JsonObject change) throws ApiManagementException {
		try {
			ApiSpaceImpl space = (ApiSpaceImpl)space (spaceNs);
			JsonObject descriptor = space.getDescriptor ();
			descriptor.putAll (change);
			space.saveDescriptor ();
		} catch (Exception e) {
			throw new ApiManagementException (e.getMessage (), e);
		}
	}

	@Override
	public void stop (long worker) throws ApiManagementException {
		Thread [] threads = listThreads ();
		for (Thread t : threads) {
			if (worker == t.getId ()) {
				t.interrupt ();
			}
		}
	}

	@Override
	public void restart (String spaceNs) throws ApiManagementException {
		try {
			((ApiSpaceImpl)space (spaceNs)).restart ();
		} catch (Exception e) {
			throw new ApiManagementException (e.getMessage (), e);
		}
	}

	@Override
	public <T> T async (Callable<T> callable, boolean waitForResult) throws ApiAsyncExecutionException {
		JsonObject quotas = Json.getObject (getDescriptor (), ApiSpace.Spec.runtime.class.getSimpleName ());
		
		long timeout = Json.getLong (quotas, ApiSpace.Spec.runtime.Timeout, 10000);
		
		Future<T> future = executor.submit (callable);
		
		if (waitForResult) {
			try {
				return future.get (timeout, TimeUnit.MILLISECONDS);
	   		} catch (TimeoutException e) {
	   			future.cancel (true);
	            throw new ApiAsyncExecutionException (e.getMessage (), e);
			} catch (Exception e) {
	            throw new ApiAsyncExecutionException (e.getMessage (), e);
			} 
		}
		return null;
	}
	
	@Override
	public void async (Runnable runnable) throws ApiAsyncExecutionException {
		executor.submit (runnable);
	}
	
	@Override
	public Tracer tracer () {
		if (tracer == null) {
			return NoTracing.Instance;
		}
		return tracer;
	}
	
	@Override
	public JsonObject describe (DescribeOption... options) {
		
		if (options == null || options.length == 0) {
			return JsonObject.Blank;
		}
		
		Map<DescribeOption.Option, DescribeOption> opts = DescribeUtils.toMap (options);
		
		JsonObject describe = new JsonObject ();
		
		if (opts.containsKey (DescribeOption.Option.info)) {
			describe.set (ApiSpace.Spec.Namespace, getNamespace ());
			describe.set (ApiSpace.Spec.Name, getName ());
			describe.set (ApiSpace.Spec.Description, getDescription ());
			describe.set (Describe.Status, isStarted () ? ApiStatus.Running.name () : ApiStatus.Stopped.name ());
			describe.set (ApiSpace.Spec.Blocked, isBlocked ());

			if (opts.size () == 1) {
				return describe;
			}
		}
		
		descriptor = descriptor.duplicate ();
		
		if (opts.containsKey (DescribeOption.Option.secrets)) {
			describe.set (DescribeOption.Option.secrets.name (), descriptor.get (Spec.secrets.class.getSimpleName ()));
		}
		
		if (opts.containsKey (DescribeOption.Option.features)) {
			describe.set (DescribeOption.Option.features.name (), descriptor.get (Spec.Features));
		}
		
		if (opts.containsKey (DescribeOption.Option.runtime)) {
			describe.set (DescribeOption.Option.runtime.name (), descriptor.get (Spec.runtime.class.getSimpleName ()));
		}
		
		if (opts.containsKey (DescribeOption.Option.apis)) {
			final JsonArray aApis = new JsonArray ();
			describe.set (DescribeOption.Option.apis.name (), aApis);
			
			list (new Selector () {
				@Override
				public boolean select (Api api) {
					aApis.add (api.describe (DescribeOption.Info));
					return false;
				}
			});
		}
		
		if (opts.containsKey (DescribeOption.Option.workers)) {
			JsonArray aThreads = new JsonArray ();
			describe.set (DescribeOption.Option.workers.name (), aThreads);
			
			Thread [] threads = listThreads ();
			
			if (threads == null) {
				return describe;
			}

			for (Thread t : threads) {
				if (t == null) {
					continue;
				}
				
				String status = t.getState ().name ();
				if (State.WAITING.equals (t.getState ())) {
					status = StateAvailable;
				}
				
				JsonObject oth = (JsonObject)new JsonObject ().set (Describe.Worker.Id, t.getId ()).set (Describe.Worker.Name, t.getName ()).set (Describe.Worker.Status, status);
				if (t instanceof SpaceThread) {
					SpaceThread st = (SpaceThread)t;
					ApiRequest request = st.getRequest ();
					if (request != null) {
						JsonObject oRequest = new JsonObject ();
						oRequest.set (ApiRequest.Fields.Id, request.getId ());
						oRequest.set (ApiRequest.Fields.Verb, request.getVerb ().name ());
						oRequest.set (ApiRequest.Fields.Endpoint, request.getEndpoint ());
						oRequest.set (ApiRequest.Fields.Timestamp, Lang.toUTC (request.getTimestamp ()));
						
						oth.set (Describe.Worker.request.class.getSimpleName (), oRequest);
						
						if (request.getService () != null) {
							JsonObject oService = new JsonObject ();
							String script = Json.getString (request.getService ().getRuntime (), Describe.Worker.request.Script);
							oService.set (Describe.Worker.request.Script, script);
							if (script == null) {
								oService.set (Describe.Worker.request.Script, request.getService ().getSpi ().getClass ().getSimpleName ());
							}
							oService.set (ApiService.Spec.Endpoint, request.getService ().getEndpoint ());
							
							oth.set (Describe.Worker.Service, oService);
						}
						
					}
				}
				aThreads.add (oth);
			}
		}

		return describe;		
	}
	
	public Api install (File apiHome) throws ApiManagementException {
		
		Api api = new ApiImpl (this, apiHome);
		
		// stop any existing version of the api
		ApiImpl old = (ApiImpl)api (api.getNamespace ());
		if (old != null) { 
			if (old.status ().equals (ApiStatus.Running) || old.status ().equals (ApiStatus.Paused)) {
				// clear memory
				old.stop (false);
			}
			// clear memory
			old.clear ();
			
			// remove status
			statusManager.delete (old);
		}
		
		// register api
		register (api);

		tracer.log (Tracer.Level.Info, "api {0} / {1} installed", getNamespace (), api.getNamespace ());
		
		// call plugins onEvent Install
		try {
			server.getPluginsRegistry ().onEvent (Event.Install, api);
		} catch (PluginRegistryException e) {
			throw new ApiManagementException (e.getMessage (), e);
		}
		
		// get api status if any
		ApiStatus status = statusManager.get (api);
		
		tracer.log (Tracer.Level.Info, "\t\tfound with status {0}", status);

		if (ApiStatus.Running.equals (status) || ApiStatus.Paused.equals (status)) {
			// start api
			((ApiImpl)api).start (ApiStatus.Paused.equals (api.status ()));
			
			// notify plugins
			try {
				server.getPluginsRegistry ().onEvent (Event.Start, api);
			} catch (PluginRegistryException e) {
				throw new ApiManagementException (e.getMessage (), e);
			}
		}

		return api;
	}
	
	private void register (Api api) {
		apis.put (api.getNamespace (), api);
	}
	
	private void remove (String namespace) {
		apis.remove (namespace);
	}
	
	public Iterator<String> all () {
		if (apis.isEmpty ()) {
			return null;
		}
		return apis.keySet ().iterator ();
	}
	
	public void shutdown () {
		tracer.log (Tracer.Level.Info, "Shutting down Space {0}", getNamespace ());
		
		stop ();
		
		if (!apis.isEmpty ()) {
			Iterator<Api> ip = apis.values ().iterator ();
			while (ip.hasNext ()) {
				ApiImpl api = (ApiImpl)ip.next ();
				api.stop (false);
				ip.remove ();
			}
		}	
	}
	
	public boolean restart () throws Exception {
		stop ();
		return start ();
	}

	public boolean start () throws Exception {
		
		if (isStarted () || isBlocked ()) {
			return false;
		}
		
		// init tracer
		JsonObject oTracer = Json.getObject (descriptor, ConfigKeys.Tracer);
		if (!Json.isNullOrEmpty (oTracer)) {
			tracer = (Tracer)BeanUtils.create (ApiSpaceImpl.class.getClassLoader (), oTracer, getServer ().getPluginsRegistry ());
		}
		if (tracer == null) {
			tracer = server.tracer ();
		} else {
			tracer.onInstall (this);
		}
		
		tracer.log (Tracer.Level.Info, "Init {0} StatusManager", getNamespace ());
		
		statusManager = new DefaultStatusManager (this);
		
		tracer.log (Tracer.Level.Info, "Loading {0} keystore", getNamespace ());
		
		keystore = server.getKeyStoreManager ().read (this);

		tracer.log (Tracer.Level.Info, "Starting Space {0} Executor", getNamespace ());
		
		group = new ThreadGroup (getNamespace ());
		
		group.setMaxPriority (Thread.MAX_PRIORITY);

		executor = new ThreadPoolExecutor (
			server.weight (), server.weight (),
			0L, TimeUnit.MILLISECONDS,
			new ArrayBlockingQueue<Runnable> (server.weight () * 2), 
			new SpaceThreadFactory (group)
		);
		
		// executor.setRejectedExecutionHandler (new WaitPolicy ());
		
		return true;
	}

	public void stop () {
		if (!isStarted ()) {
			return;
		}
		
		tracer.log (Tracer.Level.Info, "Shutting down Space {0} Executor", getNamespace ());
		
		executor.shutdown ();
	    try {
			executor.awaitTermination (10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			tracer.log (Tracer.Level.Error, Lang.BLANK, e);
		}
	    
	    tracer.onShutdown (this);
	    
	}

	public ApiServer getServer () {
		return server;
	}

	public void setScriptingEngine (ScriptingEngine scriptingEngine) {
		this.scriptingEngine = scriptingEngine;
	}
	public ScriptingEngine getScriptingEngine () {
		return scriptingEngine;
	}
	
	private String contextRecyclableKey (String feature, String name) {
		return this.getNamespace () + Lang.DOT + feature + Lang.DOT + name;
	}

	private Thread [] listThreads () {
		if (group == null) {
			return null;
		}
		Thread [] threads = new Thread [group.activeCount ()];
		group.enumerate (threads, false);
		return threads;
	}

	public SpaceKeyStore keystore () {
		return keystore;
	}

	public void _refresh (JsonObject descriptor) throws ApiManagementException {
		
		JsonObject newFeatures = Json.getObject (descriptor, ApiSpace.Spec.Features);
		
		// sync features
		deleteAndUpdateFeatures (newFeatures);
		addNewFeatures 			(newFeatures);
		
		// sync secrets
		String secretsKey = ApiSpace.Spec.secrets.class.getSimpleName ();
		this.descriptor.remove (secretsKey);
		this.descriptor.set (secretsKey, Json.getObject (descriptor, secretsKey));
		
		try {
			saveDescriptor ();
		} catch (IOException e) {
			throw new ApiManagementException (e.getMessage (), e);
		}
		
	}
	
	private void deleteAndUpdateFeatures (JsonObject newFeatures) throws ApiManagementException {
		JsonObject eFeatures = getFeatures ();
		if (Json.isNullOrEmpty (eFeatures)) {
			return;
		}
		
		Iterator<String> fnames = eFeatures.keys ();
		while (fnames.hasNext ()) {
			String fname = fnames.next ();
			JsonObject eFeatureSet = Json.getObject (eFeatures, fname);
			if (Json.isNullOrEmpty (eFeatureSet)) {
				continue;
			}
			
			Iterator<String> names = eFeatureSet.keys ();
			while (names.hasNext ()) {
				String name = names.next ();
				JsonObject newFeature = (JsonObject)Json.find (newFeatures, fname, name);
				if (Json.isNullOrEmpty (newFeature)) {
					// delete
					deleteFeature (fname, name);
					continue;
				} 
				// compare json, and update if changed
				if (!Json.areEqual (Json.getObject (eFeatureSet, name), newFeature)) {
					deleteFeature (fname, name);
					addFeature (name, fname, Json.getString (newFeature, ApiSpace.Features.Provider), Json.getObject (newFeature, ApiSpace.Features.Spec));
				}
			}
			
		}
	}
	
	private void addNewFeatures (JsonObject newFeatures) throws ApiManagementException {
		if (Json.isNullOrEmpty (newFeatures)) {
			return;
		}
		Iterator<String> fnames = newFeatures.keys ();
		while (fnames.hasNext ()) {
			String fname = fnames.next ();
			JsonObject eFeatureSet = Json.getObject (newFeatures, fname);
			if (Json.isNullOrEmpty (eFeatureSet)) {
				continue;
			}
			
			Iterator<String> names = eFeatureSet.keys ();
			while (names.hasNext ()) {
				String name = names.next ();
				JsonObject newFeature = Json.getObject (eFeatureSet, name);
				// add feature
				addFeature (name, fname, Json.getString (newFeature, ApiSpace.Features.Provider), Json.getObject (newFeature, ApiSpace.Features.Spec));
			}
		}
	}

}