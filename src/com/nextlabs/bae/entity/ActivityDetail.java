package com.nextlabs.bae.entity;

import java.io.Serializable;
import java.sql.Timestamp;

import com.google.gson.annotations.Expose;

public class ActivityDetail implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Expose
	private String id;
	@Expose
	private String activity;
	@Expose
	private String titleValue;
	@Expose
	private String referenceValue;
	@Expose
	private Timestamp dateValue;

	public ActivityDetail() {
	}

	public ActivityDetail(String id, String activity, String titleValue,
			String referenceValue, Timestamp dateValue) {
		super();
		this.id = id;
		this.activity = activity;
		this.titleValue = titleValue;
		this.referenceValue = referenceValue;
		this.dateValue = dateValue;
	}

	public String getActivity() {
		return activity;
	}

	public void setActivity(String activity) {
		this.activity = activity;
	}

	public String getTitleValue() {
		return titleValue;
	}

	public void setTitleValue(String titleValue) {
		this.titleValue = titleValue;
	}

	public String getReferenceValue() {
		return referenceValue;
	}

	public void setReferenceValue(String referenceValue) {
		this.referenceValue = referenceValue;
	}

	public Timestamp getDateValue() {
		return dateValue;
	}

	public void setDateValue(Timestamp dateValue) {
		this.dateValue = dateValue;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object object) {
		if (object == null) {
			return false;
		}
		if (object == this) {
			return true;
		}
		if (!(object instanceof ActivityDetail)) {
			return false;
		}

		ActivityDetail checkDetail = (ActivityDetail) object;
		if (this.id.equals(checkDetail.getId())
				&& (this.titleValue.equals(checkDetail.getTitleValue()))
				&& this.referenceValue.equals(checkDetail.getReferenceValue())
				&& this.dateValue.getTime() == checkDetail.getDateValue()
						.getTime()) {
			return true;
		} else {
			return false;
		}
	}
}
