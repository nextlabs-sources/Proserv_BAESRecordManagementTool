package com.nextlabs.bae.bean;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.ToggleSelectEvent;
import org.primefaces.event.UnselectEvent;
import org.primefaces.event.data.FilterEvent;

import com.nextlabs.bae.common.PropertyLoader;
import com.nextlabs.bae.dbhelper.ActivityDBHelper;
import com.nextlabs.bae.dbhelper.ActivityDetailDBHelper;
import com.nextlabs.bae.dbhelper.LogDBHelper;
import com.nextlabs.bae.dbhelper.RecordCategoryDBHelper;
import com.nextlabs.bae.entity.Activity;
import com.nextlabs.bae.entity.AuditLog;
import com.nextlabs.bae.entity.RecordCategory;
import com.nextlabs.bae.model.LazyActivityDataModel;
import com.nextlabs.bae.model.LazyRecordCategoryDataModel;

@ManagedBean(name = "manageActivityBean")
@ViewScoped
public class ManageActivityBean implements Serializable {
	private static final Log LOG = LogFactory.getLog(ManageActivityBean.class);
	private static final long serialVersionUID = 1L;

	@ManagedProperty(value = "#{userSessionBean}")
	private UserSessionBean userSessionBean;
	private LazyActivityDataModel activityLazyList;
	private Activity selectedActivity;
	private String selectedActivityValue;
	private Activity newActivity;
	private List<RecordCategory> recordsOfSelectedActivity;
	private LazyRecordCategoryDataModel lazyRecordsOfSelectedActivity;
	private Boolean activityNewActive;

	/**
	 * Run after the bean is constructed
	 */
	@PostConstruct
	public void init() {
		try {
			if (!userSessionBean.checkDB()) {
				returnMessage("database", FacesMessage.SEVERITY_ERROR,
						"DB_FAILED_MSG", "DB_FAILED_DES");
				return;
			}
			activityLazyList = new LazyActivityDataModel();
			newActivity = new Activity();
			activityNewActive = true;
			lazyRecordsOfSelectedActivity = new LazyRecordCategoryDataModel();
		} catch (Exception e) {
			LOG.error("ManageActivityBean init(): " + e.getMessage(), e);
			returnUnexpectedError(null);
		}
	}

	/**
	 * Redirect to manageActivityDetail page to view activity details
	 * 
	 * @param activityName
	 *            Activity name
	 * @return the page
	 */
	public String getActivityDetails(String id) {
		LOG.info("ManageActivityBean getActivityDetails " + id);
		selectedActivity = ActivityDBHelper
				.getActivityById(id);
		if (selectedActivity == null) {
			LOG.error("ManageActivityBean getActivityDetails() Deleted entry");
			returnMessage(null, FacesMessage.SEVERITY_ERROR,
					"RECORD_DELETED_MSG", "RECORD_DELETED_DES");
			return null;
		}
		FacesContext.getCurrentInstance().getExternalContext().getFlash()
				.put("selectedActivity", id);
		return "manageActivityDetail?faces-redirect=true";
	}

	public void onFilter(FilterEvent event) {
	}

	/**
	 * Get substring text of guidance to display in table
	 * 
	 * @param text
	 *            Full text
	 * @return The displayed text
	 */
	public String getInstructionShortText(String text) {
		if (text.indexOf("<br>") != -1) {
			return text.substring(0, text.indexOf("<br>"));
		} else {
			return text;
		}
	}

	/**
	 * Create new activity
	 */
	public void createNewActivity() {
		try {

			// check duplicate
			LOG.info("ManageActivityBean createNewActivity(): Creating new activity");
			if (ActivityDBHelper.getActivityByName(newActivity.getName()) != null) {
				LOG.error("ManageActivityBean createNewActivity() Activity exists");
				returnMessage("create-new-msg", FacesMessage.SEVERITY_ERROR,
						"ACTIVITY_DUPLICATE_MSG", "ACTIVITY_DUPLICATE_DES");
				return;
			}

			// clean rich text
			Jsoup.clean(newActivity.getRecordOfficersGuidance(),
					Whitelist.basic());

			// try to create the activity
			newActivity.setActive(activityNewActive ? 1 : 0);
			if (!ActivityDBHelper.createNewActivity(newActivity.getName(),
					newActivity.getRecordOfficersGuidance(),
					newActivity.getActive())) {
				returnUnexpectedError("create-new-msg");
			} else {
				LogDBHelper.createLog(new AuditLog(new Timestamp(new Date()
						.getTime()), userSessionBean.getUserName(), newActivity
						.getName(), "Activity", "Create", "", "Name: "
						+ newActivity.getName() + " -- Active: "
						+ activityNewActive
						+ " -- Record Officer Guidances: Not recorded in log",
						""));
				RequestContext.getCurrentInstance().execute(
						"PF('create-new-dialog').hide()");
			}

		} catch (Exception e) {
			LOG.error(
					"ManageActivityBean createNewActivity(): " + e.getMessage(),
					e);
			returnUnexpectedError(null);
		}
	}

