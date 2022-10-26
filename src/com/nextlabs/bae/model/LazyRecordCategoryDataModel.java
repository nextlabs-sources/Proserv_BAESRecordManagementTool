package com.nextlabs.bae.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import com.nextlabs.bae.dbhelper.RecordCategoryDBHelper;
import com.nextlabs.bae.entity.RecordCategory;

public class LazyRecordCategoryDataModel extends LazyDataModel<RecordCategory> {

	private static final long serialVersionUID = 1L;
	private String constraintName;
	private String constraintValue;
	private List<RecordCategory> data;
	private static final Log LOG = LogFactory
			.getLog(LazyRecordCategoryDataModel.class);

	public LazyRecordCategoryDataModel() {
		this.constraintName = null;
		data = new ArrayList<RecordCategory>();
	}

	public LazyRecordCategoryDataModel(String constraintName,
			String constraintValue) {
		this.constraintName = constraintName;
		this.constraintValue = constraintValue;
		data = new ArrayList<RecordCategory>();
	}

	@Override
	public List<RecordCategory> load(int first, int pageSize, String sortField,
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
		data = RecordCategoryDBHelper.getRecordCategoryListLazy(first,
				pageSize, sortField, sortOrderString, filters);
		this.setRowCount(RecordCategoryDBHelper.countAllRecordCategory(filters));
		return data;
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

	@Override
	public RecordCategory getRowData(String rowKey) {
		LOG.info(rowKey);
		for (RecordCategory record : data) {
			if (record.getRecordCategory().trim().equals(rowKey.trim()))
				return record;
		}

		return null;
	}

	@Override
	public Object getRowKey(RecordCategory record) {
		return record.getRecordCategory();
	}

}
