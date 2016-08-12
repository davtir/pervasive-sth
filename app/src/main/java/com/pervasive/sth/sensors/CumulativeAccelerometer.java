package com.pervasive.sth.sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.util.Log;

import com.pervasive.sth.exceptions.DeviceSensorCriticalException;
import com.pervasive.sth.exceptions.DeviceSensorException;

/**
 * @brief This class implements the cumulative accelerometer facilities
 */
public class CumulativeAccelerometer extends Accelerometer {

	/*
	 * The number of samples that listened that are not considered as outlier
	 */
	private long _validSamples;

	/*
	 * The lower threshold for outlier samples identification
	 */
	private float _outlierThreshold;

	/*
	 * Cumulative values for the acceleration
	 */
	private float[] _cumulativeAcceleration;

	/**
	 * @brief Initialize the instance
	 */
	public CumulativeAccelerometer(Sensor accelerometer, float outlierThreshold) throws DeviceSensorCriticalException, DeviceSensorException {
		super(accelerometer);
		_cumulativeAcceleration = new float[3];
		_validSamples = 0;
		_outlierThreshold = outlierThreshold;
	}

	/**
	 * @brief Returns the cumulative values for the acceleration
	 */
	public float[] getCumulativeAcceleration() {
		return _cumulativeAcceleration;
	}

	/**
	 * @brief Returns the number of valid samples currently recorded
	 */
	public long getCurrentlyValidSamples() {
		return _validSamples;
	}

	/**
	 * @brief Sets the threshold to identify outlier samples
	 */
	public void setOutlierThreshold(float value) {
		_outlierThreshold = value;
	}

	/**
	 * @brief Returns the mean value for the acceleration
	 */
	public float[] getMeanAcceleration() {
		float[] res = {0.0f, 0.0f, 0.0f};

		if (_validSamples == 0)
			return res;

		res[0] = _cumulativeAcceleration[0] / _validSamples;
		res[1] = _cumulativeAcceleration[1] / _validSamples;
		res[2] = _cumulativeAcceleration[2] / _validSamples;

		_cumulativeAcceleration[0] = 0.0f;
		_cumulativeAcceleration[1] = 0.0f;
		_cumulativeAcceleration[2] = 0.0f;
		_validSamples = 0;

		return res;
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		super.onSensorChanged(event);
		if ( event.sensor.getType() == _accelerometer.getType() ) {
			if ( Math.abs(event.values[0]) >= _outlierThreshold && Math.abs(event.values[1]) >= _outlierThreshold && Math.abs(event.values[2]) >= _outlierThreshold ) {
				_cumulativeAcceleration[0] += Math.abs(event.values[0]);
				_cumulativeAcceleration[1] += Math.abs(event.values[1]);
				_cumulativeAcceleration[2] += Math.abs(event.values[2]);
				++_validSamples;
			}
		}
	}
}
