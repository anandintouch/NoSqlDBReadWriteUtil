package com.paypal.risk.rda.cache.common;

import java.util.Properties;

import com.paypal.rti.rtds.RTDSServiceExecutor;


/**
 * This class is used to create and return Service Executor Instance.
 * 
 * @author anprakash
 *
 */
public class RTDSServiceExecutorInstance {
	
	private static volatile RTDSServiceExecutor _instance = null;
	static String rtdsService = "realtimedataserv7233.qa.paypal.com";
	static int rtdsPort=14607;
	
	
	public static RTDSServiceExecutor getServiceExecutorInstance(Properties properties) {
		// Double Checked Locking
		if (_instance == null) {
			synchronized (RTDSServiceExecutorInstance.class) {
				if (_instance == null) {
					_instance = new RTDSServiceExecutor(properties.getProperty("rtdsService"),
							Integer.parseInt(properties.getProperty("rtdsPort")));
				}
			}
		}
		return _instance;
	}

}
