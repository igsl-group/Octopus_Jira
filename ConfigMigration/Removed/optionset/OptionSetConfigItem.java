package com.igsl.configmigration.optionset;

import java.util.Map;

import com.atlassian.jira.issue.fields.option.OptionSet;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigItem;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class OptionSetConfigItem extends JiraConfigItem {

	public static final String KEY_OPTION_IDS = "Option IDs";
	public static final String KEY_OPTIONS = "Options";
	public static final String KEY_FIELD_CONFIG = "Field Config";
	
	@Override
	public void fromJiraObject(Object o, Object... params) throws Exception {
		OptionSet obj = (OptionSet) o;
		Map<String, String> map = this.getMap();
		map.put(KEY_NAME, Integer.toString(obj.hashCode()));
		map.put(KEY_ID, Integer.toString(obj.hashCode()));
		map.put(KEY_OPTION_IDS, OM.writeValueAsString(obj.getOptionIds()));
		map.put(KEY_OPTIONS, OM.writeValueAsString(obj.getOptions()));
	}
	
	@Override
	public int compareTo(JiraConfigItem o) {
		if (o != null) {
			return compare(
					this, o, 
					KEY_OPTION_IDS, KEY_OPTIONS, KEY_FIELD_CONFIG);
		}
		return 1;
	}

}
