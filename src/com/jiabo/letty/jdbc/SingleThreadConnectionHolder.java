package com.jiabo.letty.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

public class SingleThreadConnectionHolder {
	private static ThreadLocal<ConnectionHolder> localConnectionHolder = new ThreadLocal<ConnectionHolder>();

	public static Connection getConnection(DataSource dataSource)
			throws SQLException {
		return getConnectionHolder().getConnection(dataSource);
	}

	public static void removeConnection(DataSource dataSource) {
		getConnectionHolder().removeConnection(dataSource);
	}

	private static ConnectionHolder getConnectionHolder() {
		ConnectionHolder connectionHolder = localConnectionHolder.get();
		if (connectionHolder == null) {
			connectionHolder = new ConnectionHolder();
			localConnectionHolder.set(connectionHolder);
		}
		return connectionHolder;
	}

}