package com.jiabo.letty.jdbc.dialect;

import java.lang.reflect.Field;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiabo.letty.jdbc.annotation.Column;
import com.jiabo.letty.jdbc.annotation.Key;
import com.jiabo.letty.jdbc.utils.ModelUtils;

public class MySQLDialect implements Dialect {
	private final Logger log = LoggerFactory.getLogger(MySQLDialect.class);

	@Override
	public String dropTable(Class<?> t) {
		String sql = "DROP TABLE IF EXISTS " + ModelUtils.getTableName(t);
		return sql;
	}

	@Override
	public String createTable(Class<?> t) {
		StringBuffer sb = new StringBuffer("CREATE TABLE IF NOT EXISTS ");
		sb.append(ModelUtils.getTableName(t)).append(" (");
		String key = null;
		int count = 0;
		for (Field f : t.getDeclaredFields()) {
			if (f.isAnnotationPresent(Key.class)) {
				key = "PRIMARY KEY (" + f.getName() + ")";
				sb.append(getColumn(f)).append(" ").append(getType(f))
						.append(" NOT NULL ");
				if (f.getType().equals(Integer.class)
						|| f.getType().equals(int.class)) {
					sb.append(" AUTO_INCREMENT");
				}
			} else {
				sb.append(f.getName()).append(" ").append(getType(f));
			}
			if (++count != t.getDeclaredFields().length)
				sb.append(",\r\n");
		}
		if (key != null)
			sb.append(",\r\n").append(key).append("\r\n");
		sb.append(")ENGINE=Innodb  DEFAULT CHARSET=utf8;  \r\n");
		log.debug(sb.toString());
		return sb.toString();
	}

	public String getColumn(Field f) {
		if (f.isAnnotationPresent(Column.class))
			return f.getAnnotation(Column.class).value();
		return f.getName();
	}

	public String getType(Field f) {
		if (f.isAnnotationPresent(Column.class))
			return f.getAnnotation(Column.class).type();
		Class<?> type = f.getType();
		if (type.equals(Integer.class) || type.equals(int.class))
			return "INT(11)";
		if (type.equals(String.class)) {
			return f.isAnnotationPresent(Key.class) ? "VARCHAR(50)"
					: "VARCHAR(500)";
		}

		if (type.equals(Date.class))
			return "DATETIME";
		return null;
	}

	@Override
	public String top(Integer start, Integer offset) {
		return " limit " + start + "," + offset;
	}

	@Override
	public String count(String sql) {
		return "select count(1) from (" + sql + " ) as t";
	}

}
