package com.pervasive.sth.tasks;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.pervasive.sth.distances.BluetoothTracker;
import com.pervasive.sth.distances.GPSTracker;
import com.pervasive.sth.entities.Device;
import com.pervasive.sth.entities.Media;
import com.pervasive.sth.entities.Suggestion;
import com.pervasive.sth.entities.SuggestionsGenerator;
import com.pervasive.sth.entities.TreasureStatus;
import com.pervasive.sth.rest.WSInterface;
import com.pervasive.sth.sensors.SensorsReader;
import com.pervasive.sth.smarttreasurehunt.HunterActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Created by davtir on 15/05/16.
 */
public class HunterTask extends AsyncTask<Void, Void, Void> {

    private final String pathName = Environment.getExternalStorageDirectory().getAbsolutePath()+"/STH";
    Context _context;
    GPSTracker _gps;
    BluetoothTracker _bluetooth;
    WSInterface _webserver;
    SensorsReader _sr;
    Device _hunter;
    String _treasureID;
    TreasureStatus _treasureStatus;
    double distance;

    SuggestionsGenerator _suggestionGenerator;

    public HunterTask(Context context, GPSTracker gps, BluetoothTracker ble, Device hunter) {
        _context = context;
        _gps = gps;
        _bluetooth = ble;
        _treasureID = "";
        _sr = new SensorsReader(context);
        _hunter = hunter;
        _webserver = new WSInterface();
        _suggestionGenerator = new SuggestionsGenerator(_context, _hunter);
    }

    public String getTreasureID() {
        return _treasureID;
    }

    @Override
    protected Void doInBackground(Void... params) {
        Log.d("HunterTask", "Started");

        // End when a cancel request is received
        while ( !isCancelled() ) {
            try {
                _treasureStatus = _webserver.retrieveTreasureStatus();
                if ( _treasureStatus.isFound() ) {
                    Log.i("HunterTask", "The treasure have been found!");
                    break;
                }
            } catch ( Exception e ) {
                Log.e("HunterTask", e.getMessage());
                continue;
            }

            // Get treasure string from WS
            Device treasure;
            try {
                treasure = _webserver.retrieveDevice();
                _treasureID = treasure.getMACAddress();
            } catch ( Exception e ) {
                // Error while executing get on WS
                Log.e("HunterTask", e.getMessage());
                continue;
            }

            //Log.d(this.getClass().getName(), "*********The random suggestion returned is: " + _suggestionGenerator.createRandomSuggestionType());

            Suggestion suggestion = _suggestionGenerator.createRandomSuggestion(treasure);

            Intent intent = new Intent(HunterActivity.SUGGESTION_ACTION);
            intent.putExtra("SUGGESTION", suggestion);
            _context.sendBroadcast(intent);

           // analizeSensors(treasure);
            notifyGpsDistance(treasure);
            startBluetoothDiscovery(5000);
        }

        try {
            _webserver.deleteDevice(_hunter.getMACAddress());
        } catch ( Exception e ) {
            e.printStackTrace();
        }

        Media picture = _treasureStatus.getWinner();

        File f = new File(pathName);
        if (!f.exists())
            f.mkdir();
        FileOutputStream fo = null;
        try {
            fo = new FileOutputStream(picture.get_mediaName());
            fo.write(picture.get_data());
        } catch (Exception e) {
            e.printStackTrace();
        }


        Intent winnerIntent = new Intent(HunterActivity.WINNER_ACTION);
        winnerIntent.putExtra("WINNER_UPDATE", picture.get_mediaName());
        _context.sendBroadcast(winnerIntent);

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(HunterActivity.FOUND_ACTION);
        intent.putExtra("TREASURE_FOUND", true);
        _context.sendBroadcast(intent);

        return null;
    }

    private void analizeSensors(Device treasure) {
        setDeviceSensors();
        if ( _sr.isPhotoresistorAvailable() ) {
            analizeLuxValues(treasure);
        }

        if ( _sr.isThermometerAvailable() ) {
            analizeTemperatureValues(treasure);
        }
    }

