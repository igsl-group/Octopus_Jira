package com.igsl.configmigration.workflow.mapper.serialization;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.dataformat.xml.ser.XmlBeanSerializer;

/**
 * Custom Jackson XMLSerializer to inject XML headers.
 */
public class XMLSerializer extends JsonSerializer<XMLDocumentHeader> {

	private final XmlBeanSerializer defaultSerializer;

	public XMLSerializer(XmlBeanSerializer defaultSerializer) {
	    this.defaultSerializer = defaultSerializer;
	}

	@Override
	public void serialize(XMLDocumentHeader src, JsonGenerator generator, SerializerProvider providers) throws IOException {
		if (src.getHeaders() == null || src.getHeaders().isEmpty()) {
			// No custom header, use default
			defaultSerializer.serialize(src, generator, providers);
		    return;
		}
		// Add header
		boolean pretty = providers.isEnabled(SerializationFeature.INDENT_OUTPUT);
		StringBuilder builder = new StringBuilder();
		for (String header : src.getHeaders()) {
			builder.append(header);
		    if (pretty) {
		    	// Add line break
		    	builder.append("\n");
		    }
		}
		generator.writeRaw(builder.toString());
		// Use default
		defaultSerializer.serialize(src, generator, providers);
	}
	
}
