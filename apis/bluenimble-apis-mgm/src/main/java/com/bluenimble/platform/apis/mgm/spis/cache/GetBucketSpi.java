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
package com.bluenimble.platform.apis.mgm.spis.cache;

import com.bluenimble.platform.api.Api;
import com.bluenimble.platform.api.ApiAccessDeniedException;
import com.bluenimble.platform.api.ApiOutput;
import com.bluenimble.platform.api.ApiRequest;
import com.bluenimble.platform.api.ApiResponse;
import com.bluenimble.platform.api.ApiServiceExecutionException;
import com.bluenimble.platform.api.ApiSpace;
import com.bluenimble.platform.api.impls.JsonApiOutput;
import com.bluenimble.platform.api.impls.spis.AbstractApiServiceSpi;
import com.bluenimble.platform.api.security.ApiConsumer;
import com.bluenimble.platform.apis.mgm.CommonSpec;
import com.bluenimble.platform.apis.mgm.utils.MgmUtils;
import com.bluenimble.platform.cache.Cache;

public class GetBucketSpi extends AbstractApiServiceSpi {

	private static final long serialVersionUID = -3682312790255625219L;

	interface Spec {
		String Start 	= "start";
		String Page 	= "page";
	}
	
	@Override
	public ApiOutput execute (Api api, final ApiConsumer consumer, ApiRequest request,
			ApiResponse response) throws ApiServiceExecutionException {
		
		String 	provider 	= (String)request.get (CommonSpec.Provider);
		String 	bucket 		= (String)request.get (CommonSpec.Bucket);
		
		int 	start 	= (Integer)request.get (Spec.Start);
		int 	page 	= (Integer)request.get (Spec.Page);
		
		ApiSpace space;
		try {
			space = MgmUtils.space (consumer, api);
		} catch (ApiAccessDeniedException e) {
			throw new ApiServiceExecutionException (e.getMessage (), e).status (ApiResponse.FORBIDDEN);
		}
		
		Cache cache = space.feature (Cache.class, provider, request);
		
		return new JsonApiOutput (cache.get (bucket, start, page));
	}

}