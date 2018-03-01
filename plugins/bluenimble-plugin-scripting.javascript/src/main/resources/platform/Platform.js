var JC_Byte					= Java.type ('java.lang.Byte');
var JC_String				= Java.type ('java.lang.String');
var JC_Thread 				= Java.type ('java.lang.Thread');
var JC_Callable				= Java.type ('com.bluenimble.platform.api.ContextualCallable');

var JC_ByteArrayOutputStream= Java.type ('java.io.ByteArrayOutputStream');

var JC_ApiByteArrayOutput	= Java.type ('com.bluenimble.platform.api.impls.ApiByteArrayOutput');

var JC_Instant				= Java.type ('java.time.Instant');
var JC_LocalDateTime		= Java.type ('java.time.LocalDateTime');
var JC_ZonedDateTime		= Java.type ('java.time.ZonedDateTime');
var JC_ZoneOffset			= Java.type ('java.time.ZoneOffset');
var JC_ZoneId				= Java.type ('java.time.ZoneId');
var JC_DateTimeFormatter	= Java.type ('java.time.format.DateTimeFormatter');
var JC_Month				= Java.type ('java.time.Month');

var JC_Encodings 			= Java.type ('com.bluenimble.platform.Encodings');
var JC_Lang 				= Java.type ('com.bluenimble.platform.Lang');
var JC_Crypto 				= Java.type ('com.bluenimble.platform.Crypto');
var JC_Crypto_Algorithm 	= Java.type ('com.bluenimble.platform.Crypto.Algorithm');
var JC_Crypto_Hmac 			= Java.type ('com.bluenimble.platform.Crypto.Hmac');
var JC_Crypto_Hashing 		= Java.type ('com.bluenimble.platform.Crypto.Hashing');
var JC_Base64 				= Java.type ('com.bluenimble.platform.encoding.Base64');

var JC_JsonObject 			= Java.type ('com.bluenimble.platform.json.JsonObject');
var JC_JsonArray 			= Java.type ('com.bluenimble.platform.json.JsonArray');
var JC_Json 				= Java.type ('com.bluenimble.platform.Json');

var JC_IOUtils 				= Java.type ('com.bluenimble.platform.IOUtils');

var JC_FeaturesUtils 		= Java.type ('com.bluenimble.platform.server.plugins.scripting.utils.FeaturesUtils');

var JC_Tracer_Level 		= Java.type ('com.bluenimble.platform.api.tracing.Tracer.Level');

var JC_ApiConsumer_Fields 	= Java.type ('com.bluenimble.platform.api.security.ApiConsumer.Fields');

var JC_ApiRequest 			= Java.type ('com.bluenimble.platform.api.ApiRequest');
var JC_ContainerApiRequest 	= Java.type ('com.bluenimble.platform.api.impls.ContainerApiRequest');
var JC_ApiRequest_Scope 	= Java.type ('com.bluenimble.platform.api.ApiRequest.Scope');
var JC_ApiStreamSource 		= Java.type ('com.bluenimble.platform.api.impls.DefaultApiStreamSource');

var JC_ApiResponse 			= Java.type ('com.bluenimble.platform.api.ApiResponse');

var JC_HttpStatues 			= Java.type ('com.bluenimble.platform.server.plugins.scripting.utils.HttpStatuses');

var ApiHeaders 				= Java.type ('com.bluenimble.platform.api.ApiHeaders');

var JC_Database 			= Java.type ('com.bluenimble.platform.db.Database');
var JC_Database_Fields 		= Java.type ('com.bluenimble.platform.db.Database.Fields');
var JC_Database_IndexType 	= Java.type ('com.bluenimble.platform.db.Database.IndexType');
var JC_DatabaseObject 		= Java.type ('com.bluenimble.platform.db.DatabaseObject');
var JC_DatabaseObjectSerializer		
							= Java.type ('com.bluenimble.platform.db.DatabaseObjectSerializer');
var JC_DefaultDatabaseObjectSerializer	
							= Java.type ('com.bluenimble.platform.db.impls.DefaultDatabaseObjectSerializer');

var JC_Query_Visitor		= Java.type ('com.bluenimble.platform.db.Database.Visitor');
var JC_JsonQuery			= Java.type ('com.bluenimble.platform.db.query.impls.JsonQuery');

var JC_Storage 				= Java.type ('com.bluenimble.platform.storage.Storage');
var JC_Folder_Visitor 		= Java.type ('com.bluenimble.platform.storage.Folder.Visitor');
var JC_Folder_Filter 		= Java.type ('com.bluenimble.platform.storage.Folder.Filter');
var JC_OnlyFolders_Filter 	= Java.type ('com.bluenimble.platform.storage.impls.filters.OnlyFoldersFilter');
var JC_OnlyFiles_Filter 	= Java.type ('com.bluenimble.platform.storage.impls.filters.OnlyFilesFilter');
var JC_EmptyFoldersFilter	= Java.type ('com.bluenimble.platform.storage.impls.filters.EmptyFoldersFilter');
var JC_NotEmptyFoldersFilter= Java.type ('com.bluenimble.platform.storage.impls.filters.NotEmptyFoldersFilter');
var JC_StartsWith_Filter 	= Java.type ('com.bluenimble.platform.storage.impls.filters.StartsWithFilter');
var JC_EndsWith_Filter 		= Java.type ('com.bluenimble.platform.storage.impls.filters.EndsWithFilter');
var JC_Expression_Filter 	= Java.type ('com.bluenimble.platform.storage.impls.filters.ExpressionFilter');
var JC_Contains_Filter 		= Java.type ('com.bluenimble.platform.storage.impls.filters.ContainsFilter');

