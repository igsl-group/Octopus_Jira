package com.igsl.configmigration;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.SessionData.ImportData;

/**
 * Implementations that are to be displayed in the interface should add @ConfigType annotation.
 */
@JsonDeserialize(using=JiraConfigUtilDeserializer.class)
@JsonIgnoreProperties(value={"implementation"}, allowGetters=true)
public abstract class JiraConfigUtil {
	
	protected static final ObjectMapper OM = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

	/**
	 * Return implementation name. This is used to identify the JiraConfigUtil to be used.
	 * @return String
	 */
	public final String getImplementation() {
		return this.getClass().getCanonicalName();
	}
	
	/**
	 * Get display name of this JiraConfigUtil.
	 * @return
	 */
	@JsonIgnore
	public abstract String getName();
	
	/**
	 * Return TypeReference of a Jira object class for Jackson parser.
	 * @return TypeReference
	 */
	@JsonIgnore
	public abstract TypeReference<?> getTypeReference();
	
	/**
	 * Read all Jira objects in current environment and store them into JiraConfigItem.
	 * @param params Parameters. For most implementations where you can load all items, this is not used. 
	 * @return
	 * @throws Exception
	 */
	public abstract Map<String, JiraConfigItem> readAllItems(Object... params) throws Exception;
	
	/**
	 * Find Jira object
	 * @param params Parameters. For most implementations, #0 is identifier.
	 * @return Object
	 * @throws Exception
	 */
	public abstract Object findObject(Object... params) throws Exception;
	
	/**
	 * Merge items. 
	 * @param oldItem Existing item, can be null.
	 * @param newItem New item.
	 * @return Underlying Jira object.
	 * @throws Exception
	 */
	public abstract Object merge(JiraConfigItem oldItem, JiraConfigItem newItem) throws Exception;
	
	/**
	 * Merge items. Result will be stored in ImportData.
	 * @param items
	 * @throws Exception
	 */
	public abstract void merge(Map<String, ImportData> items) throws Exception;
	
}
