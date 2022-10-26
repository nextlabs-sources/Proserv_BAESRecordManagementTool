package com.nextlabs.bae.entity;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.nextlabs.bae.dbhelper.DepartmentOfRecordDBHelper;

public class RecordCategory implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Expose
	private String recordCategory;
	@Expose
	private String categoryName;
	@Expose
	private String recordType;
	@Expose
	private String recordActivity;
	@Expose
	private int retentionPeriod;
	@Expose
	private String departmentOfRecord;
	@Expose
	private int autoDelete;
	@Expose
	private String comments;
	@Expose
	private int active;

	public RecordCategory() {

	}

	public RecordCategory(String recordCategory, String categoryName,
			String recordType, String recordActivity, int retentionPeriod,
			String departmentOfRecord, int autoDelete, String comments,
			int active) {
		super();
		this.recordCategory = recordCategory;
		this.categoryName = categoryName;
		this.recordType = recordType;
		this.recordActivity = recordActivity;
		this.retentionPeriod = retentionPeriod;
		this.departmentOfRecord = departmentOfRecord;
		this.autoDelete = autoDelete;
		this.comments = comments;
		this.active = active;
	}

	public String getRecordCategory() {
		return recordCategory;
	}

	public void setRecordCategory(String recordCategory) {
		this.recordCategory = recordCategory;
	}

	public String getRecordType() {
		return recordType;
	}

	public void setRecordType(String recordType) {
		this.recordType = recordType;
	}

	public String getRecordActivity() {
		return recordActivity;
	}

	public void setRecordActivity(String recordActivity) {
		this.recordActivity = recordActivity;
	}

	public int getRetentionPeriod() {
		return retentionPeriod;
	}

	public void setRetentionPeriod(int retentionPeriod) {
		this.retentionPeriod = retentionPeriod;
	}

	public String getDepartmentOfRecord() {
		return departmentOfRecord;
	}

	public void setDepartmentOfRecord(String departmentOfRecord) {
		this.departmentOfRecord = departmentOfRecord;
	}

	public int getAutoDelete() {
		return autoDelete;
	}

	public void setAutoDelete(int autoDelete) {
		this.autoDelete = autoDelete;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public int getActive() {
		return active;
	}

	public void setActive(int active) {
		this.active = active;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public String getFunctionalMailbox() {
		DepartmentOfRecord department = DepartmentOfRecordDBHelper
				.getDepartmentOfRecordByName(departmentOfRecord);
		if (department != null) {
			return department.getFunctionalMailbox();
		} else {
			return "";
		}
	}

	@Override
	public boolean equals(Object object) {
		if (object == null) {
			return false;
		}
		if (object == this) {
			return true;
		}
		if (!(object instanceof RecordCategory)) {
			return false;
		}

		RecordCategory checkCat = (RecordCategory) object;

		if (this.comments == null) {
			this.comments = "";
		}

		if (checkCat.getComments() == null) {
			checkCat.setComments("");
		}

		if (this.recordActivity == null) {
			this.recordActivity = "";
		}

		if (checkCat.getRecordActivity() == null) {
			checkCat.setRecordActivity("");
		}

		if (this.recordCategory.equals(checkCat.getRecordCategory())
				&& this.categoryName.equals(checkCat.getCategoryName())
				&& this.active == checkCat.getActive()
				&& this.departmentOfRecord.equals(checkCat
						.getDepartmentOfRecord())
				&& this.recordType.equals(checkCat.getRecordType())
				&& this.comments.equals(checkCat.getComments())
				&& this.recordActivity.equals(checkCat.getRecordActivity())
				&& this.autoDelete == checkCat.getAutoDelete()
				&& this.retentionPeriod == checkCat.getRetentionPeriod()) {
			return true;
		}
		return false;
	}
}
