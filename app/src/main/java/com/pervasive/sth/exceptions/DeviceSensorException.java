package com.pervasive.sth.exceptions;

/**
 * Created by davtir on 12/08/16.
 */
public class DeviceSensorException extends Exception {

	public DeviceSensorException() {
		super();
	}

	public DeviceSensorException(String msg) {
		super(msg);
	}

	public DeviceSensorException(String msg, Throwable throwable) {
		super(msg, throwable);
	}

	public DeviceSensorException(Throwable throwable) {
		super(throwable);
	}
}