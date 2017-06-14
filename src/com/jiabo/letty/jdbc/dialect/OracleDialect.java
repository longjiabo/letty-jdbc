package com.jiabo.letty.jdbc.dialect;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.jiabo.letty.jdbc.annotation.Column;
import com.jiabo.letty.jdbc.annotation.Key;
import com.jiabo.letty.jdbc.annotation.Table;
import com.jiabo.letty.jdbc.utils.ModelUtils;

//FIXME
public class OracleDialect implements Dialect {

	@Override
	public String dropTable(Class<?> t) {
		String sql = "drop table FUSION." + ModelUtils.getTableName(t);
		return sql;
	}

	private <T> String deleteSeq(Class<T> clazz) {
		Table table = clazz.getAnnotation(Table.class);
		String seq = table.value().toUpperCase() + "_";
		for (Field field : clazz.getDeclaredFields()) {
			if (field.isAnnotationPresent(Key.class)) {
				seq += field.getName() + "_seq";
				break;
			}
		}
		String sql = "drop SEQUENCE " + seq;
		return sql;
	}

	@Override
	public String createTable(Class<?> clazz) {
		List<String> sql = new ArrayList<String>();
		Table tab = clazz.getAnnotation(Table.class);
		if (tab == null)
			throw new RuntimeException(
					"this class do not have annitation Table");
		StringBuffer sb = new StringBuffer();
		sb.append("create table ").append("FUSION").append(".")
				.append(tab.value().toUpperCase()).append("\n");
		sb.append("(");
		Field[] fiels = clazz.getDeclaredFields();
		for (int i = 0; i < fiels.length; i++) {
			Field field = fiels[i];
			sb.append(" ").append(field.getName()).append(" ");
			sb.append(getType(field));
			if (field.isAnnotationPresent(Key.class)) {
				createTrigger(sql, "FUSION", tab.value().toUpperCase(),
						field.getName());
				sb.append(" not null enable");
			}
			if (i != fiels.length - 1)
				sb.append(",\n");
		}
		sb.append(")");
		sql.add(sb.toString());
		for (String s : sql) {
			System.out.println(s);
		}
		return null;
	}

	private void createTrigger(List<String> sqls, String sm, String table,
			String id) {
		String seq = table + "_" + id + "_seq";
		String sql = "CREATE SEQUENCE   "
				+ seq
				+ "  MINVALUE 1 MAXVALUE 999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 2 NOORDER  NOCYCLE";
		sqls.add(sql);
		String trigger = "create or replace trigger " + table + "_" + id + "\n"
				+ "before insert on " + sm + "." + table + "\n"
				+ "for each row \n begin \n" + "  if inserting then \n"
				+ "      if :NEW." + id + " is null then \n"
				+ "         select " + seq + ".nextval into :NEW." + id
				+ " from dual;\n" + "      end if; \n" + "   end if; \n"
				+ "end;";
		sqls.add(trigger);
	}

	private static String getType(Field field) {
		if (field.isAnnotationPresent(Column.class)) {
			return field.getAnnotation(Column.class).type();

		}
		Class<?> type = field.getType();
		if (Integer.class.equals(type) || int.class.equals(type)) {
			return "number";
		}
		if (String.class.equals(type)) {
			return "varchar2(500)";
		}
		if (Date.class.equals(type)) {
			return "date";
		}
		return null;
	}

	@Override
	public String top(Integer start, Integer offset) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String count(String sql) {
		// TODO Auto-generated method stub
		return null;
	}

}
