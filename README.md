# rda-java-client
##Notes:
* This library requires rtds-client 1.2 or later. Please check the release notes for the RTDS version that is supported for each rda-reader version. [Of note: We've switched to the rtds-basic api, so hopefully RTDS versions won't be an issue going forward]
* We're importing protobuf libraries from the protobuf-schema library generated in RCMD. If your library already includes tsa-persistence, we recommend removing that and getting the protobuf objects from this client.
  * While we're on the topic of protobuf, our objects are currently using the following protobuf versions:
    * Helix: 2.4.1
    * Raptor: 2.4.1, 2.6.1
* Feel like something is missing or incorrect? Send us a note at DL-PP-Risk-Data-Acquisition@paypal.com

##Usage :

**Read**

	Fraudnet.Builder fnBuilder = new Fraudnet().newBuilder()
                .riskClientMetadataId("foo")
                .gsid("bar");
	Dyson.Builder dyBuilder = new Dyson().newBuilder()
                .riskClientMetadataId("12345678")
                .gsid("bar")
                .alternateId("baz");
        SetTxnContext.Builder stcBuilder = new SetTxnContext().newBuilder()
                .trackingId("foobar");
	RDAReader reader = new RDAReader();
	reader.setRtdsClient(client);
	RDAResult result = reader.find(fnBuilder, dyBuilder, stcBuilder);

	if ( result.dataFound() ) {
		RiskClientDataEventVO fraudnetData = result.fraudnet();
		JSONObject dyson = result.dyson();
                JSONObject stc = result.stc();
	}


----------


#rda-util-test
***About this module:***
This RDA cache util test client library can be used to Read and Write data from/to RTDS (RealTimeDataServ) Caching System directly. All Risk data can be written to a various caches as mentioned below based on "CacheName" and "Key" . Later it can be read based on the "key" provided for any  Analytic or test purpose.

**Why new RDA Cache library:**
One of the major problems we have is that lots of developers, qa, etc. don't know how to set up Fraudnet or Dyson data in their functional tests. We'd like to create a library that people can add to their functional tests (automation tests) that will set up Fraudnet and Dyson data.

**Advantage:**

 - We don'y need to go to any tool and test, QA team and use this library in their functional test and provide maven dependency for this module and can run end to end test suite.
 - Extensible for future scenarios - Whenever new cache is introduced,we can just provide new cache name and it can be easily integrated and used for Read/Write data from RTDS cache directly. 
 - Flexible to customize data  - In case of payload's key/value attributes modified or newly added, then just update the JSON payload file and provide the file name to the write API call and it 
would seamlessly write to the given cache.

 - For Fraudnet call, fraudnetType (P1/P2/P3) values can be used in the API call parameter to load respective file from src/test/resources and it would accordingly load the file to the RTDS cache.

 - Configurability - Host,Port,File path, name etc. are all configurable.
 
**API's :**
 

> ***public RDAResult readFromRDACache(String cacheName, String key)***

This API is used to read RDA data from  RTDS cache based on cacheName and key provided.
 
 

> ***public RDAResult writeToRDACache(String cacheName, String key , int***
> ***fraudnetType, String payloadFileName)***

 
This API is used to write RDA data to  RTDS cache based on cacheName , key, fraudnetType, payloadFileName provided. Parameters can be changed based on use cases.

**Parameters Details:**

 1. **"payloadFileName"**  is used to pass the modified or new file name with the updated key/value in it as per need .In case of P1,P2 and P3 calls passed in fraudnetType, payloadFileName can be passed as "null" .
 
 2. **"fraudnetType"** is used only for Fraudnet data load ,it's a call type like P1,P2 and P3 to load respective fraudnet files (fraudnet_p1.json , fraudnet_p2.json , fraudnet_p3.json). Values should be 1,2 or 3.
 For a Magnes call, "fraudnetType" is ignored and "payloadFileName" is used to load modified file .If "payloadFileName" is null then ,existing magnes file will be used from src/test/resources path. 
 
Both the API calls return "RDAResult" object with all the data populated in it along with the status of write call . Caller can used RDAResult to read payload data.

**Cache Names:**

 3. LIVE_ProxCache

 4. LIVE_DysonCache

 5. DysonAlternateID

**Configuration:**
 File name "app.properties" can be used to configure all the required values like hostname,port,existing and modified/new file name with the path.
 

    rtdsService=realtimedataserv7233.qa.paypal.com
    rtdsPort=14607
    FN_P1_FILE_PATH = src/test/resources/fraudnet_p1.json
    FN_P2_FILE_PATH = src/test/resources/fraudnet_p2.json
    FN_P3_FILE_PATH = src/test/resources/fraudnet_p3.json
    MAGNES_FILE_PATH = src/test/resources/dyson_async.json
    MODIFIED_FILE_PATH = src/test/resources/
