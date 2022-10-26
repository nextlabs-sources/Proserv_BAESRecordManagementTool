package com.nextlabs.bae.dbhelper;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.nextlabs.bae.entity.DepartmentOfRecord;

public class DepartmentOfRecordDBHelper implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final Log LOG = LogFactory
			.getLog(DepartmentOfRecordDBHelper.class);

	/**
	 * 
	 * @param start
	 * @param size
	 * @param sortField
	 * @param sortOrder
	 * @param filters
	 * @return
	 */
	public static List<DepartmentOfRecord> getDepartmentOfRecordListLazy(
			int start, int size, String sortField, String sortOrder,
			Map<String, Object> filters) {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		List<DepartmentOfRecord> resultList = new ArrayList<DepartmentOfRecord>();
		Map<String, Integer> parameterIndex = new HashMap<String, Integer>();

		try {
			connection = DBHelper.getDatabaseConnection();
			StringBuilder queryString = new StringBuilder(
					"SELECT outer.* FROM (SELECT ROWNUM rn, inner.* FROM (SELECT * FROM department_of_record");
			StringBuilder whereClause = new StringBuilder();
			// concat all filters

			int countFilters = 1;
			for (Map.Entry<String, Object> filter : filters.entrySet()) {
				if (!(filter.getValue().equals("") || filter.getValue() == null)) {
					String key = Character.toUpperCase(filter.getKey()
							.charAt(0)) + filter.getKey().substring(1);
					whereClause.append("UPPER(" + key + ") LIKE UPPER(?) AND ");
					parameterIndex.put(key, countFilters);
					countFilters++;
				}

			}

			if (whereClause.toString().trim().length() != 0) {
				whereClause = new StringBuilder(whereClause.substring(0,
						whereClause.length() - 5));
				queryString.append(" WHERE " + whereClause);
			}

			// concate sort
			queryString.append(" ORDER BY "
					+ Character.toUpperCase(sortField.charAt(0))
					+ sortField.substring(1) + "");
			if (!sortOrder.equals("")) {
				queryString.append(" " + sortOrder);
			}

			// lazy loading
			queryString.append(" ) inner) outer WHERE outer.rn >= "
					+ (start + 1) + " AND outer.rn <= " + (start + size));

			preparedStatement = connection.prepareStatement(queryString
					.toString());

			for (Map.Entry<String, Object> filter : filters.entrySet()) {
				if (!(filter.getValue().equals("") || filter.getValue() == null)) {
					// try as string using like
					String key = Character.toUpperCase(filter.getKey()
							.charAt(0)) + filter.getKey().substring(1);
					String value = filter.getValue().toString() + "%";
					preparedStatement.setString(parameterIndex.get(key), value);
				}

			}

			resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {

				String name = resultSet.getString("name");
				String functionalMailbox = resultSet
						.getString("functionalMailbox");
				DepartmentOfRecord departmentOfRecord = new DepartmentOfRecord(
						name, functionalMailbox);
				resultList.add(departmentOfRecord);
			}
		} catch (Exception ex) {
			LOG.error(
					"DepartmentOfRecordDBHelper getDepartmentOfRecordListLazy(): "
							+ ex.getMessage(), ex);
		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
				if (preparedStatement != null) {
					preparedStatement.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException ex) {
				LOG.error(
						"DepartmentOfRecordDBHelper getDepartmentOfRecordListLazy(): "
								+ ex.getMessage(), ex);
			}
		}
		return resultList;
	}

	/**
	 * 
	 * @param filters
	 * @return
	 */
	public static int countAllDepartmentOfRecord(Map<String, Object> filters) {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		int resultCount = 0;
		Map<String, Integer> parameterIndex = new HashMap<String, Integer>();

		if (filters == null) {
			filters = new HashMap<String, Object>();
		}

		try {
			connection = DBHelper.getDatabaseConnection();
			StringBuilder queryString = new StringBuilder(
					"SELECT COUNT(*) FROM department_of_record");
			StringBuilder whereClause = new StringBuilder();

			int countFilters = 1;
			for (Map.Entry<String, Object> filter : filters.entrySet()) {
				if (!(filter.getValue().equals("") || filter.getValue() == null)) {
					String key = Character.toUpperCase(filter.getKey()
							.charAt(0)) + filter.getKey().substring(1);
					whereClause.append("UPPER(" + key + ") LIKE UPPER(?) AND ");
					parameterIndex.put(key, countFilters);
					countFilters++;
				}

			}

			if (whereClause.toString().trim().length() != 0) {
				whereClause = new StringBuilder(whereClause.substring(0,
						whereClause.length() - 5));
				queryString.append(" WHERE " + whereClause);
			}

			preparedStatement = connection.prepareStatement(queryString
					.toString());

			for (Map.Entry<String, Object> filter : filters.entrySet()) {
				if (!(filter.getValue().equals("") || filter.getValue() == null)) {
					String key = Character.toUpperCase(filter.getKey()
							.charAt(0)) + filter.getKey().substring(1);
					String value = filter.getValue().toString() + "%";
					preparedStatement.setString(parameterIndex.get(key), value);
				}

			}

			// LOG.info("Lazy query count: " + preparedStatement.toString());

			resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				resultCount = resultSet.getInt(1);
			}
		} catch (Exception ex) {
			LOG.error(
					"DepartmentOfRecordDBHelper countAllDepartmentOfRecord(): "
							+ ex.getMessage(), ex);
		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
				if (preparedStatement != null) {
					preparedStatement.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException ex) {
				LOG.error(
						"DepartmentOfRecordDBHelper countAllDepartmentOfRecord(): "
								+ ex.getMessage(), ex);
			}
		}
		return resultCount;
	}

	/**
	 * 
	 * @param name
	 * @return
	 */
	public static DepartmentOfRecord getDepartmentOfRecordByName(String name) {
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		DepartmentOfRecord departmentOfRecord = null;

		try {
			connection = DBHelper.getDatabaseConnection();
			statement = connection
					.prepareStatement("SELECT * FROM department_of_record WHERE Name = ?");
			statement.setString(1, name);
			resultSet = statement.executeQuery();
			if (resultSet.next()) {
				String functionalMailbox = resultSet
						.getString("functionalMailbox");
				departmentOfRecord = new DepartmentOfRecord(name,
						functionalMailbox);
			}
		} catch (Exception ex) {
			LOG.error("DepartmentOfRecordDBHelper getDepartmentOfRecord(): "
					+ ex.getMessage(), ex);
		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
				if (statement != null) {
					statement.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException ex) {
				LOG.error(
						"DepartmentOfRecordDBHelper getDepartmentOfRecord(): "
								+ ex.getMessage(), ex);
			}
		}
		return departmentOfRecord;
	}

	/**
	 * 
	 * @return
	 */
	public static List<DepartmentOfRecord> getAllDepartmentOfRecords() {
		List<DepartmentOfRecord> results = new ArrayList<DepartmentOfRecord>();
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;

		try {
			connection = DBHelper.getDatabaseConnection();
			statement = connection
					.prepareStatement("SELECT * FROM department_of_record ORDER BY Name");
			resultSet = statement.executeQuery();
			while (resultSet.next()) {
				String name = resultSet.getString("name");
				String functionalMailbox = resultSet
						.getString("functionalMailbox");
				results.add(new DepartmentOfRecord(name, functionalMailbox));
			}
		} catch (Exception ex) {
			LOG.error(
					"DepartmentOfRecordDBHelper getAllDepartmentOfRecords(): "
							+ ex.getMessage(), ex);
		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
				if (statement != null) {
					statement.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException ex) {
				LOG.error(
						"DepartmentOfRecordDBHelper getAllDepartmentOfRecords(): "
								+ ex.getMessage(), ex);
			}
		}
		return results;
	}

	/**
	 * 
	 * @param limit
	 * @param query
	 * @return
	 */
	public static List<String> getLimitedDepartmentOfRecordNames(int limit,
			String query) {
		List<String> results = new ArrayList<String>();
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;

		try {
			connection = DBHelper.getDatabaseConnection();
			statement = connection
					.prepareStatement("SELECT Name FROM department_of_record WHERE UPPER(Name) LIKE UPPER(?) AND ROWNUM <= "
							+ limit);
			statement.setString(1, query + "%");
			resultSet = statement.executeQuery();
			while (resultSet.next()) {
				String name = resultSet.getString("name");
				results.add(name);
			}
		} catch (Exception ex) {
			LOG.error(
					"DepartmentOfRecordDBHelper getLimitedDepartmentOfRecordNames(): "
							+ ex.getMessage(), ex);
		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
				if (statement != null) {
					statement.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException ex) {
				LOG.error(
						"DepartmentOfRecordDBHelper getLimitedDepartmentOfRecordNames(): "
								+ ex.getMessage(), ex);
			}
		}
		return results;
	}

	/**
	 * 
	 * @param name
	 * @param functionalMailbox
	 * @return
	 */
	public static boolean createNewDepartmentOfRecord(String name,
			String functionalMailbox) {
		Connection connection = null;
		PreparedStatement statement = null;

		try {
			connection = DBHelper.getDatabaseConnection();
			statement = connection
					.prepareStatement("INSERT INTO department_of_record (name, functionalMailbox) VALUES (?, ?)");
			statement.setString(1, name);
			statement.setString(2, functionalMailbox);
			// LOG.info(queryString);
			statement.execute();
			connection.commit();
		} catch (Exception ex) {
			LOG.error(
					"DepartmentOfRecordDBHelper createNewDepartmentOfRecord(): "
							+ ex.getMessage(), ex);
			return false;
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException ex) {
				LOG.error(
						"DepartmentOfRecordDBHelper createNewDepartmentOfRecord(): "
								+ ex.getMessage(), ex);
				return false;
			}
		}
		return true;
	}

	/**
	 * 
	 * @param name
	 * @param functionalMailbox
	 * @return
	 */
	public static boolean updateDepartmentOfRecord(String name,
			String functionalMailbox) {
		Connection connection = null;
		PreparedStatement statement = null;

		try {
			connection = DBHelper.getDatabaseConnection();
			statement = connection
					.prepareStatement("UPDATE department_of_record SET functionalMailbox = ? WHERE name = ?");
			statement.setString(1, functionalMailbox);
			statement.setString(2, name);
			statement.executeUpdate();
			connection.commit();
		} catch (Exception ex) {
			LOG.error("DepartmentOfRecordDBHelper updateDepartmentOfRecord(): "
					+ ex.getMessage(), ex);
			return false;
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException ex) {
				LOG.error(
						"DepartmentOfRecordDBHelper updateDepartmentOfRecord(): "
								+ ex.getMessage(), ex);
				return false;
			}
		}
		return true;
	}

	/**
	 * 
	 * @param name
	 * @return
	 */
	public static boolean deleteDepartmentOfRecord(String name) {
		Connection connection = null;
		PreparedStatement statement = null;

		try {
			connection = DBHelper.getDatabaseConnection();
			statement = connection
					.prepareStatement("DELETE FROM department_of_record WHERE name = ?");
			statement.setString(1, name);
			statement.executeUpdate();
			connection.commit();
		} catch (Exception ex) {
			LOG.error("DepartmentOfRecordDBHelper deleteDepartmentOfRecord(): "
					+ ex.getMessage(), ex);
			return false;
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException ex) {
				LOG.error(
						"DepartmentOfRecordDBHelper deleteDepartmentOfRecord(): "
								+ ex.getMessage(), ex);
				return false;
			}
		}
		return true;
	}

}
