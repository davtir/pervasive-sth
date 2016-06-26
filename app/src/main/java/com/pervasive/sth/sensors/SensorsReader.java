package com.pervasive.sth.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Created by davtir on 16/05/16.
 */
public class SensorsReader implements SensorEventListener {
    Context _cnt;
    SensorManager _manager;
    Sensor _photoresistor;
    Sensor _accelerometer;
    Sensor _gyroscope;
    Sensor _thermometer;
    public static double LUX_DARK_THRESHOLD = 0.0;
    public static double LUX_TWILIGHT_THRESHOLD = 5;
    public static double LUX_DAYLIGHT_THRESHOLD = 1000;
    public static double LUX_JOURNEY_ON_THE_SUN_THRESHOLD = 5000;

    /*
     *  Ambient light level in SI lux units
     */
    float _luminosity;

    /*
     * Tempretature in degree Celsius
     */
    float _temperature;

    /*
     *   _acceleration[0]: Acceleration along x-axis (m/s^2) without considering force of gravity
     *   _acceleration[1]: Acceleration along y-axis (m/s^2) without considering force of gravity
     *   _acceleration[2]: Acceleration along z-axis (m/s^2) without considering force of gravity
     */
    float[] _acceleration;

    float[] _cumulativeAcc;

    long _counter;

    /*
     *   _rotation[0]: Angular speed around the x-axis (Rad/sec)
     *   _rotation[1]: Angular speed around the y-axis (Rad/sec)
     *   _rotation[2]: Angular speed around the z-axis (Rad/sec)
     */
    float[] _rotation;

    public SensorsReader(Context cnt) {
        _cnt = cnt;
        _manager = (SensorManager)cnt.getSystemService(Context.SENSOR_SERVICE);

        _photoresistor = _manager.getDefaultSensor(Sensor.TYPE_LIGHT);
        _accelerometer = _manager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        _gyroscope = _manager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        _thermometer = _manager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);

        _cumulativeAcc = new float[3];
        _cumulativeAcc[0] = 0.0f;
        _cumulativeAcc[1] = 0.0f;
        _cumulativeAcc[2] = 0.0f;
        _counter = 0;

        registerSensorsListeners();
    }

    public float getLuminosity() {
        if ( _photoresistor == null )
            throw new RuntimeException("Photoresistor is not available.");
        return _luminosity;
    }

    public float getTemperature() {
        if ( _thermometer == null )
            throw new RuntimeException("Thermometer is not available.");
        return _temperature;
    }

    public float[] getAcceleration() {
        if ( _accelerometer == null )
            throw new RuntimeException("Accelerometer is not available.");

        return _acceleration;
    }

    public float getAverageResultantAcceleration() {
        if ( _counter == 0 )
            return 0.0f;

        float resultant = 0.0f;
        resultant = (float) Math.sqrt(Math.pow(_cumulativeAcc[0], 2) + Math.pow(_cumulativeAcc[1], 2));
        resultant = ((float) Math.sqrt(Math.pow(resultant, 2) + Math.pow(_cumulativeAcc[2], 2))) / (float)_counter;

        _cumulativeAcc[0] = 0.0f;
        _cumulativeAcc[1] = 0.0f;
        _cumulativeAcc[2] = 0.0f;
        _counter = 0;

        return resultant;
    }

    public float[] getRotation() {
        if ( _gyroscope == null )
            throw new RuntimeException("Gyroscope is not available.");
        return _rotation;
    }

    public boolean isPhotoresistorAvailable() {
        return (_manager.getDefaultSensor(Sensor.TYPE_LIGHT) != null);
    }

    public boolean isThermometerAvailable() {
        return (_manager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE) != null);
    }

    public boolean isAccelerometerAvailable() {
        return (_manager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null);
    }

    public boolean isGyroscopeAvailable() {
        return (_manager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null);
    }

    public void registerSensorsListeners() {
        if ( _photoresistor != null ) {
            _manager.registerListener(this, _photoresistor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        if ( _accelerometer != null ) {
            _manager.registerListener(this, _accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }

        if ( _gyroscope != null ) {
            _manager.registerListener(this, _gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        }

        if ( _thermometer != null ) {
            _manager.registerListener(this, _thermometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    public void onAccuracyChanged (Sensor sensor, int accuracy) {
    }

    public void onSensorChanged (SensorEvent event) {
        switch ( event.sensor.getType() ) {
            case Sensor.TYPE_LIGHT:
                _luminosity = event.values[0];
                break;
            case Sensor.TYPE_LINEAR_ACCELERATION:
                _acceleration = event.values;
                if ( Math.abs(_acceleration[0]) >= 0.5f || Math.abs( _acceleration[1]) >= 0.5f ||  Math.abs(_acceleration[2]) >= 0.5f ) {
                    _cumulativeAcc[0] +=  Math.abs(_acceleration[0]);
                    _cumulativeAcc[1] +=  Math.abs(_acceleration[1]);
                    _cumulativeAcc[2] +=  Math.abs(_acceleration[2]);
                    ++_counter;
                }
                break;
            case Sensor.TYPE_GYROSCOPE:
                _rotation = event.values;
                break;
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                _temperature = event.values[0];
                break;
            default:
                break;
        }
    }

    public static double getLuxThreshold(double lux) {
        if(lux >= LUX_JOURNEY_ON_THE_SUN_THRESHOLD)
            return LUX_JOURNEY_ON_THE_SUN_THRESHOLD;
        if(lux >= LUX_DAYLIGHT_THRESHOLD)
            return LUX_DAYLIGHT_THRESHOLD;
        if(lux >= LUX_TWILIGHT_THRESHOLD)
            return LUX_TWILIGHT_THRESHOLD;
        if(lux >= LUX_DARK_THRESHOLD)
           return LUX_DARK_THRESHOLD;

        return -Double.MAX_VALUE;
    }
}
