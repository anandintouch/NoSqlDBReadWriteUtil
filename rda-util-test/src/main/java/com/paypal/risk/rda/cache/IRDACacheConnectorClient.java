package com.paypal.risk.rda.cache;

import com.paypal.risk.rda.reader.RDAResult;


/**
 * Client interface for reading and writing to RDA Cache for any key value store through RTDS 
 * abstraction.
 * 
 * @author anprakash
 *
 */
public interface IRDACacheConnectorClient {
	
	/**
	 * Read API to read RDA cache for a given CacheName and Key.
	 * 
	 * @param cacheName  Name of the cache.
	 * @param key  Key name.
	 * @return RDAResult  Object which has payload for a given key is populated in it.
	 * @throws Exception
	 */
	public RDAResult readFromRDACache(String cacheName, String key) throws Exception;
	
	/**
	 * Write API to write to RDA cache for a given CacheName,Key,FraudnetType and file name.
	 * 
	 * fraudnetType = is used  only for Fraudnet data load ,it's a call type like P1,P2 and P3 to
	 * load respective fraudnet files (fraudnet_p1.json , fraudnet_p2.json , fraudnet_p3.json). 
	 * Values passed in the param should be 1,2 or 3.
	 * 
	 * payloadFileName = is used to pass the modified or new file name with the updated key/value in it as per need .
	 * In case of P1,P2 and P3 calls passed in fraudnetType, payloadFileName can be passed as "null".
	 * 
	 * @param cacheName Name of the cache.
	 * @param key  Key name.
	 * @param fraudnetType  Fraudnet call type.
	 * @param payloadFileName File name to load.
	 * @return RDAResult  Object which has status populated in it.
	 * @throws Exception
	 */
	public RDAResult writeToRDACache(String cacheName, String key , int fraudnetType,
			String payloadFileName) throws Exception;

}
