package com.igsl.configmigration;

public class ParseUtil {
	
	public static Long tryParseLong(String value) {
		try {
			return Long.parseLong(value); 
		} catch (Exception ex) {
			return null;
		}
	}
}
