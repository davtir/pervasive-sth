package com.pervasive.sth.tasks;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;

import com.pervasive.sth.entities.Audio;
import com.pervasive.sth.rest.WSInterface;
import com.pervasive.sth.smarttreasurehunt.TreasureActivity;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by Alex on 29/05/2016.
 */
public class TreasureMediaTask extends AsyncTask<Void, Void, Void> {

    private final String pathName = Environment.getExternalStorageDirectory().getAbsolutePath()+"/STH";
    private final String audioFileName = pathName+"/treasure_audio.3gp";
    //private final String pictureFileName = pathName+"/treasure_pic.jpg";
    private MediaRecorder mRecorder = null;
    WSInterface _webserver;
    Context _context;

    public TreasureMediaTask(Context context) {
        Log.d(this.getClass().getName(),"Inizio Costruttore");

        _context = context;
        _webserver = new WSInterface();
        File f = new File(pathName);
        if (!f.exists())
            f.mkdir();
        Log.d(this.getClass().getName(),"Fine Costruttore");
    }

    @Override
    protected Void doInBackground(Void... params) {

        Log.d(this.getClass().getName(),"Entrato in mediaExecute");
        while(!isCancelled()) {

            Log.d(this.getClass().getName(),"ENTRATO TREASURE MEDIA TASK");
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setOutputFile(audioFileName);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            try {
                mRecorder.prepare();
            } catch (IOException e) {
                Log.e("TreasureMediaTask", e.getMessage());
            }

            mRecorder.start();

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            mRecorder.stop();

            mRecorder.release();

            uploadAudio();

            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        }
        return null;

    }

    private void uploadAudio() {
        File f = new File(audioFileName);
        try {

            FileInputStream fis = new FileInputStream(f);

            byte[] data = new byte[(int) f.length()];

            fis.read(data);


            _webserver.uploadAudio(new Audio(audioFileName,data));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    protected void onPostExecute(Void aVoid) {
        mRecorder.release();
    }
}
