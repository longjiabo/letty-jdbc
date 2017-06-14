package com.jiabo.letty.jdbc.dialect;

import java.lang.reflect.Field;
import java.util.Date;

import com.jiabo.letty.jdbc.annotation.Column;
import com.jiabo.letty.jdbc.annotation.Key;
import com.jiabo.letty.jdbc.annotation.Table;
import com.jiabo.letty.jdbc.utils.ModelUtils;

public class SqliteDialect implements Dialect {

	@Override
	public String dropTable(Class<?> t) {
		String sql = "drop table " + ModelUtils.getTableName(t);
		return sql;
	}

	@Override
	public String createTable(Class<?> t) {
		StringBuffer sb = new StringBuffer("CREATE TABLE IF NOT EXISTS ");
		sb.append(t.getAnnotation(Table.class).value()).append(" (");
		int count = 0;
		for (Field f : t.getDeclaredFields()) {
			if (f.isAnnotationPresent(Key.class)) {
				sb.append(getColumn(f)).append(" INTEGER  PRIMARY KEY ")
						.append("NOT NULL");
			} else {
				sb.append(f.getName()).append(" ").append(getType(f));
			}
			if (++count != t.getDeclaredFields().length)
				sb.append(",\r\n");
		}
		sb.append(")\r\n");
		return sb.toString();
	}

	private String getColumn(Field f) {
		if (f.isAnnotationPresent(Column.class))
			return f.getAnnotation(Column.class).value();
		return f.getName();
	}

	private String getType(Field f) {
		if (f.isAnnotationPresent(Column.class))
			return f.getAnnotation(Column.class).type();
		Class<?> type = f.getType();
		if (type.equals(Integer.class) || type.equals(int.class)) {
			return "INT";
		}
		if (type.equals(String.class))
			return "VARCHAR(500)";
		if (type.equals(Date.class))
			return "DATETIME";
		return null;
	}

	@Override
	public String top(Integer start, Integer offset) {
		return " limit " + start + " offset " + offset;
	}

	@Override
	public String count(String sql) {
		// TODO Auto-generated method stub
		return null;
	}
}
