package com.gildedroses.inventory.operations.utils.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestUtils {

	private static ObjectMapper jsonMapper = new ObjectMapper();
	
	public static <T> T getFileAsJson(final String fileName, final Class<T> clazz) {
		 
		InputStream inventoryStream = TestUtils.class.getResourceAsStream("/" + fileName);
    	try {
			return jsonMapper.readValue(inventoryStream, clazz);
		} catch (IOException e) {
            throw new RuntimeException("Inventory read exception.");
		}
	}
	
	public static <T> List<T> getFileAsJsonArray(final String fileName, final Class<T> clazz) {
		JavaType retType = jsonMapper.getTypeFactory().constructCollectionType(List.class, clazz);
		InputStream inventoryStream = TestUtils.class.getResourceAsStream("/" + fileName);
    	try {
			return jsonMapper.readValue(inventoryStream, retType);
		} catch (IOException e) {
            throw new RuntimeException("Inventory read exception.");
		}
	}
	
	public static <T> String getObjectAsString(T obj) {
		try {
			return jsonMapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			return "";
		}
	}
}
