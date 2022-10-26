package com.nextlabs.bae.bean;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.annotation.PreDestroy;
import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.naming.directory.Attributes;
import javax.naming.ldap.LdapContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.nextlabs.bae.adhelper.ActiveDirectoryHelper;
import com.nextlabs.bae.common.PropertyLoader;
import com.nextlabs.bae.dbhelper.DBHelper;

/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

@ManagedBean(name = "userSessionBean")
@SessionScoped
public class UserSessionBean implements Serializable {

	private static final Log LOG = LogFactory.getLog(UserSessionBean.class);
	private static final long serialVersionUID = 1L;
	private String userName;
	private String password;
	private String errorMessage;
	private boolean isLoggedIn = false;
	private List<String> userGroups;
	private Attributes loggedInUser;
	private String lastViewedActivity;
	private boolean isAdmin;

	/*
	 * run before the login page is rendered
	 */

	public void preRender() {
		try {

			LOG.info("UserSessionBean preRender(): Prerender Login");

			ExternalContext externalContext = FacesContext.getCurrentInstance()
					.getExternalContext();

			updateProperties();

			// redirect to home page if user is logged in
			if (isLoggedIn) {
				LOG.info("UserSessionBean preRender(): User is logged in");
				if (checkDB()) {
					externalContext.redirect(externalContext
							.getRequestContextPath() + "/home.xhtml");
				}
				return;
			}

			isLoggedIn = false;

		} catch (Exception e) {
			LOG.error("UserSessionBean preRender(): " + e.getMessage(), e);
		}
	}

	/*
	 * run before all other pages are rendered
	 */

	public void preRenderBasic() {
		try {
			LOG.info("UserSessionBean preRenderBasic(): Prerender Pages");
			if (!isLoggedIn) {
				unauthorize();
				return;
			}

			// if properties are not loaded, load them

			if (PropertyLoader.propertiesPath == null
					|| PropertyLoader.constantPath == null) {
				updateProperties();
			}
		} catch (Exception e) {
			LOG.error("UserSessionBean preRenderBasic(): " + e.getMessage(), e);
		}
	}

	/*
	 * unauthorized handlers, redirect user to login page
	 */
	private void unauthorize() {
		LOG.info("UserSessionBean unauthorize(): Not logged in");
		// not logged in
		try {
			FacesContext.getCurrentInstance().getExternalContext()
					.redirect("login.xhtml");
		} catch (Exception e) {
			LOG.error("UserSessionBean unauthorize(): " + e.getMessage(), e);
		}
	}

	/*
	 * verify database connection
	 */
	public boolean checkDB() {
		Connection connection = DBHelper.getDatabaseConnection();
		if (connection == null) {
			returnMessage("login-button", FacesMessage.SEVERITY_ERROR,
					"DB_FAILED_MSG", "DB_FAILED_DES");
			return false;
		} else {
			try {
				connection.close();
			} catch (SQLException e) {
				LOG.error("UserSessionBean checkDB(): Cannot close DB connection");
			}
			return true;
		}
	}

	/*
	 * update properties
	 */
	public void updateProperties() {
		// get properties file
		FacesContext ctx = FacesContext.getCurrentInstance();
		String propertiesPath = ctx.getExternalContext().getInitParameter(
				"RecordManagementProperties");
		String constantPath = ctx.getExternalContext().getInitParameter(
				"RecordManagementConstant");

		PropertyLoader.propertiesPath = propertiesPath;
		PropertyLoader.constantPath = constantPath;

		LOG.info("Properties file path: " + PropertyLoader.propertiesPath);
		LOG.info("Constant file path: " + PropertyLoader.constantPath);

		PropertyLoader.loadProperties();
		PropertyLoader.loadConstant();
	}

	public UserSessionBean() {
	}

