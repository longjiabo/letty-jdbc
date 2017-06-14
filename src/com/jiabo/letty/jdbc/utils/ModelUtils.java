package com.jiabo.letty.jdbc.utils;

import com.jiabo.letty.jdbc.annotation.Table;

public class ModelUtils {

	public static String getTableName(Class<?> clazz) {
		if (clazz.isAnnotationPresent(Table.class))
			return clazz.getAnnotation(Table.class).value().toLowerCase();
		return null;
	}
}