	/**
	 * Get details of select-to-delete activity
	 */
	public void getDeleteActivity() {
		selectedActivity = ActivityDBHelper
				.getActivityByName(selectedActivityValue);
		if (selectedActivity == null) {
			LOG.error("ManageActivityBean deleteActivity() Deleted entry");
			returnMessage(null, FacesMessage.SEVERITY_ERROR,
					"RECORD_DELETED_MSG", "RECORD_DELETED_DES");
			return;
		}
		RequestContext.getCurrentInstance().execute(
				"PF('delete-dialog').show()");
	}

	/**
	 * Handlers for deleting activity
	 */

	public void deleteActivity() {
		try {
			LOG.info("ManageActivityBean deleteActivity(): Deleting activity "
					+ selectedActivity.getName());
			
			Activity updatedActivity = ActivityDBHelper
					.getActivityById(selectedActivity.getId());
			if (updatedActivity == null) {
				LOG.error("ManageActivityBean deleteActivity() Deleted entry");
				returnMessage(null, FacesMessage.SEVERITY_ERROR,
						"RECORD_DELETED_MSG", "RECORD_DELETED_DES");
				RequestContext.getCurrentInstance().execute(
						"PF('delete-dialog').hide()");
				return;
			}

			// check if there are categories associated with the activity
			if (RecordCategoryDBHelper.getRecordCategoriesByActivity(
					selectedActivity.getId()).size() > 0) {
				LOG.error("ManageActivityBean deleteActivity() Trying to delete activity that is associated with categories");
				returnMessage(null, FacesMessage.SEVERITY_ERROR,
						"ACTIVITY_ASSOCIATED_CATEGORIES_MSG",
						"ACTIVITY_ASSOCIATED_CATEGORIES_DES");
			} else {

				// delete activity's details together with the activity
				if (ActivityDetailDBHelper
						.deleteActivityDetailByActivity(selectedActivity
								.getId())
						&& ActivityDBHelper.deleteActivity(selectedActivity
								.getId())) {
					returnMessage(null, FacesMessage.SEVERITY_INFO,
							"ACTIVITY_SUCCESSFUL_MSG",
							"ACTIVITY_SUCCESSFUL_DELETE_DES");
					LogDBHelper
							.createLog(new AuditLog(
									new Timestamp(new Date().getTime()),
									userSessionBean.getUserName(),
									selectedActivity.getName(),
									"Activity and its Details",
									"Delete",
									"Name: "
											+ selectedActivity.getName()
											+ " -- Active: "
											+ (selectedActivity.getActive() == 1)
											+ " -- Record Officer Guidances: Not recorded in log",
									"", ""));
				} else {
					returnUnexpectedError(null);
				}
			}
			RequestContext.getCurrentInstance().execute(
					"PF('delete-dialog').hide()");

		} catch (Exception e) {
			LOG.error("ManageActivityBean deleteActivity(): " + e.getMessage(),
					e);
			returnUnexpectedError(null);
		}
	}

	public void onToggleSelect(ToggleSelectEvent event) {
	}

	public void onRowUnselect(UnselectEvent event) {
	}

	public void onRowSelect(SelectEvent event) {
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

	public LazyActivityDataModel getActivityLazyList() {
		return activityLazyList;
	}

	public void setActivityLazyList(LazyActivityDataModel activityLazyList) {
		this.activityLazyList = activityLazyList;
	}

	public Activity getSelectedActivity() {
		return selectedActivity;
	}

	public void setSelectedActivity(Activity selectedActivity) {
		this.selectedActivity = selectedActivity;
	}

	public String getSelectedActivityValue() {
		return selectedActivityValue;
	}

	public void setSelectedActivityValue(String selectedActivityValue) {
		this.selectedActivityValue = selectedActivityValue;
	}

	public Activity getNewActivity() {
		return newActivity;
	}

	public void setNewActivity(Activity newActivity) {
		this.newActivity = newActivity;
	}

	public List<RecordCategory> getRecordsOfSelectedActivity() {
		return recordsOfSelectedActivity;
	}

	public void setRecordsOfSelectedActivity(
			List<RecordCategory> recordsOfSelectedActivity) {
		this.recordsOfSelectedActivity = recordsOfSelectedActivity;
	}

	public LazyRecordCategoryDataModel getLazyRecordsOfSelectedActivity() {
		return lazyRecordsOfSelectedActivity;
	}

	public void setLazyRecordsOfSelectedActivity(
			LazyRecordCategoryDataModel lazyRecordsOfSelectedActivity) {
		this.lazyRecordsOfSelectedActivity = lazyRecordsOfSelectedActivity;
	}

	public Boolean getActivityNewActive() {
		return activityNewActive;
	}

	public void setActivityNewActive(Boolean activityNewActive) {
		this.activityNewActive = activityNewActive;
	}

}
