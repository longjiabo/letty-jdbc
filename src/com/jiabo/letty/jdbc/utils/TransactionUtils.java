package com.jiabo.letty.jdbc.utils;

import com.jiabo.letty.jdbc.DataSource;
import com.jiabo.letty.jdbc.TransactionEnabledProxyManager;
import com.jiabo.letty.jdbc.TransactionManager;

/**
 * 事务包装
 * 
 * @author jialong
 * 
 */
public class TransactionUtils {

	/**
	 * 事务对象封装，封装之后的对象使用的transactional注解的方法可以支持事务
	 * 
	 * @param object
	 * @return
	 */
	public static <T> T transActionWapper(T object) {
		TransactionEnabledProxyManager tm = new TransactionEnabledProxyManager(
				new TransactionManager());
		return tm.proxyFor(object);
	}

	public static <T> T transActionWapper(T t, DataSource dataSource) {
		TransactionEnabledProxyManager tm = new TransactionEnabledProxyManager(
				new TransactionManager(dataSource));
		return tm.proxyFor(t);
	}
	
}
