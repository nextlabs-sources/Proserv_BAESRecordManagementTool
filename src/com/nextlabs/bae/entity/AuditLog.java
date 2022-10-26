package com.nextlabs.bae.entity;

import java.io.Serializable;
import java.sql.Timestamp;

public class AuditLog implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Timestamp time;
	private String admin;
	private String target;
	private String targetType;
	private String action;
	private String oldValue;
	private String newValue;
	private String extraDetails;

	public AuditLog(Timestamp time, String admin, String target,
			String targetType, String action, String oldValue, String newValue,
			String extraDetails) {
		this.time = time;
		this.admin = admin;
		this.target = target;
		this.targetType = targetType;
		this.action = action;
		this.oldValue = oldValue;
		this.newValue = newValue;
		this.extraDetails = extraDetails;
	}

	public Timestamp getTime() {
		return time;
	}

	public void setTime(Timestamp time) {
		this.time = time;
	}

	public String getAdmin() {
		return admin;
	}

	public void setAdmin(String admin) {
		this.admin = admin;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getOldValue() {
		return oldValue;
	}

	public void setOldValue(String oldValue) {
		this.oldValue = oldValue;
	}

	public String getNewValue() {
		return newValue;
	}

	public void setNewValue(String newValue) {
		this.newValue = newValue;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getTargetType() {
		return targetType;
	}

	public void setTargetType(String targetType) {
		this.targetType = targetType;
	}

	public String getExtraDetails() {
		return extraDetails;
	}

	public void setExtraDetails(String extraDetails) {
		this.extraDetails = extraDetails;
	}

}
