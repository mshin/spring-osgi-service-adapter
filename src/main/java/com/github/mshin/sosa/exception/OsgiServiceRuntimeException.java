package com.github.mshin.sosa.exception;

/**
 * 
 * @author MunChul Shin
 *
 */
public class OsgiServiceRuntimeException extends RuntimeException {

	private static final long serialVersionUID = -4180412314143494596L;

	public OsgiServiceRuntimeException(Throwable e) {
		super(e);
	}

	public OsgiServiceRuntimeException(String s) {
		super(s);
	}

	public OsgiServiceRuntimeException(String s, Throwable t) {
		super(s, t);
	}
}
