package com.pervasive.sth.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.pervasive.sth.exceptions.DeviceSensorCriticalException;
import com.pervasive.sth.exceptions.DeviceSensorException;

/**
 * Created by davtir on 16/05/16.
 */
public class SensorsReader {
	Context _cnt;
	SensorManager _manager;
	Photoresistor _photoresistor;
	Thermometer _thermometer;
	CumulativeAccelerometer _accelerometer;

	private static final String LOG_TAG = SensorsReader.class.getName();

	public SensorsReader(Context cnt) throws DeviceSensorCriticalException {
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

	public Photoresistor getPhotoresistor() {
		return _photoresistor;
	}

	public CumulativeAccelerometer getCumulativeAccelerometer() {
		return _accelerometer;
	}

	public Thermometer getThermometer() {
		return _thermometer;
	}

	public boolean isPhotoresistorAvailable() {
		return (_photoresistor != null);
	}

	public boolean isThermometerAvailable() {
		return (_manager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE) != null);
	}

	public boolean isAccelerometerAvailable() {
		return (_manager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null);
	}
}
