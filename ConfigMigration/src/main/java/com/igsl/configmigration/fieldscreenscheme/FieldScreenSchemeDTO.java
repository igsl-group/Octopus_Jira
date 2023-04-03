package com.igsl.configmigration.fieldscreenscheme;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeItem;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigProperty;
import com.igsl.configmigration.JiraConfigUtil;

/**
 * Status wrapper.
 */
@JsonDeserialize(using = JsonDeserializer.None.class)
public class FieldScreenSchemeDTO extends JiraConfigDTO {
	
	private static final Logger LOGGER = Logger.getLogger(FieldScreenSchemeDTO.class);
	
	private String description;
	private Long id;
	private String name;
	private List<FieldScreenSchemeItemDTO> fieldScreenSchemeItems;
	
	@Override
	public void fromJiraObject(Object obj) throws Exception {
		FieldScreenScheme o = (FieldScreenScheme) obj;
		this.description = o.getDescription();
		this.fieldScreenSchemeItems = new ArrayList<>();
		for (FieldScreenSchemeItem item : o.getFieldScreenSchemeItems()) {
			FieldScreenSchemeItemDTO dto = new FieldScreenSchemeItemDTO();
			dto.setJiraObject(item, this);
			this.fieldScreenSchemeItems.add(dto);
		}
		this.id = o.getId();
		this.name = o.getName();
		this.uniqueKey = this.name;
		LOGGER.debug("ScreenScheme [" + name + "] has scheme items: " + this.fieldScreenSchemeItems.size());
	}

	@Override
	protected Map<String, JiraConfigProperty> getCustomConfigProperties() {
		Map<String, JiraConfigProperty> r = new TreeMap<>();
		r.put("Description", new JiraConfigProperty(this.description));
		r.put("Field Screen Scheme Items", 
				new JiraConfigProperty(FieldScreenSchemeUtil.class, this.fieldScreenSchemeItems));
		r.put("ID", new JiraConfigProperty(this.id));
		r.put("Name", new JiraConfigProperty(this.name));
		return r;
	}

	@Override
	public String getInternalId() {
		return Long.toString(this.getId());
	}
	
	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList(
				"getName",
				"getDescription",
				"getFieldScreenSchemeItems");
	}

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return FieldScreenSchemeUtil.class;
	}

	@Override
	public Class<?> getJiraClass() {
		return FieldScreenScheme.class;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<FieldScreenSchemeItemDTO> getFieldScreenSchemeItems() {
		return fieldScreenSchemeItems;
	}

	public void setFieldScreenSchemeItems(List<FieldScreenSchemeItemDTO> fieldScreenSchemeItems) {
		this.fieldScreenSchemeItems = fieldScreenSchemeItems;
	}

}
