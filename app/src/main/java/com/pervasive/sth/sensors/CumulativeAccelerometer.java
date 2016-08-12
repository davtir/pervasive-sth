package com.pervasive.sth.sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.util.Log;

import com.pervasive.sth.exceptions.DeviceSensorCriticalException;
import com.pervasive.sth.exceptions.DeviceSensorException;

/**
 * Created by davtir on 12/08/16.
 */
public class CumulativeAccelerometer extends Accelerometer {

	long _validSamples;

	float _outlierThreshold;

	float[] _cumulativeAcceleration;

	public CumulativeAccelerometer(Sensor accelerometer, float outlierThreshold) throws DeviceSensorCriticalException, DeviceSensorException {
		super(accelerometer);
		_cumulativeAcceleration = new float[3];
		_validSamples = 0;
		_outlierThreshold = outlierThreshold;
	}

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
