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

import com.nextlabs.bae.common.EmailValidator;
import com.nextlabs.bae.common.PropertyLoader;
import com.nextlabs.bae.dbhelper.DepartmentOfRecordDBHelper;
import com.nextlabs.bae.dbhelper.LogDBHelper;
import com.nextlabs.bae.dbhelper.RecordCategoryDBHelper;
import com.nextlabs.bae.entity.AuditLog;
import com.nextlabs.bae.entity.DepartmentOfRecord;
import com.nextlabs.bae.entity.RecordCategory;
import com.nextlabs.bae.model.LazyDepartmentOfRecordDataModel;
import com.nextlabs.bae.model.LazyRecordCategoryDataModel;

@ManagedBean(name = "manageDepartmentOfRecordBean")
@ViewScoped
public class ManageDepartmentOfRecordBean implements Serializable {
	private static final Log LOG = LogFactory
			.getLog(ManageDepartmentOfRecordBean.class);
	private static final long serialVersionUID = 1L;

	@ManagedProperty(value = "#{userSessionBean}")
	private UserSessionBean userSessionBean;
	// lazy loading for departmentOfRecord
	private LazyDepartmentOfRecordDataModel departmentOfRecordLazyList;
	private DepartmentOfRecord selectedDepartmentOfRecord;
	private String selectedDepartmentOfRecordValue;
	private List<DepartmentOfRecord> selectedActivities;
	private DepartmentOfRecord newDepartmentOfRecord;
	private List<RecordCategory> recordsOfSelectedDepartmentOfRecord;
	private LazyRecordCategoryDataModel lazyRecordsOfSelectedDepartmentOfRecord;
	private DepartmentOfRecord oldDepartment;

	@PostConstruct
	public void init() {
		try {
			if (!userSessionBean.checkDB()) {
				returnMessage("database", FacesMessage.SEVERITY_ERROR,
						"DB_FAILED_MSG", "DB_FAILED_DES");
				return;
			}
			departmentOfRecordLazyList = new LazyDepartmentOfRecordDataModel();
			selectedActivities = new ArrayList<DepartmentOfRecord>();
			newDepartmentOfRecord = new DepartmentOfRecord();
			lazyRecordsOfSelectedDepartmentOfRecord = new LazyRecordCategoryDataModel();
		} catch (Exception e) {
			LOG.error("ManageDepartmentOfRecordBean init(): " + e.getMessage(),
					e);
			returnUnexpectedError(null);
		}
	}

	public void onFilter(FilterEvent event) {
		selectedActivities.clear();
	}

	/*
	 * create new departmentOfRecord
	 */
	public void createNewDepartmentOfRecord() {
		try {
			LOG.info("ManageDepartmentOfRecordBean createNewDepartmentOfRecord(): Creating new departmentOfRecord");

			// check duplicate
			if (DepartmentOfRecordDBHelper
					.getDepartmentOfRecordByName(newDepartmentOfRecord
							.getName()) != null) {
				LOG.error("ManageDepartmentOfRecordBean createNewDepartmentOfRecord() DepartmentOfRecord exists");
				returnMessage("create-new-msg", FacesMessage.SEVERITY_ERROR,
						"DEPARTMENT_DUPLICATE_MSG", "DEPARTMENT_DUPLICATE_DES");
				return;
			}

			// validate email address
			if (!(newDepartmentOfRecord.getFunctionalMailbox() == null || newDepartmentOfRecord
					.getFunctionalMailbox().isEmpty())) {
				if (!new EmailValidator().validate(newDepartmentOfRecord
						.getFunctionalMailbox())) {
					returnMessage("create-new-msg",
							FacesMessage.SEVERITY_ERROR,
							"DEPARTMENT_INVALID_INPUT_MSG",
							"DEPARTMENT_INVALID_EMAIL_DES");
					return;
				}
			}

			// try to create
			if (!DepartmentOfRecordDBHelper.createNewDepartmentOfRecord(
					newDepartmentOfRecord.getName(),
					newDepartmentOfRecord.getFunctionalMailbox())) {
				returnUnexpectedError("create-new-msg");
			} else {
				returnMessage(null, FacesMessage.SEVERITY_INFO,
						"DEPARTMENT_SUCCESSFUL_MSG",
						"DEPARTMENT_SUCCESSFUL_CREATE_DES");
				LogDBHelper.createLog(new AuditLog(new Timestamp(new Date()
						.getTime()), userSessionBean.getUserName(),
						newDepartmentOfRecord.getName(),
						"Department of Record", "Create", "", "Name: "
								+ newDepartmentOfRecord.getName()
								+ " -- Functional Mailbox: "
								+ newDepartmentOfRecord.getFunctionalMailbox(),
						""));
			}
			RequestContext.getCurrentInstance().execute(
					"PF('create-new-dialog').hide()");

		} catch (Exception e) {
			LOG.error(
					"ManageDepartmentOfRecordBean createNewDepartmentOfRecord(): "
							+ e.getMessage(), e);
			returnUnexpectedError(null);
		}
	}

