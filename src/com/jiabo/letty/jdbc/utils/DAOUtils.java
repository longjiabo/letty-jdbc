package com.jiabo.letty.jdbc.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiabo.letty.jdbc.DBType;
import com.jiabo.letty.jdbc.DataSource;
import com.jiabo.letty.jdbc.annotation.Column;
import com.jiabo.letty.jdbc.annotation.Key;
import com.jiabo.letty.jdbc.annotation.Table;

public class DAOUtils extends JDBCUtils {
	private static final Logger log = LoggerFactory.getLogger(DAOUtils.class);

	public DAOUtils() {
		super();
	}

	public DAOUtils(DataSource dataSource) {
		super(dataSource);
	}

	public <T> T queryById(Object id, Class<T> clazz) {
		String key = null;
		for (Field field : clazz.getDeclaredFields()) {
			if (field.isAnnotationPresent(Key.class)) {
				key = field.getName();
				break;
			}
		}
		String sql = "select *  from " + ModelUtils.getTableName(clazz)
				+ " where " + key + "=?";
		List<T> list = queryForObjects(sql, new Object[] { id }, clazz);
		if (list.isEmpty() || list.size() > 1)
			return null;
		return list.get(0);
	}

	public <T> void deleteById(Object id, Class<T> clazz) {
		String key = null;
		for (Field field : clazz.getDeclaredFields()) {
			if (field.isAnnotationPresent(Key.class)) {
				key = field.getName();
				break;
			}
		}
		String sql = "delete  from " + ModelUtils.getTableName(clazz)
				+ " where " + key + "=?";
		execute(sql, new Object[] { id });
	}

	public boolean update(Object obj) {
		Table table = obj.getClass().getAnnotation(Table.class);
		if (table == null)
			return false;
		String tableName = ModelUtils.getTableName(obj.getClass());
		Field id = null;
		for (Field field : obj.getClass().getDeclaredFields()) {
			if (field.isAnnotationPresent(Key.class)) {
				field.setAccessible(true);
				id = field;
			}
		}
		if (id == null)
			return false;
		Map<String, Object> result = parseObjFieldForInsert(obj);
		StringBuffer sql = new StringBuffer("update ").append(tableName)
				.append(" set ");
		int index = 0;
		int count = result.entrySet().size();
		Object[] params = new Object[count + 1];
		for (Map.Entry<String, Object> entry : result.entrySet()) {
			sql.append(entry.getKey()).append("=?");
			if (index != count - 1) {
				sql.append(",");
			}
			params[index] = entry.getValue();
			index++;
		}
		sql.append(" where ").append(id.getName()).append("=?");
		try {
			params[count] = id.get(obj);
		} catch (Exception e) {
			log.error("", e);
		}
		log.info(sql.toString());
		return execute(sql.toString(), params);
	}

	public boolean insert(Object obj) {
		Map<String, Object> result = parseObjFieldForInsert(obj);
		StringBuffer sql = new StringBuffer();
		sql.append("insert into ");
		Table table = obj.getClass().getAnnotation(Table.class);
		if (table == null)
			return false;
		sql.append(ModelUtils.getTableName(obj.getClass())).append(" ( ");
		int count = result.entrySet().size();
		int index = 0;
		Object[] params = new Object[result.entrySet().size()];
		for (Map.Entry<String, Object> entry : result.entrySet()) {
			sql.append(entry.getKey());
			if (index == count - 1) {
				sql.append(" )");
			} else {
				sql.append(",");
			}
			params[index] = entry.getValue();
			index++;
		}
		sql.append(" values (");
		for (int i = 0; i < count; i++) {
			sql.append("?");
			if (i == count - 1) {
				sql.append(")");
			} else {
				sql.append(",");
			}
		}
		log.debug(sql.toString());
		log.debug(Arrays.toString(params));
		execute(sql.toString(), params);
		if (getDBType().equals(DBType.SQLITE)) {
			String sq = "select last_insert_rowid() as id from "
					+ ModelUtils.getTableName(obj.getClass())
					+ " order by id desc limit 1";
			setId(obj, (Integer) queryForObject(sq, null));
		}
		return true;
	}

	private void setId(Object obj, int id) {
		for (Field field : obj.getClass().getDeclaredFields()) {
			if (field.isAnnotationPresent(Key.class)) {
				field.setAccessible(true);
				try {
					field.set(obj, id);
				} catch (IllegalArgumentException e) {
					log.error("", e);
				} catch (IllegalAccessException e) {
					log.error("", e);
				}
			}
		}
	}

	private Map<String, Object> parseObjFieldForInsert(Object obj) {
		Map<String, Object> map = new HashMap<String, Object>();
		Field[] fields = obj.getClass().getDeclaredFields();
		for (Field field : fields) {
			try {
				String column = field.getName();
				if (field.isAnnotationPresent(Column.class)) {
					String val = field.getAnnotation(Column.class).value();
					if (val != null && !"".equals(val))
						column = val;
				}
				field.setAccessible(true);
				map.put(column, field.get(obj));
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return map;
	}

	public <T> T queryForObject(String sql, Object[] params, Class<T> clazz) {
		List<T> list = queryForObjects(sql, params, clazz);
		if (list.isEmpty() || list.size() > 1)
			return null;
		return list.get(0);
	}

	public <T> List<T> queryForObjects(String sql, Object[] params,
			Class<T> clazz) {
		if (!sql.startsWith("select"))
			sql = "select * from " + ModelUtils.getTableName(clazz) + " " + sql;
		List<Map<String, Object>> list = queryForList(sql, params);
		Field[] fields = clazz.getDeclaredFields();
		List<T> result = new ArrayList<T>();
		for (Map<String, Object> map : list) {
			T data = null;
			try {
				data = clazz.newInstance();
			} catch (Exception e1) {
				log.error("", e1);
			}
			for (Field f : fields) {
				try {
					f.setAccessible(true);
					String column = f.getName();
					if (f.isAnnotationPresent(Column.class)) {
						String val = f.getAnnotation(Column.class).value();
						if (val != null && !"".equals(val.trim()))
							column = val;
					}
					// FIXME
					if (getDBType().equals(DBType.ORACLE)) {
						f.set(data, map.get(column.toUpperCase()));
					} else {
						f.set(data, map.get(column));
					}
				} catch (IllegalAccessException e) {
					log.error("", e);
				}
			}
			result.add(data);
		}
		return result;
	}
}
