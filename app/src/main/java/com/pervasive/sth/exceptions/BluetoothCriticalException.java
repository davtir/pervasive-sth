package com.pervasive.sth.exceptions;

/**
 * @brief	This class represents a critical exception generated
 * 			by the bluetooth module
 */
public class BluetoothCriticalException extends Exception {

	public BluetoothCriticalException() {
		super();
	}

	public BluetoothCriticalException(String msg) {
		super(msg);
	}

	public BluetoothCriticalException(String msg, Throwable throwable) {
		super(msg, throwable);
	}

	public BluetoothCriticalException(Throwable throwable) {
		super(throwable);
	}
}