	/*
	 * handlers for updating departmentOfRecords
	 */
	public void updateDepartmentOfRecord() {
		try {
			LOG.info("ManageDepartmentOfRecordBean updateDepartmentOfRecord(): Updating departmentOfRecord");

			DepartmentOfRecord updatedDepartment = DepartmentOfRecordDBHelper
					.getDepartmentOfRecordByName(selectedDepartmentOfRecord
							.getName());

			if (updatedDepartment == null) {
				LOG.error("ManageDepartmentOfRecord updateDepartmentOfRecord() Deleted entry");
				returnMessage(null, FacesMessage.SEVERITY_ERROR,
						"RECORD_DELETED_MSG", "RECORD_DELETED_DES");
				RequestContext.getCurrentInstance().execute(
						"PF('edit-dialog').hide()");
				return;
			}

			if (!oldDepartment.equals(updatedDepartment)) {
				LOG.error("ManageDepartmentOfRecord updateDepartmentOfRecord() Dirty entry");
				returnMessage(null, FacesMessage.SEVERITY_ERROR,
						"RECORD_DIRTY_MSG", "RECORD_DIRTY_DES");
				getDepartmentOfRecordDetails();
				return;
			}

			// check email format
			if (!(selectedDepartmentOfRecord.getFunctionalMailbox() == null || selectedDepartmentOfRecord
					.getFunctionalMailbox().isEmpty())) {
				if (!new EmailValidator().validate(selectedDepartmentOfRecord
						.getFunctionalMailbox())) {
					returnMessage(null, FacesMessage.SEVERITY_ERROR,
							"DEPARTMENT_INVALID_INPUT_MSG",
							"DEPARTMENT_INVALID_EMAIL_DES");
					return;
				}
			}

			// try to update
			if (DepartmentOfRecordDBHelper.updateDepartmentOfRecord(
					selectedDepartmentOfRecord.getName(),
					selectedDepartmentOfRecord.getFunctionalMailbox())) {
				returnMessage(null, FacesMessage.SEVERITY_INFO,
						"DEPARTMENT_SUCCESSFUL_MSG",
						"DEPARTMENT_SUCCESSFUL_UPDATE_DES");
				LogDBHelper
						.createLog(new AuditLog(
								new Timestamp(new Date().getTime()),
								userSessionBean.getUserName(),
								selectedDepartmentOfRecord.getName(),
								"Department of Record",
								"Update",
								"Name: "
										+ oldDepartment.getName()
										+ " -- Functional Mailbox: "
										+ (oldDepartment.getFunctionalMailbox() == null ? ""
												: oldDepartment
														.getFunctionalMailbox()),
								"Name: "
										+ selectedDepartmentOfRecord.getName()
										+ " -- Functional Mailbox: "
										+ selectedDepartmentOfRecord
												.getFunctionalMailbox(), ""));
			} else {
				returnUnexpectedError(null);
			}
			RequestContext.getCurrentInstance().execute(
					"PF('edit-dialog').hide()");
		} catch (Exception e) {
			LOG.error(
					"ManageDepartmentOfRecordBean updateDepartmentOfRecord(): "
							+ e.getMessage(), e);
			returnUnexpectedError(null);
		}
	}

	/*
	 * handlers for deleting departmentOfRecords
	 */

	public void deleteDepartmentOfRecord() {
		try {
			LOG.info("ManageDepartmentOfRecordBean deleteDepartmentOfRecord(): Deleting departmentOfRecord "
					+ selectedDepartmentOfRecord.getName());
			
			DepartmentOfRecord updatedDepartment = DepartmentOfRecordDBHelper
					.getDepartmentOfRecordByName(selectedDepartmentOfRecord
							.getName());

			if (updatedDepartment == null) {
				LOG.error("ManageDepartmentOfRecord updateDepartmentOfRecord() Deleted entry");
				returnMessage(null, FacesMessage.SEVERITY_ERROR,
						"RECORD_DELETED_MSG", "RECORD_DELETED_DES");
				RequestContext.getCurrentInstance().execute(
						"PF('delete-dialog').hide()");
				return;
			}

			// check categories association
			if (RecordCategoryDBHelper.getRecordCategoriesByDepartment(
					selectedDepartmentOfRecord.getName()).size() > 0) {
				LOG.error("ManageDepartmentOfRecord deleteDepartmentOfRecord() DepartmentOfRecord still has categories");
				returnMessage(null, FacesMessage.SEVERITY_ERROR,
						"DEPARTMENT_ASSOCIATED_CATEGORIES_MSG",
						"DEPARTMENT_ASSOCIATED_CATEGORIES_DES");
				return;
			}

			// try to delete
			if (DepartmentOfRecordDBHelper
					.deleteDepartmentOfRecord(selectedDepartmentOfRecord
							.getName())) {
				returnMessage(null, FacesMessage.SEVERITY_INFO,
						"DEPARTMENT_SUCCESSFUL_MSG",
						"DEPARTMENT_SUCCESSFUL_DELETE_DES");

				// create log
				LogDBHelper.createLog(new AuditLog(new Timestamp(new Date()
						.getTime()), userSessionBean.getUserName(),
						selectedDepartmentOfRecord.getName(),
						"Department of Record", "Delete", "Name: "
								+ selectedDepartmentOfRecord.getName()
								+ " -- Functional Mailbox: "
								+ (selectedDepartmentOfRecord
										.getFunctionalMailbox() == null ? ""
										: selectedDepartmentOfRecord
												.getFunctionalMailbox()), "",
						""));
			} else {
				LOG.error("ManageDepartmentOfRecordBean deleteDepartmentOfRecord() Failed to delete");
				returnUnexpectedError(null);
			}

			RequestContext.getCurrentInstance().execute(
					"PF('delete-dialog').hide()");
		} catch (Exception e) {
			LOG.error(
					"ManageDepartmentOfRecordBean deleteDepartmentOfRecord(): "
							+ e.getMessage(), e);
			returnUnexpectedError(null);
		}
	}

