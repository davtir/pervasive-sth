package com.pervasive.sth.rest;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import com.pervasive.sth.entities.*;

import org.json.JSONArray;
import org.json.JSONObject;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

/**
 * Created by davtir on 22/05/16.
 */
public class WSInterface {

    private static final String BASE_URI = "http://192.168.1.6:8084/STHServer/webresources";
    //private static final String BASE_URI = "http://192.168.1.6:8084/STHServer/webresources";
    //private static final String BASE_URI = "http://pervasive.acsys.it:8080/STHServer/webresources";

    private static final String DEV_PATH = "/device";
    private static final String DEL_PATH = "/delete";
    private static final String END_PATH = "/endgame";
    private static final String AUDIO_PATH = "/audio";
    private static final String PIC_PATH = "/picture";

    private final String pathName = Environment.getExternalStorageDirectory().getAbsolutePath()+"/STH";

    private final RESTClient deviceClient;
    private final RESTClient deleteClient;
    private final RESTClient endGameClient;
    private final RESTClient audioClient;
    private final RESTClient pictureClient;


    public WSInterface() {
        deviceClient = new RESTClient(BASE_URI + DEV_PATH);
        deleteClient = new RESTClient(BASE_URI + DEL_PATH);
        endGameClient = new RESTClient(BASE_URI + END_PATH);
        audioClient = new RESTClient(BASE_URI + AUDIO_PATH);
        pictureClient = new RESTClient(BASE_URI + PIC_PATH);
        deviceClient.addHeader("content-type", "application/json");
        deleteClient.addHeader("content-type", "text/plain");
        endGameClient.addHeader("content-type", "application/json");
        audioClient.addHeader("content-type", "application/json");
        pictureClient.addHeader("content-type", "application/json");
    }

    public void updateDeviceEntry(Device device) throws Exception {
        JSONObject jsonDevice = new JSONObject();

        double[] acc = device.getAcceleration();
        double[] rot = device.getRotation();

        jsonDevice.put("ID", device.getMACAddress());
        jsonDevice.put("NAME", device.getName());
        jsonDevice.put("ROLE", device.getRole());
        jsonDevice.put("LATITUDE", device.getLatitude());
        jsonDevice.put("LONGITUDE", device.getLongitude());
        jsonDevice.put("LUMINOSITY", device.getLuminosity());
        jsonDevice.put("TEMPERATURE", device.getTemperature());
        JSONArray jArr = new JSONArray();
        JSONArray jRot = new JSONArray();
        jArr.put(acc[0]);
        jArr.put(acc[1]);
        jArr.put(acc[2]);
        jsonDevice.put("ACCELERATION", jArr);

        jRot.put(rot[0]);
        jRot.put(rot[1]);
        jRot.put(rot[2]);
        jsonDevice.put("ROTATION", jRot);

        Log.d("WSInterface", "Posting" + jsonDevice.toString());

        deviceClient.executePost(jsonDevice.toString());

        Log.d("WSInterface", "Updated device info.");
    }

    public Device retrieveDevice() throws Exception {

        //The retrieved device is the treasure now; in future it can be even other hunters
        JSONObject jsonDevice = new JSONObject(deviceClient.executeGet());

        String id = (String) jsonDevice.get("ID");
        String name = (String) jsonDevice.get("NAME");
        String role = (String) jsonDevice.get("ROLE");
        double latitude = jsonDevice.getDouble("LATITUDE");
        double longitude = jsonDevice.getDouble("LONGITUDE");
        double luminosity = jsonDevice.getDouble("LUMINOSITY");
        double temperature = jsonDevice.getDouble("TEMPERATURE");
        double[] acceleration = new double[3];


        JSONArray jArr = jsonDevice.getJSONArray("ACCELERATION");
        if(jArr.length() != 3) {
            throw new RuntimeException("Invalid acceleration array length");
        }

        acceleration[0] = jArr.getDouble(0);
        acceleration[1] = jArr.getDouble(1);
        acceleration[2] = jArr.getDouble(2);

        double[] rotation = new double[3];
        jArr = jsonDevice.getJSONArray("ROTATION");
        if(jArr.length() != 3) {
            throw new RuntimeException("Invalid rotation array length");
        }

        rotation[0] = jArr.getDouble(0);
        rotation[1] = jArr.getDouble(1);
        rotation[2] = jArr.getDouble(2);

        return new Device(id, name, role, latitude, longitude, luminosity, temperature, acceleration, rotation);
    }

