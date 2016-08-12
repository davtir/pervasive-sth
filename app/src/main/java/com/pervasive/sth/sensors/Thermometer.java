package com.pervasive.sth.sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.pervasive.sth.exceptions.DeviceSensorCriticalException;
import com.pervasive.sth.exceptions.DeviceSensorException;

/**
 * Created by davtir on 12/08/16.
 */
public class Thermometer implements SensorEventListener {

	/*
	 * The handler to the accelerometer sensor
	 */
	protected final Sensor _thermometer;

	/*
	 * Temperature in Celsius degree
	 */
	protected float _thermometerValue;

	protected boolean _isListening;

	/**
	 * @brief Initialize sensor manager and sensor handler
	 */
	public Thermometer(Sensor thermometer) throws DeviceSensorCriticalException, DeviceSensorException {
		//
		if ( thermometer == null ) {
			throw new DeviceSensorException("Thermometer is not supported by the device");
		}

		if (  thermometer.getType() != Sensor.TYPE_AMBIENT_TEMPERATURE ) {
			throw new DeviceSensorCriticalException("Invalid sensor type in input (type = " + thermometer.getType() + ")");
		}

		_thermometer = thermometer;
		_isListening = false;
	}

	/**
	 * @brief Returns the luminosity value percept by the sensor in SI lux units
	 */
	public float getThermometerValues() {
		return _thermometerValue;
	}

	/**
	 * @brief Returns the android handler associated to the accelerometer sensor
	 */
	public Sensor getAccelerometerHandler() {
		return _thermometer;
	}

	public boolean isListening() {
		return _isListening;
	}

	public void startListening(SensorManager manager) throws DeviceSensorCriticalException {
		if ( manager == null ) {
			throw new DeviceSensorCriticalException("Invalid sensor manager in input");
		}

		if ( !_isListening ) {
			manager.registerListener(this, _thermometer, SensorManager.SENSOR_DELAY_NORMAL);
			_isListening = true;
		}
	}

	public void stopListening(SensorManager manager) throws DeviceSensorCriticalException {
		if ( manager != null && _isListening ) {
			manager.unregisterListener(this);
			_isListening = false;
		}
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) { }

	public void onSensorChanged(SensorEvent event) {
		if ( event.sensor.getType() == _thermometer.getType() ) {
			_thermometerValue = event.values[0];
		}
	}
}