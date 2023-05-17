package com.igsl.configmigration.mailserver;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.atlassian.mail.MailProtocol;
import com.atlassian.mail.server.MailServer;
import com.atlassian.mail.server.SMTPMailServer;
import com.atlassian.mail.server.managers.jira.EventAwareSMTPMailServer;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.igsl.configmigration.JiraConfigDTO;
import com.igsl.configmigration.JiraConfigProperty;
import com.igsl.configmigration.JiraConfigUtil;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class MailServerDTO extends JiraConfigDTO {

	private static final Logger LOGGER = Logger.getLogger(MailServerDTO.class);
	
	private String description;
	private String name;
	private String hostName;
	private Long id;
	private MailProtocol mailProtocol;
	private String password;
	private String port;
	private String socksHost;
	private String socksPort;
	private Long timeout;
	private String type;
	private String userName;
	
	// Applies only if TLS is used in mailProtocol
	private boolean tlsHostNameCheckRequired;
	
	// If type = smtp or class is SMTPMailServer
	private boolean smtp;
	private boolean tlsRequired;
	private String prefix;
	private String fromAddress;
	private String jndiLocation;
	
	@Override
	public void fromJiraObject(Object obj) throws Exception {
		MailServer o = (MailServer) obj;
		LOGGER.debug("Mail Server Object: " + o.getName() + ": " + obj.getClass().getCanonicalName());
		this.description = o.getDescription();
		this.hostName = o.getHostname();
		this.id = o.getId();
		this.mailProtocol = o.getMailProtocol();
		this.name = o.getName();
		this.password = o.getPassword();
		this.port = o.getPort();		
		this.socksHost = o.getSocksHost();
		this.socksPort = o.getSocksPort();
		this.timeout = o.getTimeout();
		this.type = o.getType();
		this.userName = o.getUsername();
		this.tlsHostNameCheckRequired = o.isTlsHostnameCheckRequired();
		if (o instanceof SMTPMailServer) {
			this.smtp = true;
			SMTPMailServer s = (SMTPMailServer) o;
			this.tlsRequired = s.isTlsRequired();
			this.prefix = s.getPrefix();
			this.fromAddress = s.getDefaultFrom();
			this.jndiLocation = s.getJndiLocation();
		}
		this.uniqueKey = this.name;
	}
	
	@Override
	protected Map<String, JiraConfigProperty> getCustomConfigProperties() {
		Map<String, JiraConfigProperty> r = new TreeMap<>();
		r.put("Description", new JiraConfigProperty(this.description));
		r.put("Host Name", new JiraConfigProperty(this.hostName));
		r.put("ID", new JiraConfigProperty(this.id));
		r.put("Mail Protocol", new JiraConfigProperty(this.mailProtocol));
		r.put("Name", new JiraConfigProperty(this.name));
		r.put("Password", new JiraConfigProperty(this.password));
		r.put("Port", new JiraConfigProperty(this.port));
		r.put("SOCKS Host", new JiraConfigProperty(this.socksHost));
		r.put("SOCKS Port", new JiraConfigProperty(this.socksPort));
		r.put("Timeout", new JiraConfigProperty(this.timeout));
		r.put("Type", new JiraConfigProperty(this.type));
		r.put("User Name", new JiraConfigProperty(this.userName));
		r.put("TNS Host Name Check Required", new JiraConfigProperty(this.tlsHostNameCheckRequired));
		if (this.smtp) {
			r.put("Subject Prefix", new JiraConfigProperty(this.prefix));
			r.put("TLS Required", new JiraConfigProperty(this.tlsRequired));
			r.put("Default From Address", new JiraConfigProperty(this.fromAddress));
			r.put("JNDI Session", new JiraConfigProperty(this.jndiLocation));
		}
		return r;
	}
	
	@Override
	public String getInternalId() {
		return this.getName();
	}

	@Override
	protected List<String> getCompareMethods() {
		return Arrays.asList(
				"getName");
	}

	@Override
	public Class<? extends JiraConfigUtil> getUtilClass() {
		return MailServerUtil.class;
	}

	@Override
	public Class<?> getJiraClass() {
		return MailServer.class;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public MailProtocol getMailProtocol() {
		return mailProtocol;
	}

	public void setMailProtocol(MailProtocol mailProtocol) {
		this.mailProtocol = mailProtocol;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getSocksHost() {
		return socksHost;
	}

	public void setSocksHost(String socksHost) {
		this.socksHost = socksHost;
	}

	public String getSocksPort() {
		return socksPort;
	}

	public void setSocksPort(String socksPort) {
		this.socksPort = socksPort;
	}

	public Long getTimeout() {
		return timeout;
	}

	public void setTimeout(Long timeout) {
		this.timeout = timeout;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public boolean isTlsHostNameCheckRequired() {
		return tlsHostNameCheckRequired;
	}

	public void setTlsHostNameCheckRequired(boolean tlsHostNameCheckRequired) {
		this.tlsHostNameCheckRequired = tlsHostNameCheckRequired;
	}

	public boolean isTlsRequired() {
		return tlsRequired;
	}

	public void setTlsRequired(boolean tlsRequired) {
		this.tlsRequired = tlsRequired;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getFromAddress() {
		return fromAddress;
	}

	public void setFromAddress(String fromAddress) {
		this.fromAddress = fromAddress;
	}

	public String getJndiLocation() {
		return jndiLocation;
	}

	public void setJndiLocation(String jndiLocation) {
		this.jndiLocation = jndiLocation;
	}

	public boolean isSmtp() {
		return smtp;
	}

	public void setSmtp(boolean smtp) {
		this.smtp = smtp;
	}

}
