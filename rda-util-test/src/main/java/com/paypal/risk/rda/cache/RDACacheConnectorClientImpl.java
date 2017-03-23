package com.paypal.risk.rda.cache;

import java.io.ByteArrayInputStream;
import java.util.Set;

import net.sf.json.JSONObject;

import com.ebay.kernel.logger.LogLevel;
import com.ebay.kernel.logger.Logger;
import com.paypal.risk.RiskClientDataEventVO;
import com.paypal.risk.RiskClientDataJsonVO;
import com.paypal.risk.identity.persistence.schema.DysonEnvelopeSchema;
import com.paypal.risk.identity.persistence.schema.DysonEnvelopeSchema.DysonEnvelope;
import com.paypal.risk.identity.persistence.schema.RaaSTxnCtxEnvelopeSchema;
import com.paypal.risk.identity.persistence.schema.RaaSTxnCtxEnvelopeSchema.RaaSTxnCtxEnvelope;
import com.paypal.risk.identity.utils.VOSerializer;
import com.paypal.risk.rda.cache.common.RDACacheConstants;
import com.paypal.risk.rda.cache.common.RTDSServiceExecutorInstance;
import com.paypal.risk.rda.cache.util.ApplicationProperties;
import com.paypal.risk.rda.cache.util.LoadDataUtil;
import com.paypal.risk.rda.reader.RDAResult;
import com.paypal.risk.rda.reader.domain.Dyson;
import com.paypal.risk.rda.reader.domain.Fraudnet;
import com.paypal.risk.rda.reader.domain.KeysPrefetch;
import com.paypal.risk.rda.reader.domain.SetTxnContext;
import com.paypal.rti.rtds.RTDSServiceExecutor;
import com.paypal.vo.ValueObject;
import com.paypal.vo.serialization.UniversalDeserializer;

/**
 * This class is an implementation of Client interface for reading and writing from/to RDA Cache
 * for any key value store through RTDS cache system.
 * 
 * @author anprakash
 *
 */
public class RDACacheConnectorClientImpl implements IRDACacheConnectorClient {

	public static Logger logger = Logger.getInstance( RDACacheConnectorClientImpl.class );
	private static volatile RTDSServiceExecutor executor = null;
	
    private Fraudnet fraudnet = null;
    private Dyson dyson = null;
    private SetTxnContext raas = null;
    private KeysPrefetch prefetch = null;
	
	static {
		executor = RTDSServiceExecutorInstance.getServiceExecutorInstance(ApplicationProperties.get());
	}
	
	@Override
	public RDAResult readFromRDACache(String cacheName, String key) throws Exception {
		
		RDAResult rdaResult = null;
		logger.log(LogLevel.DEBUG, "Reading from cache '"+cacheName+"' for key '"+key+"'");

		if (RDACacheConstants.DYSON_CACHE_NAME.equals(cacheName)) {
			rdaResult = readMagnesPayload(cacheName, key);
		} else if(RDACacheConstants.FRAUDNET_CACHE_NAME.equals(cacheName)) {
			rdaResult = readFraudnetPayload(cacheName,key);
			
		} else if(RDACacheConstants.RAAS_CACHE_NAME.equals(cacheName)) {
			rdaResult = readStcPayload(cacheName, key);
		} else {
			logger.log(LogLevel.INFO, "Cache '"+cacheName+"' is not a valid cache name.");
		}
		
		return rdaResult;
	}

