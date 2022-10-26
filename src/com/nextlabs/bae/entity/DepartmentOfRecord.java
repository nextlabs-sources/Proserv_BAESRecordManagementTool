package com.nextlabs.bae.entity;

import java.io.Serializable;

import com.google.gson.annotations.Expose;

public class DepartmentOfRecord implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Expose
	private String name;
	@Expose
	private String functionalMailbox;

	public DepartmentOfRecord() {
	}

	public DepartmentOfRecord(String name, String functionalMailbox) {
		super();
		this.name = name;
		this.functionalMailbox = functionalMailbox;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFunctionalMailbox() {
		return functionalMailbox;
	}

	public void setFunctionalMailbox(String functionalMailbox) {
		this.functionalMailbox = functionalMailbox;
	}

	@Override
	public boolean equals(Object object) {
		if (object == null) {
			return false;
		}
		if (object == this) {
			return true;
		}
		if (!(object instanceof DepartmentOfRecord)) {
			return false;
		}

		DepartmentOfRecord checkDep = (DepartmentOfRecord) object;
		if (this.functionalMailbox == null) {
			this.functionalMailbox = "";
		}

		if (checkDep.getFunctionalMailbox() == null) {
			checkDep.setFunctionalMailbox("");
		}

		if (this.name.equals(checkDep.getName())
				&& this.functionalMailbox.equals(checkDep
						.getFunctionalMailbox())) {
			return true;
		} else {
			return false;
		}
	}
}
