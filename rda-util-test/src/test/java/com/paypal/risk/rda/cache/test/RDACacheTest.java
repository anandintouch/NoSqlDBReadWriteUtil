package com.paypal.risk.rda.cache.test;

import java.io.ByteArrayInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.paypal.risk.RiskClientDataJsonVO;
import com.paypal.risk.rda.cache.IRDACacheConnectorClient;
import com.paypal.risk.rda.cache.RDACacheConnectorClientImpl;
import com.paypal.risk.rda.cache.util.ApplicationProperties;
import com.paypal.risk.rda.reader.RDAResult;
import com.paypal.vo.serialization.UniversalDeserializer;

/**
 * Test class to write and read payloads to various cache based on a given Key.
 * 
 * @author anprakash
 *
 */
public class RDACacheTest {
	
	@Before
	public void setUp(){
		// Setting up logger
		System.setProperty("java.util.logging.config.file",
				"./src/test/resources/logging.properties");

		com.ebay.kernel.logger.Logger.initLogProperties("logging.properties");
		
	}
	
	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testRDAFraudnetCache() throws Exception {
		UniversalDeserializer deserializer = new UniversalDeserializer();
		String cache = "LIVE_ProxCache";
		String key = "540caa11b5e144d497c86972695de9cf";
		String operationType = "read";  // write
		String fileName = "modified_payload.json";
		
		System.out.println("cache: "+cache+" key: "+key);
		
		IRDACacheConnectorClient cacheConnector = new RDACacheConnectorClientImpl();

		if(operationType.equalsIgnoreCase("write")) {

			RDAResult result = cacheConnector.writeToRDACache(cache, key, 2, null);
			
			Assert.assertNotNull(result);
			
			System.out.println("Status is: "+ result.getStatus());
			RiskClientDataJsonVO jsonVo = (RiskClientDataJsonVO) deserializer.
					deserialize(new ByteArrayInputStream(result.fraudnet().getPayload().getSerializedData()));
			System.out.println("Fraudnet Payload is: "+ jsonVo.getPayload());
			
		} else {
			RDAResult resp = cacheConnector.readFromRDACache(cache, key);
			if( resp.fraudnet() == null)
				System.out.println("No data found for the Key: "+ key);
			
		}

	}
	
	@Test
	public void testRDAMagnesCache() throws Exception {
		
		String rtdsHost = ApplicationProperties.get().getProperty("rtdsService");
		int rtdsPort = Integer.parseInt(ApplicationProperties.get().getProperty("rtdsPort"));
		
		String cache = "LIVE_DysonCache";
		String invalidcache = ""; //"MagnesInvalidPayload";
		String key = "ap_feb8";
		String operationType = "read";  // write
		String fileName = "dyson_async_invalid.json";
		
		System.out.println("cache: "+cache+" key: "+key);
		
		IRDACacheConnectorClient cacheConnector = new RDACacheConnectorClientImpl();

		if (cache.equals("LIVE_DysonCache")) {
			if(operationType.equalsIgnoreCase("write")) {
				RDAResult result = cacheConnector.writeToRDACache(cache, key, 0, null);
				
				Assert.assertNotNull(result);
				
				System.out.println("Status is: "+ result.getStatus());
				System.out.println("Magnes Payload is: "+ result.dyson().toString());
			}
			else {
				RDAResult result = cacheConnector.readFromRDACache(cache, key);
				
		    	System.out.println("----------------------------------------------------------------------------------------------------------------------------------------------");
		    	System.out.println("The Dyson Payload for the cache name - "+cache+" , correlationId - "+key +" , RTDS Host - "+rtdsHost+" , and RTDS Port - "+rtdsPort);
		    	System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------");
		    	System.out.println(result.dyson().toString());
		    	System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------");
			}
		}

		
		if (invalidcache.equals("MagnesInvalidPayload")) {
			if(operationType.equalsIgnoreCase("write")) {
				RDAResult result = cacheConnector.writeToRDACache(invalidcache, key, 0, fileName);
				
				Assert.assertNotNull(result);
				
				System.out.println("Status is: "+ result.getStatus());
				System.out.println("Invalid Magnes Payload is: "+ result.dyson().toString());
			}
			else {
				RDAResult result = cacheConnector.readFromRDACache(invalidcache, key);
				
		    	System.out.println("----------------------------------------------------------------------------------------------------------------------------------------------");
		    	System.out.println("The Dyson Payload for the cache name - "+invalidcache+" , correlationId - "+key +" , RTDS Host - "+rtdsHost+" , and RTDS Port - "+rtdsPort);
		    	System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------");
		    	System.out.println(result.dyson().toString());
		    	System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------");
			}
		}
		

	}
	
	@Test
	public void testRDASTCCache() throws Exception {

		String rtdsHost = ApplicationProperties.get().getProperty("rtdsService");
		int rtdsPort = Integer.parseInt(ApplicationProperties.get().getProperty("rtdsPort"));
		
		String cache = "LIVE_RaasTransactionContextCache";
		String key = "ap5";// randomNumber() ;
		String operationType = "read";  // write
		String fileName = "modified_payload.json";
		
		System.out.println("cache: "+cache+" key: "+key);
		
		IRDACacheConnectorClient cacheConnector = new RDACacheConnectorClientImpl();

		if(operationType.equalsIgnoreCase("write")) {

			RDAResult result = cacheConnector.writeToRDACache(cache, key, 0, null);
			
			Assert.assertNotNull(result);
			
			System.out.println("Status is: "+ result.getStatus());
			System.out.println("STC Payload for a Key '"+key+"' is:" + result.stc().toString());

			
		} else {
			RDAResult result = cacheConnector.readFromRDACache(cache, key);
			
	    	System.out.println("----------------------------------------------------------------------------------------------------------------------------------------------");
	    	System.out.println("The Dyson Payload for the cache name - "+cache+" , correlationId - "+key +" , RTDS Host - "+rtdsHost+" , and RTDS Port - "+rtdsPort);
	    	System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------");
	    	System.out.println(result.stc().toString());
	    	System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------");
		
		}

	}
	
	public String randomNumber(){
		DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
		Date date = new Date();

		return dateFormat.format(date)+"_"+String.valueOf(Math.random());
		
	}

}
