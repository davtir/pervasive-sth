package com.pervasive.sth.tasks;

import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.pervasive.sth.entities.Audio;
import com.pervasive.sth.rest.WSInterface;
import com.pervasive.sth.smarttreasurehunt.HunterActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Alex on 29/05/2016.
 */
public class HunterMediaTask extends AsyncTask<Void, Void, Void> {

    private final String pathName = Environment.getExternalStorageDirectory().getAbsolutePath()+"/STH";

    private MediaRecorder mRecorder = null;
    WSInterface _webserver;
    Audio _audio;
    Context _context;

    public HunterMediaTask(Context context) {
        _webserver = new WSInterface();
        _context = context;
        File f = new File(pathName);
        if (!f.exists())
            f.mkdir();
        Log.d(this.getClass().getName(), "COSTRUTTORE CREATO");
    }

    @Override
    protected Void doInBackground(Void... params) {

        Log.d(this.getClass().getName(), "INIZIO DO IN BACK");
        while(!isCancelled()) {

            Log.d(this.getClass().getName(), "ENTRATO IN TREASURE MEDIA TASK EXECUTE");
            Audio audio;

            try {
                audio = _webserver.retrieveAudio();
                if(audio == null || audio.equals(_audio)) {
                    Log.d(this.getClass().getName(), "no new Audio retrieved");
                    continue;
                }

                _audio = audio;

            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                FileOutputStream fo = new FileOutputStream(_audio.get_audioName());
                fo.write(_audio.get_data());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                continue;
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }

            Intent intent = new Intent(HunterActivity.AUDIO_ACTION);
            intent.putExtra("MEDIA_AUDIO", _audio.get_audioName());
            _context.sendBroadcast(intent);

            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        }
        return null;

    }
}