	public void getDepartmentOfRecordDetails() {
		selectedDepartmentOfRecord = DepartmentOfRecordDBHelper
				.getDepartmentOfRecordByName(selectedDepartmentOfRecordValue);
		if (selectedDepartmentOfRecord == null) {
			LOG.error("ManageDepartmentOfRecord getDepartmentOfRecordDetails() Deleted entry");
			returnMessage(null, FacesMessage.SEVERITY_ERROR,
					"RECORD_DELETED_MSG", "RECORD_DELETED_DES");
			return;

		}
		if (oldDepartment == null) {
			oldDepartment = new DepartmentOfRecord();
		}

		oldDepartment.setName(selectedDepartmentOfRecord.getName());
		oldDepartment.setFunctionalMailbox(selectedDepartmentOfRecord
				.getFunctionalMailbox());
		RequestContext.getCurrentInstance().execute("PF('edit-dialog').show()");

	}

	public void getDepartmentOfRecordDetailsForDelete() {
		selectedDepartmentOfRecord = DepartmentOfRecordDBHelper
				.getDepartmentOfRecordByName(selectedDepartmentOfRecordValue);

		if (selectedDepartmentOfRecord == null) {
			LOG.error("ManageDepartmentOfRecord getDepartmentOfRecordDetailsForDelete() Deleted entry");
			returnMessage(null, FacesMessage.SEVERITY_ERROR,
					"RECORD_DELETED_MSG", "RECORD_DELETED_DES");
			return;
		}
		RequestContext.getCurrentInstance().execute(
				"PF('delete-dialog').show()");
	}

	public void onToggleSelect(ToggleSelectEvent event) {
		if (selectedActivities == null || selectedActivities.isEmpty()) {
		} else {
		}
	}

	public void onRowUnselect(UnselectEvent event) {
		if (selectedActivities == null || selectedActivities.isEmpty()) {
		}
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

	public LazyDepartmentOfRecordDataModel getDepartmentOfRecordLazyList() {
		return departmentOfRecordLazyList;
	}

	public void setDepartmentOfRecordLazyList(
			LazyDepartmentOfRecordDataModel departmentOfRecordLazyList) {
		this.departmentOfRecordLazyList = departmentOfRecordLazyList;
	}

	public DepartmentOfRecord getSelectedDepartmentOfRecord() {
		return selectedDepartmentOfRecord;
	}

	public void setSelectedDepartmentOfRecord(
			DepartmentOfRecord selectedDepartmentOfRecord) {
		this.selectedDepartmentOfRecord = selectedDepartmentOfRecord;
	}

	public List<DepartmentOfRecord> getSelectedActivities() {
		return selectedActivities;
	}

	public void setSelectedDepartmentOfRecords(
			List<DepartmentOfRecord> selectedActivities) {
		this.selectedActivities = selectedActivities;
	}

	public String getSelectedDepartmentOfRecordValue() {
		return selectedDepartmentOfRecordValue;
	}

	public void setSelectedDepartmentOfRecordValue(
			String selectedDepartmentOfRecordValue) {
		this.selectedDepartmentOfRecordValue = selectedDepartmentOfRecordValue;
	}

	public DepartmentOfRecord getNewDepartmentOfRecord() {
		return newDepartmentOfRecord;
	}

	public void setNewDepartmentOfRecord(
			DepartmentOfRecord newDepartmentOfRecord) {
		this.newDepartmentOfRecord = newDepartmentOfRecord;
	}

	public List<RecordCategory> getRecordsOfSelectedDepartmentOfRecord() {
		return recordsOfSelectedDepartmentOfRecord;
	}

	public void setRecordsOfSelectedDepartmentOfRecord(
			List<RecordCategory> recordsOfSelectedDepartmentOfRecord) {
		this.recordsOfSelectedDepartmentOfRecord = recordsOfSelectedDepartmentOfRecord;
	}

	public LazyRecordCategoryDataModel getLazyRecordsOfSelectedDepartmentOfRecord() {
		return lazyRecordsOfSelectedDepartmentOfRecord;
	}

	public void setLazyRecordsOfSelectedDepartmentOfRecord(
			LazyRecordCategoryDataModel lazyRecordsOfSelectedDepartmentOfRecord) {
		this.lazyRecordsOfSelectedDepartmentOfRecord = lazyRecordsOfSelectedDepartmentOfRecord;
	}

}
