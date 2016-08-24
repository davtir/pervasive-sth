package com.pervasive.sth.tasks;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.os.AsyncTask;
import android.util.Log;

import com.pervasive.sth.distances.GPSTracker;
import com.pervasive.sth.entities.Device;
import com.pervasive.sth.exceptions.DeviceSensorException;
import com.pervasive.sth.network.WSInterface;
import com.pervasive.sth.sensors.SensorsHandler;
import com.pervasive.sth.smarttreasurehunt.TreasureActivity;

/**
 * @brief This class implments the asynchronous task for the treasure deevice
 */
public class TreasureTask extends AsyncTask<Void, Void, Void> {

	private final String LOG_TAG = TreasureTask.class.getName();

	private Context _context;

	/*
	 * GPS handler
	 */
	private GPSTracker _gps;

	/*
	 * Web server interface
	 */
	private WSInterface _webserver;

	/*
	 * Treasure device handler
	 */
	private Device _treasure;

	/*
	 * Indicates that the treasure have been found
	 */
	private static boolean _found = false;

	/*
	 * The winner photo
	 */
	private static Bitmap _winner = null;

	/*
	 * Device sensors handler
	 */
	private SensorsHandler _sr;

	private RuntimeException _throwException;

	/**
	 * @brief Initialize the object
	 */
	public TreasureTask(Context context, GPSTracker gps, Device dev) throws Exception {
		_context = context;
		_gps = gps;
		_webserver = new WSInterface();
		_treasure = new Device(BluetoothAdapter.getDefaultAdapter().getAddress(), BluetoothAdapter.getDefaultAdapter().getName(), "T");
		_sr = new SensorsHandler(context);
		try {
			_sr.startSensorListener(Sensor.TYPE_LIGHT);
		} catch ( DeviceSensorException e ) {
			Log.w(LOG_TAG, e.getMessage());
		}

		try {
			_sr.startSensorListener(Sensor.TYPE_LINEAR_ACCELERATION);
		} catch ( DeviceSensorException e ) {
			Log.w(LOG_TAG, e.getMessage());
		}

		try {
			_sr.startSensorListener(Sensor.TYPE_AMBIENT_TEMPERATURE);
		} catch ( DeviceSensorException e ) {
			Log.w(LOG_TAG, e.getMessage());
		}
	}

	@Override
	protected Void doInBackground(Void... params) {
		// Check if a treasure already exists
		boolean treasure_exist = true;
		Device retrieved = null;
		try {
			retrieved = _webserver.retrieveDevice();
		} catch ( Exception e ) {
			treasure_exist = false;
		}

		if ( treasure_exist && retrieved != null && !retrieved.getBtAddress().equals(_treasure.getBtAddress()) ) {
			Log.d(LOG_TAG, "The treasure already exists.");
			_throwException = new RuntimeException("The treasure already exists.");
			return null;
		}

		_found = false;
		_treasure.setFound(_found);

		try {
			_webserver.updateTreasureStatus(_treasure.isFound(), null);
		} catch (Exception e) {
			_throwException = new RuntimeException(e.getMessage());
			return null;
		}

		while (!isCancelled()) {
			// Get lat and lon coordinates
			_treasure.setLatitude(_gps.getLatitude());
			_treasure.setLongitude(_gps.getLongitude());

			try {
				setDeviceSensors();
			} catch (Exception e) {
				Log.e(LOG_TAG, e.toString());
				_throwException = new RuntimeException(e.getMessage());
				return null;
			}

			Log.d(LOG_TAG, "Updating treasure data...");
			// Post on WS
			try {
				_webserver.updateDeviceEntry(_treasure);
			} catch (Exception e) {
				Log.e(LOG_TAG, e.toString());
				_throwException = new RuntimeException(e.getMessage());
				return null;
			}

			// Sleep for 10 seconds
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				Log.w(LOG_TAG, e.getMessage());
			}
		}

		_treasure.setFound(_found);
		Log.d(LOG_TAG, "Treasure status: " + _found);

		return null;
	}

	/**
	 * @brief Fills the treasure handler fields with the available sensors values
	 */
	public void setDeviceSensors() throws Exception {

		if (_sr.isPhotoresistorAvailable())
			_treasure.setLuminosity(_sr.getPhotoresistor().getLuminosityValue());
		else
			_treasure.setLuminosity(-Float.MAX_VALUE);

		if (_sr.isThermometerAvailable())
			_treasure.setTemperature(_sr.getThermometer().getThermometerValues());
		else
			_treasure.setTemperature(-Float.MAX_VALUE);

		if (_sr.isAccelerometerAvailable()) {
			_treasure.setAcceleration(_sr.getCumulativeAccelerometer().getMeanAcceleration());
		} else {
			float[] a = {-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE};
			_treasure.setAcceleration(a);
		}
	}

	/**
	 * @brief Sets the found flag to the input value
	 */
	public static void setFound(boolean value) {
		_found = value;
	}

	/*
	 * Sets the winner image
	 */
	public static void setWinner(Bitmap bitmap) {
		_winner = bitmap;
	}

	@Override
	protected void onPostExecute(Void aVoid) {
		super.onPostExecute(aVoid);
		try {
			_webserver.deleteDevice(_treasure.getBtAddress());
		} catch ( Exception e ) {
		}

		Log.d(LOG_TAG, "onPostExecute() called");
		if ( _throwException != null ) {
			Intent intent = new Intent(TreasureActivity.EXCEPTION_THROWN);
			intent.putExtra(TreasureActivity.EXCEPTION_NAME, _throwException.getMessage());
			_context.sendBroadcast(intent);
		}
	}
}
