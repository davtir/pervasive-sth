package com.pervasive.sth.distances;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.pervasive.sth.rest.RESTClient;

/**
 * Created by davtir on 15/05/16.
 */
public class TreasureTask extends AsyncTask<Void, Void, Void> {

    Context _context;
    GPSTracker _gps;
    String _bluetooth_mac;
    String _device_name;
    RESTClient _client;

    private static final String URL = "http://192.168.1.6:8084/WSPervasiveSTH/webresources/devices";

    public TreasureTask(Context context, GPSTracker gps) throws Exception {
        _context = context;
        _gps = gps;
        _bluetooth_mac = BluetoothAdapter.getDefaultAdapter().getAddress();
        _device_name = BluetoothAdapter.getDefaultAdapter().getName();

            _client = new RESTClient(URL);
            _client.addHeader("content-type", "text/plain");

        // Check if a treasure already exists
        String t_entry = "";
        try {
            t_entry = _client.executeGet();
        } catch ( Exception e) {
            Log.e("TreasureTask", "Server is unreachable." );
        }
        String[] parsed = t_entry.split(";");
        Log.d("TreasureTask", parsed[0]);

        // If the treasure exists then throw an exception
        /*if ( parsed != null && !parsed[0].equals("No dev found")) {
            Log.d("Treasure", parsed[0]);
            throw new RuntimeException("A treasure already exists.");
        }*/
    }

    @Override
    protected Void doInBackground(Void... params) {
        double lat, lon;
        while ( !isCancelled() ) {

            // Get lat and lon coordinates
            lat = _gps.getLatitude();
            lon = _gps.getLongitude();

            // Create string for WS
            String rest_msg = "T;" + _bluetooth_mac + ";" + _device_name + ";" + lat + ";" + lon;

            Log.d("TreasureTask", "Updating treasure data...");
            // Post on WS
            try {
                _client.executePost(rest_msg);
            } catch (Exception e) {
                // Error while executing post on WS
                Log.e("TreasureTask", e.getMessage());
                continue;
            }

            Log.d("TreasureTask", "Sent message: " + rest_msg);

            // Sleep for 5 seconds
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
