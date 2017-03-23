package com.paypal.risk.rda.cache.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * This class is used to load property files which has all configurable key and values.
 * 
 * @author anprakash
 *
 */
public class ApplicationProperties {
	private static Properties properties = new Properties();
	
	static{
		InputStream fis=null;
		try {
			fis = new FileInputStream("src/test/resources/app.properties");
			properties.load(fis);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static Properties get() {
		return properties;
	}

}
