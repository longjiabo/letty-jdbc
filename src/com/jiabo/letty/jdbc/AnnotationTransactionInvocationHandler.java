package com.jiabo.letty.jdbc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import com.jiabo.letty.jdbc.annotation.Transactional;

public class AnnotationTransactionInvocationHandler implements
		InvocationHandler {
	private TransactionManager transactionManager;

	public AnnotationTransactionInvocationHandler(Object object,
			TransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] objects)
			throws Throwable {
		Method originalMethod = proxy.getClass().getMethod(method.getName(),
				method.getParameterTypes());
		if (!originalMethod.isAnnotationPresent(Transactional.class)) {
			return method.invoke(proxy, objects);
		}

		transactionManager.start();
		Object result = null;
		try {
			result = method.invoke(proxy, objects);
			transactionManager.commit();
		} catch (Exception e) {
			transactionManager.rollback();
		} finally {
			transactionManager.close();
		}
		return result;
	}
}