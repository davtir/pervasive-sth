package com.pervasive.sth.distances;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.pervasive.sth.entities.Device;
import com.pervasive.sth.rest.RESTClient;
import com.pervasive.sth.rest.WSInterface;

/**
 * Created by davtir on 15/05/16.
 */
public class TreasureTask extends AsyncTask<Void, Void, Void> {

    Context _context;
    GPSTracker _gps;
    WSInterface _webserver;
    Device _treasure;
    public static boolean _found = false;

    public TreasureTask(Context context, GPSTracker gps, Device dev) throws Exception {
        _context = context;
        _gps = gps;
        _webserver = new WSInterface();
        _treasure = new Device(BluetoothAdapter.getDefaultAdapter().getAddress(), BluetoothAdapter.getDefaultAdapter().getName(), "T");
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

            // Get lat and lon coordinates
            _treasure.setLatitude(_gps.getLatitude());
            _treasure.setLongitude(_gps.getLongitude());

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

    public static void setFound(boolean value) {
        _found = value;
    }
}
