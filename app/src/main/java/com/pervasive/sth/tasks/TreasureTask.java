package com.pervasive.sth.tasks;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.os.AsyncTask;
import android.util.Log;

import com.pervasive.sth.distances.GPSTracker;
import com.pervasive.sth.entities.Device;
import com.pervasive.sth.exceptions.DeviceSensorException;
import com.pervasive.sth.network.WSInterface;
import com.pervasive.sth.sensors.SensorsReader;

/**
 * Created by davtir on 15/05/16.
 */
public class TreasureTask extends AsyncTask<Void, Void, Void> {

	private final String LOG_TAG = TreasureTask.class.getName();
	Context _context;
	GPSTracker _gps;
	WSInterface _webserver;
	Device _treasure;
	public static boolean _found = false;
	public static Bitmap _winner = null;
	SensorsReader _sr;

	public TreasureTask(Context context, GPSTracker gps, Device dev) throws Exception {
		_context = context;
		_gps = gps;
		_webserver = new WSInterface();
		_treasure = new Device(BluetoothAdapter.getDefaultAdapter().getAddress(), BluetoothAdapter.getDefaultAdapter().getName(), "T");
		_sr = new SensorsReader(context);
		try {
			_sr.startSensorListener(Sensor.TYPE_LIGHT);
			_sr.startSensorListener(Sensor.TYPE_LINEAR_ACCELERATION);
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
		} catch (Exception e) {
			treasure_exist = false;
		}

		if (treasure_exist && retrieved != null && !retrieved.getBtAddress().equals(_treasure.getBtAddress()))
			throw new RuntimeException("Treasure already exists.");

		_found = false;
		_treasure.setFound(_found);

		try {
			_webserver.updateTreasureStatus(_treasure.isFound(), null);
		} catch (Exception e) {
			e.printStackTrace();
		}

		while (!isCancelled()) {

			Log.d(this.getClass().getName(), "ENTRATO 1");
			// Get lat and lon coordinates
			_treasure.setLatitude(_gps.getLatitude());
			_treasure.setLongitude(_gps.getLongitude());

			try {
				setDeviceSensors();
			} catch (Exception e) {
				Log.e(LOG_TAG, e.toString());
				this.cancel(true); //DA SISTEMAREEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE
				break;
			}

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
/*            if ( _treasure.isFound() ) {
				Log.d("TreasureTask", "Updating...........");
                _webserver.updateTreasureStatus(_treasure.isFound(), _winner);
            }*/

			_webserver.deleteDevice(_treasure.getBtAddress());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public void setDeviceSensors() throws Exception{

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

	public static void setFound(boolean value) {
		_found = value;
	}

	public static void setWinner(Bitmap bitmap) {
		_winner = bitmap;
	}
}
