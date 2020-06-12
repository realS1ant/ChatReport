package me.fatpigsarefat.chatreport.utils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

	private Connection connection;
	private String database;
	private String username;
	private String password;

	public DatabaseConnection(String database, String username, String password) {
		this.database = database;
		this.username = username;
		this.password = password;
	}

	public String connect() {
		Statement statement;
		try {
			openConnection();
			statement = connection.createStatement();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return "A fatal error occured connecting to database (" + e.getMessage() + ")";
		} catch (SQLException e) {
			e.printStackTrace();
			return "A fatal error occured connecting to database (" + e.getMessage() + ")";
		} catch (Exception e) {
			e.printStackTrace();
			return "A fatal error occured connecting to database (" + e.getMessage() + ")";
		}
		DatabaseMetaData meta;
		ResultSet res;
		try {
			boolean exists = false;
			meta = connection.getMetaData();
			res = meta.getTables(null, null, "chatreports", new String[] { "TABLE" });
			while (res.next()) {
				if (res.getString("TABLE_NAME").equals("chatreports")) {
					exists = true;
				}
			}
			if (!exists) {
				String query = "CREATE TABLE chatreports ("
						+ "id VARCHAR(6),"
						+ "playerName TINYTEXT,"
						+ "reason TINYTEXT,"
						+ "chatHistory LONGTEXT,"
						+ "date BIGINT,"
						+ "PRIMARY KEY (id)"
						+ ")";
				statement.executeUpdate(query);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return "A fatal error occured connecting to database (" + e.getMessage() + ")";
		}
		try {
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return "A fatal error occured connecting to database (" + e.getMessage() + ")";
		}
		return "Database initialized.";
	}

	public String close() {
		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return "A fatal error occured closing connection to database.";
		}
		return "Database connection closed.";
	}
	
	public boolean truncateTable() {
		try {
			Statement statement = connection.createStatement();
			statement.executeUpdate("TRUNCATE TABLE chatreports");
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public String query(String sql) {
		try {
			Statement statement = connection.createStatement();
			statement.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return null;
	}
	
	public ResultSet queryWithResult(String sql) {
		try {
			Statement statement = connection.createStatement();
			ResultSet rs = statement.executeQuery(sql);
			return rs;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	private void openConnection() throws SQLException, ClassNotFoundException {
		if (connection != null && !connection.isClosed()) {
			return;
		}

		synchronized (this) {
			if (connection != null && !connection.isClosed()) {
				return;
			}
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection(database, this.username, this.password);
		}
	}
}
