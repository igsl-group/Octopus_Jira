package com.igsl.configmigration.mailserver;

import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.mail.MailProtocol;
import com.atlassian.mail.server.MailServer;
import com.atlassian.mail.server.MailServerManager;
import com.atlassian.mail.server.PopMailServer;
import com.atlassian.mail.server.SMTPMailServer;
import com.atlassian.mail.server.impl.PopMailServerImpl;
import com.atlassian.mail.server.impl.SMTPMailServerImpl;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.DTOStore;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigUtil;
import com.igsl.configmigration.MergeResult;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class MailServerUtil extends JiraConfigUtil {

	private static final Logger LOGGER = Logger.getLogger(MailServerUtil.class);
	private static MailServerManager MANAGER = ComponentAccessor.getMailServerManager();
	
	@Override
	public String getName() {
		return "Mail Server";
	}
	
	@Override
	public JiraConfigDTO findByInternalId(String id, Object... params) throws Exception {
		Map<String, JiraConfigDTO> map = search(null, params);
		for (JiraConfigDTO dto : map.values()) {
			MailServerDTO s = (MailServerDTO) dto;
			if (s.getId().equals(id)) {
				return s;
			}
		}
		return null;
	}

	@Override
	public JiraConfigDTO findByUniqueKey(String uniqueKey, Object... params) throws Exception {
		Map<String, JiraConfigDTO> map = search(null, params);
		for (JiraConfigDTO dto : map.values()) {
			MailServerDTO s = (MailServerDTO) dto;
			if (s.getUniqueKey().equals(uniqueKey)) {
				return s;
			}
		}
		return null;
	}

	@Override
	public MergeResult merge(
			DTOStore exportStore, JiraConfigDTO oldItem, 
			DTOStore importStore, JiraConfigDTO newItem) throws Exception {
		MergeResult result = new MergeResult();
		MailServerDTO original;
		if (oldItem != null) {
			original = (MailServerDTO) oldItem;
		} else {
			original = (MailServerDTO) findByDTO(newItem);
		}
		MailServerDTO src = (MailServerDTO) newItem;
		MailServer createdJira = null;
		if (original != null) {
			createdJira = (MailServer) original.getJiraObject();
		} else {
			if (src.isSmtp()) {
				createdJira = new SMTPMailServerImpl();
			} else {
				createdJira = new PopMailServerImpl();
			}			
		}
		boolean secure = false;
		switch (src.getMailProtocol()) {
		case SECURE_POP:
		case SECURE_IMAP:
		case SECURE_SMTP:
			secure = true;
			break;
		default: 
			secure = false;
			break;
		}
		createdJira.setDescription(src.getDescription());
		createdJira.setHostname(src.getHostName());
		createdJira.setMailProtocol(src.getMailProtocol());
		createdJira.setName(src.getName());
		createdJira.setPassword(src.getPassword());
		createdJira.setPort(src.getPort());
		createdJira.setSocksHost(src.getSocksHost());
		createdJira.setSocksPort(src.getSocksPort());
		createdJira.setTimeout(src.getTimeout());
		createdJira.setUsername(src.getUserName());
		if (secure) {
			createdJira.setTlsHostnameCheckRequired(src.isTlsHostNameCheckRequired());
		}
		if (src.isSmtp()) {
			SMTPMailServer s = (SMTPMailServer) createdJira;
			s.setPrefix(src.getPrefix());
			s.setDefaultFrom(src.getFromAddress());
			s.setJndiLocation(src.getJndiLocation());
			s.setTlsRequired(src.isTlsRequired());
		}
		if (original != null) {
			MANAGER.update(createdJira);
		} else {
			Long id = MANAGER.create(createdJira);
			createdJira = MANAGER.getMailServer(id);
		}
		MailServerDTO created = new MailServerDTO();
		created.setJiraObject(createdJira);
		result.setNewDTO(created);
		return result;
	}
	
	@Override
	public Class<? extends JiraConfigDTO> getDTOClass() {
		return MailServerDTO.class;
	}

	@Override
	public boolean isVisible() {
		return true;
	}
	
	@Override
	public boolean isReadOnly() {
		return false;
	}

	@Override
	public Map<String, JiraConfigDTO> search(String filter, Object... params) throws Exception {
		for (MailProtocol mp : MailProtocol.values()) {
			LOGGER.debug("Mail protocol: " + mp.getProtocol());
		}
		Map<String, JiraConfigDTO> result = new TreeMap<>();
		for (PopMailServer s : MANAGER.getPopMailServers()) {
			MailServerDTO item = new MailServerDTO();
			item.setJiraObject(s);
			if (!matchFilter(item, filter)) {
				continue;
			}
			result.put(item.getUniqueKey(), item);					
		}
		for (SMTPMailServer s : MANAGER.getSmtpMailServers()) {
			MailServerDTO item = new MailServerDTO();
			item.setJiraObject(s);
			if (!matchFilter(item, filter)) {
				continue;
			}
			result.put(item.getUniqueKey(), item);					
		}
		return result;
	}

}
