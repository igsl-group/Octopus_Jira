package com.igsl.ldapuserattributes;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.PartialResultException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

public class LDAPUserAttributes {
	
	private static final Logger LOGGER = Logger.getLogger(LDAPUserAttributes.class);
	private static final ObjectMapper OM = new ObjectMapper();
	private static final String AUTH_METHOD = "simple";
	
	private static long getLDAPDate(Date d) {
		// LDAP timestamp is the no. of 100 ns interval since 1601-01-01 UTC.
		Instant ldapEpoch = Instant.parse("1601-01-01T00:00:00Z");
	    ZoneId zone = ZoneId.systemDefault();
	    LocalDate date = d.toInstant().atZone(zone).toLocalDate();
	    Instant startOfDay = date.atStartOfDay(zone).toInstant();
	    Duration sinceLdapEpoch = Duration.between(ldapEpoch, startOfDay);
	    long ldapTimestamp = sinceLdapEpoch.getSeconds() * 10000000;	
	    return ldapTimestamp;
	    // e.g. 133132608000000000	2022-11-19 00:00:00
	}
	
//	public static void main(String[] args) throws Exception {
//		String[] readAttrs = new String[] {
//			"distinguishedName",
//			"sAMAccountName",
//			"displayName",
//			"mail",
//			"telephone",
//			"memberOf",
//			"createTimestamp",
//			"modifyTimestamp",
//			"objectClass",
//			"dn"
//		};
//		int pageSize = 1000;
//		Map<String, Map<String, List<String>>> ad = getLDAPUsers(
//				"ldap://192.168.56.120:389", 
//				"CN=Administrator,CN=Users,DC=win2022,DC=kcwong,DC=igsl", 
//				"P@ssw0rd", 
//				"CN=Users,DC=win2022,DC=kcwong,DC=igsl", 
//				"(&(objectClass=user))", 
//				SearchControls.SUBTREE_SCOPE,
//				readAttrs,
//				pageSize,
//				false,
//				true,
//				"accountExpires");
//		System.out.println(OM.writeValueAsString(ad));
////		Map<String, Map<String, List<String>>> apacheDS = getLDAPUsers(
////				"ldap://127.0.0.1:10389", 
////				"uid=admin,ou=system", 
////				"admin", 
////				"ou=users,ou=system", 
////				"(&(objectClass=person))", 
////				SearchControls.SUBTREE_SCOPE,
////				readAttrs,
////				pageSize,
////				false,
////				true,
////				"accountExpires");
////		System.out.println(OM.writeValueAsString(apacheDS));
//	}
	
	public static Map<String, Map<String, List<String>>> getLDAPUsers(LDAPUserAttributesConfigData data) throws Exception {
		Set<String> readAttrs = new HashSet<String>();
		readAttrs.addAll(data.getAttributeMap().keySet());
		readAttrs.add(data.getUserNameAttribute());
		return getLDAPUsers(
				data.getProviderURL(),
				data.getUserName(),
				PasswordScrambler.unscramble(data.getEncryptedPassword()),
				data.getBaseDN(),
				data.getFilter(),
				data.getScope(),
				readAttrs.toArray(new String[0]),
				data.getPageSize(),
				data.isReferral(),
				data.isIgnoreExpiredUser(),
				data.getExpiresAttribute());
	}
	
