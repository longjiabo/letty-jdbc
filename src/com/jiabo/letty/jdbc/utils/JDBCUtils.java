package com.jiabo.letty.jdbc.utils;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiabo.letty.jdbc.DBType;
import com.jiabo.letty.jdbc.DataSource;
import com.jiabo.letty.jdbc.DataSourceFactory;
import com.jiabo.letty.jdbc.SingleThreadConnectionHolder;
import com.jiabo.letty.jdbc.dialect.Dialect;
import com.jiabo.letty.jdbc.exception.LettyJDBCException;

public class JDBCUtils {

	private static final Logger log = LoggerFactory.getLogger(JDBCUtils.class);

	private DataSource dataSource;

	public Dialect getDialect() {
		return BaseUtils.getDialect(getDBType());
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource ds) {
		this.dataSource = ds;
	}

	public JDBCUtils() {
		this.dataSource = DataSourceFactory.getDefaultDataSource();
	}

	public JDBCUtils(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public DBType getDBType() {
		return dataSource.getDbType();
	}

	public Object queryForObject(String sql, Object[] params) {
		List<Map<String, Object>> list = queryForList(sql, params);
		if (list.isEmpty())
			throw new LettyJDBCException("result is empty ");
		if (list.size() > 1)
			throw new LettyJDBCException("result more than one");
		return list.get(0).values().iterator().next();
	}

	public Map<String, Object> queryForMap(String sql, Object[] params) {
		List<Map<String, Object>> list = queryForList(sql, params);
		if (list.isEmpty() || list.size() > 1)
			return null;
		return list.get(0);
	}

	public Connection getConnection() throws SQLException {
		return SingleThreadConnectionHolder.getConnection(dataSource);
	}

	public List<Map<String, Object>> queryForList(String sql, Object[] params) {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		if (sql == null || "".equals(sql.trim()))
			return list;
		Connection conn = null;
		try {
			conn = SingleThreadConnectionHolder.getConnection(dataSource);
			PreparedStatement ps = conn.prepareStatement(sql);
			int s = 0;
			if (params != null) {
				for (Object obj : params) {
					if (obj instanceof java.util.Date) {
						obj = new java.sql.Timestamp(
								((java.util.Date) obj).getTime());
					}
					ps.setObject(++s, obj);
				}
			}
			ResultSet rs = ps.executeQuery();
			ResultSetMetaData md = rs.getMetaData();
			int count = md.getColumnCount();

			while (rs.next()) {
				Map<String, Object> row = new HashMap<String, Object>();
				for (int i = 1; i <= count; i++) {
					String name = md.getColumnLabel(i);
					row.put(name,
							getDataValue(rs, i, md.getColumnType(i),
									md.getScale(i)));
				}
				list.add(row);
			}
			ps.close();
			return list;
		} catch (SQLException e) {
			log.error("", e);
		} finally {
			SingleThreadConnectionHolder.removeConnection(dataSource);
		}
		return list;
	}

	public boolean execute(String sql, Object[] params) {
		Connection conn = null;
		try {
			conn = SingleThreadConnectionHolder.getConnection(dataSource);
			PreparedStatement ps = conn.prepareStatement(sql);
			int i = 0;
			if (params != null) {
				for (Object obj : params) {
					if (obj instanceof java.util.Date) {
						obj = new java.sql.Timestamp(
								((java.util.Date) obj).getTime());
					}
					ps.setObject(++i, obj);
				}
			}
			ps.execute();
			ps.close();
			return true;
		} catch (SQLException e) {
			log.error("", e);
		} finally {
			SingleThreadConnectionHolder.removeConnection(dataSource);
		}
		return false;

	}

	private Object getDataValue(ResultSet rs, int columnIndex, int type,
			int scale) {
		try {
			switch (type) {
			case Types.LONGVARCHAR: // -1
				return rs.getLong(columnIndex);
			case Types.CLOB: {
				Clob clob = rs.getClob(columnIndex);
				if (clob == null)
					return null;
				return clob.toString();
			}
			case Types.CHAR: // 1
				return rs.getString(columnIndex);
			case Types.NUMERIC: // 2
				switch (scale) {
				case 0:
					return rs.getInt(columnIndex);
				case -127: {
					try {
						return rs.getInt(columnIndex);
					} catch (Exception e) {
						return rs.getFloat(columnIndex);
					}
				}
				default:
					return rs.getObject(columnIndex);
				}
			case Types.VARCHAR: // 12
				return rs.getString(columnIndex);
			case Types.BLOB:
				return rs.getBlob(columnIndex);
			default:
				return rs.getObject(columnIndex);
			}
		} catch (Exception e) {
			log.error("", e);
		}
		return null;
	}
}
