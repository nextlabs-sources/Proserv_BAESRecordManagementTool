package com.nextlabs.bae.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import com.nextlabs.bae.dbhelper.DepartmentOfRecordDBHelper;
import com.nextlabs.bae.entity.DepartmentOfRecord;

public class LazyDepartmentOfRecordDataModel extends
		LazyDataModel<DepartmentOfRecord> {

	private List<DepartmentOfRecord> data;
	private static final long serialVersionUID = 1L;
	private static final Log LOG = LogFactory
			.getLog(LazyDepartmentOfRecordDataModel.class);

	public LazyDepartmentOfRecordDataModel() {
		data = new ArrayList<DepartmentOfRecord>();
	}

	@Override
	public List<DepartmentOfRecord> load(int first, int pageSize,
			String sortField, SortOrder sortOrder, Map<String, Object> filters) {

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

		data = DepartmentOfRecordDBHelper.getDepartmentOfRecordListLazy(first,
				pageSize, sortField, sortOrderString, filters);
		this.setRowCount(DepartmentOfRecordDBHelper
				.countAllDepartmentOfRecord(filters));
		return data;
	}

	@Override
	public DepartmentOfRecord getRowData(String rowKey) {
		LOG.info(rowKey);
		for (DepartmentOfRecord department : data) {
			if (department.getName().trim().equals(rowKey.trim()))
				return department;
		}

		return null;
	}

	@Override
	public Object getRowKey(DepartmentOfRecord department) {
		return department.getName();
	}

}
