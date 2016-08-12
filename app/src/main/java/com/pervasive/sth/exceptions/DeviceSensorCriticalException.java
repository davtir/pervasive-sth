package com.pervasive.sth.exceptions;

/**
 * Created by davtir on 12/08/16.
 */
public class DeviceSensorCriticalException extends Exception {

	public DeviceSensorCriticalException() {
		super();
	}

	public DeviceSensorCriticalException(String msg) {
		super(msg);
	}

	public DeviceSensorCriticalException(String msg, Throwable throwable) {
		super(msg, throwable);
	}

	public DeviceSensorCriticalException(Throwable throwable) {
		super(throwable);
	}
}
