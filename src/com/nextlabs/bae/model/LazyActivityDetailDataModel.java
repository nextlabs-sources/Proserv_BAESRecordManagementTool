package com.nextlabs.bae.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import com.nextlabs.bae.dbhelper.ActivityDetailDBHelper;
import com.nextlabs.bae.entity.ActivityDetail;

public class LazyActivityDetailDataModel extends LazyDataModel<ActivityDetail> {

	private static final Log LOG = LogFactory
			.getLog(LazyActivityDetailDataModel.class);
	private static final long serialVersionUID = 1L;
	private List<ActivityDetail> data;
	private String constraintName;
	private String constraintValue;

	public LazyActivityDetailDataModel(String constraintName,
			String constraintValue) {
		if (constraintName != null) {
			this.constraintName = constraintName;
			this.constraintValue = constraintValue;
		}
		data = new ArrayList<ActivityDetail>();
	}

	@Override
	public List<ActivityDetail> load(int first, int pageSize, String sortField,
			SortOrder sortOrder, Map<String, Object> filters) {
		data.clear();

		String sortOrderString;
		// translate sort order
		if (sortOrder == null) {
			sortOrderString = "";
		} else if (sortOrder.equals(SortOrder.ASCENDING)) {
			sortOrderString = "ASC";
		} else if (sortOrder.equals(SortOrder.DESCENDING)) {
			sortOrderString = "DESC";
		} else if (sortOrder.equals(SortOrder.UNSORTED)) {
			sortOrderString = "";
		} else {
			sortOrderString = "";
		}

		if (constraintName != null) {
			filters.put(constraintName, constraintValue);
		}
		data = ActivityDetailDBHelper.getActivityDetailListLazy(first,
				pageSize, sortField, sortOrderString, filters);
		this.setRowCount(ActivityDetailDBHelper.countAllActivityDetail(filters));

		return data;
	}

	@Override
	public ActivityDetail getRowData(String rowKey) {
		LOG.info(rowKey);
		for (ActivityDetail recordActivity : data) {
			if (recordActivity.getId().trim().equals(rowKey.trim()))
				return recordActivity;
		}

		return null;
	}

	@Override
	public Object getRowKey(ActivityDetail recordActivity) {
		return recordActivity.getId();
	}

	public String getConstraintName() {
		return constraintName;
	}

	public void setConstraintName(String constraintName) {
		this.constraintName = constraintName;
	}

	public String getConstraintValue() {
		return constraintValue;
	}

	public void setConstraintValue(String constraintValue) {
		this.constraintValue = constraintValue;
	}
}
