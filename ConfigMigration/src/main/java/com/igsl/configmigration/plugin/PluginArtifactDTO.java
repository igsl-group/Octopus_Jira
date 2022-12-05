package com.igsl.configmigration.plugin;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import com.atlassian.plugin.PluginArtifact;
import com.atlassian.plugin.ReferenceMode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class PluginArtifactDTO extends JiraConfigDTO {

	private static final Logger LOGGER = Logger.getLogger(PluginArtifactDTO.class);
	
	@JsonIgnore
	private long artifactSize;
	private String artifactData;
	private String name;
	private ReferenceMode referenceMode;
	
	@Override
	public void fromJiraObject(Object o) throws Exception {
		PluginArtifact obj = (PluginArtifact) o;
		try (	BufferedInputStream in = new BufferedInputStream(obj.getInputStream());  
				ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			int c;
			while ((c = in.read()) != -1) {
				out.write(c);
			}
			artifactSize = out.toByteArray().length;
			artifactData = Base64.getEncoder().encodeToString(out.toByteArray());
		}
		this.name = obj.getName();
		this.referenceMode = obj.getReferenceMode();
	}
	
	@JsonIgnore
	public byte[] getArtifactDataBytes() {
		return Base64.getDecoder().decode(this.artifactData);
	}

	@Override
	public String getUniqueKey() {
		return this.getName();
	}

	@Override
	public String getInternalId() {
		return this.getName();
	}

	@Override
	public List<String> getMapIgnoredMethods() {
		return Arrays.asList(
				"getArtifactData",
				"getArtifactDataBytes");
	}
	
	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList(
				"getName",
				"getReferenceMode",
				"getArtifactData");
	}

	public String getArtifactData() {
		return artifactData;
	}

	public void setArtifactData(String artifactData) {
		this.artifactData = artifactData;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ReferenceMode getReferenceMode() {
		return referenceMode;
	}

	public void setReferenceMode(ReferenceMode referenceMode) {
		this.referenceMode = referenceMode;
	}

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return null;
	}

	@Override
	public Class<?> getJiraClass() {
		return PluginArtifact.class;
	}

	public long getArtifactSize() {
		return artifactSize;
	}

}
