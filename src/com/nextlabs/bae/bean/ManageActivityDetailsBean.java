package com.nextlabs.bae.bean;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
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

import com.nextlabs.bae.common.PropertyLoader;
import com.nextlabs.bae.dbhelper.ActivityDBHelper;
import com.nextlabs.bae.dbhelper.ActivityDetailDBHelper;
import com.nextlabs.bae.dbhelper.DepartmentOfRecordDBHelper;
import com.nextlabs.bae.dbhelper.LogDBHelper;
import com.nextlabs.bae.dbhelper.RecordCategoryDBHelper;
import com.nextlabs.bae.entity.Activity;
import com.nextlabs.bae.entity.ActivityDetail;
import com.nextlabs.bae.entity.AuditLog;
import com.nextlabs.bae.entity.DepartmentOfRecord;
import com.nextlabs.bae.entity.RecordCategory;
import com.nextlabs.bae.model.LazyActivityDetailDataModel;

/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

@ManagedBean(name = "manageActivityDetailsBean")
@ViewScoped
public class ManageActivityDetailsBean implements Serializable {

	private static final Log LOG = LogFactory
			.getLog(ManageActivityDetailsBean.class);
	private static final long serialVersionUID = 1L;

	private Activity activity;
	private Boolean activityActive;
	private ActivityDetail newActivityDetail;
	private Date newDate;
	private Date editDate;
	private Date oldDate;
	private ActivityDetail selectedActivityDetail;
	private String selectedActivity;
	private LazyActivityDetailDataModel recordActivityList;
	private List<RecordCategory> recordCategoryList;
	private String recordCategoryString;
	@ManagedProperty(value = "#{userSessionBean}")
	private UserSessionBean userSessionBean;
	private List<DepartmentOfRecord> departments;
	private Activity oldActivity;
	private ActivityDetail oldActivityDetail;

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
			departments = DepartmentOfRecordDBHelper
					.getAllDepartmentOfRecords();

			// get activity from flash scope
			selectedActivity = (String) FacesContext.getCurrentInstance()
					.getExternalContext().getFlash().get("selectedActivity");

