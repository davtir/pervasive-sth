package com.pervasive.sth.exceptions;

/**
 * @brief	This class represents a critical exception generated
 * 			by the REST client module
 */
public class InvalidRESTClientParametersException extends Exception {

	public InvalidRESTClientParametersException() {
		super();
	}

	public InvalidRESTClientParametersException(String msg) {
		super(msg);
	}

	public InvalidRESTClientParametersException(String msg, Throwable throwable) {
		super(msg, throwable);
	}

	public InvalidRESTClientParametersException(Throwable throwable) {
		super(throwable);
	}
}