	@Override
	public RDAResult  writeToRDACache(String cacheName, String key , int fraudnetType,
			String payloadFileName) throws Exception {
		RDAResult rdaResult = null;
		
		logger.log(LogLevel.DEBUG, "Writing to cache '"+cacheName+"' for key '"+key+"'");

		if (RDACacheConstants.DYSON_CACHE_NAME.equals(cacheName)) {
			
			rdaResult = writeMagnesPayload(cacheName, key, payloadFileName);
			
		} else if(RDACacheConstants.FRAUDNET_CACHE_NAME.equals(cacheName)) {
			
			rdaResult = writeFraudnetPayload(cacheName,key,fraudnetType,payloadFileName);
			
		} else if(RDACacheConstants.RAAS_CACHE_NAME.equals(cacheName)) {
			//Code for STC cache setting
			rdaResult = writeSTCPayload(cacheName, key, payloadFileName);
		} else {
			logger.log(LogLevel.ERROR, "Cache name '"+cacheName+"' is not a valid cache .");
		}
		
		return rdaResult;
	}

	
	private  RDAResult readFraudnetPayload(String cacheName, String key) throws Exception {
		fraudnet = new Fraudnet();
		dyson = new Dyson();
		raas = new SetTxnContext();
		prefetch = new KeysPrefetch();
		
		RDAResult rdaResult = new RDAResult(fraudnet, dyson, raas, prefetch);
		// RTDSServiceExecutorInstance.getServiceExecutorInstance(ApplicationProperties.get());
		 
		byte[] result = executor.executeRTDSGET(cacheName, key).getByteValue();
		
		for (byte b : result) {
		    if (b != 0) {
				ValueObject vo = VOSerializer.deserilizeFromBytes(result);		
				Set<String> set = vo.voFieldNames();		
				
				String rtdsHost = ApplicationProperties.get().getProperty("rtdsService");
				int rtdsPort = Integer.parseInt(ApplicationProperties.get().getProperty("rtdsPort"));
		    	
				System.out.println("----------------------------------------------------------------------------------------------------------------------------------------------");
		    	System.out.println("The Fraudnet Payload for the cache name - "+cacheName+" , correlationId - "+key +" , RTDS Host - "+rtdsHost+" , and RTDS Port - "+rtdsPort);
		    	System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------");
				
		    	for (String s : set ) 
					System.out.println("\nField name : "+s+" and Value : "+vo.get(s));
					
			    UniversalDeserializer deserializer = new UniversalDeserializer();
			    RiskClientDataEventVO eventVo = (RiskClientDataEventVO) deserializer.deserialize(new ByteArrayInputStream(result));
				 
			    RiskClientDataJsonVO jsonVo = (RiskClientDataJsonVO) deserializer.deserialize(new ByteArrayInputStream(((RiskClientDataEventVO)vo).getPayload().getSerializedData()));
			     
				rdaResult.setFraudnet(key, eventVo);
				 
				System.out.println("Payload is: \n"+jsonVo.getPayload());
				 
			    System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------");
				break;
			
		    }	
		}
		
		return rdaResult;	

	}
	
	private RDAResult readMagnesPayload(String cacheName, String key) throws Exception {

		fraudnet = new Fraudnet();
		dyson = new Dyson();
		raas = new SetTxnContext();
		prefetch = new KeysPrefetch();
		
		RDAResult rdaResult = new RDAResult(fraudnet, dyson, raas, prefetch);
		
		byte[] result = executor.executeRTDSGET(cacheName, key).getByteValue();
						
    	DysonEnvelope dysonData = DysonEnvelopeSchema.DysonEnvelope.parseFrom( result);
    	JSONObject dysonObject = LoadDataUtil.convertDysonEnvelope2JSON(dysonData);
    	rdaResult.setDyson(key, dysonObject);
    	
    	return rdaResult;
	}
	
	private  RDAResult readStcPayload(String cacheName, String key) throws Exception {

		fraudnet = new Fraudnet();
		dyson = new Dyson();
		raas = new SetTxnContext();
		prefetch = new KeysPrefetch();
		
		RDAResult rdaResult = new RDAResult(fraudnet, dyson, raas, prefetch);
		
		//byte[] result = executor.executeRTDSGET(cacheName, key).getByteValue();
		String result = executor.executeRTDSGET(cacheName, key).getStringValue();
						
    	/*RaaSTxnCtxEnvelope stcData = RaaSTxnCtxEnvelopeSchema.RaaSTxnCtxEnvelope.parseFrom( result);
    	JSONObject stcObject = LoadDataUtil.convertSTCEnvelope2JSON(stcData);*/

		if (result !=null && !result.equals("")) {
			JSONObject json = JSONObject.fromObject(result);
			rdaResult.setStc(key,json );
		}else {
			//set empty json
			rdaResult.setStc(key,new JSONObject() );
		}
    	
    	return rdaResult;
	}
	
