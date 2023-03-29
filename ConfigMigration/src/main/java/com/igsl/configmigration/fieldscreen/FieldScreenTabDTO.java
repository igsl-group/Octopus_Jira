package com.igsl.configmigration.fieldscreen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;
import com.atlassian.jira.issue.status.Status;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigProperty;
import com.igsl.configmigration.JiraConfigUtil;

/**
 * Status wrapper.
 */
@JsonDeserialize(using = JsonDeserializer.None.class)
public class FieldScreenTabDTO extends JiraConfigDTO {
	
	private Long id;
	private String name;
	private int position;
	private List<FieldScreenLayoutItemDTO> fieldScreenLayoutItems;
	
	/**
	 * #0: FieldScreenDTO
	 */
	@Override
	protected int getObjectParameterCount() {
		return 1;
	}
	
	@Override
	public void fromJiraObject(Object obj) throws Exception {
		FieldScreenDTO screen = (FieldScreenDTO) this.objectParameters[0];
		FieldScreenTab o = (FieldScreenTab) obj;
		this.id = o.getId();
		this.name = o.getName();
		this.position = o.getPosition();
		// Unique key needs to represent the tab in the parent field screen.
		// Name alone will have conflicts as the default tab in all screens are the same.
		// ID will result in the same tab created multiple times. 
		// So the solution is to include the screen's name 
		this.uniqueKey = screen.getUniqueKey() + "." + this.name;
		// Items in tab
		this.fieldScreenLayoutItems = new ArrayList<>();
		for (FieldScreenLayoutItem item : o.getFieldScreenLayoutItems()) {
			FieldScreenLayoutItemDTO dto = new FieldScreenLayoutItemDTO();
			dto.setJiraObject(item, this);
			fieldScreenLayoutItems.add(dto);
		}
	}
	
	@Override
	protected void setupRelatedObjects() throws Exception {
		for (FieldScreenLayoutItemDTO item : this.fieldScreenLayoutItems) {
			addRelatedObject(item);
			item.addReferencedObject(this);
		}
	}
	
	@Override
	public String getConfigName() {
		return this.name;
	}

	@Override
	protected Map<String, JiraConfigProperty> getCustomConfigProperties() {
		Map<String, JiraConfigProperty> r = new TreeMap<>();
		r.put("ID", new JiraConfigProperty(this.id));
		r.put("Name", new JiraConfigProperty(this.name));
		r.put("Position", new JiraConfigProperty(this.position));
		r.put("Field Screen Layout Items", 
				new JiraConfigProperty(FieldScreenLayoutItemUtil.class, this.fieldScreenLayoutItems));
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
				"getPosition",
				"getFieldScreenLayoutItems");
	}

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return FieldScreenTabUtil.class;
	}

	@Override
	public Class<?> getJiraClass() {
		return FieldScreenTab.class;
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

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public List<FieldScreenLayoutItemDTO> getFieldScreenLayoutItems() {
		return fieldScreenLayoutItems;
	}

	public void setFieldScreenLayoutItems(List<FieldScreenLayoutItemDTO> fieldScreenLayoutItems) {
		this.fieldScreenLayoutItems = fieldScreenLayoutItems;
	}

}
