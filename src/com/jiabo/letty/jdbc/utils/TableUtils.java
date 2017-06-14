package com.jiabo.letty.jdbc.utils;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiabo.letty.jdbc.DataSource;
import com.jiabo.letty.jdbc.DataSourceFactory;
import com.jiabo.letty.jdbc.annotation.Table;

/**
 * 建表工具类
 * 
 * @author jialong
 *
 */
public class TableUtils {

	private static final Logger log = LoggerFactory.getLogger(TableUtils.class);

	private JDBCUtils jdbcUtils;

	public TableUtils() {
		jdbcUtils = new JDBCUtils(DataSourceFactory.getDefaultDataSource());
	}

	public TableUtils(DataSource dataSource) {
		jdbcUtils = new JDBCUtils(dataSource);
	}

	public TableUtils(String dataSourceName) {
		jdbcUtils = new JDBCUtils(
				DataSourceFactory.getDataSource(dataSourceName));
	}

	/**
	 * 扫描model包，创建table
	 * 
	 * @param packageName
	 *            包名
	 * @param clearExistTable
	 *            是否清理已存在的表
	 */
	public void scanner(String packageName, boolean clearExistTable) {
		Set<Class<?>> set = ClassUtils.getClasses(packageName);
		for (Class<?> c : set) {
			if (!c.isAnnotationPresent(Table.class)) {
				log.info(c.getName()
						+ " is not annotation with Table,it will be escaped");
				continue;
			}
			createTable(c, clearExistTable);
		}

	}

	public void createTable(Class<?> c, boolean clearExistTable) {
		String tableName = c.getAnnotation(Table.class).value();
		if (tableName == null) {
			log.warn(c.getName() + " not contain the tablename");
			return;
		}
		if (clearExistTable) {
			jdbcUtils.execute(jdbcUtils.getDialect().dropTable(c), null);
		}
		log.info("create table..." + tableName);
		jdbcUtils.execute(jdbcUtils.getDialect().createTable(c), null);
	}
}
