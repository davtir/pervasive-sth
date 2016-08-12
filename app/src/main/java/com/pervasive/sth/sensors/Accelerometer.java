package com.pervasive.sth.sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.pervasive.sth.exceptions.DeviceSensorCriticalException;
import com.pervasive.sth.exceptions.DeviceSensorException;

/**
 * @brief This class implements the device accelerometer facilities
 */
public class Accelerometer implements SensorEventListener {

	/*
	 * The handler to the accelerometer sensor
	 */
	protected final Sensor _accelerometer;

	/*
	 * _acceleration[0]: Acceleration along x-axis (m/s^2) without considering force of gravity
	 * _acceleration[1]: Acceleration along y-axis (m/s^2) without considering force of gravity
	 * _acceleration[2]: Acceleration along z-axis (m/s^2) without considering force of gravity
	 */
	protected float[] _accelerometerValues;

	/*
	 * True if it is listening for sensor changes
	 */
	protected boolean _isListening;

	/**
	 * @brief Initialize sensor manager and sensor handler
	 */
	public Accelerometer(Sensor accelerometer) throws DeviceSensorCriticalException, DeviceSensorException {

		if ( accelerometer == null ) {
			throw new DeviceSensorException("Accelerometer is not supported by the device");
		}

		if (  accelerometer.getType() != Sensor.TYPE_LINEAR_ACCELERATION ) {
			throw new DeviceSensorCriticalException("Invalid sensor type in input (type = " + accelerometer.getType() + ")");
		}

		_accelerometer = accelerometer;
		_accelerometerValues = new float[3];
		_isListening = false;

	}

	/**
	 * @brief Returns the luminosity value percept by the sensor in SI lux units
	 */
	public float[] getAccelerometerValues() {
		return _accelerometerValues;
	}

	/**
	 * @brief Returns the android handler associated to the accelerometer sensor
	 */
	public Sensor getAccelerometerHandler() {
		return _accelerometer;
	}

	/**
	 * @brief Returns true if it is listening for sensor value changes
	 */
	public boolean isListening() {
		return _isListening;
	}

	/**
	 * @brief Start listening for sensor value changes
	 */
	public void startListening(SensorManager manager) throws DeviceSensorCriticalException {
		if ( manager == null ) {
			throw new DeviceSensorCriticalException("Invalid sensor manager in input");
		}

		if ( !_isListening ) {
			manager.registerListener(this, _accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
			_isListening = true;
		}
	}

	/**
	 * Stops listening for sensor value changes
	 */
	public void stopListening(SensorManager manager) throws DeviceSensorCriticalException {
		if ( manager != null && _isListening ) {
			manager.unregisterListener(this);
			_isListening = false;
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) { }

	@Override
	public void onSensorChanged(SensorEvent event) {
		if ( event.sensor.getType() == _accelerometer.getType() ) {
			_accelerometerValues = event.values;
		}
	}
}
