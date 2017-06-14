package com.jiabo.letty.jdbc.exception;

public class LettyJDBCException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3794975553493616746L;

	public LettyJDBCException(String msg, Throwable e) {
		super(msg, e);
	}

	public LettyJDBCException(String msg) {
		super(msg);
	}
}
