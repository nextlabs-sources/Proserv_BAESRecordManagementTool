package com.nextlabs.bae.dbhelper;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.nextlabs.bae.entity.ActivityDetail;

public class ActivityDetailDBHelper implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final Log LOG = LogFactory
			.getLog(ActivityDetailDBHelper.class);

	/**
	 * Get list of all activity detail - used for lazy loading
	 * 
	 * @param start
	 *            Page start
	 * @param size
	 *            Page size
	 * @param sortField
	 *            Field used for sorting
	 * @param sortOrder
	 *            Sort order
	 * @param filters
	 *            Search filters
	 * @return THe list of activity detail
	 */
	public static List<ActivityDetail> getActivityDetailListLazy(int start,
			int size, String sortField, String sortOrder,
			Map<String, Object> filters) {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		List<ActivityDetail> resultList = new ArrayList<ActivityDetail>();
		Map<String, Integer> parameterIndex = new HashMap<String, Integer>();

		try {
			connection = DBHelper.getDatabaseConnection();
			StringBuilder queryString = new StringBuilder(
					"SELECT outer.* FROM (SELECT ROWNUM rn, inner.* FROM (SELECT * FROM Activity_Detail");
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
				String id = resultSet.getString("id");
				String activity = resultSet.getString("activity");
				String titleValue = resultSet.getString("titleValue");
				String referenceValue = resultSet.getString("referenceValue");
				Timestamp dateValue = resultSet.getTimestamp("dateValue");
				ActivityDetail ra = new ActivityDetail(id, activity,
						titleValue, referenceValue, dateValue);

				resultList.add(ra);
			}
		} catch (Exception ex) {
			LOG.error("ActivityDetailDBHelper getActivityDetailListLazy(): "
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
						"ActivityDetailDBHelper getActivityDetailListLazy(): "
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
	public static int countAllActivityDetail(Map<String, Object> filters) {
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
					"SELECT COUNT(*) FROM Activity_Detail");
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
					"ActivityDetailDBHelper countAllActivityDetail(): "
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
				LOG.error("ActivityDetailDBHelper countAllActivityDetail(): "
						+ ex.getMessage(), ex);
			}
		}
		return resultCount;
	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	public static ActivityDetail getActivityDetail(String id) {
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		ActivityDetail recordActivity = null;

		try {
			connection = DBHelper.getDatabaseConnection();
			statement = connection
					.prepareStatement("SELECT * FROM Activity_Detail WHERE id = ?");
			statement.setString(1, id);
			resultSet = statement.executeQuery();
			if (resultSet.next()) {
				String activity = resultSet.getString("activity");
				String titleValue = resultSet.getString("titleValue");
				String referenceValue = resultSet.getString("referenceValue");
				Timestamp dateValue = resultSet.getTimestamp("dateValue");
				recordActivity = new ActivityDetail(id, activity, titleValue,
						referenceValue, dateValue);
			}
		} catch (Exception ex) {
			LOG.error(
					"ActivityDetailDBHelper getActivityDetail(): "
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
						"ActivityDetailDBHelper getActivityDetail(): "
								+ ex.getMessage(), ex);
			}
		}
		return recordActivity;
	}

	/**
	 * 
	 * @param activity
	 * @param title
	 * @param reference
	 * @param date
	 * @return
	 */
	public static ActivityDetail getActivityDetail(String activity,
			String title, String reference, Timestamp date) {
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		ActivityDetail recordActivity = null;

		try {
			connection = DBHelper.getDatabaseConnection();
			statement = connection
					.prepareStatement("SELECT * FROM Activity_Detail WHERE titleValue = ? AND referenceValue = ? AND dateValue = ? AND activity = ?");
			statement.setString(1, title);
			statement.setString(2, reference);
			statement.setTimestamp(3, date);
			statement.setString(4, activity);
			resultSet = statement.executeQuery();
			if (resultSet.next()) {
				String id = resultSet.getString("id");
				recordActivity = new ActivityDetail(id, activity, title,
						reference, date);
			}
		} catch (Exception ex) {
			LOG.error(
					"ActivityDetailDBHelper getActivityDetail(): "
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
						"ActivityDetailDBHelper getActivityDetail(): "
								+ ex.getMessage(), ex);
			}
		}
		return recordActivity;
	}

	/**
	 * 
	 * @param activity
	 * @param titleValue
	 * @param referenceValue
	 * @param dateValue
	 * @return
	 */
	public static boolean createNewActivityDetail(String activity,
			String titleValue, String referenceValue, Timestamp dateValue) {
		Connection connection = null;
		PreparedStatement statement = null;

		try {
			connection = DBHelper.getDatabaseConnection();
			statement = connection
					.prepareStatement("INSERT INTO Activity_Detail (activity, titleValue, referenceValue, dateValue, id) VALUES (?, ?, ?, ?, ?)");
			statement.setString(1, activity);
			statement.setString(2, titleValue);
			statement.setString(3, referenceValue);
			statement.setTimestamp(4, dateValue);
			statement.setString(5, UUID.randomUUID().toString());
			statement.execute();
			connection.commit();
		} catch (Exception ex) {
			LOG.error(
					"ActivityDetailDBHelper createNewActivityDetail(): "
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
				LOG.error("ActivityDetailDBHelper createNewActivityDetail(): "
						+ ex.getMessage(), ex);
				return false;
			}
		}
		return true;
	}

	/**
	 * 
	 * @param id
	 * @param activity
	 * @param titleValue
	 * @param referenceValue
	 * @param dateValue
	 * @return
	 */
	public static boolean updateActivityDetail(String id, String activity,
			String titleValue, String referenceValue, Timestamp dateValue) {
		Connection connection = null;
		PreparedStatement statement = null;

		try {
			connection = DBHelper.getDatabaseConnection();
			statement = connection
					.prepareStatement("UPDATE Activity_Detail SET titleValue = ?, referenceValue = ?, dateValue = ? WHERE activity = ? AND id = ?");
			statement.setString(4, activity);
			statement.setString(5, id);
			statement.setString(1, titleValue);
			statement.setString(2, referenceValue);
			statement.setTimestamp(3, dateValue);
			statement.executeUpdate();
			connection.commit();
		} catch (Exception ex) {
			LOG.error(
					"ActivityDetailDBHelper updateActivityDetail(): "
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
				LOG.error("ActivityDetailDBHelper updateActivityDetail(): "
						+ ex.getMessage(), ex);
				return false;
			}
		}
		return true;
	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	public static boolean deleteActivityDetail(String id) {
		Connection connection = null;
		PreparedStatement statement = null;

		try {
			connection = DBHelper.getDatabaseConnection();
			statement = connection
					.prepareStatement("DELETE FROM Activity_Detail WHERE id = ?");
			statement.setString(1, id);
			statement.executeUpdate();
			connection.commit();
		} catch (Exception ex) {
			LOG.error(
					"ActivityDetailDBHelper deleteActivityDetail(): "
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
				LOG.error("ActivityDetailDBHelper deleteActivityDetail(): "
						+ ex.getMessage(), ex);
				return false;
			}
		}
		return true;
	}

	/**
	 * 
	 * @param activity
	 * @return
	 */
	public static boolean deleteActivityDetailByActivity(String activity) {
		Connection connection = null;
		PreparedStatement statement = null;

		try {
			connection = DBHelper.getDatabaseConnection();
			statement = connection
					.prepareStatement("DELETE FROM Activity_Detail WHERE activity = ?");
			statement.setString(1, activity);
			statement.executeUpdate();
			connection.commit();
		} catch (Exception ex) {
			LOG.error(
					"ActivityDetailDBHelper deleteActivityDetail(): "
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
				LOG.error("ActivityDetailDBHelper deleteActivityDetail(): "
						+ ex.getMessage(), ex);
				return false;
			}
		}
		return true;
	}

}
