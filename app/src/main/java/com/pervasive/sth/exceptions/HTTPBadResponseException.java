package com.pervasive.sth.exceptions;

/**
 * Created by davtir on 12/08/16.
 */
public class HTTPBadResponseException extends Exception {

	public HTTPBadResponseException() {
		super();
	}

	public HTTPBadResponseException(String msg) {
		super(msg);
	}

	public HTTPBadResponseException(String msg, Throwable throwable) {
		super(msg, throwable);
	}

	public HTTPBadResponseException(Throwable throwable) {
		super(throwable);
	}
}
