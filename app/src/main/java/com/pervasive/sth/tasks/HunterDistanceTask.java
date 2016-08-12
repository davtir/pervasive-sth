package com.pervasive.sth.tasks;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.pervasive.sth.distances.BluetoothTracker;
import com.pervasive.sth.distances.GPSTracker;
import com.pervasive.sth.entities.Device;
import com.pervasive.sth.entities.TreasureStatus;
import com.pervasive.sth.exceptions.InvalidRESTClientParametersException;
import com.pervasive.sth.network.WSInterface;
import com.pervasive.sth.smarttreasurehunt.HunterActivity;

/**
 * Created by Alex on 29/05/2016.
 */
public class HunterDistanceTask extends AsyncTask<Void, Void, Void> {

	Context _context;
	GPSTracker _gps;
	BluetoothTracker _bluetooth;
	WSInterface _webserver;
	Device _hunter;
	String _treasureID;
	TreasureStatus _treasureStatus;
	double distance;


	public HunterDistanceTask(Context context, GPSTracker gps, BluetoothTracker ble, Device hunter) throws InvalidRESTClientParametersException {
		_context = context;
		_gps = gps;
		_bluetooth = ble;
		_treasureID = "";
		_hunter = hunter;
		_webserver = new WSInterface();
	}

	public String getTreasureID() {
		return _treasureID;
	}

	@Override
	protected Void doInBackground(Void... params) {
		Log.d("HunterDistanceTask", "Started");

		// End when a cancel request is received
		while (!isCancelled()) {
			try {
				_treasureStatus = _webserver.retrieveTreasureStatus();
				if (_treasureStatus.isFound()) {
					Log.i("HunterTask", "The treasure have been found!");
					break;
				}
			} catch (Exception e) {
				Log.e("HunterTask", e.getMessage());
				continue;
			}

			// Get treasure string from WS
			Device treasure;
			try {
				treasure = _webserver.retrieveDevice();
				_treasureID = treasure.getBtAddress();
			} catch (Exception e) {
				// Error while executing get on WS
				Log.e("HunterTask", e.getMessage());
				continue;
			}

			notifyGpsDistance(treasure);
			startBluetoothDiscovery(10000);
		}

		try {
			_webserver.deleteDevice(_hunter.getBtAddress());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
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
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
