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

    public float[] getRotation() {
        if ( _gyroscope == null )
            throw new RuntimeException("Gyroscope is not available.");
        return _rotation;
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
}