			if (selectedActivity == null) {
				if (userSessionBean.getLastViewedActivity() == null) {
					LOG.error("ManageActivityDetailsBean init() Cannot receive activity id");
					return;
				} else {
					selectedActivity = userSessionBean.getLastViewedActivity();
				}
			}
			getActivity(selectedActivity);
			newActivityDetail = new ActivityDetail();
		} catch (Exception e) {
			LOG.error("ManageActivityDetailsBean init(): " + e.getMessage(), e);
			returnUnexpectedError(null);
		}
	}

	public ManageActivityDetailsBean() {
	}

	/**
	 * Get information of activity and its related content, including activity
	 * detail list
	 * 
	 * @param activityId
	 *            activityId
	 * @return
	 */
	public String getActivity(String activityId) {
		try {
			activity = ActivityDBHelper.getActivityById(activityId);
			if (activity == null) {
				LOG.error("ManageActivityDetailBean getActivity() Deleted entry");
				returnMessage(null, FacesMessage.SEVERITY_ERROR,
						"ACTIVITY_DELETED_MSG", "ACTIVITY_DELETED_DES");
				FacesContext context = FacesContext.getCurrentInstance();
				context.getExternalContext().getFlash().setKeepMessages(true);
				FacesContext.getCurrentInstance().getExternalContext()
						.redirect("manageActivity.xhtml");
				return null;
			}
			// save old activity to use to check duplicate in activity name on
			// edit
			oldActivity = new Activity(activity.getId(), activity.getName(),
					activity.getRecordOfficersGuidance(), activity.getActive());
			recordActivityList = new LazyActivityDetailDataModel("activity",
					activityId);
			recordCategoryList = RecordCategoryDBHelper
					.getRecordCategoriesByActivity(activityId);

			// build record category string
			recordCategoryString = "";
			for (RecordCategory rc : recordCategoryList) {
				recordCategoryString += rc.getRecordCategory() + ", ";
			}
			if (recordCategoryString.length() > 0) {
				recordCategoryString = recordCategoryString.substring(0,
						recordCategoryString.length() - 2);
			}
			activityActive = (activity.getActive() == 1) ? true : false;
			LOG.info("ManageActivityDetailsBean getActivity() Done!");
		} catch (Exception e) {
			LOG.error(
					"ManageActivityDetailsBean getActivity(): "
							+ e.getMessage(), e);
			returnUnexpectedError(null);
		}
		return null;
	}

	/**
	 * Update activity information
	 */
	public void updateActivity() {
		try {
			// check if any changes have been made in the database by other
			// session
			Activity updatedActivity = ActivityDBHelper
					.getActivityById(activity.getId());
			if (updatedActivity == null) {
				LOG.error("ManageActivityDetailBean updateActivity() Deleted entry");
				returnMessage(null, FacesMessage.SEVERITY_ERROR,
						"ACTIVITY_DELETED_MSG", "ACTIVITY_DELETED_DES");
				FacesContext context = FacesContext.getCurrentInstance();
				context.getExternalContext().getFlash().setKeepMessages(true);
				FacesContext.getCurrentInstance().getExternalContext()
						.redirect("manageActivity.xhtml");
				return;
			}

			if (!oldActivity.equals(updatedActivity)) {
				LOG.error("ManageActivityDetailBean updateActivity() Dirty entry");
				returnMessage(null, FacesMessage.SEVERITY_ERROR,
						"RECORD_DIRTY_MSG", "RECORD_DIRTY_DES");
				this.getActivity(activity.getId());
				return;
			}

			activity.setActive(activityActive ? 1 : 0);
			// clean rich text
			Jsoup.clean(activity.getRecordOfficersGuidance(), Whitelist.basic());

			// check duplicate
			if (!activity.getName().equals(oldActivity.getName())) {
				if (ActivityDBHelper.getActivityByName(activity.getName()) != null) {
					LOG.error("ManageActivityDetailsBean updateActivity() Name exists");
					returnMessage(null, FacesMessage.SEVERITY_ERROR,
							"ACTIVITY_DUPLICATE_MSG", "ACTIVITY_DUPLICATE_DES");
					activity.setName(oldActivity.getName());
					return;
				}
			}

			// try to update
			if (!ActivityDBHelper.updateActivity(activity.getName(),
					activity.getRecordOfficersGuidance(), activity.getId(),
					activity.getActive())) {
				returnUnexpectedError(null);
			}

			LogDBHelper
					.createLog(new AuditLog(
							new Timestamp(new Date().getTime()),
							userSessionBean.getUserName(),
							activity.getName(),
							"Activity",
							"Update",
							"Name: "
									+ oldActivity.getName()
									+ " -- Active: "
									+ (oldActivity.getActive() == 1)
									+ " -- Record Officer Guidances: Not recorded in log",
							"Name: "
									+ activity.getName()
									+ " -- Active: "
									+ (activity.getActive() == 1)
									+ " -- Record Officer Guidances: Not recorded in log",
							"Record Category: " + recordCategoryString));

			oldActivity.setName(activity.getName());
			oldActivity.setActive(activity.getActive());
			oldActivity.setRecordOfficersGuidance(activity
					.getRecordOfficersGuidance());

			returnMessage(null, FacesMessage.SEVERITY_INFO,
					"ACTIVITY_SUCCESSFUL_MSG", "ACTIVITY_SUCCESSFUL_UPDATE_DES");

			RequestContext.getCurrentInstance().execute(
					"PF('edit-activity-dialog').hide()");

		} catch (Exception e) {
			LOG.error(
					"ManageActivityDetailsBean updateActivity(): "
							+ e.getMessage(), e);
			returnUnexpectedError(null);
		}
	}

	/**
	 * Create new activity detail
	 */
	public void createNewActivityDetail() {
		try {

			Activity updatedActivity = ActivityDBHelper
					.getActivityById(activity.getId());
			if (updatedActivity == null) {
				LOG.error("ManageActivityDetailBean createNewActivityDetail() Deleted entry");
				returnMessage(null, FacesMessage.SEVERITY_ERROR,
						"ACTIVITY_DELETED_MSG", "ACTIVITY_DELETED_DES");
				FacesContext context = FacesContext.getCurrentInstance();
				context.getExternalContext().getFlash().setKeepMessages(true);
				FacesContext.getCurrentInstance().getExternalContext()
						.redirect("manageActivity.xhtml");
				return;
			}

			newActivityDetail.setDateValue(new Timestamp(newDate.getTime()));

			// check duplicate
			if (ActivityDetailDBHelper.getActivityDetail(activity.getId(),
					newActivityDetail.getTitleValue(),
					newActivityDetail.getReferenceValue(),
					newActivityDetail.getDateValue()) != null) {
				returnMessage(null, FacesMessage.SEVERITY_ERROR,
						"ACTIVITY_DETAIL_DUPLICATE_MSG",
						"ACTIVITY_DETAIL_DUPLICATE_DES");
				return;
			}

			// try to create
			if (ActivityDetailDBHelper.createNewActivityDetail(
					activity.getId(), newActivityDetail.getTitleValue(),
					newActivityDetail.getReferenceValue(),
					newActivityDetail.getDateValue())) {
				returnMessage(null, FacesMessage.SEVERITY_INFO,
						"ACTIVITY_DETAIL_SUCCESSFUL_MSG",
						"ACTIVITY_DETAIL_SUCCESSFUL_CREATE_DES");

				// create log
				LogDBHelper.createLog(new AuditLog(new Timestamp(new Date()
						.getTime()), userSessionBean.getUserName(),
						newActivityDetail.getTitleValue(), "Activity Detail",
						"Create", "", "Title: "
								+ newActivityDetail.getTitleValue()
								+ " -- Reference: "
								+ newActivityDetail.getReferenceValue()
								+ " -- Date: "
								+ new SimpleDateFormat("MM/dd/yyyy")
										.format(newActivityDetail
												.getDateValue()),
						"Record Category: " + recordCategoryString));
			} else {
				LOG.error("ManageActivityDetailsBean createNewActivityDetail() Cannot create new detail");
				returnUnexpectedError(null);
			}
			RequestContext.getCurrentInstance().execute(
					"PF('create-new-dialog').hide()");
		} catch (Exception e) {
			LOG.error("ManageActivityDetailsBean createNewActivityDetail(): "
					+ e.getMessage(), e);
			returnUnexpectedError(null);
		}
	}

	/**
	 * Update activity detail
	 */
	public void updateActivityDetail() {
		try {

			// check if any changes have been made in the database by other
			// sesison
			Activity updatedActivity = ActivityDBHelper
					.getActivityById(activity.getId());
			if (updatedActivity == null) {
				LOG.error("ManageActivityDetailBean updateActivityDetail() Deleted entry");
				returnMessage(null, FacesMessage.SEVERITY_ERROR,
						"ACTIVITY_DELETED_MSG", "ACTIVITY_DELETED_DES");
				FacesContext context = FacesContext.getCurrentInstance();
				context.getExternalContext().getFlash().setKeepMessages(true);
				FacesContext.getCurrentInstance().getExternalContext()
						.redirect("manageActivity.xhtml");
				return;
			}

			ActivityDetail updatedDetail = ActivityDetailDBHelper
					.getActivityDetail(selectedActivityDetail.getId());
			if (updatedDetail == null) {
				LOG.error("ManageActivityDetailBean updateActivityDetail() Deleted entry");
				returnMessage(null, FacesMessage.SEVERITY_ERROR,
						"RECORD_DELETED_MSG", "RECORD_DELETED_DES");
				RequestContext.getCurrentInstance().execute(
						"PF('edit-dialog').hide()");
				return;
			}

			if (!oldActivityDetail.equals(updatedDetail)) {
				LOG.error("ManageActivityDetailBean updateActivityDetail() Dirty entry");
				returnMessage(null, FacesMessage.SEVERITY_ERROR,
						"RECORD_DIRTY_MSG", "RECORD_DIRTY_DES");
				getSelectedActivityDetail(selectedActivityDetail.getId());
				return;
			}

			// if no changes were made, return immediately
			if (oldActivityDetail.getTitleValue().equals(
					selectedActivityDetail.getTitleValue())
					&& oldActivityDetail.getReferenceValue().equals(
							selectedActivityDetail.getReferenceValue())
					&& oldDate.getTime() == editDate.getTime()) {
				LOG.info("ManageActivityDetailsBean updateActivityDetail() No changes");
				RequestContext.getCurrentInstance().execute(
						"PF('edit-dialog').hide()");
				return;
			}

			oldActivityDetail.setDateValue(new Timestamp(oldDate.getTime()));
			selectedActivityDetail.setDateValue(new Timestamp(editDate
					.getTime()));

			// check duplicate
			if (ActivityDetailDBHelper.getActivityDetail(activity.getId(),
					selectedActivityDetail.getTitleValue(),
					selectedActivityDetail.getReferenceValue(),
					selectedActivityDetail.getDateValue()) != null) {
				returnMessage(null, FacesMessage.SEVERITY_ERROR,
						"ACTIVITY_DETAIL_DUPLICATE_MSG",
						"ACTIVITY_DETAIL_DUPLICATE_DES");
				return;
			}

			// try to update
			if (ActivityDetailDBHelper.updateActivityDetail(
					selectedActivityDetail.getId(), activity.getId(),
					selectedActivityDetail.getTitleValue(),
					selectedActivityDetail.getReferenceValue(),
					selectedActivityDetail.getDateValue())) {
				returnMessage(null, FacesMessage.SEVERITY_INFO,
						"ACTIVITY_DETAIL_SUCCESSFUL_MSG",
						"ACTIVITY_DETAIL_SUCCESSFUL_UPDATE_DES");

				// create log
				LogDBHelper.createLog(new AuditLog(new Timestamp(new Date()
						.getTime()), userSessionBean.getUserName(),
						selectedActivityDetail.getTitleValue(),
						"Activity Detail", "Update", "Title: "
								+ oldActivityDetail.getTitleValue()
								+ " -- Reference: "
								+ oldActivityDetail.getReferenceValue()
								+ " -- Date: "
								+ new SimpleDateFormat("MM/dd/yyyy")
										.format(oldActivityDetail
												.getDateValue()), "Title: "
								+ selectedActivityDetail.getTitleValue()
								+ " -- Reference: "
								+ selectedActivityDetail.getReferenceValue()
								+ " -- Date: "
								+ new SimpleDateFormat("MM/dd/yyyy")
										.format(selectedActivityDetail
												.getDateValue()),
						"Record Category: " + recordCategoryString));
			} else {
				LOG.error("ManageActivityDetailsBean updateActivityDetail() Cannot update detail");
				returnUnexpectedError(null);
			}

			oldActivityDetail.setTitleValue(selectedActivityDetail
					.getTitleValue());
			oldActivityDetail.setReferenceValue(selectedActivityDetail
					.getReferenceValue());
			oldActivityDetail.setDateValue(selectedActivityDetail
					.getDateValue());

			RequestContext.getCurrentInstance().execute(
					"PF('edit-dialog').hide()");
		} catch (Exception e) {
			LOG.error(
					"ManageActivityDetailsBean updateActivityDetail(): "
							+ e.getMessage(), e);
			returnUnexpectedError(null);
		}
	}

	/**
	 * Delete activity detail
	 */
	public void deleteActivityDetail() {
		try {
			// check if changes have been made by other session
			Activity updatedActivity = ActivityDBHelper
					.getActivityById(activity.getId());
			if (updatedActivity == null) {
				LOG.error("ManageActivityDetailBean deleteActivityDetail() Deleted entry");
				returnMessage(null, FacesMessage.SEVERITY_ERROR,
						"ACTIVITY_DELETED_MSG", "ACTIVITY_DELETED_DES");
				FacesContext context = FacesContext.getCurrentInstance();
				context.getExternalContext().getFlash().setKeepMessages(true);
				FacesContext.getCurrentInstance().getExternalContext()
						.redirect("manageActivity.xhtml");
				return;
			}

			ActivityDetail updatedDetail = ActivityDetailDBHelper
					.getActivityDetail(selectedActivityDetail.getId());
			if (updatedDetail == null) {
				LOG.error("ManageActivityDetailBean deleteActivityDetail() Deleted entry");
				returnMessage(null, FacesMessage.SEVERITY_ERROR,
						"RECORD_DELETED_MSG", "RECORD_DELETED_DES");
				RequestContext.getCurrentInstance().execute(
						"PF('delete-dialog').hide()");
				return;
			}

			// try to delete
			if (ActivityDetailDBHelper
					.deleteActivityDetail(selectedActivityDetail.getId())) {
				returnMessage(null, FacesMessage.SEVERITY_INFO,
						"ACTIVITY_DETAIL_SUCCESSFUL_MSG",
						"ACTIVITY_DETAIL_SUCCESSFUL_DELETE_DES");

				LogDBHelper.createLog(new AuditLog(new Timestamp(new Date()
						.getTime()), userSessionBean.getUserName(),
						selectedActivityDetail.getTitleValue(),
						"Activity Detail", "Delete", "Title: "
								+ selectedActivityDetail.getTitleValue()
								+ " -- Reference: "
								+ selectedActivityDetail.getReferenceValue()
								+ " -- Date: "
								+ new SimpleDateFormat("MM/dd/yyyy")
										.format(selectedActivityDetail
												.getDateValue()), "",
						"Record Category: " + recordCategoryString));
			} else {
				LOG.error("ManageActivityDetailsBean deleteActivityDetail() Failed to delete");
			}
			RequestContext.getCurrentInstance().execute(
					"PF('delete-dialog').hide()");
		} catch (Exception e) {
			LOG.error(
					"ManageActivityDetailsBean deleteActivityDetail(): "
							+ e.getMessage(), e);
			returnUnexpectedError(null);
		}
	}

	/**
	 * Check if raw text of rich text field is empty or not
	 * 
	 * @param rich
	 *            Rich text
	 * @return True if empty, False otherwise
	 */
	public boolean checkRichTextEmpty(String rich) {
		String text = Jsoup.parse(rich).text();
		return text.isEmpty();
	}

	/**
	 * Prepare the detail for selected activity detail
	 * 
	 * @param id
	 *            activityId
	 */
	public void getSelectedActivityDetail(String id) {
		try {
			Activity updatedActivity = ActivityDBHelper
					.getActivityById(activity.getId());
			if (updatedActivity == null) {
				LOG.error("ManageActivityDetailBean getSelectedActivityDetail() Deleted entry");
				returnMessage(null, FacesMessage.SEVERITY_ERROR,
						"ACTIVITY_DELETED_MSG", "ACTIVITY_DELETED_DES");
				FacesContext context = FacesContext.getCurrentInstance();
				context.getExternalContext().getFlash().setKeepMessages(true);
				FacesContext.getCurrentInstance().getExternalContext()
						.redirect("manageActivity.xhtml");
				return;
			}

			selectedActivityDetail = ActivityDetailDBHelper
					.getActivityDetail(id);
			if (selectedActivityDetail == null) {
				LOG.error("ManageActivityDetailBean getSelectedActivityDetail() Deleted entry");
				returnMessage(null, FacesMessage.SEVERITY_ERROR,
						"RECORD_DELETED_MSG", "RECORD_DELETED_DES");
				return;
			}

			editDate = new Date(selectedActivityDetail.getDateValue().getTime());
			if (oldActivityDetail == null) {
				oldActivityDetail = new ActivityDetail();
			}
			oldActivityDetail.setId(selectedActivityDetail.getId());
			oldActivityDetail.setReferenceValue(selectedActivityDetail
					.getReferenceValue());
			oldActivityDetail.setTitleValue(selectedActivityDetail
					.getTitleValue());
			oldActivityDetail.setDateValue(selectedActivityDetail
					.getDateValue());
			oldDate = editDate;
			RequestContext.getCurrentInstance().execute(
					"PF('edit-dialog').show()");
		} catch (Exception e) {
			LOG.error("ManageActivityDetailsBean getSelectedActivityDetail(): "
					+ e.getMessage(), e);
			returnUnexpectedError(null);
		}

	}

	/**
	 * 
	 * @param id
	 */
	public void getSelectedActivityDetailForDelete(String id) {
		try {
			Activity updatedActivity = ActivityDBHelper
					.getActivityById(activity.getId());
			if (updatedActivity == null) {
				LOG.error("ManageActivityDetailBean getSelectedActivityDetail() Deleted entry");
				returnMessage(null, FacesMessage.SEVERITY_ERROR,
						"ACTIVITY_DELETED_MSG", "ACTIVITY_DELETED_DES");
				FacesContext context = FacesContext.getCurrentInstance();
				context.getExternalContext().getFlash().setKeepMessages(true);
				FacesContext.getCurrentInstance().getExternalContext()
						.redirect("manageActivity.xhtml");
				return;
			}

			selectedActivityDetail = ActivityDetailDBHelper
					.getActivityDetail(id);
			if (selectedActivityDetail == null) {
				LOG.error("ManageActivityDetailBean getSelectedActivityDetail() Deleted entry");
				returnMessage(null, FacesMessage.SEVERITY_ERROR,
						"RECORD_DELETED_MSG", "RECORD_DELETED_DES");
				return;
			}
			
			RequestContext.getCurrentInstance().execute(
					"PF('delete-dialog').show()");
		} catch (Exception e) {
			LOG.error("ManageActivityDetailsBean getSelectedActivityDetail(): "
					+ e.getMessage(), e);
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

	public Activity getActivity() {
		return activity;
	}

	public UserSessionBean getUserSessionBean() {
		return userSessionBean;
	}

	public void setUserSessionBean(UserSessionBean userSessionBean) {
		this.userSessionBean = userSessionBean;
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}

	public LazyActivityDetailDataModel getActivityDetailList() {
		return recordActivityList;
	}

	public void setActivityDetailList(
			LazyActivityDetailDataModel recordActivityList) {
		this.recordActivityList = recordActivityList;
	}

	public String getSelectedActivity() {
		return selectedActivity;
	}

	public void setSelectedActivity(String selectedActivity) {
		this.selectedActivity = selectedActivity;
	}

	public ActivityDetail getNewActivityDetail() {
		return newActivityDetail;
	}

	public void setNewActivityDetail(ActivityDetail newActivityDetail) {
		this.newActivityDetail = newActivityDetail;
	}

	public Date getNewDate() {
		return newDate;
	}

	public void setNewDate(Date newDate) {
		this.newDate = newDate;
	}

	public Date getEditDate() {
		return editDate;
	}

	public void setEditDate(Date editDate) {
		this.editDate = editDate;
	}

	public ActivityDetail getSelectedActivityDetail() {
		return selectedActivityDetail;
	}

	public void setSelectedActivityDetail(ActivityDetail selectedActivityDetail) {
		this.selectedActivityDetail = selectedActivityDetail;
	}

	public List<RecordCategory> getRecordCategoryList() {
		return recordCategoryList;
	}

	public void setRecordCategoryList(List<RecordCategory> recordCategoryList) {
		this.recordCategoryList = recordCategoryList;
	}

	public LazyActivityDetailDataModel getRecordActivityList() {
		return recordActivityList;
	}

	public void setRecordActivityList(
			LazyActivityDetailDataModel recordActivityList) {
		this.recordActivityList = recordActivityList;
	}

	public String getRecordCategoryString() {
		return recordCategoryString;
	}

	public void setRecordCategoryString(String recordCategoryString) {
		this.recordCategoryString = recordCategoryString;
	}

	public Boolean getActivityActive() {
		return activityActive;
	}

	public void setActivityActive(Boolean activityActive) {
		this.activityActive = activityActive;
	}

	public List<DepartmentOfRecord> getDepartments() {
		return departments;
	}

	public void setDepartments(List<DepartmentOfRecord> departments) {
		this.departments = departments;
	}

	public Activity getOldActivity() {
		return oldActivity;
	}

	public void setOldActivity(Activity oldActivity) {
		this.oldActivity = oldActivity;
	}

}