	/*
	 * login method
	 */
	public String login() {
		try {
			if (!checkDB()) {
				return null;
			}

			if (userName == null || userName.trim().equals("")
					|| password == null || password.trim().equals("")) {
				LOG.info("UserSessionBean login(): Invalid input");
				returnMessage(null, FacesMessage.SEVERITY_ERROR,
						"LOGIN_FAILED_MSG", "LOGIN_FAILED_INVALID_INPUT_DES");
				return null;
			}

			// AD Authentication
			LdapContext context = ActiveDirectoryHelper.getLDAPContext(
					userName, password);
			if (context == null) {
				LOG.info("Invalid user name or password");
				returnMessage(null, FacesMessage.SEVERITY_ERROR,
						"LOGIN_FAILED_MSG",
						"LOGIN_FAILED_INVALID_CREDENTIAL_DES");
				return null;
			}

			String filter = "(userPrincipalName=" + userName + ")";

			loggedInUser = ActiveDirectoryHelper.getUser(filter, null, context);

			// get all groups that the logged in user is member of
			userGroups = ActiveDirectoryHelper.getAllUserGroup(filter, context);

			String recordOfficersGroup = PropertyLoader.bAESProperties
					.getProperty("record-officers-group");
			String administratorsGroup = PropertyLoader.bAESProperties
					.getProperty("administrators-group");

			if (recordOfficersGroup == null || administratorsGroup == null) {
				LOG.error("Cannot get security groups from properties file");
				returnMessage(null, FacesMessage.SEVERITY_ERROR,
						"LOGIN_FAILED_MSG",
						"LOGIN_FAILED_NO_SECURITY_GROUPS_DES");
				return null;
			}

			if (userGroups.contains(recordOfficersGroup.trim())) {
				isAdmin = false;
			} else if (userGroups.contains(administratorsGroup.trim())) {
				isAdmin = true;
			} else {
				LOG.info("UserSessionBean login(): Unauthorized user "
						+ userName + " tried to login");
				returnMessage(null, FacesMessage.SEVERITY_ERROR,
						"UNAUTHORIZED_MSG", "UNAUTHORIZED_DES");
				return null;
			}

			LOG.info("UserSessionBean login(): Logged in user: "
					+ ActiveDirectoryHelper.getAttribute(loggedInUser,
							"distinguishedName"));

			// set login flag
			isLoggedIn = true;
			errorMessage = "";

			context.close();
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return "home?faces-redirect=true";
	}

	/*
	 * logout method
	 */
	public String logout() {
		isLoggedIn = false;
		FacesContext.getCurrentInstance().getExternalContext()
				.invalidateSession();
		return "login?faces-redirect=true";
	}

	public void returnUnexpectedError(String id) {
		returnMessage(id, FacesMessage.SEVERITY_ERROR, "UNEXPECTED_ERROR_MSG",
				"UNEXPECTED_ERROR_DES");
	}

	public void returnMessage(String id, Severity level, String sum, String des) {
		FacesContext.getCurrentInstance().addMessage(
				id,
				new FacesMessage(level, PropertyLoader.bAESConstant
						.getProperty(sum), PropertyLoader.bAESConstant
						.getProperty(des)));
	}

	public String getContextHelper(String message) {
		try {
			if (PropertyLoader.bAESConstant == null) {
				FacesContext.getCurrentInstance().getExternalContext()
						.redirect("login.xhtml");
				return null;
			} else {
				return PropertyLoader.bAESConstant.getProperty(message);
			}
		} catch (Exception e) {
			LOG.error("UserSessionBean getContextHelper(): " + e.getMessage(),
					e);
			return null;
		}
	}

	/*
	 * run before the session destroyed
	 */
	@PreDestroy
	public void preDestroy() {
		PropertyLoader.bAESConstant = null;
		PropertyLoader.bAESProperties = null;
		isLoggedIn = false;
	}

	/*
	 * getters and setters
	 */
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public boolean isLoggedIn() {
		return isLoggedIn;
	}

	public void setLoggedIn(boolean isLoggedIn) {
		this.isLoggedIn = isLoggedIn;
	}

	public List<String> getUserGroups() {
		return userGroups;
	}

	public void setUserGroups(List<String> userGroups) {
		this.userGroups = userGroups;
	}

	public String getLastViewedActivity() {
		return lastViewedActivity;
	}

	public void setLastViewedActivity(String lastViewedActivity) {
		this.lastViewedActivity = lastViewedActivity;
	}

	public Attributes getLoggedInUser() {
		return loggedInUser;
	}

	public void setLoggedInUser(Attributes loggedInUser) {
		this.loggedInUser = loggedInUser;
	}

	public boolean getIsAdmin() {
		return isAdmin;
	}

	public void setIsAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
	}

}