	private RDAResult writeFraudnetPayload(String cacheName, String key, int fraudnetType, String payloadFileName) throws Exception{
		fraudnet = new Fraudnet();
		dyson = new Dyson();
		raas = new SetTxnContext();
		prefetch = new KeysPrefetch();
		
		RDAResult rdaResult = new RDAResult(fraudnet, dyson, raas, prefetch);
		String result = null;
		
		if (fraudnetType != 0 || payloadFileName != null) {
			
			RiskClientDataEventVO clientDataEventVO = LoadDataUtil.loadFraudnetData(fraudnetType,key,payloadFileName);
			result = executor.executePUT(cacheName, clientDataEventVO.getCorrelationId(), LoadDataUtil.serializeVoToString(clientDataEventVO));
			
			if (result.trim().equals("200/OK")) {
				rdaResult.setStatus("Success");
				rdaResult.setFraudnet(clientDataEventVO.getCorrelationId(), clientDataEventVO);
				logger.log(LogLevel.INFO, "Writing to cache '"+cacheName+"' for key '"+key+"' is Successfull !");
			} else {
				rdaResult.setStatus("Failed");
				logger.log(LogLevel.INFO, "Writing to cache '"+cacheName+"' for key '"+key+"' Failed ");
			}

		} else {
			rdaResult.setStatus("Failed");
			logger.log(LogLevel.ERROR, "FraudnetType  '"+fraudnetType+"'" +" or File name '"+payloadFileName+"'"+" is not valid: ");
			
		}

		return rdaResult;		
	}
	
	private RDAResult writeMagnesPayload(String cacheName, String key, String payloadFileName) throws Exception{
		fraudnet = new Fraudnet();
		dyson = new Dyson();
		raas = new SetTxnContext();
		prefetch = new KeysPrefetch();
		
		RDAResult rdaResult = new RDAResult(fraudnet, dyson, raas, prefetch);
		
		JSONObject jsnObj = LoadDataUtil.loadMagnesData(payloadFileName);
		
		DysonEnvelopeSchema.DysonEnvelope message = LoadDataUtil.convert2Protobuf(jsnObj);

		String result = executor.executePUT(cacheName, key, message.toByteArray());
		
		if (result.trim().equals("200/OK")) {
			rdaResult.setStatus("Success");
			rdaResult.setDyson(key, jsnObj);
			logger.log(LogLevel.INFO, "Writing to cache '"+cacheName+"' for key '"+key+"' is Successfull !");
		} else {
			rdaResult.setStatus("Failed");
			logger.log(LogLevel.INFO, "Writing to cache '"+cacheName+"' for key '"+key+"' Failed ");
		}
		return rdaResult;
		
	}
	
	private RDAResult writeSTCPayload(String cacheName, String key, String payloadFileName) throws Exception{
		fraudnet = new Fraudnet();
		dyson = new Dyson();
		raas = new SetTxnContext();
		prefetch = new KeysPrefetch();
		
		RDAResult rdaResult = new RDAResult(fraudnet, dyson, raas, prefetch);
		
		JSONObject jsnObj = LoadDataUtil.loadSTCData(payloadFileName);
		//DysonEnvelopeSchema.DysonEnvelope message = LoadDataUtil.convert2Protobuf(jsnObj);

		String result = executor.executePUT(cacheName, key, jsnObj.toString());
		
		if (result.trim().equals("200/OK")) {
			rdaResult.setStatus("Success");
			rdaResult.setStc(key,jsnObj);
			
			logger.log(LogLevel.INFO, "Writing to cache '"+cacheName+"' for key '"+key+"' is Successfull !");
		} else {
			rdaResult.setStatus("Failed");
			logger.log(LogLevel.INFO, "Writing to cache '"+cacheName+"' for key '"+key+"' Failed ");
		}
		
		return rdaResult;
		
	}

}
