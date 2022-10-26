package com.nextlabs.bae.bean;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.nextlabs.bae.common.PropertyLoader;

@ManagedBean(name = "manageHomeBean")
@ViewScoped
public class ManageHomeBean implements Serializable {

	private static final Log LOG = LogFactory.getLog(ManageHomeBean.class);
	private static final long serialVersionUID = 1L;
	private String pageTitle;
	@ManagedProperty(value = "#{userSessionBean}")
	private UserSessionBean userSessionBean;

	@PostConstruct
	public void init() {
		try {
			if (!userSessionBean.checkDB()) {
				returnMessage("database", FacesMessage.SEVERITY_ERROR,
						"DB_FAILED_MSG", "DB_FAILED_DES");
				return;
			}
			pageTitle = "Quick Start";
		} catch (Exception e) {
			LOG.error("ManageHomeBean init():" + e.getMessage(), e);
			returnUnexpectedError(null);
		}

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

	/*
	 * getters and setters
	 */
	public UserSessionBean getUserSessionBean() {
		return userSessionBean;
	}

	public void setUserSessionBean(UserSessionBean userSessionBean) {
		this.userSessionBean = userSessionBean;
	}

	public String getPageTitle() {
		return pageTitle;
	}

	public void setPageTitle(String pageTitle) {
		this.pageTitle = pageTitle;
	}
	
	

}
