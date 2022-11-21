package com.igsl.configmigration;

import java.io.IOException;
import java.util.Map;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ValueNode;

public class JiraConfigUtilDeserializer extends StdDeserializer<JiraConfigUtil> {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(JiraConfigUtilDeserializer.class);
	
	public static final String IMPLMEMENTATION = "implementation";
	
	protected JiraConfigUtilDeserializer() {
		super(JiraConfigUtil.class);
	}

	@Override
	public JiraConfigUtil deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
		TreeNode node = p.readValueAsTree();		
		TreeNode impl = node.get(IMPLMEMENTATION);
		if (impl != null && impl.isValueNode()) {
			LOGGER.debug("Node is value node");
			ValueNode vn = (ValueNode) impl;
			String implementation = vn.asText();
			LOGGER.debug("Implementation: [" + implementation + "]");
			Map<String, Class<? extends JiraConfigUtil>> map = JiraConfigTypeRegistry.getConfigUtilMap();
			if (map.containsKey(implementation)) {
				Class<? extends JiraConfigUtil> cls = map.get(implementation);
				LOGGER.debug("cls: " + cls.getCanonicalName());
				JiraConfigUtil result = p.getCodec().treeToValue(node, cls);
				LOGGER.debug("result: " + result);
				return result;
			} else {
				LOGGER.debug("ERROR: No match found in map");
			}
		} else {
			LOGGER.debug("ERROR: Node is not value node");
		}
		return null;
	}

}
