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
		ResultSet res2;
		try {
			boolean exists = false;
			boolean archivesExists= false;
			meta = connection.getMetaData();
			res = meta.getTables(null, null, "chatreports", new String[] { "TABLE" });
			res2 = meta.getTables(null, null, "archivedreports", new String[] { "TABLE" });
			while (res.next()) {
				if (res.getString("TABLE_NAME").equals("chatreports")) {
					exists = true;
				}
			}
			while (res2.next()) {
				if(res2.getString("TABLE_NAME").equals("archivedreports")){
					archivesExists = true;
				}
			}
			if (!exists) {
				String query =
						"CREATE TABLE chatreports ("
						+ "id VARCHAR(6),"
						+ "playerName TINYTEXT,"
						+ "reason TINYTEXT,"
						+ "chatHistory LONGTEXT,"
						+ "date BIGINT,"
						+ "PRIMARY KEY (id)"
						+ ")";
				statement.executeUpdate(query);
				System.out.println("Table chatreports doesn't exist, creating it.");
			}
			if (!archivesExists) {
				System.out.println("OKAY" + !archivesExists);
				String query = "CREATE TABLE archivedreports ("
						+ "id VARCHAR(6),"
						+ "playerName TINYTEXT,"
						+ "reason TINYTEXT,"
						+ "chatHistory LONGTEXT,"
						+ "date BIGINT,"
						+ "PRIMARY KEY (id)"
						+ ")";
				statement.executeUpdate(query);
				System.out.println("Table archivedreports doesn't exist, creating it.");
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

	public void check() {
		try {
			if(connection.isValid(4) || connection.isClosed()){
				connect();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
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

	public boolean truncateArchive() {
		try {
			Statement statement = connection.createStatement();
			statement.executeUpdate("TRUNCATE TABLE archivedreports");
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
