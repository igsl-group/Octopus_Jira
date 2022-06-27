package com.igsl.customfieldtypes.rest;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;
import com.atlassian.query.operator.Operator;
import com.google.gson.Gson;
/**
 * REST API for IGSL custom field types.
 */
@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class RestApi {

	private static Logger LOGGER = LoggerFactory.getLogger(RestApi.class);
	
	@POST
	@Path("/getCustomFieldDefaultValue")
	public Response getCustomFieldDefaultValue(GetCustomFieldDefaultValueData data) {
		Object result = null;
		try {
			MutableIssue issue = ComponentAccessor.getIssueManager().getIssueObject(data.getIssueKey());
			CustomField field = ComponentAccessor.getFieldManager().getCustomField(data.getCustomFieldId());
			if (field != null) {
				data.setResult(field.getDefaultValue(issue));
				result = data;
			}
		} catch (Exception e) {
			LOGGER.error("Error getting default value", e);
			return Response.status(HttpServletResponse.SC_BAD_REQUEST).build();
		}
		return Response.ok().entity(result).build();
	}
	
	/**
	 * Get issue data for change request custom field type.
	 * @param data ChangeRequestData
	 * @return Response containing List of ChangeRequestData
	 */
	@POST
	@Path("/changeRequest")
	public Response changeRequest(ChangeRequestData data) {
		List<ChangeRequestData> result = new ArrayList<ChangeRequestData>();
		try {
			ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
			JqlQueryBuilder builder = JqlQueryBuilder.newBuilder();
			if (data != null && data.getSearchString() != null) {
				JqlClauseBuilder search = 
					JqlQueryBuilder.newClauseBuilder()
						.addStringCondition("summary", Operator.LIKE, data.getSearchString())
						.or().addStringCondition("id", Operator.LIKE, data.getSearchString())
						.or().addStringCondition("key", Operator.LIKE, data.getSearchString())
						.or().addStringCondition("description", Operator.LIKE, data.getSearchString());
				builder.where().and().addClause(search.buildClause());
			}
			Query q = builder.buildQuery();
			LOGGER.debug("Query: " + q.toString());
			SearchProvider searchProvider = ComponentAccessor.getComponent(SearchProvider.class);
			SearchResults searchResults = searchProvider.search(q, user, PagerFilter.getUnlimitedFilter());
			for (Issue issue : searchResults.getIssues()) {
				ChangeRequestData item = new ChangeRequestData();
				item.setIssueId(issue.getId());
				item.setIssueKey(issue.getKey());
				item.setProjectManager(issue.getProjectObject().getLeadUserName());
				item.setIssueSummary(issue.getSummary());
				result.add(item);
				LOGGER.debug("item: " + new Gson().toJson(item));
			}
		} catch (Exception e) {
			LOGGER.error("Error searching", e);
			return Response.status(HttpServletResponse.SC_BAD_REQUEST).build();
		}
		GenericEntity<List<ChangeRequestData>> entity = new GenericEntity<List<ChangeRequestData>>(result) {};
		return Response.ok().entity(result).build();
	}
	
}
