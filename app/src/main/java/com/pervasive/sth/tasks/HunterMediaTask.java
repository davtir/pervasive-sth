package com.pervasive.sth.tasks;

import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.pervasive.sth.entities.Media;
import com.pervasive.sth.rest.WSInterface;
import com.pervasive.sth.smarttreasurehunt.HunterActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Alex on 29/05/2016.
 */
public class HunterMediaTask extends AsyncTask<Void, Void, Void> {

	private final String pathName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/STH";

	private MediaRecorder mRecorder = null;
	WSInterface _webserver;
	Media _audio;
	Media _picture;
	Context _context;

	public HunterMediaTask(Context context) {
		_webserver = new WSInterface();
		_context = context;
		File f = new File(pathName);
		if ( !f.exists() )
			f.mkdir();
		Log.d(this.getClass().getName(), "COSTRUTTORE CREATO");
	}

	@Override
	protected Void doInBackground(Void... params) {

		Log.d(this.getClass().getName(), "INIZIO DO IN BACK");
		while ( !isCancelled() ) {

			Log.d(this.getClass().getName(), "ENTRATO IN TREASURE MEDIA TASK EXECUTE");
			Media audio, picture;

			try {
				audio = _webserver.retrieveAudio();
				if ( audio == null || audio.equals(_audio) ) {
					Log.d(this.getClass().getName(), "no new Audio retrieved");
					continue;
				}

				_audio = audio;

			} catch ( Exception e ) {
				e.printStackTrace();
			}

			try {
				FileOutputStream fo = new FileOutputStream(_audio.getMediaName());
				fo.write(_audio.getData());
			} catch ( FileNotFoundException e ) {
				e.printStackTrace();
				continue;
			} catch ( IOException e ) {
				e.printStackTrace();
				continue;
			}

			Intent audioIntent = new Intent(HunterActivity.AUDIO_ACTION);
			audioIntent.putExtra("MEDIA_AUDIO", _audio.getMediaName());
			_context.sendBroadcast(audioIntent);

			try {
				picture = _webserver.retrievePicture();
				if ( picture == null /*|| picture.equals(_picture)*/ ) {
					Log.d(this.getClass().getName(), "no new Picture retrieved");
					continue;
				}

				_picture = picture;
				Log.d(this.getClass().getName(), "PICTURE RETRIEVED FROM WEBSERVER------------");
			} catch ( Exception e ) {
				e.printStackTrace();
			}

			try {
				FileOutputStream fo = new FileOutputStream(_picture.getMediaName());
				fo.write(_picture.getData());
				Log.d(this.getClass().getName(), "PHOTO WROTE ON SMARTPHONE------------");
			} catch ( FileNotFoundException e ) {
				e.printStackTrace();
				continue;
			} catch ( IOException e ) {
				e.printStackTrace();
				continue;
			}

			Intent pictureIntent = new Intent(HunterActivity.PICTURE_ACTION);
			pictureIntent.putExtra("MEDIA_PICTURE", _picture.getMediaName());
			_context.sendBroadcast(pictureIntent);
			Log.d(this.getClass().getName(), "INTENT SENT------------");

			try {
				Thread.sleep(60000);
			} catch ( InterruptedException e ) {
				e.printStackTrace();
			}


		}
		return null;

	}
}
