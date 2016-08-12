package com.pervasive.sth.sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.pervasive.sth.exceptions.DeviceSensorCriticalException;
import com.pervasive.sth.exceptions.DeviceSensorException;

/**
 * @brief	This class handle the device photoresistor sensor
 */
public class Photoresistor implements SensorEventListener  {

	/*
	 * The handler to the photoresistor sensor
	 */
	private final Sensor _photoresistor;

	/*
 	 *  Ambient light level in SI lux units
 	 */
	private float _luminosityValue;

	public boolean _isListening;

	/*
	 *	Luminosity thresholds
	 */
	public static final double LUX_DARK_THRESHOLD = 0.0;
	public static final double LUX_TWILIGHT_THRESHOLD = 5;
	public static final double LUX_DAYLIGHT_THRESHOLD = 1000;
	public static final double LUX_JOURNEY_ON_THE_SUN_THRESHOLD = 5000;

	/**
	 * @brief Initialize sensor manager and sensor handler
	 */
	public Photoresistor(Sensor photoresistor) throws DeviceSensorCriticalException, DeviceSensorException {
		//
		if ( photoresistor == null ) {
			throw new DeviceSensorException("Photoresistor is not supported by the device");
		}

		if (  photoresistor.getType() != Sensor.TYPE_LIGHT ) {
			throw new DeviceSensorCriticalException("Invalid sensor type in input (type = " + photoresistor.getType() + ")");
		}

		_photoresistor = photoresistor;
		_isListening = false;
	}

	/**
	 * @brief Returns the luminosity value percepted by the sensor in SI lux units
	 */
	public float getLuminosityValue() {
		return _luminosityValue;
	}

	/**
	 * @brief Returns the android handler associated to the photoresistor sensor
	 */
	public Sensor getPhotoresistorHandler() {
		return _photoresistor;
	}

	public void startListening(SensorManager manager) throws DeviceSensorCriticalException {
		if ( manager == null ) {
			throw new DeviceSensorCriticalException("Invalid sensor manager in input");
		}

		if ( !_isListening ) {
			manager.registerListener(this, _photoresistor, SensorManager.SENSOR_DELAY_NORMAL);
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
		if ( event.sensor.getType() == _photoresistor.getType() ) {
			_luminosityValue = event.values[0];
		}
	}

	/**
	 * @brief This function returns the belonging threshold associated to the luminosity value in input
	 */
	public static double getLuxThreshold(double lux) {
		if (lux >= LUX_JOURNEY_ON_THE_SUN_THRESHOLD) {
			return LUX_JOURNEY_ON_THE_SUN_THRESHOLD;
		} else if (lux >= LUX_DAYLIGHT_THRESHOLD) {
			return LUX_DAYLIGHT_THRESHOLD;
		} else if (lux >= LUX_TWILIGHT_THRESHOLD) {
			return LUX_TWILIGHT_THRESHOLD;
		} else return LUX_DARK_THRESHOLD;
	}
}
