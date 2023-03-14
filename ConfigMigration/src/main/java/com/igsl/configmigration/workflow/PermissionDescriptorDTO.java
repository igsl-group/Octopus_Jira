package com.igsl.configmigration.workflow;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigProperty;
import com.igsl.configmigration.JiraConfigUtil;
import com.opensymphony.workflow.loader.PermissionDescriptor;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class PermissionDescriptorDTO extends AbstractDescriptorDTO {
	
	private int entityId;
	private int id;
	private String name;
	private RestrictionDescriptorDTO restriction;
	
	@Override
	protected void fromJiraObject(Object obj) throws Exception {
		PermissionDescriptor o = (PermissionDescriptor) obj;
		this.entityId = o.getEntityId();
		this.id = o.getId();
		this.name = o.getName();
		//o.getParent();
		this.restriction = new RestrictionDescriptorDTO();
		this.restriction.setJiraObject(o.getRestriction());
		this.uniqueKey = Integer.toString(this.getEntityId());
	}

	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList(
				"getName",
				"getRestriction");
	}

	@Override
	public Class<?> getJiraClass() {
		return PermissionDescriptor.class;
	}

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return null;
	}

	@Override
	public String getInternalId() {
		return this.getUniqueKey();
	}

	public int getEntityId() {
		return entityId;
	}

	public void setEntityId(int entityId) {
		this.entityId = entityId;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public RestrictionDescriptorDTO getRestriction() {
		return restriction;
	}

	public void setRestriction(RestrictionDescriptorDTO restriction) {
		this.restriction = restriction;
	}

	@Override
	protected Map<String, JiraConfigProperty> getCustomConfigProperties() {
		// TODO Auto-generated method stub
		return null;
	}

}
