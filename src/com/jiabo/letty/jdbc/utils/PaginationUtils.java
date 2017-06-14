package com.jiabo.letty.jdbc.utils;

import java.util.List;
import java.util.Map;

import com.jiabo.letty.jdbc.DataSource;
import com.jiabo.letty.jdbc.bean.Pagination;

public class PaginationUtils extends DAOUtils {
	public PaginationUtils() {
		super();
	}

	public PaginationUtils(DataSource dataSource) {
		super(dataSource);
	}

	public <T> void queryForObjects(String sql, Object[] params,
			Class<T> clazz, Pagination pg) {
		String countsql = null;
		if (sql.toLowerCase().startsWith("where")) {
			countsql = "select count(1) from " + ModelUtils.getTableName(clazz)
					+ " " + sql;
		} else {
			countsql = "select count(1) from ( " + sql + " )";
		}
		Long count = (Long) queryForObject(countsql, params);
		pg.setTotal(count.intValue());
		if (pg.getPage() != -1 && pg.getPageSize() != -1) {
			sql += getDialect().top((pg.getPage() - 1) * pg.getPageSize(),
					pg.getPageSize());
		}
		List<T> list = queryForObjects(sql, params, clazz);
		pg.setData(list);
	}

	public void queryForObjects(String sql, Object[] params, Pagination pg) {
		String countsql = getDialect().count(sql);
		Long count = (Long) queryForObject(countsql, params);
		pg.setTotal(count.intValue());
		if (pg.getPage() != null && pg.getPage() != -1
				&& pg.getPageSize() != null && pg.getPageSize() != -1) {
			sql = "select * from ( "
					+ sql
					+ " ) as T"
					+ getDialect().top((pg.getPage() - 1) * pg.getPageSize(),
							pg.getPageSize());
		}
		List<Map<String, Object>> list = queryForList(sql, params);
		pg.setData(list);
	}
}
