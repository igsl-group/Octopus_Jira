package com.igsl.configmigration.workflow.mapper.serialization;

import java.util.List;

/**
 * Top-level POJO can implement this to add XML headers in serialization output.
 * 
 * Set it up with: 
 * JacksonXmlModule module = new JacksonXmlModule();
 * module.setSerializerModifier(new XMLSerializationModifier());
 * ObjectMapper mapper = new XmlMapper(module);
 */
public interface XMLDocumentHeader {
	
	/**
	 * Return list of XML headers to be included, like XML encoding and DOCTYPE
	 * @return List<String>
	 */
	public List<String> getHeaders();
}
