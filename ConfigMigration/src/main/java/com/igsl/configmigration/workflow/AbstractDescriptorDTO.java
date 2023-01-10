package com.igsl.configmigration.workflow;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;
import com.opensymphony.workflow.loader.AbstractDescriptor;

@JsonDeserialize(using = JsonDeserializer.None.class)
public abstract class AbstractDescriptorDTO extends JiraConfigDTO {
	
	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList();
	}

	@Override
	public Class<?> getJiraClass() {
		return AbstractDescriptor.class;
	}

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return null;
	}

}
