package com.nextlabs.bae.adhelper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.NoPermissionException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.nextlabs.bae.common.PropertyLoader;

public class ActiveDirectoryHelper implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Log LOG = LogFactory
			.getLog(ActiveDirectoryHelper.class);

	/**
	 * Get LdapContext using given credentials
	 * 
	 * @param userName
	 *            login account name
	 * @param password
	 *            password
	 * @return LdapContext
	 */
	public static LdapContext getLDAPContext(String userName, String password) {
		LdapContext ctx = null;
		try {
			Hashtable<String, String> env = new Hashtable<String, String>();
			env.put(Context.INITIAL_CONTEXT_FACTORY,
					"com.sun.jndi.ldap.LdapCtxFactory");
			env.put(LdapContext.CONTROL_FACTORIES,
					"com.sun.jndi.ldap.ControlFactory");
			env.put(Context.SECURITY_AUTHENTICATION, "simple");
			if (PropertyLoader.bAESProperties.getProperty("ssl-authentication")
					.equals("true")) {
				LOG.info("LDAP Authenticated with SSL");
				env.put(Context.SECURITY_PROTOCOL, "ssl");
			}
			env.put(Context.SECURITY_PRINCIPAL, userName);
			env.put(Context.SECURITY_CREDENTIALS, password);
			env.put(Context.PROVIDER_URL,
					"ldap://"
							+ PropertyLoader.bAESProperties
									.getProperty("ad-server")
							+ ":"
							+ PropertyLoader.bAESProperties
									.getProperty("ad-port") + "/");
			env.put(Context.STATE_FACTORIES, "PersonStateFactory");
			env.put(Context.OBJECT_FACTORIES, "PersonObjectFactory");
			ctx = new InitialLdapContext(env, null);

		} catch (Exception ex) {
			LOG.error(
					"ActiveDirectoryHelper getLDAPContext(): "
							+ ex.getMessage(), ex);
		}
		return ctx;
	}

	/**
	 * Get attributes of a user from Active Directory
	 * 
	 * @param filter
	 *            Search filter
	 * @param attributes
	 *            String array containing attributes to be returned
	 * @param ctx
	 *            LdapContext
	 * @return Attributes object
	 */
	public static Attributes getUser(String filter, String[] attributes,
			LdapContext ctx) {
		Attributes returnAttributes = null;
		try {

			LOG.info("ActiveDirectoryHelper getUser(): filter = " + filter);
			SearchControls constraint = new SearchControls();
			if (attributes != null) {
				constraint.setReturningAttributes(attributes);
			}
			constraint.setSearchScope(SearchControls.SUBTREE_SCOPE);

			NamingEnumeration<SearchResult> answer = searchAD(
					PropertyLoader.bAESProperties
							.getProperty("ldap-domain-name"),
					filter, constraint, ctx);

			if (answer == null) {
				LOG.error("ActiveDirectoryHelper getUser(): Error in search: return null");
				return returnAttributes;
			}

			if (!answer.hasMoreElements()) {
				LOG.info("ActiveDirectoryHelper getUser(): No user found!");
				return returnAttributes;
			}

			if (answer.hasMoreElements()) {
				LOG.info("ActiveDirectoryHelper getUser(): User found!");
				SearchResult sr = (SearchResult) answer.next();
				returnAttributes = sr.getAttributes();
			}
		} catch (Exception e) {
			LOG.error("ActiveDirectoryHelper getUser(): " + e.getMessage(), e);
		}
		return returnAttributes;
	}

	/**
	 * Search Active Directory
	 * 
	 * @param domain
	 *            Search domain
	 * @param filter
	 *            Search filter
	 * @param constraint
	 *            Search constraint
	 * @param ctx
	 *            LdapContext
	 * @return A NamingEnumeration of LDAP SearchResult
	 */
	public static NamingEnumeration<SearchResult> searchAD(String domain,
			String filter, SearchControls constraint, LdapContext ctx) {
		NamingEnumeration<SearchResult> answer = null;
		try {
			answer = ctx.search(domain, filter, constraint);
		} catch (NoPermissionException pe) {
			LOG.info("ActiveDirectoryHelper searchAD(): Not enough permission to search AD.Try to use master account instead.");
			ctx = getLDAPContext(
					PropertyLoader.bAESProperties
							.getProperty("edit-account-name"),
					PropertyLoader.bAESProperties
							.getProperty("edit-account-password"));
			try {
				answer = ctx.search(domain, filter, constraint);
			} catch (NamingException ne) {
				LOG.error(
						"ActiveDirectoryHelper searchAD(): " + ne.getMessage(),
						ne);
			}
		} catch (NamingException ne) {
			LOG.error("ActiveDirectoryHelper searchAD(): " + ne.getMessage(),
					ne);
		}
		return answer;
	}

	/**
	 * Get all security groups of a ldap user
	 * 
	 * @param filter
	 *            Search filter for user
	 * @param ctx
	 *            LdapContext
	 * @return List of strings of groups
	 */
	public static List<String> getAllUserGroup(String filter, LdapContext ctx) {
		List<String> result = new ArrayList<String>();
		try {
			String[] attributeID = new String[] { "memberOf" };
			Attributes attributes = getUser(filter, attributeID, ctx);
			Attribute attr = attributes.get("memberOf");
			NamingEnumeration<?> nenum = attr.getAll();
			LOG.info("ActiveDirectoryHelper getAllUserGroup(): Found "
					+ attr.size() + " groups");
			while (nenum.hasMore()) {
				String value = (String) nenum.next();
				String[] tValue = value.split(",");
				tValue = tValue[0].split("=");
				value = tValue[1].trim();
				LOG.info("ActiveDirectoryHelper getAllUserGroup(): Member of: "
						+ value);
				result.add(value);
			}
		} catch (Exception e) {
			LOG.error(
					"ActiveDirectoryHelper getAllUserGroup(): "
							+ e.getMessage(), e);
		}
		return result;
	}

	/**
	 * Get the string value of an attribute of an object
	 * 
	 * @param attributes
	 *            Object attributes
	 * @param attributeName
	 *            Attribute name
	 * @return The String value of the attribute
	 */
	public static String getAttribute(Attributes attributes,
			String attributeName) {
		Attribute attrGet = attributes.get(attributeName);
		if (attrGet == null || attrGet.toString().trim().equals("")) {
			return "";
		} else {
			String temp = attrGet.toString();
			String[] tList = temp.split(":");
			return tList[1].trim();
		}
	}
}
