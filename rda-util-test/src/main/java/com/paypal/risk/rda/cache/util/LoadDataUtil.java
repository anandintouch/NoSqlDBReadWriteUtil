package com.paypal.risk.rda.cache.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import net.sf.json.JSONObject;

import org.apache.commons.io.IOUtils;
import org.testng.Reporter;

import com.ebay.kernel.logger.LogLevel;
import com.ebay.kernel.logger.Logger;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.paypal.common.OpaqueDataElementVO;
import com.paypal.risk.RiskClientDataEventVO;
import com.paypal.risk.RiskClientDataJsonVO;
import com.paypal.risk.identity.persistence.schema.DysonEnvelopeSchema;
import com.paypal.risk.identity.persistence.schema.DysonEnvelopeSchema.DysonEnvelope;
import com.paypal.risk.identity.persistence.schema.RaaSTxnCtxEnvelopeSchema;
import com.paypal.risk.identity.persistence.schema.RaaSTxnCtxEnvelopeSchema.KeyValuePair;
import com.paypal.risk.identity.persistence.schema.RaaSTxnCtxEnvelopeSchema.RaaSTxnCtxEnvelope;
import com.paypal.vo.ValueObject;
import com.paypal.vo.serialization.Formats;
import com.paypal.vo.serialization.Serializer;
import com.paypal.vo.serialization.UniversalSerializer;

/**
 * This class has all the utility static factory methods for loading file,converting JSON to prtobuf,serializing 
 * VO to String etc.
 * 
 * @author anprakash
 *
 */
public class LoadDataUtil {
	
	public static Logger logger = Logger.getInstance( LoadDataUtil.class );
	
	/**
	 * Loads the Fraudnet data for the given fraudnetType(1,2,3) .If filename is provided then it will
	 * load data from that particular file which is configured in the property file.
	 * payloadFileName - this file can be modified or new file with new/modified key/value pairs to write.
	 * 
	 * @param fraudnetType Fraudnet call type p1,p2,p3 (i.e. 1,2,3 can be passed in the parameter )
	 * @param key  Key name
	 * @param payloadFileName File name
	 * @return RiskClientDataEventVO  Returns this VO with populated data in it.
	 * @throws IOException
	 */
	public static RiskClientDataEventVO loadFraudnetData(int fraudnetType, String key, String payloadFileName) throws IOException {
		RiskClientDataEventVO fn = new RiskClientDataEventVO();
		fn.setFso("123456");
		fn.setIpAddress("127.0.0.1");
		fn.setVisitorId("12345678");
		fn.setCorrelationId(key);
		
		File jsonRequest = null;
		
		if (fraudnetType != 0 && payloadFileName == null ) {
			if (fraudnetType == 1) {
				jsonRequest = new File(ApplicationProperties.get().getProperty("FN_P1_FILE_PATH"));
			} else if (fraudnetType == 2) {
				jsonRequest = new File(ApplicationProperties.get().getProperty("FN_P2_FILE_PATH"));
			}else if (fraudnetType == 3) {
				jsonRequest = new File(ApplicationProperties.get().getProperty("FN_P3_FILE_PATH"));
			}
		} else if (payloadFileName != null && !payloadFileName.isEmpty()){
			jsonRequest = new File(ApplicationProperties.get().getProperty("MODIFIED_FILE_PATH")+payloadFileName);
		}
		
		RiskClientDataJsonVO jsonVo = new RiskClientDataJsonVO();
		
		if(jsonRequest != null)
			jsonVo.setPayload(readString(jsonRequest));
		
		fn.setPayload(serializeVOToOpaque(jsonVo, RiskClientDataJsonVO.class.getName()));
		
		return fn;

	}
	
	/**
	 * Loads the Magnes data for the given file name.If filename is null then it will
	 * load data from the default MAGNES_FILE_PATH which is configured in the property file.
	 * 
	 * @param payloadFileName File name
	 * @return JSONObject JSON object with populated data
	 */
	public static JSONObject loadMagnesData( String payloadFileName) {
		
		JSONObject dysonData = null;
		
		try {
			
			File json = null;
			
			if( payloadFileName != null && !payloadFileName.isEmpty()) {
				json = new File(ApplicationProperties.get().getProperty("MODIFIED_FILE_PATH")+payloadFileName);
			} else {
				json = new File(ApplicationProperties.get().getProperty("MAGNES_FILE_PATH"));
			}
			
			dysonData = JSONObject.fromObject(readString(json));
		} catch (IOException e) {
			logger.log(LogLevel.ERROR, "Error while reading File-"+payloadFileName+" ",e.getMessage());
		}
		
		return dysonData;
	}
	
	public static JSONObject loadSTCData(String payloadFileName){
		JSONObject dysonData = null;
		
		try {
			
			File json = null;
			
			if( payloadFileName != null && !payloadFileName.isEmpty()) {
				json = new File(ApplicationProperties.get().getProperty("MODIFIED_FILE_PATH")+payloadFileName);
			} else {
				json = new File(ApplicationProperties.get().getProperty("STC_FILE_PATH"));
			}
			
			dysonData = JSONObject.fromObject(readString(json));
		} catch (IOException e) {
			logger.log(LogLevel.ERROR, "Error while reading File-"+payloadFileName+" ",e.getMessage());
		}
		
		return dysonData;
		
	}
	