	public static Map<String, Map<String, List<String>>> getLDAPUsers(
			String url, String principal, String credential, 
			String baseDN, String filter, int scope, String[] readAttrs, 
			int pageSize, 
			boolean referral, 
			boolean ignoreExpiredUser, String expiresAttribute) 
			throws Exception {
		Map<String, Map<String, List<String>>> output = new HashMap<String, Map<String, List<String>>>();
		// Note: Jira uses OSGi and does not export com.sun.* classes. 
		// So LdapCtxFactory is not available when using a JobRunner's classloader.
		// We need to switch class loader for this thread.
		final Thread currentThread = Thread.currentThread();
		final ClassLoader originalClassLoader = currentThread.getContextClassLoader();
		try {
			ClassLoader rootClassLoader = ClassLoader.getSystemClassLoader();
			currentThread.setContextClassLoader(rootClassLoader);
			LdapContext ctx = null;
			try {
				Hashtable<String, String> env = new Hashtable<String, String>();
				env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
				env.put(Context.PROVIDER_URL, url);
				env.put(Context.SECURITY_AUTHENTICATION, AUTH_METHOD);
				env.put(Context.SECURITY_PRINCIPAL, principal);
				env.put(Context.SECURITY_CREDENTIALS, credential);
				if (referral) {
					env.put(Context.REFERRAL, "follow");
				} else {
					env.put(Context.REFERRAL, "ignore");
				}
				ctx = new InitialLdapContext(env, null);
				ctx.setRequestControls(new Control[] {
						new PagedResultsControl(pageSize, Control.CRITICAL)
				});
				SearchControls sc = new SearchControls();
				sc.setSearchScope(scope);
				boolean ignoreExpiredAttribute = true;
				if (ignoreExpiredUser) {
					// Add expiresAttribute to the list
					List<String> attrs = new ArrayList<>();
					attrs.addAll(Arrays.asList(readAttrs));
					if (attrs.contains(expiresAttribute)) {
						ignoreExpiredAttribute = false;
					} else {
						attrs.add(expiresAttribute);
					}
					sc.setReturningAttributes(attrs.toArray(new String[0]));
				} else {
					sc.setReturningAttributes(readAttrs);
				}
				sc.setDerefLinkFlag(true);
				sc.setReturningObjFlag(false);
				byte[] pageCookie = null;
				int total = 0;
				LOGGER.debug("Start of LDAP query");
				Date now = new Date();
				do {
					LOGGER.debug("filter: " + filter);
					NamingEnumeration<SearchResult> results = ctx.search(
							baseDN, 
							filter, 
							sc);
					LOGGER.debug("results: " + results);
					int count = 0;
					try {
						while (results != null && results.hasMore()) {
							count++;
							SearchResult result = results.next();
							Attributes attrs = result.getAttributes();
							if (ignoreExpiredUser) {
								Attribute expires = attrs.get(expiresAttribute);
								long expiresValue = Long.parseLong(expires.get().toString());
								if (expiresValue != 0 && expiresValue != 9223372036854775807L && expiresValue <= getLDAPDate(now)) {
									// User expired
									LOGGER.debug("Processed user #" + count + ": Ignored expired user " + result.getName());
									continue;
								}
							}
							Map<String, List<String>> userData = new HashMap<String, List<String>>();
							for (int i = 0; i < readAttrs.length; i++) {
								Attribute attr = attrs.get(readAttrs[i]);
								if (attr != null) {
									if (!ignoreExpiredAttribute && !attr.getID().equals(expiresAttribute)) {
										NamingEnumeration<?> values = attr.getAll();
										List<String> valueList = new ArrayList<String>();
										while (values.hasMore()) {
											Object value = values.next();
											valueList.add(String.valueOf(value));
										}
										userData.put(attr.getID(), valueList);
									}
								}
							}
							output.put(result.getName(), userData);
							LOGGER.debug("Processed user #" + count + ": " + result.getName());
						}
					} catch (PartialResultException prex) {
						if (referral) {
							LOGGER.warn("Partial result found", prex);
						}
						// else ignore it
					}
					Control[] ctrls = ctx.getResponseControls();
					if (ctrls != null) {
						LOGGER.debug("After loop Response controls: " + ctrls.length);
						for (int i = 0; i < ctrls.length; i++) {
							LOGGER.debug("Response control: " + ctrls[i].getID() + " - " + ctrls[i].getClass().getCanonicalName());
							if (ctrls[i] instanceof PagedResultsResponseControl) {
								PagedResultsResponseControl prrc = (PagedResultsResponseControl) ctrls[i];
								total = prrc.getResultSize();
				                pageCookie = prrc.getCookie();
				                LOGGER.debug("New page cookie: " + OM.writeValueAsString(pageCookie));
							}
						}
					}
					ctx.setRequestControls(new Control[] {
				             new PagedResultsControl(pageSize, pageCookie, Control.CRITICAL) 
						});
				} while (pageCookie != null);
			} finally {
				if (ctx != null) {
					ctx.close();
				}
			}
		} finally {
			currentThread.setContextClassLoader(originalClassLoader);
		}
		return output;
	}
	
}
