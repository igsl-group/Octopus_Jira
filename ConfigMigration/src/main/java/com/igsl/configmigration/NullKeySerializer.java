package com.igsl.configmigration;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * To serialize null keys as empty strings for display purpose.
 * Deserialization is not included.
 */
public class NullKeySerializer extends JsonSerializer<Object> {
	
	@Override
	public void serialize(Object nullKey, JsonGenerator jsonGenerator, SerializerProvider unused) throws IOException, JsonProcessingException {
	    jsonGenerator.writeFieldName("");
	}

}