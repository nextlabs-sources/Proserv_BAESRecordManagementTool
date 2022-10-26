package com.nextlabs.bae.bean;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
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
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.ToggleSelectEvent;
import org.primefaces.event.UnselectEvent;
import org.primefaces.event.data.FilterEvent;

import com.nextlabs.bae.common.PropertyLoader;
import com.nextlabs.bae.dbhelper.ActivityDBHelper;
import com.nextlabs.bae.dbhelper.DepartmentOfRecordDBHelper;
import com.nextlabs.bae.dbhelper.LogDBHelper;
import com.nextlabs.bae.dbhelper.RecordCategoryDBHelper;
import com.nextlabs.bae.entity.Activity;
import com.nextlabs.bae.entity.AuditLog;
import com.nextlabs.bae.entity.DepartmentOfRecord;
import com.nextlabs.bae.entity.Pair;
import com.nextlabs.bae.entity.RecordCategory;
import com.nextlabs.bae.model.LazyRecordCategoryDataModel;

/* 
 * To change this record header, choose Record Headers in Activity Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

@ManagedBean(name = "manageRecordCategoryBean")
@ViewScoped
public class ManageRecordCategoryBean implements Serializable {

	/**
	 * 
	 */
	private static final Log LOG = LogFactory
			.getLog(ManageRecordCategoryBean.class);
	private static final long serialVersionUID = 1L;

	@ManagedProperty(value = "#{userSessionBean}")
	private UserSessionBean userSessionBean;
	private RecordCategory newRecordCategory;
	private LazyRecordCategoryDataModel recordCategoryLazyList;
	private String selectedRecordCategoryValue;
	private RecordCategory selectedRecordCategory;
	private RecordCategory oldRecordCategory;
	private List<Pair<Integer, String>> yearList;
	private List<Pair<Integer, String>> monthList;
	private List<Pair<Integer, String>> dayList;
	private Boolean newRetentionPermanent;
	private Boolean editRetentionPermanent;
	private String selectedRecordCategoryActivity;
	private String newRecordCategoryActivity;
	private Boolean newRecordCategoryActive;
	private Boolean selectedRecordCategoryActive;
	private List<DepartmentOfRecord> departments;
	private int newRetentionYear;
	private int newRetentionMonth;
	private int newRetentionDay;
	private int editRetentionYear;
	private int editRetentionMonth;
	private int editRetentionDay;
	private int retentionSpan;
	private List<Pair<String, Boolean>> displayColumns;

	@PostConstruct
	public void init() {
		try {
			if (!userSessionBean.checkDB()) {
				returnMessage("database", FacesMessage.SEVERITY_ERROR,
						"DB_FAILED_MSG", "DB_FAILED_DES");
				return;
			}

			displayColumns = new ArrayList<Pair<String, Boolean>>();
			displayColumns.add(new Pair<String, Boolean>("Active", true));
			displayColumns.add(new Pair<String, Boolean>("Category", true));
			displayColumns.add(new Pair<String, Boolean>("Description", true));
			displayColumns.add(new Pair<String, Boolean>("Type", true));
			displayColumns.add(new Pair<String, Boolean>("Activity", true));
			displayColumns.add(new Pair<String, Boolean>("Functional Mailbox",
					true));
			displayColumns.add(new Pair<String, Boolean>("Retention Period",
					true));
			displayColumns.add(new Pair<String, Boolean>("Auto Delete", true));

			recordCategoryLazyList = new LazyRecordCategoryDataModel();
			newRecordCategoryActivity = "";
			newRecordCategory = new RecordCategory();
			newRecordCategoryActive = true;
			// set up retention period options
			yearList = new ArrayList<Pair<Integer, String>>();
			yearList.add(new Pair<Integer, String>(0, "0 year"));
			yearList.add(new Pair<Integer, String>(1, "1 year"));
			for (int i = 2; i <= 100; i++) {
				yearList.add(new Pair<Integer, String>(i, i + " years"));
			}

			monthList = new ArrayList<Pair<Integer, String>>();
			monthList.add(new Pair<Integer, String>(0, "0 month"));
			monthList.add(new Pair<Integer, String>(1, "1 month"));
			for (int i = 2; i <= 11; i++) {
				monthList.add(new Pair<Integer, String>(i, i + " months"));
			}

			dayList = new ArrayList<Pair<Integer, String>>();
			dayList.add(new Pair<Integer, String>(0, "0 day"));
			dayList.add(new Pair<Integer, String>(1, "1 day"));
			for (int i = 2; i <= 29; i++) {
				dayList.add(new Pair<Integer, String>(i, i + " days"));
			}

			newRetentionPermanent = false;
			newRetentionDay = 0;
			newRetentionMonth = 0;
			newRetentionYear = 1;
			retentionSpan = 3;

			departments = DepartmentOfRecordDBHelper
					.getAllDepartmentOfRecords();
		} catch (Exception e) {
			LOG.error("ManageRecordCategoryBean init(): " + e.getMessage(), e);
			returnUnexpectedError(null);
		}

	}

	public ManageRecordCategoryBean() {
	}

	/*
	 * handler for viewing record list
	 */

	public void onRowSelect(SelectEvent event) {
	}

	public void onToggleSelect(ToggleSelectEvent event) {
	}

	public void onRowUnselect(UnselectEvent event) {
	}

	public void onFilter(FilterEvent event) {
	}

	/*
	 * Suggest activity names based on partial input
	 */
	public List<String> suggestActivity(String query) {
		List<String> results = new ArrayList<String>();
		try {
			results = ActivityDBHelper.getLimitedActivityNames(10, query);
		} catch (Exception e) {
			LOG.error(
					"ManageRecordCategoryBean suggestActivity(): "
							+ e.getMessage(), e);
			returnUnexpectedError(null);
		}
		return results;
	}

	/*
	 * create new record category
	 */
	public void createNewRecordCategory() {
		try {
			LOG.info("ManageRecordCategoryBean createNewRecordCategory(): Creating new record");
			if (RecordCategoryDBHelper.getRecordCategory(newRecordCategory
					.getRecordCategory().trim()) != null) {
				LOG.error("ManageRecordCategoryBean createNewRecord() RecordCategory exists");
				returnMessage("create-new-msg", FacesMessage.SEVERITY_ERROR,
						"RECORD_CATEGORY_DUPLICATE_MSG",
						"RECORD_CATEGORY_DUPLICATE_DES");
				return;
			}

			if (newRetentionPermanent) {
				newRecordCategory.setRetentionPeriod(0);
			} else {
				newRecordCategory.setRetentionPeriod(newRetentionDay
						+ newRetentionMonth * 30 + newRetentionYear * 365);
				if (newRecordCategory.getRetentionPeriod() == 0) {
					returnMessage("create-new-msg",
							FacesMessage.SEVERITY_ERROR,
							"RECORD_CATEGORY_INVALID_PERIOD_MSG",
							"RECORD_CATEGORY_INVALID_PERIOD_DES");
					return;
				}
			}

			newRecordCategory.setActive(newRecordCategoryActive ? 1 : 0);
			Activity activity = ActivityDBHelper
					.getActivityByName(newRecordCategoryActivity);
			if (newRecordCategory.getRecordType().equals("Complex")) {
				newRecordCategory.setRecordActivity(activity.getId());
			} else {
				newRecordCategory.setRecordActivity(null);
			}

			if (!RecordCategoryDBHelper.createNewRecordCategory(
					newRecordCategory.getRecordCategory(),
					newRecordCategory.getCategoryName(),
					newRecordCategory.getRecordType(),
					newRecordCategory.getRecordActivity(),
					newRecordCategory.getRetentionPeriod(),
					newRecordCategory.getDepartmentOfRecord(),
					newRecordCategory.getAutoDelete(),
					newRecordCategory.getComments(),
					newRecordCategory.getActive())) {
				LOG.error("ManageRecordCategoryBean createNewRecordCategory() Failed to create new record category");
				returnUnexpectedError("create-new-msg");
			} else {
				RequestContext.getCurrentInstance().execute(
						"PF('create-new-dialog').hide()");

				returnMessage(null, FacesMessage.SEVERITY_INFO,
						"RECORD_CATEGORY_SUCCESSFUL_MSG",
						"RECORD_CATEGORY_SUCCESSFUL_CREATE_DES");

				LogDBHelper
						.createLog(new AuditLog(
								new Timestamp(new Date().getTime()),
								userSessionBean.getUserName(),
								newRecordCategory.getRecordCategory(),
								"Record Category",
								"Create",
								"",
								"Category: "
										+ newRecordCategory.getRecordCategory()
										+ " -- Name: "
										+ newRecordCategory.getCategoryName()
										+ " -- Type: "
										+ newRecordCategory.getRecordType()
										+ " -- Activity: "
										+ (newRecordCategoryActivity.length() == 0 ? "N.A."
												: newRecordCategoryActivity)
										+ " -- Retention Period: "
										+ getDisplayRetention(newRecordCategory
												.getRetentionPeriod())
										+ " -- Department of Record: "
										+ newRecordCategory
												.getDepartmentOfRecord()
										+ " -- Auto Delete: "
										+ (newRecordCategory.getAutoDelete() == 1)
										+ " -- Active: "
										+ (newRecordCategory.getActive() == 1),
								""));
			}

		} catch (Exception e) {
			LOG.error("ManageRecordCategoryBean createNewRecordCategory(): "
					+ e.getMessage(), e);
			returnUnexpectedError(null);
		}
	}

	public void filterActivityCreateRecord(FilterEvent event) {
		LOG.info(event.getFilters().size());
	}

	public String getActivityName(String id) {
		Activity activity = ActivityDBHelper.getActivityById(id);
		if (activity != null) {
			return activity.getName();
		} else {
			return "";
		}
	}

	/*
	 * handlers for editing record
	 */
	public void getEditRecordCategory() {
		selectedRecordCategory = RecordCategoryDBHelper
				.getRecordCategory(selectedRecordCategoryValue);
		if (selectedRecordCategory == null) {
			LOG.error("ManageRecordCategoryBean getEditRecordCategory() Entry deleted.");
			returnMessage(null, FacesMessage.SEVERITY_ERROR,
					"RECORD_DELETED_MSG", "RECORD_DELETED_DES");
			return;
		}

		if (oldRecordCategory == null) {
			oldRecordCategory = new RecordCategory();
		}

		oldRecordCategory.setActive(selectedRecordCategory.getActive());
		oldRecordCategory.setRecordActivity(selectedRecordCategory
				.getRecordActivity());
		oldRecordCategory.setRecordCategory(selectedRecordCategory
				.getRecordCategory());
		oldRecordCategory.setCategoryName(selectedRecordCategory
				.getCategoryName());
		oldRecordCategory.setRecordType(selectedRecordCategory.getRecordType());
		oldRecordCategory.setRetentionPeriod(selectedRecordCategory
				.getRetentionPeriod());
		oldRecordCategory.setAutoDelete(selectedRecordCategory.getAutoDelete());
		oldRecordCategory.setComments(selectedRecordCategory.getComments());
		oldRecordCategory.setDepartmentOfRecord(selectedRecordCategory
				.getDepartmentOfRecord());

		selectedRecordCategoryActive = (selectedRecordCategory.getActive() == 1) ? true
				: false;
		Activity activity = ActivityDBHelper
				.getActivityById(selectedRecordCategory.getRecordActivity());
		if (activity != null) {
			selectedRecordCategoryActivity = activity.getName();
		} else {
			selectedRecordCategoryActivity = "";
		}

		if (selectedRecordCategory.getRetentionPeriod() != 0) {
			int temp = selectedRecordCategory.getRetentionPeriod();
			editRetentionYear = temp / 365;
			editRetentionMonth = (temp - (editRetentionYear * 365)) / 30;
			editRetentionDay = temp - editRetentionYear * 365
					- editRetentionMonth * 30;
			LOG.info(editRetentionYear + " " + editRetentionMonth + " "
					+ editRetentionDay);
			editRetentionPermanent = false;
		} else {
			editRetentionPermanent = true;
		}
		
		RequestContext.getCurrentInstance().execute(
				"PF('edit-record-category').show()");
	}
	
	public void getDeleteRecordCategory() {
		selectedRecordCategory = RecordCategoryDBHelper
				.getRecordCategory(selectedRecordCategoryValue);
		if (selectedRecordCategory == null) {
			LOG.error("ManageRecordCategoryBean getEditRecordCategory() Entry deleted.");
			returnMessage(null, FacesMessage.SEVERITY_ERROR,
					"RECORD_DELETED_MSG", "RECORD_DELETED_DES");
			return;
		}
		selectedRecordCategoryActive = (selectedRecordCategory.getActive() == 1) ? true
				: false;
		Activity activity = ActivityDBHelper
				.getActivityById(selectedRecordCategory.getRecordActivity());
		if (activity != null) {
			selectedRecordCategoryActivity = activity.getName();
		} else {
			selectedRecordCategoryActivity = "";
		}

		if (selectedRecordCategory.getRetentionPeriod() != 0) {
			int temp = selectedRecordCategory.getRetentionPeriod();
			editRetentionYear = temp / 365;
			editRetentionMonth = (temp - (editRetentionYear * 365)) / 30;
			editRetentionDay = temp - editRetentionYear * 365
					- editRetentionMonth * 30;
			LOG.info(editRetentionYear + " " + editRetentionMonth + " "
					+ editRetentionDay);
			editRetentionPermanent = false;
		} else {
			editRetentionPermanent = true;
		}
		RequestContext.getCurrentInstance().execute(
				"PF('delete-dialog').show()");
	}

	public void updateRecordCategory() {
		try {
			RecordCategory updatedCategory = RecordCategoryDBHelper
					.getRecordCategory(selectedRecordCategory.getRecordCategory());

			if (updatedCategory == null) {
				LOG.error("ManageRecordCategoryBean updateRecordCategory() Entry deleted.");
				returnMessage(null, FacesMessage.SEVERITY_ERROR,
						"RECORD_DELETED_MSG", "RECORD_DELETED_DES");
				RequestContext.getCurrentInstance().execute(
						"PF('edit-record-category').hide()");
				return;
			}

			if (!oldRecordCategory.equals(updatedCategory)) {
				LOG.error("ManageRecordCategoryBean updateRecordCategory() Dirty entry.");
				returnMessage("edit-msg", FacesMessage.SEVERITY_ERROR,
						"RECORD_DIRTY_MSG", "RECORD_DIRTY_DES");
				getEditRecordCategory();
				return;
			}

			selectedRecordCategory.setActive(selectedRecordCategoryActive ? 1
					: 0);
			Activity activity = ActivityDBHelper
					.getActivityByName(selectedRecordCategoryActivity);

			if (editRetentionPermanent) {
				selectedRecordCategory.setRetentionPeriod(0);
			} else {
				selectedRecordCategory.setRetentionPeriod(editRetentionDay
						+ editRetentionMonth * 30 + editRetentionYear * 365);
				if (selectedRecordCategory.getRetentionPeriod() == 0) {
					returnMessage("edit-msg", FacesMessage.SEVERITY_ERROR,
							"RECORD_CATEGORY_INVALID_PERIOD_MSG",
							"RECORD_CATEGORY_INVALID_PERIOD_DES");
					return;
				}

			}

			if (selectedRecordCategory.getRecordType().equals("Complex")) {
				selectedRecordCategory.setRecordActivity(activity.getId());
			} else {
				selectedRecordCategory.setRecordActivity(null);
			}

			Activity oldActivity = ActivityDBHelper
					.getActivityById(oldRecordCategory.getRecordActivity());
			if (oldActivity != null) {
				oldRecordCategory.setRecordActivity(oldActivity.getName());
			} else {
				oldRecordCategory.setRecordActivity(null);
			}

			// try to update
			if (!RecordCategoryDBHelper.updateRecordCategory(
					selectedRecordCategory.getRecordCategory(),
					selectedRecordCategory.getCategoryName(),
					selectedRecordCategory.getRecordType(),
					selectedRecordCategory.getRecordActivity(),
					selectedRecordCategory.getRetentionPeriod(),
					selectedRecordCategory.getDepartmentOfRecord(),
					selectedRecordCategory.getAutoDelete(),
					selectedRecordCategory.getComments(),
					selectedRecordCategory.getActive())) {
				LOG.error("ManageRecordCategoryBean updateRecordCategory() Failed to update record category");
				returnUnexpectedError(null);
			} else {
				returnMessage(null, FacesMessage.SEVERITY_INFO,
						"RECORD_CATEGORY_SUCCESSFUL_MSG",
						"RECORD_CATEGORY_SUCCESSFUL_UPDATE_DES");
				RequestContext.getCurrentInstance().execute(
						"PF('edit-record-category').hide()");

				// create log
				LogDBHelper
						.createLog(new AuditLog(
								new Timestamp(new Date().getTime()),
								userSessionBean.getUserName(),
								selectedRecordCategory.getRecordCategory(),
								"Record Category",
								"Update",
								"Category: "
										+ oldRecordCategory.getRecordCategory()
										+ " -- Name: "
										+ oldRecordCategory.getCategoryName()
										+ " -- Type: "
										+ oldRecordCategory.getRecordType()
										+ " -- Activity: "
										+ ((oldRecordCategory
												.getRecordActivity() == null) ? "N.A."
												: oldRecordCategory
														.getRecordActivity())
										+ " -- Retention Period: "
										+ getDisplayRetention(oldRecordCategory
												.getRetentionPeriod())
										+ " -- Department of Record: "
										+ oldRecordCategory
												.getDepartmentOfRecord()
										+ " -- Auto Delete: "
										+ (oldRecordCategory.getAutoDelete() == 1)
										+ " -- Active: "
										+ (oldRecordCategory.getActive() == 1),
								"Category: "
										+ selectedRecordCategory
												.getRecordCategory()
										+ " -- Name: "
										+ selectedRecordCategory
												.getCategoryName()
										+ " -- Type: "
										+ selectedRecordCategory
												.getRecordType()
										+ " -- Activity: "
										+ (selectedRecordCategoryActivity
												.length() == 0 ? "N.A."
												: selectedRecordCategoryActivity)
										+ " -- Retention Period: "
										+ getDisplayRetention(selectedRecordCategory
												.getRetentionPeriod())
										+ " -- Department of Record: "
										+ selectedRecordCategory
												.getDepartmentOfRecord()
										+ " -- Auto Delete: "
										+ (selectedRecordCategory
												.getAutoDelete() == 1)
										+ " -- Active: "
										+ (selectedRecordCategory.getActive() == 1),
								""));
			}
		} catch (Exception e) {
			LOG.error(
					"ManageRecordCategoryBean updateRecordCategory(): "
							+ e.getMessage(), e);
			returnUnexpectedError(null);
		}
	}

	/*
	 * handlers for deleting records
	 */
	public void deleteRecordCategory() {
		try {

			LOG.info("ManageRecordCategoryBean deleteRecordCategory(): Deleting record category"
					+ selectedRecordCategory);
			
			RecordCategory updatedCategory = RecordCategoryDBHelper
					.getRecordCategory(selectedRecordCategory.getRecordCategory());

			if (updatedCategory == null) {
				LOG.error("ManageRecordCategoryBean deleteRecordCategory() Entry deleted.");
				returnMessage(null, FacesMessage.SEVERITY_ERROR,
						"RECORD_DELETED_MSG", "RECORD_DELETED_DES");
				RequestContext.getCurrentInstance().execute(
						"PF('delete-dialog').hide()");
				return;
			}

			// try to delete
			if (RecordCategoryDBHelper
					.deleteRecordCategory(selectedRecordCategoryValue)) {
				returnMessage(null, FacesMessage.SEVERITY_INFO,
						"RECORD_CATEGORY_SUCCESSFUL_MSG",
						"RECORD_CATEGORY_SUCCESSFUL_DELETE_DES");

				// create log
				LogDBHelper
						.createLog(new AuditLog(
								new Timestamp(new Date().getTime()),
								userSessionBean.getUserName(),
								selectedRecordCategory.getRecordCategory(),
								"Record Category",
								"Delete",
								"Category: "
										+ selectedRecordCategory
												.getRecordCategory()
										+ " -- Name: "
										+ selectedRecordCategory
												.getCategoryName()
										+ " -- Type: "
										+ selectedRecordCategory
												.getRecordType()
										+ " -- Activity: "
										+ (selectedRecordCategoryActivity
												.length() == 0 ? "N.A."
												: selectedRecordCategoryActivity)
										+ " -- Retention Period: "
										+ getDisplayRetention(selectedRecordCategory
												.getRetentionPeriod())
										+ " -- Department of Record: "
										+ selectedRecordCategory
												.getDepartmentOfRecord()
										+ " -- Auto Delete: "
										+ (selectedRecordCategory
												.getAutoDelete() == 1)
										+ " -- Active: "
										+ (selectedRecordCategory.getActive() == 1),
								"", ""));
			} else {
				returnUnexpectedError(null);
			}
			RequestContext.getCurrentInstance().execute(
					"PF('delete-dialog').hide()");

		} catch (Exception e) {
			LOG.error(
					"ManageRecordCategoryBean deleteRecordCategory(): "
							+ e.getMessage(), e);
			returnUnexpectedError(null);
		}

	}

	public int getYear(int days) {
		return days / 365;
	}

	public int getMonth(int days) {
		return (days - getYear(days) * 365) / 30;
	}

	public int getDay(int days) {
		return days - getYear(days) * 365 - getMonth(days) * 30;
	}

	public String getDisplayRetention(int days) {
		if (days == 0) {
			return "Permanent";
		} else {
			return getYear(days) + "y " + getMonth(days) + "m " + getDay(days)
					+ "d";
		}
	}

	public void selectNewCategoryType() {
		newRecordCategoryActivity = "";
	}

	public void selectEditCategoryType() {
		selectedRecordCategoryActivity = "";
	}

	/*
	 * handlers for autocomplete
	 */

	public List<String> autoCompleteForActivityInput(String query) {
		List<String> results = new ArrayList<String>();
		try {
		} catch (Exception e) {
			LOG.error(
					"ManageRecordCategoryBean autoCompleteForActivityInput(): "
							+ e.getMessage(), e);
			returnUnexpectedError("bulk-update-record-form");
		}
		return results;
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

	public boolean renderColumn(String columnName) {
		for (Pair<String, Boolean> item : displayColumns) {
			if (item.getElement0().equals(columnName)) {
				return item.getElement1();
			}
		}
		return true;
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

	public RecordCategory getNewRecordCategory() {
		return newRecordCategory;
	}

	public void setNewRecordCategory(RecordCategory newRecordCategory) {
		this.newRecordCategory = newRecordCategory;
	}

	public LazyRecordCategoryDataModel getRecordCategoryLazyList() {
		return recordCategoryLazyList;
	}

	public void setRecordCategoryLazyList(
			LazyRecordCategoryDataModel recordCategoryLazyList) {
		this.recordCategoryLazyList = recordCategoryLazyList;
	}

	public String getSelectedRecordCategoryValue() {
		return selectedRecordCategoryValue;
	}

	public void setSelectedRecordCategoryValue(
			String selectedRecordCategoryValue) {
		this.selectedRecordCategoryValue = selectedRecordCategoryValue;
	}

	public RecordCategory getSelectedRecordCategory() {
		return selectedRecordCategory;
	}

	public void setSelectedRecordCategory(RecordCategory selectedRecordCategory) {
		this.selectedRecordCategory = selectedRecordCategory;
	}

	public String getSelectedRecordCategoryActivity() {
		return selectedRecordCategoryActivity;
	}

	public void setSelectedRecordCategoryActivity(
			String selectedRecordCategoryActivity) {
		this.selectedRecordCategoryActivity = selectedRecordCategoryActivity;
	}

	public String getNewRecordCategoryActivity() {
		return newRecordCategoryActivity;
	}

	public void setNewRecordCategoryActivity(String newRecordCategoryActivity) {
		this.newRecordCategoryActivity = newRecordCategoryActivity;
	}

	public Boolean getNewRecordCategoryActive() {
		return newRecordCategoryActive;
	}

	public void setNewRecordCategoryActive(Boolean newRecordCategoryActive) {
		this.newRecordCategoryActive = newRecordCategoryActive;
	}

	public Boolean getSelectedRecordCategoryActive() {
		return selectedRecordCategoryActive;
	}

	public void setSelectedRecordCategoryActive(
			Boolean selectedRecordCategoryActive) {
		this.selectedRecordCategoryActive = selectedRecordCategoryActive;
	}

	public List<DepartmentOfRecord> getDepartments() {
		return departments;
	}

	public void setDepartments(List<DepartmentOfRecord> departments) {
		this.departments = departments;
	}

	public List<Pair<Integer, String>> getYearList() {
		return yearList;
	}

	public void setYearList(List<Pair<Integer, String>> yearList) {
		this.yearList = yearList;
	}

	public List<Pair<Integer, String>> getMonthList() {
		return monthList;
	}

	public void setMonthList(List<Pair<Integer, String>> monthList) {
		this.monthList = monthList;
	}

	public List<Pair<Integer, String>> getDayList() {
		return dayList;
	}

	public void setDayList(List<Pair<Integer, String>> dayList) {
		this.dayList = dayList;
	}

	public Boolean getNewRetentionPermanent() {
		return newRetentionPermanent;
	}

	public void setNewRetentionPermanent(Boolean newRetentionPermanent) {
		this.newRetentionPermanent = newRetentionPermanent;
	}

	public Boolean getEditRetentionPermanent() {
		return editRetentionPermanent;
	}

	public void setEditRetentionPermanent(Boolean editRetentionPermanent) {
		this.editRetentionPermanent = editRetentionPermanent;
	}

	public int getNewRetentionYear() {
		return newRetentionYear;
	}

	public void setNewRetentionYear(int newRetentionYear) {
		this.newRetentionYear = newRetentionYear;
	}

	public int getNewRetentionMonth() {
		return newRetentionMonth;
	}

	public void setNewRetentionMonth(int newRetentionMonth) {
		this.newRetentionMonth = newRetentionMonth;
	}

	public int getNewRetentionDay() {
		return newRetentionDay;
	}

	public void setNewRetentionDay(int newRetentionDay) {
		this.newRetentionDay = newRetentionDay;
	}

	public int getEditRetentionYear() {
		return editRetentionYear;
	}

	public void setEditRetentionYear(int editRetentionYear) {
		this.editRetentionYear = editRetentionYear;
	}

	public int getEditRetentionMonth() {
		return editRetentionMonth;
	}

	public void setEditRetentionMonth(int editRetentionMonth) {
		this.editRetentionMonth = editRetentionMonth;
	}

	public int getEditRetentionDay() {
		return editRetentionDay;
	}

	public void setEditRetentionDay(int editRetentionDay) {
		this.editRetentionDay = editRetentionDay;
	}

	public int getRetentionSpan() {
		return retentionSpan;
	}

	public void setRetentionSpan(int retentionSpan) {
		this.retentionSpan = retentionSpan;
	}

	public List<Pair<String, Boolean>> getDisplayColumns() {
		return displayColumns;
	}

	public void setDisplayColumns(List<Pair<String, Boolean>> displayColumns) {
		this.displayColumns = displayColumns;
	}

}
