package com.nextlabs.bae.entity;

import java.io.Serializable;

import com.google.gson.annotations.Expose;

public class Activity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Expose
	private String id;
	@Expose
	private String name;
	@Expose
	private String recordOfficersGuidance;
	@Expose
	private int active;

	public Activity() {
	}

	public Activity(String id, String name, String recordOfficersGuidance,
			int active) {
		super();
		this.id = id;
		this.name = name;
		this.recordOfficersGuidance = recordOfficersGuidance;
		this.active = active;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRecordOfficersGuidance() {
		return recordOfficersGuidance;
	}

	public void setRecordOfficersGuidance(String recordOfficersGuidance) {
		this.recordOfficersGuidance = recordOfficersGuidance;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getActive() {
		return active;
	}

	public void setActive(int active) {
		this.active = active;
	}

	@Override
	public boolean equals(Object object) {
		if (object == null) {
			return false;
		}
		if (object == this) {
			return true;
		}
		if (!(object instanceof Activity)) {
			return false;
		}

		Activity checkActivity = (Activity) object;

		if (this.recordOfficersGuidance == null) {
			this.recordOfficersGuidance = "";
		}

		if (checkActivity.getRecordOfficersGuidance() == null) {
			checkActivity.setRecordOfficersGuidance("");
		}

		if (this.id.equals(checkActivity.getId())
				&& this.name.equals(checkActivity.getName())
				&& this.active == checkActivity.getActive()
				&& this.recordOfficersGuidance.equals(checkActivity
						.getRecordOfficersGuidance())) {
			return true;
		} else {
			return false;
		}
	}
}
