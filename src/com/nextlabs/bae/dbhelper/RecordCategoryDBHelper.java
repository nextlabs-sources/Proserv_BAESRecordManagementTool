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

import com.nextlabs.bae.entity.RecordCategory;

public class RecordCategoryDBHelper implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Log LOG = LogFactory
			.getLog(RecordCategoryDBHelper.class);

	/**
	 * 
	 * @param start
	 * @param size
	 * @param sortField
	 * @param sortOrder
	 * @param filters
	 * @return
	 */
	public static List<RecordCategory> getRecordCategoryListLazy(int start,
			int size, String sortField, String sortOrder,
			Map<String, Object> filters) {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		List<RecordCategory> resultList = new ArrayList<RecordCategory>();
		Map<String, Integer> parameterIndex = new HashMap<String, Integer>();

		try {
			connection = DBHelper.getDatabaseConnection();
			StringBuilder queryString = new StringBuilder(
					"SELECT outer.* FROM (SELECT ROWNUM rn, inner.* FROM "
							+ "(SELECT r.*, a.name FROM Record_Category r "
							+ "LEFT JOIN Activity a ON a.id = r.recordActivity "
							+ "LEFT JOIN department_of_record d on d.name = r.departmentOfRecord");
			StringBuilder whereClause = new StringBuilder();
			// concat all filters

			int countFilters = 1;
			for (Map.Entry<String, Object> filter : filters.entrySet()) {
				if (!(filter.getValue().equals("") || filter.getValue() == null)) {
					String key = Character.toUpperCase(filter.getKey()
							.charAt(0)) + filter.getKey().substring(1);
					if (key.equalsIgnoreCase("RecordActivity")) {
						whereClause.append("UPPER(a.name) LIKE UPPER(?) AND ");
					} else if (key.equalsIgnoreCase("FunctionalMailbox")) {
						whereClause
								.append("UPPER(d.functionalMailbox) LIKE UPPER(?) AND ");
					} else {
						whereClause.append("UPPER(r." + key
								+ ") LIKE UPPER(?) AND ");
					}
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

			if (sortField.equalsIgnoreCase("recordActivity")) {
				queryString.append(" ORDER BY a.name");
			} else if (sortField.equalsIgnoreCase("functionalMailbox")) {
				queryString.append(" ORDER BY d.functionalMailbox");
			} else {
				queryString.append(" ORDER BY r."
						+ Character.toUpperCase(sortField.charAt(0))
						+ sortField.substring(1) + "");
			}
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
				String recordCategory = resultSet.getString("recordCategory");
				String categoryName = resultSet.getString("categoryName");
				String recordType = resultSet.getString("recordType");
				String recordActivity = resultSet.getString("recordActivity");
				int retentionPeriod = resultSet.getInt("retentionPeriod");
				String departmentOfRecord = resultSet
						.getString("departmentOfRecord");
				int autoDelete = resultSet.getInt("autoDelete");
				String comments = resultSet.getString("comments");
				int active = resultSet.getInt("active");
				RecordCategory record = new RecordCategory(recordCategory,
						categoryName, recordType, recordActivity,
						retentionPeriod, departmentOfRecord, autoDelete,
						comments, active);
				resultList.add(record);
			}
		} catch (Exception ex) {
			LOG.error("RecordCategoryDBHelper getRecordCategoryListLazy(): "
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
						"RecordCategoryDBHelper getRecordCategoryListLazy(): "
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
	public static int countAllRecordCategory(Map<String, Object> filters) {
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
					"SELECT COUNT(*) FROM Record_Category r "
							+ "LEFT JOIN Activity a ON a.id = r.recordActivity "
							+ "LEFT JOIN department_of_record d ON d.name = r.departmentOfRecord");
			StringBuilder whereClause = new StringBuilder();

			int countFilters = 1;
			for (Map.Entry<String, Object> filter : filters.entrySet()) {
				if (!(filter.getValue().equals("") || filter.getValue() == null)) {
					String key = Character.toUpperCase(filter.getKey()
							.charAt(0)) + filter.getKey().substring(1);
					if (key.equalsIgnoreCase("recordActivity")) {
						whereClause.append("UPPER(a.name) LIKE UPPER(?) AND ");
					} else if (key.equalsIgnoreCase("FunctionalMailbox")) {
						whereClause
								.append("UPPER(d.functionalMailbox) LIKE UPPER(?) AND ");
					} else {
						whereClause.append("UPPER(r." + key
								+ ") LIKE UPPER(?) AND ");
					}
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
					"RecordCategoryDBHelper countAllRecordCategory(): "
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
				LOG.error("RecordCategoryDBHelper countAllRecordCategory(): "
						+ ex.getMessage(), ex);
			}
		}
		return resultCount;
	}

	/**
	 * 
	 * @param recordCategory
	 * @return
	 */
	public static RecordCategory getRecordCategory(String recordCategory) {
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		RecordCategory record = null;

		try {
			connection = DBHelper.getDatabaseConnection();
			statement = connection
					.prepareStatement("SELECT * FROM Record_Category WHERE recordCategory = ?");
			statement.setString(1, recordCategory);
			resultSet = statement.executeQuery();
			if (resultSet.next()) {
				String categoryName = resultSet.getString("categoryName");
				String recordType = resultSet.getString("recordType");
				String recordActivity = resultSet.getString("recordActivity");
				int retentionPeriod = resultSet.getInt("retentionPeriod");
				String departmentOfRecord = resultSet
						.getString("departmentOfRecord");
				int autoDelete = resultSet.getInt("autoDelete");
				String comments = resultSet.getString("comments");
				int active = resultSet.getInt("active");
				record = new RecordCategory(recordCategory, categoryName,
						recordType, recordActivity, retentionPeriod,
						departmentOfRecord, autoDelete, comments, active);
			}
		} catch (Exception ex) {
			LOG.error(
					"RecordCategoryDBHelper getRecordCategory(): "
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
						"RecordCategoryDBHelper getRecordCategory(): "
								+ ex.getMessage(), ex);
			}
		}
		return record;
	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	public static List<RecordCategory> getRecordCategoriesByActivity(String id) {
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		List<RecordCategory> results = new ArrayList<RecordCategory>();

		try {
			connection = DBHelper.getDatabaseConnection();
			statement = connection
					.prepareStatement("SELECT * FROM Record_Category WHERE recordActivity = ?");
			statement.setString(1, id);
			resultSet = statement.executeQuery();
			while (resultSet.next()) {
				String recordCategory = resultSet.getString("recordCategory");
				String categoryName = resultSet.getString("categoryName");
				String recordType = resultSet.getString("recordType");
				String recordActivity = resultSet.getString("recordActivity");
				int retentionPeriod = resultSet.getInt("retentionPeriod");
				String departmentOfRecord = resultSet
						.getString("departmentOfRecord");
				int autoDelete = resultSet.getInt("autoDelete");
				String comments = resultSet.getString("comments");
				int active = resultSet.getInt("active");
				RecordCategory record = new RecordCategory(recordCategory,
						categoryName, recordType, recordActivity,
						retentionPeriod, departmentOfRecord, autoDelete,
						comments, active);
				results.add(record);
			}
		} catch (Exception ex) {
			LOG.error(
					"RecordCategoryDBHelper getRecordCategoriesByActivity(): "
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
						"RecordCategoryDBHelper getRecordCategoryiesByActivity(): "
								+ ex.getMessage(), ex);
			}
		}
		return results;
	}

	/**
	 * 
	 * @param departmentOfRecord
	 * @return
	 */
	public static List<RecordCategory> getRecordCategoriesByDepartment(
			String departmentOfRecord) {
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		List<RecordCategory> results = new ArrayList<RecordCategory>();

		try {
			connection = DBHelper.getDatabaseConnection();
			statement = connection
					.prepareStatement("SELECT * FROM Record_Category WHERE departmentOfRecord = ?");
			statement.setString(1, departmentOfRecord);
			resultSet = statement.executeQuery();
			while (resultSet.next()) {
				String recordCategory = resultSet.getString("recordCategory");
				String categoryName = resultSet.getString("categoryName");
				String recordType = resultSet.getString("recordType");
				String recordActivity = resultSet.getString("recordActivity");
				int retentionPeriod = resultSet.getInt("retentionPeriod");
				int autoDelete = resultSet.getInt("autoDelete");
				String comments = resultSet.getString("comments");
				int active = resultSet.getInt("active");
				RecordCategory record = new RecordCategory(recordCategory,
						categoryName, recordType, recordActivity,
						retentionPeriod, departmentOfRecord, autoDelete,
						comments, active);
				results.add(record);
			}
		} catch (Exception ex) {
			LOG.error(
					"RecordCategoryDBHelper getRecordCategoriesByActivity(): "
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
						"RecordCategoryDBHelper getRecordCategoryiesByActivity(): "
								+ ex.getMessage(), ex);
			}
		}
		return results;
	}

	/**
	 * 
	 * @param recordCategory
	 * @param categoryName
	 * @param recordType
	 * @param recordActivity
	 * @param retentionPeriod
	 * @param departmentOfRecord
	 * @param autoDelete
	 * @param comments
	 * @param active
	 * @return
	 */
	public static boolean createNewRecordCategory(String recordCategory,
			String categoryName, String recordType, String recordActivity,
			int retentionPeriod, String departmentOfRecord, int autoDelete,
			String comments, int active) {
		Connection connection = null;
		PreparedStatement statement = null;

		try {
			connection = DBHelper.getDatabaseConnection();
			statement = connection
					.prepareStatement("INSERT INTO Record_Category (recordCategory, recordType, recordActivity, retentionPeriod, departmentOfRecord, autoDelete,  comments, active, categoryName) VALUES (?,?,?,?,?,?,?,?, ?)");
			statement.setString(1, recordCategory);
			statement.setString(2, recordType);
			statement.setString(3, recordActivity);
			statement.setInt(4, retentionPeriod);
			statement.setString(5, departmentOfRecord);
			statement.setInt(6, autoDelete);
			statement.setString(7, comments);
			statement.setInt(8, active);
			statement.setString(9, categoryName);

			// LOG.info(queryString);
			statement.execute();
			connection.commit();
		} catch (Exception ex) {
			LOG.error(
					"RecordCategoryDBHelper createNewRecordCategory(): "
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
				LOG.error("RecordCategoryDBHelper createNewRecordCategory(): "
						+ ex.getMessage(), ex);
				return false;
			}
		}
		return true;
	}

	/**
	 * 
	 * @param recordCategory
	 * @param categoryName
	 * @param recordType
	 * @param recordActivity
	 * @param retentionPeriod
	 * @param departmentOfRecord
	 * @param autoDelete
	 * @param comments
	 * @param active
	 * @return
	 */
	public static boolean updateRecordCategory(String recordCategory,
			String categoryName, String recordType, String recordActivity,
			int retentionPeriod, String departmentOfRecord, int autoDelete,
			String comments, int active) {
		Connection connection = null;
		PreparedStatement statement = null;

		try {
			connection = DBHelper.getDatabaseConnection();
			statement = connection
					.prepareStatement("UPDATE Record_Category SET recordType = ?, recordActivity = ?, retentionPeriod = ?, departmentOfRecord = ?, autoDelete = ?, comments = ?, active = ?, categoryName = ? WHERE recordCategory = ?");
			statement.setString(9, recordCategory);
			statement.setString(1, recordType);
			statement.setString(2, recordActivity);
			statement.setInt(3, retentionPeriod);
			statement.setString(4, departmentOfRecord);
			statement.setInt(5, autoDelete);
			statement.setString(6, comments);
			statement.setInt(7, active);
			statement.setString(8, categoryName);
			statement.executeUpdate();
			connection.commit();
		} catch (Exception ex) {
			LOG.error(
					"RecordCategoryDBHelper updateRecordCategory(): "
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
				LOG.error("RecordCategoryDBHelper updateRecordCategory(): "
						+ ex.getMessage(), ex);
				return false;
			}
		}
		return true;
	}

	/**
	 * 
	 * @param recordCategory
	 * @return
	 */
	public static boolean deleteRecordCategory(String recordCategory) {
		Connection connection = null;
		PreparedStatement statement = null;

		try {
			connection = DBHelper.getDatabaseConnection();
			statement = connection
					.prepareStatement("DELETE FROM Record_Category WHERE recordCategory = ?");
			statement.setString(1, recordCategory);
			statement.executeUpdate();
			connection.commit();
		} catch (Exception ex) {
			LOG.error(
					"RecordCategoryDBHelper deleteRecordCategory(): "
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
				LOG.error("RecordCategoryDBHelper deleteRecordCategory(): "
						+ ex.getMessage(), ex);
				return false;
			}
		}
		return true;
	}

	/**
	 * 
	 * @param recordCategory
	 * @return
	 */
	public static boolean deactivateRecordCategory(String recordCategory) {
		Connection connection = null;
		PreparedStatement statement = null;

		try {
			connection = DBHelper.getDatabaseConnection();
			statement = connection
					.prepareStatement("UPDATE Record_Category SET active = 0 WHERE recordCategory = ?");
			statement.setString(1, recordCategory);
			statement.executeUpdate();
			connection.commit();
		} catch (Exception ex) {
			LOG.error("RecordCategoryDBHelper deactivateRecordCategory(): "
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
				LOG.error("RecordCategoryDBHelper deactivateRecordCategory(): "
						+ ex.getMessage(), ex);
				return false;
			}
		}
		return true;
	}

	/**
	 * 
	 * @param recordCategory
	 * @return
	 */
	public static boolean activateRecordCategory(String recordCategory) {
		Connection connection = null;
		PreparedStatement statement = null;

		try {
			connection = DBHelper.getDatabaseConnection();
			statement = connection
					.prepareStatement("UPDATE Record_Category SET active = 1 WHERE recordCategory = ?");
			statement.setString(1, recordCategory);
			statement.executeUpdate();
			connection.commit();
		} catch (Exception ex) {
			LOG.error(
					"RecordCategoryDBHelper activateRecordCategory(): "
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
				LOG.error("RecordCategoryDBHelper activateRecordCategory(): "
						+ ex.getMessage(), ex);
				return false;
			}
		}
		return true;
	}

	/**
	 * 
	 * @param activity
	 * @param department
	 * @return
	 */
	public static boolean updateRecordCategoriesDepartment(String activity,
			String department) {
		Connection connection = null;
		PreparedStatement statement = null;

		try {
			connection = DBHelper.getDatabaseConnection();
			statement = connection
					.prepareStatement("UPDATE Record_Category SET departmentOfRecord = ? WHERE recordActivity = ?");
			statement.setString(1, department);
			statement.setString(2, activity);
			statement.executeUpdate();
			connection.commit();
		} catch (Exception ex) {
			LOG.error(
					"RecordCategoryDBHelper updateRecordCategoriesDepartment(): "
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
						"RecordCategoryDBHelper updateRecordCategoriesDepartment(): "
								+ ex.getMessage(), ex);
				return false;
			}
		}
		return true;
	}

}
