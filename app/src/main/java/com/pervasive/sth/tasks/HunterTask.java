package com.pervasive.sth.tasks;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.pervasive.sth.entities.Device;
import com.pervasive.sth.entities.Media;
import com.pervasive.sth.entities.Suggestion;
import com.pervasive.sth.entities.SuggestionsGenerator;
import com.pervasive.sth.entities.TreasureStatus;
import com.pervasive.sth.exceptions.DeviceSensorCriticalException;
import com.pervasive.sth.exceptions.DeviceSensorException;
import com.pervasive.sth.exceptions.InvalidRESTClientParametersException;
import com.pervasive.sth.network.WSInterface;
import com.pervasive.sth.sensors.SensorsReader;
import com.pervasive.sth.smarttreasurehunt.HunterActivity;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by davtir on 15/05/16.
 */
public class HunterTask extends AsyncTask<Void, Void, Void> {

	private final String LOG_TAG = HunterTask.class.getName();
	private final String pathName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/STH";
	Context _context;
	WSInterface _webserver;
	SensorsReader _sr;
	Device _hunter;
	String _treasureID;
	TreasureStatus _treasureStatus;

	SuggestionsGenerator _suggestionGenerator;

	public HunterTask(Context context, Device hunter) throws InvalidRESTClientParametersException, DeviceSensorCriticalException {
		_context = context;
		_treasureID = "";
		_hunter = hunter;
		_webserver = new WSInterface();
		_suggestionGenerator = new SuggestionsGenerator(_context, _hunter);
		_sr = new SensorsReader(context);
		try {
			_sr.startSensorListener(Sensor.TYPE_LIGHT);
			_sr.startSensorListener(Sensor.TYPE_LINEAR_ACCELERATION);
			_sr.startSensorListener(Sensor.TYPE_AMBIENT_TEMPERATURE);
		} catch ( DeviceSensorException e ) {
			Log.w(LOG_TAG, e.getMessage());
		}
	}

	public String getTreasureID() {
		return _treasureID;
	}

	@Override
	protected Void doInBackground(Void... params) {
		Log.d("HunterTask", "Started");

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

			try {
				Thread.sleep(15000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			Suggestion suggestion = null;
			try {
				suggestion = _suggestionGenerator.createRandomSuggestion(treasure);
			} catch (Exception e) {
				Log.e(LOG_TAG, e.toString());
				this.cancel(true); //DA SISTEMAREEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE
				break;
			}

			Intent intent = new Intent(HunterActivity.SUGGESTION_ACTION);
			intent.putExtra("SUGGESTION", suggestion);
			_context.sendBroadcast(intent);
		}

		try {
			_webserver.deleteDevice(_hunter.getBtAddress());
		} catch (Exception e) {
			e.printStackTrace();
		}

		Media picture;
		try {
			picture = _treasureStatus.getWinner();
			File f = new File(pathName);
			if ( !f.exists() ) {
				f.mkdir();
			}
			FileOutputStream fo = null;
			fo = new FileOutputStream(picture.getMediaName());
			fo.write(picture.getData());

			Intent winnerIntent = new Intent(HunterActivity.WINNER_ACTION);
			winnerIntent.putExtra("WINNER_UPDATE", picture.getMediaName());
			_context.sendBroadcast(winnerIntent);
		} catch ( Exception e ) {
			Log.e(LOG_TAG, e.toString());
			Intent intent = new Intent(HunterActivity.EXIT_ACTION);
			intent.putExtra("EXIT_GAME", true);
			_context.sendBroadcast(intent);
			this.cancel(true);
		}

		return null;
	}
}

