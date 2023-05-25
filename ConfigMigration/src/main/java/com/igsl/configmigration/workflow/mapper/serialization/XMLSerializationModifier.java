package com.igsl.configmigration.workflow.mapper.serialization;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;
import com.fasterxml.jackson.dataformat.xml.ser.XmlBeanSerializer;

/**
 * Serialization modifier for Jackson XML.
 */
public class XMLSerializationModifier extends BeanSerializerModifier {
	@Override
	public JsonSerializer<?> modifySerializer(
			SerializationConfig config, 
			BeanDescription beanDesc,
			JsonSerializer<?> serializer) {
		if (!(serializer instanceof BeanSerializerBase)) {
			return serializer;
		}
		XmlBeanSerializer defaultSerializer = new XmlBeanSerializer((BeanSerializerBase) serializer);
		// If bean description is a XMLDocumentHeader, use our custom serializer
		if (XMLDocumentHeader.class.isAssignableFrom(beanDesc.getBeanClass())) {
			return new XMLSerializer(defaultSerializer);
		}
		// Otherwise use default
		return defaultSerializer;
	}
}
