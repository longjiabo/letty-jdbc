package com.jiabo.letty.jdbc;

import java.lang.reflect.Proxy;

public class TransactionEnabledProxyManager {
	private TransactionManager transactionManager;

	public TransactionEnabledProxyManager(TransactionManager transactionManager) {

		this.transactionManager = transactionManager;
	}

	@SuppressWarnings("unchecked")
	public <T> T proxyFor(T object) {
		return (T) Proxy.newProxyInstance(object.getClass().getClassLoader(),
				object.getClass().getInterfaces(),
				new AnnotationTransactionInvocationHandler(object,
						transactionManager));
	}
}

