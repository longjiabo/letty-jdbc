package com.jiabo.letty.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataSource {

	private String url;
	private String user;
	private String password;
	private DBType dbType;
	private int connectionPoolSize;

	private static final Logger log = LoggerFactory.getLogger(DataSource.class);
	private Queue<Connection> conns = new LinkedList<Connection>();

	public DataSource(String url, String user, String password) {
		this.url = url;
		this.user = user;
		this.password = password;
		if (url.startsWith("jdbc:mysql")) {
			try {
				Class.forName("com.mysql.jdbc.Driver");
				dbType = DBType.MYSQL;
			} catch (ClassNotFoundException e) {
				log.error("can not load driver", e);
			}
		} else if (url.startsWith("jdbc:sqlite")) {
			try {
				Class.forName("org.sqlite.JDBC");
				dbType = DBType.SQLITE;
			} catch (ClassNotFoundException e) {
				log.error("can not load driver", e);
			}
		} else if (url.startsWith("jdbc:oracle")) {
			try {
				Class.forName("oracle.jdbc.driver.OracleDriver");
				dbType = DBType.ORACLE;
			} catch (ClassNotFoundException e) {
				log.error("can not load driver", e);
			}
		}
	}

	public synchronized Connection getConnection() throws SQLException {
		Connection con = conns.poll();
		if (con == null) {
			con = DriverManager.getConnection(url, user, password);
		}
		return con;
	}

	public void releaseConnection(Connection conn) {
		if (conn == null)
			return;
		if (conns.size() > connectionPoolSize) {
			try {
				conn.close();
			} catch (SQLException e) {
				log.error(e.getMessage());
			}
		} else {
			conns.add(conn);
		}
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public DBType getDbType() {
		return dbType;
	}

	public void setDbType(DBType dbType) {
		this.dbType = dbType;
	}

	public int getConnectionPoolSize() {
		return connectionPoolSize;
	}

	public void setConnectionPoolSize(int connectionPoolSize) {
		this.connectionPoolSize = connectionPoolSize;
	}

}
