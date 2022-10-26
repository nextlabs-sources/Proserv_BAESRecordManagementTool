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
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.nextlabs.bae.entity.Activity;

public class ActivityDBHelper implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final Log LOG = LogFactory.getLog(ActivityDBHelper.class);

	/**
	 * Return activity list for lazy loading
	 * 
	 * @param start
	 *            Begin index of the page
	 * @param size
	 *            Size of the page
	 * @param sortField
	 *            Field used to sort the list
	 * @param sortOrder
	 *            Order used to sort the list
	 * @param fileters
	 *            A map of filters to use in query
	 * @return The result list
	 */
	public static List<Activity> getActivityListLazy(int start, int size,
			String sortField, String sortOrder, Map<String, Object> filters) {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		List<Activity> resultList = new ArrayList<Activity>();
		Map<String, Integer> parameterIndex = new HashMap<String, Integer>();

		try {
			connection = DBHelper.getDatabaseConnection();
			StringBuilder queryString = new StringBuilder(
					"SELECT outer.* FROM (SELECT ROWNUM rn, inner.* FROM (SELECT * FROM Activity");
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
				String name = resultSet.getString("name");
				String recordOfficersGuidance = resultSet
						.getString("recordOfficersGuidance");
				int active = resultSet.getInt("active");
				Activity activity = new Activity(id, name,
						recordOfficersGuidance, active);
				resultList.add(activity);
			}
		} catch (Exception ex) {
			LOG.error(
					"ActivityDBHelper getActivityListLazy(): "
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
						"ActivityDBHelper getActivityListLazy(): "
								+ ex.getMessage(), ex);
			}
		}
		return resultList;
	}

	/**
	 * Count all activity for lazy loading
	 * 
	 * @param filters
	 *            Search filters
	 * @return Number of activity
	 */
	public static int countAllActivity(Map<String, Object> filters) {
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
					"SELECT COUNT(*) FROM Activity");
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
					"ActivityDBHelper countAllActivity(): " + ex.getMessage(),
					ex);
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
						"ActivityDBHelper countAllActivity(): "
								+ ex.getMessage(), ex);
			}
		}
		return resultCount;
	}

	/**
	 * Get an activity from database
	 * 
	 * @param name
	 *            Activity name
	 * @return The activity
	 */
	public static Activity getActivityByName(String name) {
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		Activity activity = null;

		try {
			connection = DBHelper.getDatabaseConnection();
			statement = connection
					.prepareStatement("SELECT * FROM Activity WHERE Name = ?");
			statement.setString(1, name);
			resultSet = statement.executeQuery();
			if (resultSet.next()) {
				String id = resultSet.getString("id");
				String recordOfficersGuidance = resultSet
						.getString("recordOfficersGuidance");
				int active = resultSet.getInt("active");
				activity = new Activity(id, name, recordOfficersGuidance,
						active);
			}
		} catch (Exception ex) {
			LOG.error("ActivityDBHelper getActivity(): " + ex.getMessage(), ex);
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
				LOG.error("ActivityDBHelper getActivity(): " + ex.getMessage(),
						ex);
			}
		}
		return activity;
	}

	/**
	 * Get activity by id
	 * 
	 * @param id
	 *            Activity id
	 * @return The activity
	 */
	public static Activity getActivityById(String id) {
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		Activity activity = null;

		try {
			connection = DBHelper.getDatabaseConnection();
			statement = connection
					.prepareStatement("SELECT * FROM Activity WHERE id = ?");
			statement.setString(1, id);
			resultSet = statement.executeQuery();
			if (resultSet.next()) {
				String name = resultSet.getString("name");
				String recordOfficersGuidance = resultSet
						.getString("recordOfficersGuidance");
				int active = resultSet.getInt("active");
				activity = new Activity(id, name, recordOfficersGuidance,
						active);
			}
		} catch (Exception ex) {
			LOG.error("ActivityDBHelper getActivity(): " + ex.getMessage(), ex);
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
				LOG.error("ActivityDBHelper getActivity(): " + ex.getMessage(),
						ex);
			}
		}
		return activity;
	}

	/**
	 * Get a limit number of activity names
	 * 
	 * @param limit
	 *            The limit
	 * @param query
	 *            Names should start with these characters
	 * @return List of activity names
	 */
	public static List<String> getLimitedActivityNames(int limit, String query) {
		List<String> results = new ArrayList<String>();
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;

		try {
			connection = DBHelper.getDatabaseConnection();
			statement = connection
					.prepareStatement("SELECT Name FROM Activity WHERE UPPER(Name) LIKE UPPER(?) AND ROWNUM <= "
							+ limit);
			statement.setString(1, query + "%");
			resultSet = statement.executeQuery();
			while (resultSet.next()) {
				String name = resultSet.getString("name");
				results.add(name);
			}
		} catch (Exception ex) {
			LOG.error(
					"ActivityDBHelper getLimitedActivityNames(): "
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
						"ActivityDBHelper getLimitedActivityNames(): "
								+ ex.getMessage(), ex);
			}
		}
		return results;
	}

	/**
	 * Create a new activity
	 * 
	 * @param name
	 *            Activity name
	 * @param recordOfficersGuidance
	 *            Activity guidance for Record Officers
	 * @param active
	 *            Active status
	 * @return True if succeed, False otherwise
	 */
	public static boolean createNewActivity(String name,
			String recordOfficersGuidance, int active) {
		Connection connection = null;
		PreparedStatement statement = null;

		try {
			connection = DBHelper.getDatabaseConnection();
			statement = connection
					.prepareStatement("INSERT INTO Activity (name, recordOfficersGuidance, id, active) VALUES (?, ?, ?, ?)");
			statement.setString(1, name);
			statement.setString(2, recordOfficersGuidance);
			statement.setString(3, UUID.randomUUID().toString());
			statement.setInt(4, active);
			// LOG.info(queryString);
			statement.execute();
			connection.commit();
		} catch (Exception ex) {
			LOG.error(
					"ActivityDBHelper createNewActivity(): " + ex.getMessage(),
					ex);
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
						"ActivityDBHelper createNewActivity(): "
								+ ex.getMessage(), ex);
				return false;
			}
		}
		return true;
	}

	/**
	 * Update an activity information
	 * 
	 * @param name
	 *            Activity name
	 * @param recordOfficersGuidance
	 *            Activity guidance for officers
	 * @param id
	 *            Activity id
	 * @param active
	 *            Active status
	 * @return True if succeed, False otherwise
	 */
	public static boolean updateActivity(String name,
			String recordOfficersGuidance, String id, int active) {
		Connection connection = null;
		PreparedStatement statement = null;

		try {
			connection = DBHelper.getDatabaseConnection();
			statement = connection
					.prepareStatement("UPDATE Activity SET recordOfficersGuidance = ?, name = ?, active = ? WHERE id = ?");
			statement.setString(1, recordOfficersGuidance);
			statement.setString(2, name);
			statement.setInt(3, active);
			statement.setString(4, id);
			statement.executeUpdate();
			connection.commit();
		} catch (Exception ex) {
			LOG.error("ActivityDBHelper updateActivity(): " + ex.getMessage(),
					ex);
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
						"ActivityDBHelper updateActivity(): " + ex.getMessage(),
						ex);
				return false;
			}
		}
		return true;
	}

	/**
	 * Delete an activity from database
	 * 
	 * @param id
	 *            Activity id
	 * @return True if succeed, False otherwise
	 */
	public static boolean deleteActivity(String id) {
		Connection connection = null;
		PreparedStatement statement = null;

		try {
			connection = DBHelper.getDatabaseConnection();
			statement = connection
					.prepareStatement("DELETE FROM Activity WHERE id = ?");
			statement.setString(1, id);
			statement.executeUpdate();
			connection.commit();
		} catch (Exception ex) {
			LOG.error("ActivityDBHelper deleteActivity(): " + ex.getMessage(),
					ex);
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
						"ActivityDBHelper deleteActivity(): " + ex.getMessage(),
						ex);
				return false;
			}
		}
		return true;
	}

	/**
	 * Deactivate an activity
	 * 
	 * @param id
	 *            Activity id
	 * @return True if succeed, False otherwise
	 */
	public static boolean deactivateActivity(String id) {
		Connection connection = null;
		PreparedStatement statement = null;

		try {
			connection = DBHelper.getDatabaseConnection();
			statement = connection
					.prepareStatement("UPDATE Activity SET active = 0 WHERE id = ?");
			statement.setString(1, id);
			statement.executeUpdate();
			connection.commit();
		} catch (Exception ex) {
			LOG.error(
					"ActivityDBHelper deactivateActivity(): " + ex.getMessage(),
					ex);
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
						"ActivityDBHelper deactivateActivity(): "
								+ ex.getMessage(), ex);
				return false;
			}
		}
		return true;
	}

	/**
	 * Activate an activity
	 * 
	 * @param id
	 *            Activity id
	 * @return True if succeed, False otherwise
	 */
	public static boolean activateActivity(String id) {
		Connection connection = null;
		PreparedStatement statement = null;

		try {
			connection = DBHelper.getDatabaseConnection();
			statement = connection
					.prepareStatement("UPDATE Activity SET active = 1 WHERE id = ?");
			statement.setString(1, id);
			statement.executeUpdate();
			connection.commit();
		} catch (Exception ex) {
			LOG.error(
					"ActivityDBHelper activateActivity(): " + ex.getMessage(),
					ex);
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
						"ActivityDBHelper activateActivity(): "
								+ ex.getMessage(), ex);
				return false;
			}
		}
		return true;
	}

}