    public void updateTreasureStatus(Boolean status, Bitmap bitmap) throws Exception {

        Log.d("WSInterface", "Status=" + status.toString());
        byte[] pictureData = {};
        if ( bitmap != null ) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            pictureData = stream.toByteArray();
        }
        Log.d("WSInterface", "Trying to update winner picture");

        JSONObject endGame = new JSONObject();


        JSONArray jArr = new JSONArray();

        String encodedData = Base64.encodeToString(pictureData, Base64.DEFAULT);

        endGame.put("STATUS", status.toString());
        endGame.put("PIC_NAME", pathName+"/Winner.bmp");
        endGame.put("PIC_DATA",encodedData);

        endGameClient.executePost(endGame.toString());

        Log.d("WSInterface", "updated picture info.");
    }

    public TreasureStatus retrieveTreasureStatus() throws Exception {
        JSONObject jsonEndgame = new JSONObject(endGameClient.executeGet());

        boolean status = Boolean.parseBoolean(jsonEndgame.getString("STATUS"));
        String picName = (String) jsonEndgame.get("PIC_NAME");
        byte[] picData = Base64.decode(jsonEndgame.getString("PIC_DATA"), Base64.DEFAULT);

        Log.d("WSInterface", "Got picture info.");

        TreasureStatus res = new TreasureStatus(status);
        res.setWinner(new Media(picName, picData));

        return res;
    }

    public void deleteDevice(String id) throws Exception {
        deleteClient.executePost(id);
    }

    public void uploadAudio(Media mediaFile) throws Exception {

        Log.d("WSInterface", "Trying to update audio info.");

        JSONObject jsonAudio = new JSONObject();

        jsonAudio.put("AUDIO_NAME", mediaFile.getMediaName());

        JSONArray jArr = new JSONArray();
        byte[] audioData = mediaFile.getData();

        String encodedData = Base64.encodeToString(audioData, Base64.DEFAULT);

        jsonAudio.put("AUDIO_DATA",encodedData);

        audioClient.executePost(jsonAudio.toString());

        Log.d("WSInterface", "updated audio info.");
    }

    public Media retrieveAudio() throws Exception {
        JSONObject jsonAudio = new JSONObject(audioClient.executeGet());

        String name = (String) jsonAudio.get("AUDIO_NAME");
        byte[] audioData = Base64.decode(jsonAudio.getString("AUDIO_DATA"), Base64.DEFAULT);

        Log.d("WSInterface", "Got audio info.");

        return new Media(name, audioData);
    }

    public void uploadPicture(Media mediaFile) throws Exception {

        Log.d("WSInterface", "Trying to update picture info.");

        JSONObject jsonPicture = new JSONObject();

        jsonPicture.put("PIC_NAME", mediaFile.getMediaName());

        JSONArray jArr = new JSONArray();
        byte[] pictureData = mediaFile.getData();

        String encodedData = Base64.encodeToString(pictureData, Base64.DEFAULT);

        jsonPicture.put("PIC_DATA",encodedData);

        pictureClient.executePost(jsonPicture.toString());

        Log.d("WSInterface", "updated picture info.");
    }

    public Media retrievePicture() throws Exception {
        JSONObject jsonPicture = new JSONObject(pictureClient.executeGet());

        String name = (String) jsonPicture.get("PIC_NAME");
        byte[] picData = Base64.decode(jsonPicture.getString("PIC_DATA"), Base64.DEFAULT);

        Log.d("WSInterface", "Got picture info.");

        return new Media(name, picData);
    }
}
