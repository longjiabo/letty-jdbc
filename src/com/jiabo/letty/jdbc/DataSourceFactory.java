package com.jiabo.letty.jdbc;

import java.util.HashMap;
import java.util.Map;

import com.jiabo.letty.jdbc.exception.LettyJDBCException;

public class DataSourceFactory {
	private static final String DEFAULT_DATASOURCE = "DEFAULT_SOURCE";

	private static Map<String, DataSource> map = new HashMap<String, DataSource>();

	public static DataSource getDefaultDataSource() {
		DataSource datasource = map.get(DEFAULT_DATASOURCE);
		if (datasource == null)
			throw new LettyJDBCException(
					"no default dataSource found,you must init the default datasource");
		return datasource;
	}

	public static void initDefaultDataSource(String url, String user,
			String password) {
		createDataSource(url, user, password, DEFAULT_DATASOURCE);
	}

	public static DataSource getDataSource(String dataSourceName) {
		DataSource datasource = map.get(dataSourceName);
		if (datasource == null)
			throw new LettyJDBCException("no dataSource found with "
					+ dataSourceName);
		return datasource;
	}

	public static DataSource createDataSource(String url, String user,
			String password, String dataSourceName) {
		DataSource dataSource = new DataSource(url, user, password);
		map.put(dataSourceName, dataSource);
		return dataSource;
	}
}