var JC_Messenger 			= Java.type ('com.bluenimble.platform.messaging.Messenger');
var JC_Messenger_Sender 	= Java.type ('com.bluenimble.platform.messaging.impls.JsonSender');
var JC_Messenger_Recipient 	= Java.type ('com.bluenimble.platform.messaging.impls.JsonRecipient');

var JC_Remote_Callback		= Java.type ('com.bluenimble.platform.remote.Remote.Callback');

var JC_Converters 			= Java.type ('com.bluenimble.platform.server.plugins.scripting.utils.Converters');

var JC_ApiUtils 			= Java.type ('com.bluenimble.platform.server.plugins.scripting.utils.ApiUtils');

var JC_ApiResource_Selector = Java.type ('com.bluenimble.platform.api.ApiResource.Selector');

var JC_ApiResourceOutput 	= Java.type ('com.bluenimble.platform.api.impls.ApiResourceOutput');

var JC_ApiSpace_Features 	= Java.type ('com.bluenimble.platform.api.ApiSpace.Features');

var JC_Libs_Http 			= Java.type ('com.bluenimble.platform.http.utils.Http');
var JC_Libs_OAuth_Signer 	= Java.type ('com.bluenimble.platform.api.impls.scripting.libraries.OAuthHttpRequestVisitor');
var JC_Libs_BN_Signer 		= Java.type ('com.bluenimble.platform.api.impls.scripting.libraries.BlueNimbleHttpRequestVisitor');

(function () {
	// tools
	// load (tools + '/ast/esprima.js'); 
	// load (tools + '/ast/estraverse.js'); 
	// load (tools + '/ast/escodegen.js'); 
	
	// lang	
	if (typeof Lang === 'undefined') {
		load (core + '/lang/Lang.js');
	}
	if (typeof Thread === 'undefined') {
		load (core + '/lang/Thread.js');
	}
	if (typeof Task === 'undefined') {
		load (core + '/lang/Task.js');
	}
	if (typeof Crypto === 'undefined') {
		load (core + '/lang/Crypto.js');
	}
	if (typeof Base64 === 'undefined') {
		load (core + '/lang/Base64.js');
	}
	if (typeof Hex === 'undefined') {
		load (core + '/lang/Hex.js');
	}
	if (typeof DateTime === 'undefined') {
		load (core + '/lang/DateTime.js');
	}
	if (typeof Json === 'undefined') {
		load (core + '/lang/Json.js');
	}

	if (typeof InputStream === 'undefined') {
		load (core + '/lang/InputStream.js');
	}
	if (typeof OutputStream === 'undefined') {
		load (core + '/lang/OutputStream.js');
	}
	
	// database	
	if (typeof Database === 'undefined') {
		load (core + '/database/Database.js');
	}
	if (typeof DatabaseObject === 'undefined') {
		load (core + '/database/DatabaseObject.js');
	}
	
	// storage	
	if (typeof Storage === 'undefined') {
		load (core + '/storage/Storage.js');
	}
	if (typeof StorageObject === 'undefined') {
		load (core + '/storage/StorageObject.js');
	}
	if (typeof Folder === 'undefined') {
		load (core + '/storage/Folder.js');
	}
	
	// messenger	
	if (typeof Messenger === 'undefined') {
		load (core + '/messenger/Messenger.js');
	}

	// cache	
	if (typeof Cache === 'undefined') {
		load (core + '/cache/Cache.js');
	}

	// remote	
	if (typeof Remote === 'undefined') {
		load (core + '/remote/Remote.js');
	}

	// api	
	if (typeof ApiContext === 'undefined') {
		load (core + '/ApiContext.js');
	}
	if (typeof Tracer === 'undefined') {
		load (core + '/Tracer.js');
	}
	if (typeof Api === 'undefined') {
		load (core + '/Api.js');
	}
	if (typeof ApiConsumer === 'undefined') {
		load (core + '/ApiConsumer.js');
	}
	if (typeof ApiRequest === 'undefined') {
		load (core + '/ApiRequest.js');
	}
	if (typeof ApiRequestTrack === 'undefined') {
		load (core + '/ApiRequestTrack.js');
	}
	if (typeof ApiResponse === 'undefined') {
		load (core + '/ApiResponse.js');
	}
	if (typeof ApiResource === 'undefined') {
		load (core + '/ApiResource.js');
	}
	if (typeof ApiResourcesManager === 'undefined') {
		load (core + '/ApiResourcesManager.js');
	}
	if (typeof ApiStreamSource === 'undefined') {
		load (core + '/ApiStreamSource.js');
	}
	if (typeof ApiService === 'undefined') {
		load (core + '/ApiService.js');
	}
	if (typeof ApiOutput === 'undefined') {
		load (core + '/ApiOutput.js');
	}
	
	return {
		ApiContext: function (proxy) {
			return new ApiContext (proxy);
		},
		Api: function (proxy) {
			return new Api (proxy);
		},
		ApiConsumer: function (proxy) {
			return new ApiConsumer (proxy);
		},
		ApiRequest: function (proxy) {
			return new ApiRequest (proxy);
		},
		ApiResponse: function (proxy) {
			return new ApiResponse (proxy);
		},
		ApiService: function (proxy) {
			return new ApiService (proxy);
		},
		Libs: {
			Lang: Lang,
			Thread: Thread,
			Task: Task,
			Crypto: Crypto,
			Base64: Base64,
			Hex: Hex,
			DateTime: DateTime,
			Json: Json,
			ApiConsumer: ApiConsumer.prototype,
			ApiRequest: ApiRequest.prototype,
			ApiOutput: ApiOutput.prototype
		}
	};
	
})();