    public void setDeviceSensors() {

        float[] acc;
        float[] rot;

        if ( _sr.isPhotoresistorAvailable() ) {
            _hunter.setLuminosity(_sr.getLuminosity());
        } else _hunter.setLuminosity(-Float.MAX_VALUE);

        if ( _sr.isThermometerAvailable() ) {
            _hunter.setTemperature(_sr.getTemperature());
        } else _hunter.setTemperature(-Float.MAX_VALUE);

        acc = _sr.getAcceleration();
        if ( _sr.isAccelerometerAvailable() && acc != null ) {
            _hunter.setAcceleration(_sr.getAcceleration());
        } else {
            float[] a = {-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE};
            _hunter.setAcceleration(a);
        }

        rot = _sr.getRotation();
        if ( _sr.isGyroscopeAvailable() && rot != null ) {
            _hunter.setRotation(_sr.getRotation());
        } else {
            float[] r = {-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE};
            _hunter.setRotation(r);
        }
    }

    public void analizeLuxValues(Device treasure) {
        double t_threshold;
        double t_lux = treasure.getLuminosity();
        if ( t_lux >= (t_threshold = SensorsReader.LUX_JOURNEY_ON_THE_SUN_THRESHOLD) )
            Log.d(this.getClass().getName(), "Pija lo shuttle");
        else if ( t_lux >= (t_threshold = SensorsReader.LUX_DAYLIGHT_THRESHOLD) )
            Log.d(this.getClass().getName(), "Ben illuminato");
        else if ( t_lux >= (t_threshold = SensorsReader.LUX_TWILIGHT_THRESHOLD) )
            Log.d(this.getClass().getName(), "Non molto illuminato");
        else if ( t_lux >= (t_threshold = SensorsReader.LUX_DARK_THRESHOLD) )
            Log.d(this.getClass().getName(), "Scuro zi");

        double h_lux = _hunter.getLuminosity();
        double h_threshold = SensorsReader.getLuxThreshold(h_lux);
        if ( h_threshold < t_threshold )
            Log.d(this.getClass().getName(), "Treasure più illuminato di Hunter");
        else if ( h_threshold > t_threshold )
            Log.d(this.getClass().getName(), "Hunter più illuminato di Treasure");
        else Log.d(this.getClass().getName(), "Hunter stessa fascia di Treasure");

    }

    public void analizeTemperatureValues(Device treasure) {
        double t_temp = treasure.getTemperature();
        double h_temp = _hunter.getTemperature();
        double deltaT = t_temp - h_temp;

        if(deltaT < 0)
            Log.d(this.getClass().getName(), "Treasure temperature is "+Math.abs(deltaT)+" degrees lower than you");
        else if(deltaT > 0)
                Log.d(this.getClass().getName(), "Treasure temperature is "+Math.abs(deltaT)+" degrees higher than you");
            else
                Log.d(this.getClass().getName(), "Treasure temperature is equal to yours");
    }

    private void notifyGpsDistance(Device treasure) {

        // Get treasure coordinates
        double t_lat = treasure.getLatitude();
        double t_lon = treasure.getLongitude();

        // Get hunter coordinates
        _hunter.setLatitude(_gps.getLatitude());
        _hunter.setLongitude(_gps.getLongitude());

        // Compute gps distance
        distance = _gps.gpsDistance(t_lat, t_lon);
        Log.d("HunterTask", "Distance from treasure: " + distance + " m");

        // Notify computed distance to HunterActivity
        Intent intent = new Intent(HunterActivity.GPS_ACTION);
        intent.putExtra("GPS_DISTANCE", distance);
        _context.sendBroadcast(intent);
    }

    private void startBluetoothDiscovery(long sleeptime_ms) {
        // Start bluetooth discovery
        _bluetooth.discover();
        try {
            Thread.sleep(sleeptime_ms);
        } catch ( InterruptedException e ) {
            e.printStackTrace();
        }
    }
}

