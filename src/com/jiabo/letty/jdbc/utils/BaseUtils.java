package com.jiabo.letty.jdbc.utils;

import java.util.HashMap;
import java.util.Map;

import com.jiabo.letty.jdbc.DBType;
import com.jiabo.letty.jdbc.dialect.Dialect;
import com.jiabo.letty.jdbc.dialect.MySQLDialect;
import com.jiabo.letty.jdbc.dialect.OracleDialect;
import com.jiabo.letty.jdbc.dialect.SqliteDialect;

public class BaseUtils {

	private static Map<DBType, Dialect> map = new HashMap<DBType, Dialect>();

	public static Dialect getDialect(DBType dbType) {
		if (map.isEmpty()) {
			map.put(DBType.MYSQL, new MySQLDialect());
			map.put(DBType.SQLITE, new SqliteDialect());
			map.put(DBType.ORACLE, new OracleDialect());
		}
		return map.get(dbType);
	}
}
