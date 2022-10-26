package com.nextlabs.bae.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import com.nextlabs.bae.dbhelper.ActivityDBHelper;
import com.nextlabs.bae.entity.Activity;

public class LazyActivityDataModel extends LazyDataModel<Activity> {

	private List<Activity> data;
	private static final long serialVersionUID = 1L;
	private static final Log LOG = LogFactory
			.getLog(LazyActivityDataModel.class);

	public LazyActivityDataModel() {
		data = new ArrayList<Activity>();
	}

	@Override
	public List<Activity> load(int first, int pageSize, String sortField,
			SortOrder sortOrder, Map<String, Object> filters) {

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

		data = ActivityDBHelper.getActivityListLazy(first, pageSize, sortField,
				sortOrderString, filters);
		this.setRowCount(ActivityDBHelper.countAllActivity(filters));
		return data;
	}

	@Override
	public Activity getRowData(String rowKey) {
		LOG.info(rowKey);
		for (Activity activity : data) {
			if (activity.getName().trim().equals(rowKey.trim()))
				return activity;
		}

		return null;
	}

	@Override
	public Object getRowKey(Activity activity) {
		return activity.getName();
	}

}