	public static String readString(File file) throws IOException {
		logger.log(LogLevel.INFO, "File path is: "+file);
		
		BufferedReader br = new BufferedReader(new FileReader(file));
		String fileString="", line;
		while ((line = br.readLine()) != null) {
			fileString += line;
		}
		InputStream input = new ByteArrayInputStream(fileString.getBytes());
        if (input != null) {
            try {
                return IOUtils.toString(input);
            } finally {
                IOUtils.closeQuietly(input);
            }
        }
        return null;
	}
	
	/**
	 * Serialize VO object to the String.
	 * 
	 * @param vo  ValueObject
	 * @return String
	 * @throws IOException
	 */
	public static String serializeVoToString(ValueObject vo) throws IOException {
		UniversalSerializer serializer = new UniversalSerializer(Formats.COMPRESSEDBINARY, false, false, false);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        serializer.serialize(vo, baos);
        return baos.toString("UTF-8");
	}
	
	/**
	 * Serialize ValueObject to the OpaqueDataElementVO.
	 * 
	 * @param inputVO
	 * @param inputVOName
	 * @return
	 */
	private static OpaqueDataElementVO serializeVOToOpaque(ValueObject inputVO,
			String inputVOName) {
		OpaqueDataElementVO odeVO = new OpaqueDataElementVO();

		Serializer serializer = new UniversalSerializer(Formats.BINARY, false,
				false, false);
		ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();

		try {
			serializer.serialize(inputVO, byteOutput);
		} catch (IOException e) {
			Reporter.log(inputVOName + " failed to serialize!", true);
		}

		if (inputVOName != null && inputVOName.length() > 0) {
			odeVO.setClassName(inputVOName);
		} else {
			odeVO.setClassName(inputVO.voClassName());
		}
		odeVO.setSerializationForm((byte) 'B');
		odeVO.setSerializedData(byteOutput.toByteArray());

		return odeVO;
	}
	
	/**
	 * Converts JSON object to Google proto buffer format object.
	 * 
	 * @param json
	 * @return
	 */
	public static DysonEnvelopeSchema.DysonEnvelope convert2Protobuf(JSONObject json) {
		DysonEnvelopeSchema.DysonEnvelope.Builder builder = DysonEnvelopeSchema.DysonEnvelope.newBuilder();
		@SuppressWarnings("unchecked")
		Set<String> jsonKeySet = new HashSet<String>(json.keySet());
		for(FieldDescriptor desc: builder.getDescriptorForType().getFields()) {
			if(json.has(desc.getName())) {
				switch(desc.getJavaType()) {
					case INT:
						builder.setField(desc, json.optInt(desc.getName()));
						break;
					case LONG:
						builder.setField(desc, json.optLong(desc.getName()));
						break;
					case FLOAT:
					case DOUBLE:
						builder.setField(desc, json.optDouble(desc.getName()));
						break;
					case BOOLEAN:
						builder.setField(desc, json.optBoolean(desc.getName()));
						break;
					case STRING:
						if(desc.isRepeated()) {
							for(String t: json.optString(desc.getName()).split(",")) {
								builder.addRepeatedField(desc, t);
							}
						} else {
							builder.setField(desc, json.optString(desc.getName()));
						}
						break;
					case MESSAGE:
					default:
				}
			}
			jsonKeySet.remove(desc.getName());
		}

		for(String jsonKey: jsonKeySet) {
			builder.addKeyValueFields(DysonEnvelopeSchema.KeyValueItem.newBuilder().setKey(jsonKey)
					.setValue(json.getString(jsonKey)).build());
		}

		return builder.build();
	}
	
	/**
	 * Converts DysonEnvelope message to the JSON object.
	 * 
	 * @param message DysonEnvelope message
	 * @return JSONObject
	 */
	public static JSONObject convertDysonEnvelope2JSON(DysonEnvelope message) {
		JSONObject json = new JSONObject();
		for(Entry<FieldDescriptor, Object> entry: message.getAllFields().entrySet()) {
			FieldDescriptor desc = entry.getKey();
			if(!desc.getJavaType().equals(JavaType.MESSAGE)) {
				json.put(desc.getName(), entry.getValue());
			}
		}

		if(message.getKeyValueFieldsList() != null) {
			for(DysonEnvelopeSchema.KeyValueItem item: message.getKeyValueFieldsList()) {
				json.put(item.getKey(), item.getValue());
			}
		}
		return json;
	}
	
	/**
	 * Converts RaaSTxnCtxEnvelope message to the JSON object.
	 * 
	 * @param message RaaSTxnCtxEnvelope message
	 * @return JSONObject
	 */
	public static JSONObject convertSTCEnvelope2JSON(RaaSTxnCtxEnvelope message) {
		JSONObject json = new JSONObject();
		for(Entry<FieldDescriptor, Object> entry: message.getAllFields().entrySet()) {
			FieldDescriptor desc = entry.getKey();
			if(!desc.getJavaType().equals(JavaType.MESSAGE)) {
				json.put(desc.getName(), entry.getValue());
			}
		}

		if(message.getAdditionalDataList() != null) {
			for(KeyValuePair item: message.getAdditionalDataList()) {
				json.put(item.getKey(), item.getValue());
			}
		}
		return json;
	}

}
