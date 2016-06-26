package com.pervasive.sth.tasks;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.pervasive.sth.distances.GPSTracker;
import com.pervasive.sth.entities.Device;
import com.pervasive.sth.rest.RESTClient;
import com.pervasive.sth.rest.WSInterface;
import com.pervasive.sth.sensors.SensorsReader;

/**
 * Created by davtir on 15/05/16.
 */
public class TreasureTask extends AsyncTask<Void, Void, Void> {

    Context _context;
    GPSTracker _gps;
    WSInterface _webserver;
    Device _treasure;
    public static boolean _found = false;
    SensorsReader _sr;

    public TreasureTask(Context context, GPSTracker gps, Device dev) throws Exception {
        _context = context;
        _gps = gps;
        _webserver = new WSInterface();
        _treasure = new Device(BluetoothAdapter.getDefaultAdapter().getAddress(), BluetoothAdapter.getDefaultAdapter().getName(), "T");
        _sr = new SensorsReader(context);
    }

    @Override
    protected Void doInBackground(Void... params) {
        // Check if a treasure already exists
        boolean treasure_exist = true;
        Device retrieved = null;
        try {
            retrieved = _webserver.retrieveDevice();
        } catch ( Exception e) {
            treasure_exist = false;
        }

        if ( treasure_exist && retrieved != null && !retrieved.getMACAddress().equals(_treasure.getMACAddress()))
            throw new RuntimeException("Treasure already exists.");

        _found = false;
        _treasure.setFound(_found);
        try {
            _webserver.updateTreasureStatus(_treasure.isFound());
        } catch (Exception e) {
            e.printStackTrace();
        }

        while ( !isCancelled() ) {

            Log.d(this.getClass().getName(),"ENTRATO 1");
            // Get lat and lon coordinates
            _treasure.setLatitude(_gps.getLatitude());
            _treasure.setLongitude(_gps.getLongitude());

            setDeviceSensors();

            //Log.d(this.getClass().getName(), "Mean acceleration value: " + _sr.getAverageResultantAcceleration());

            Log.d("TreasureTask", "Updating treasure data...");
            // Post on WS
            try {
                _webserver.updateDeviceEntry(_treasure);
            } catch (Exception e) {
                // Error while executing post on WS
                Log.e("TreasureTask", e.getMessage());
                continue;
            }

            // Sleep for 10 seconds
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Log.d("TreasureTask", "Task cancelled");
        _treasure.setFound(_found);
        Log.d("TreasureTask", "Status from activity: " + _found);
        try {
            Log.d("TreasureTask", "Found = " + _treasure.isFound());
            if ( _treasure.isFound() ) {
                Log.d("TreasureTask", "Updating...........");
                _webserver.updateTreasureStatus(_treasure.isFound());
            }

            _webserver.deleteDevice(_treasure.getMACAddress());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void setDeviceSensors () {
        float[] acc;
        float[] rot;

        if (_sr.isPhotoresistorAvailable())
            _treasure.setLuminosity(_sr.getLuminosity());
        else
            _treasure.setLuminosity(-Float.MAX_VALUE);

        if(_sr.isThermometerAvailable())
            _treasure.setTemperature(_sr.getTemperature());
        else
            _treasure.setTemperature(-Float.MAX_VALUE);

        acc = _sr.getAcceleration();
        if(_sr.isAccelerometerAvailable() && acc != null) {
            _treasure.setAcceleration(_sr.getAcceleration());
        }
        else {
            float[] a = {-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE};
            _treasure.setAcceleration(a);
        }

        rot = _sr.getRotation();
        if(_sr.isGyroscopeAvailable() && rot !=null)
            _treasure.setRotation(_sr.getRotation());
        else {
            float[] r = {-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE};
            _treasure.setRotation(r);
        }
    }

    public static void setFound(boolean value) {
        _found = value;
    }
}
