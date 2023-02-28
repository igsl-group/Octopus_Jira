package com.igsl.configmigration;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A bidirectional reference for JiraConfigDTO parent-child relationships
 */
public class JiraConfigRef extends JiraConfigDTO {

	@JsonIgnore
	private JiraConfigDTO parent;
	@JsonIgnore
	private JiraConfigDTO child;
	
	private String parentInternalId;
	private String parentUniqueKey;
	private String childInternalId;
	private String childUniqueKey;
	
	public void setJiraConfigDTO(JiraConfigDTO parent, JiraConfigDTO child) {
		this.parent = parent;
		this.parentInternalId = parent.getInternalId();
		this.parentUniqueKey = parent.getUniqueKey();
		this.child = child;
		if (child != null) {
			this.childInternalId = child.getInternalId();
			this.childUniqueKey = child.getUniqueKey();
			this.child.addReference(this);
		}
	}
	
	public void updateChild(JiraConfigDTO child) {
		if (this.child != null) {
			this.child.removeReference(this);
			this.childInternalId = null;
			this.childUniqueKey = null;
		}
		this.child = child;
		if (child != null) {
			this.childUniqueKey = child.getUniqueKey();
			this.child.addReference(this);
		}
	}
	
	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList(
				"getParent",
				"getChild");
	}

	@Override
	public Class<?> getJiraClass() {
		return null;
	}

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return null;
	}

	@Override
	public String getUniqueKey() {
		return getChildUniqueKey();
	}

	@Override
	public String getInternalId() {
		return getChildInternalId();
	}

	@Override
	protected void fromJiraObject(Object obj) throws Exception {
		throw new IllegalStateException("JiraConfigRef does not store Jira object");
	}

	public JiraConfigDTO getParent() {
		return parent;
	}

	public JiraConfigDTO getChild() {
		return child;
	}

	public String getParentInternalId() {
		return parentInternalId;
	}

	public String getParentUniqueKey() {
		return parentUniqueKey;
	}

	public String getChildInternalId() {
		return childInternalId;
	}

	public String getChildUniqueKey() {
		return childUniqueKey;
	}

}
