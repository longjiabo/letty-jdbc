package com.jiabo.letty.jdbc.dialect;


public interface Dialect {
	String dropTable(Class<?> t);

	String createTable(Class<?> t);

	String top(Integer start, Integer offset);
	
	String count(String sql);

}
