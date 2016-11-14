package com.groupeseb.kite.exceptions;

/**
 * Exception use with fail on error is disbabled.
 * Instead of failed, each test thows a CheckFailException exception.
 */
public class CheckFailException extends Exception {

	private static final long serialVersionUID = -2679804620413010676L;

	public CheckFailException(String message) {
		super(message);
	}
}
