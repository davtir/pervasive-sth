package com.pervasive.sth.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.util.Log;

import com.pervasive.sth.exceptions.DeviceSensorCriticalException;
import com.pervasive.sth.exceptions.DeviceSensorException;

/**
 * @brief This class implements the support for handling device sensors
 */
public class SensorsHandler {
	/*
	 * The activity context
	 */
	private Context _cnt;

	/*
	 * The android sensor manager
	 */
	private SensorManager _manager;

	/*
	 * The photoresistor sensor
	 */
	private Photoresistor _photoresistor;

	/*
	 * The thermometer sensor
	 */
	private Thermometer _thermometer;

	/*
	 * The cumulative accelerometer sensor
	 */
	private CumulativeAccelerometer _accelerometer;

	private static final String LOG_TAG = SensorsHandler.class.getName();

	/**
	 * @brief Initialize the SensorHandler instance
	 */
	public SensorsHandler(Context cnt) throws DeviceSensorCriticalException {
		_cnt = cnt;
		_manager = (SensorManager) cnt.getSystemService(Context.SENSOR_SERVICE);

		try {
			_photoresistor = new Photoresistor(_manager.getDefaultSensor(Sensor.TYPE_LIGHT));
		} catch ( DeviceSensorException e) {
			Log.w(LOG_TAG, e.getMessage());
		}

		try {
			_thermometer = new Thermometer(_manager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE));
		} catch ( DeviceSensorException e) {
			Log.w(LOG_TAG, e.getMessage());
		}

		try {
			_accelerometer = new CumulativeAccelerometer(_manager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), 0.5f);
		} catch ( DeviceSensorException e) {
			Log.w(LOG_TAG, e.getMessage());
		}
	}

	/**
	 * @brief	Starts all supported sensors listeners
	 */
	public void startAllSupportedSensorsListeners()throws DeviceSensorCriticalException, DeviceSensorException {
		startSensorListener(Sensor.TYPE_LIGHT);
		startSensorListener(Sensor.TYPE_LINEAR_ACCELERATION);
		startSensorListener(Sensor.TYPE_AMBIENT_TEMPERATURE);
	}

	/**
	 * @brief	Stops all supported sensors listeners
	 */
	public void stopAllSupportedSensorsListeners()throws DeviceSensorCriticalException, DeviceSensorException {
		stopSensorListener(Sensor.TYPE_LIGHT);
		stopSensorListener(Sensor.TYPE_LINEAR_ACCELERATION);
		stopSensorListener(Sensor.TYPE_AMBIENT_TEMPERATURE);
	}

	/**
	 * @brief Starts the listener of the given sensor type
	 */
	public void startSensorListener(int sensorType) throws DeviceSensorCriticalException, DeviceSensorException {
		switch ( sensorType ) {
			case Sensor.TYPE_LIGHT:
				if ( isPhotoresistorAvailable() ) {
					_photoresistor.startListening(_manager);
				}
			case Sensor.TYPE_LINEAR_ACCELERATION:
				if ( isAccelerometerAvailable() ) {
					_accelerometer.startListening(_manager);
				}
				break;
			case Sensor.TYPE_AMBIENT_TEMPERATURE:
				if ( isThermometerAvailable() ) {
					_thermometer.startListening(_manager);
				}
				break;
			default:
				break;
		}
	}

	/**
	 * @brief Stops the listener of the given sensor type
	 */
	public void stopSensorListener(int sensorType) throws DeviceSensorCriticalException {
		switch ( sensorType ) {
			case Sensor.TYPE_LIGHT:
				if ( isPhotoresistorAvailable() ) {
					_photoresistor.stopListening(_manager);
				}
			case Sensor.TYPE_ACCELEROMETER:
				if ( isAccelerometerAvailable() ) {
					_accelerometer.stopListening(_manager);
				}
				break;
			case Sensor.TYPE_AMBIENT_TEMPERATURE:
				if ( isThermometerAvailable() ) {
					_thermometer.stopListening(_manager);
				}
				break;
			default:
				break;
		}
	}

	/**
	 * @brief Returns the photoresistor object
	 */
	public Photoresistor getPhotoresistor() {
		return _photoresistor;
	}

	/**
	 * @brief Returns the cumulative accelerometer object
	 */
	public CumulativeAccelerometer getCumulativeAccelerometer() {
		return _accelerometer;
	}

	/**
	 * @brief Returns the thermometer object
	 */
	public Thermometer getThermometer() {
		return _thermometer;
	}

	/**
	 * @brief	Returns true if photoresistor is supported by the device, false otherwise
	 */
	public boolean isPhotoresistorAvailable() {
		return (_photoresistor != null);
	}

	/**
	 * @brief	Returns true if thermometer is supported by the device, false otherwise
	 */
	public boolean isThermometerAvailable() {
		return (_manager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE) != null);
	}

	/**
	 * @brief	Returns true if accelerometer is supported by the device, false otherwise
	 */
	public boolean isAccelerometerAvailable() {
		return (_manager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null);
	}
}
