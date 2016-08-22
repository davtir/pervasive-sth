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
 * @brief Hunter Task that deals with distance computations
 */
public class HunterDistanceTask extends AsyncTask<Void, Void, Void> {

	private Context _context;

	// GPS handler
	private GPSTracker _gps;

	//Bluetooth handler
	private BluetoothTracker _bluetooth;

	// Web server interface
	private WSInterface _webserver;

	// Hunter device
	private Device _hunter;

	// Treasure device
	private String _treasureID;

	// Actual status of the treasure
	private TreasureStatus _treasureStatus;

	private double distance;

	private final String LOG_TAG = HunterDistanceTask.class.getName();

	/**
	 * @brief Initialize the object
	 */
	public HunterDistanceTask(Context context, GPSTracker gps, BluetoothTracker ble, Device hunter) throws InvalidRESTClientParametersException {
		_context = context;
		_gps = gps;
		_bluetooth = ble;
		_treasureID = "";
		_hunter = hunter;
		_webserver = new WSInterface();
	}

	@Override
	protected Void doInBackground(Void... params) {
		Log.d(LOG_TAG, "HunterDistanceTask started");

		// End when a cancel request is received
		while (!isCancelled()) {
			// Get the treasure status from the web server
			try {
				_treasureStatus = _webserver.retrieveTreasureStatus();
				if (_treasureStatus.isFound()) {
					Log.i(LOG_TAG, "The treasure have been found!");
					break;
				}
			} catch (Exception e) {
				Log.w(LOG_TAG, e.getMessage());
				continue;
			}

			// Get treasure from WS
			Device treasure;
			try {
				treasure = _webserver.retrieveDevice();
				_treasureID = treasure.getBtAddress();
			} catch (Exception e) {
				// Error while executing get on WS
				Log.e(LOG_TAG, e.getMessage());
				continue;
			}

			notifyGpsDistance(treasure);
			startBluetoothDiscovery(10000);
		}


/*		try {
			_webserver.deleteDevice(_hunter.getBtAddress());
		} catch (Exception e) {
			e.printStackTrace();
		}*/

		return null;
	}

	/**
	 * @brief Sends an intent to the hunter activity in order to notify an update of the GPS distance
	 */
	private void notifyGpsDistance(Device treasure) {

		// Get treasure coordinates
		double t_lat = treasure.getLatitude();
		double t_lon = treasure.getLongitude();

		// Get hunter coordinates
		_hunter.setLatitude(_gps.getLatitude());
		_hunter.setLongitude(_gps.getLongitude());

		// Compute gps distance
		distance = _gps.gpsDistance(t_lat, t_lon);
		Log.d(LOG_TAG, "Distance from treasure: " + distance + " m");

		// Notify computed distance to HunterActivity
		Intent intent = new Intent(HunterActivity.GPS_ACTION);
		intent.putExtra("GPS_DISTANCE", distance);
		_context.sendBroadcast(intent);
	}

	/**
	 * @brief Starts the discovery procedure of the bluetooth
	 */
	private void startBluetoothDiscovery(long sleeptime_ms) {
		// Start bluetooth discovery
		_bluetooth.discover();
		try {
			Thread.sleep(sleeptime_ms);
		} catch (InterruptedException e) {
			Log.w(LOG_TAG, e.getMessage());
		}
	}
}
