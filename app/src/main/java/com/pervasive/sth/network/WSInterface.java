package com.pervasive.sth.network;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import com.pervasive.sth.entities.*;
import com.pervasive.sth.exceptions.InvalidRESTClientParametersException;

import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Base64;

import java.io.ByteArrayOutputStream;

/**
 * @brief	This class represents the interface to the STH RESTful web server
 */
public class WSInterface {

	/*
	 * The server URI
	 */
	//private static final String BASE_URI = "http://192.168.1.2:8084/STHServer/webresources";
	//private static final String BASE_URI = "http://192.168.1.6:8084/STHServer/webresources";
	//private static final String BASE_URI = "http://192.168.1.8:8084/STHServer/webresources";
	private static final String BASE_URI = "http://pervasive.acsys.it:8080/STHServer/webresources";

	/*
	 * The WS entry point for the treasure device
	 */
	private static final String DEV_PATH = "/device";

	/*
 	 * The WS entry point for treasure device deletion
 	 */
	private static final String DEL_PATH = "/delete";

	/*
	 * The WS entry point for checking game status
	 */
	private static final String END_PATH = "/endgame";

	/*
	 * The WS entry point for audio retrieval
	 */
	private static final String AUDIO_PATH = "/audio";

	/*
	 * The WS entry point for picture retrieval
	 */
	private static final String PIC_PATH = "/picture";

	/*
	 * The path at which media file are written
	 */
	private final String pathName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/STH";

	/*
	 * The REST client associated to the '/device' entry point
	 */
	private final RESTClient deviceClient;

	/*
	 * The REST client associated to the '/delete' entry point
	 */
	private final RESTClient deleteClient;

	/*
	 * The REST client associated to the '/endgame' entry point
	 */
	private final RESTClient endGameClient;

	/*
	 * The REST client associated to the '/audio' entry point
	 */
	private final RESTClient audioClient;

	/*
	 * The REST client associated to the '/picture' entry point
	 */
	private final RESTClient pictureClient;

	private final String LOG_TAG = WSInterface.class.getName();

	public WSInterface() throws InvalidRESTClientParametersException {
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

	/**
	 *
	 * @param	device
	 * @throws	Exception
	 * @brief	Updates the device informations on WS
	 */
	public void updateDeviceEntry(Device device) throws Exception {
		JSONObject jsonDevice = new JSONObject();

		double[] acc = device.getAcceleration();

		jsonDevice.put("ID", device.getBtAddress());
		jsonDevice.put("NAME", device.getName());
		jsonDevice.put("ROLE", device.getRole());
		jsonDevice.put("LATITUDE", device.getLatitude());
		jsonDevice.put("LONGITUDE", device.getLongitude());
		jsonDevice.put("LUMINOSITY", device.getLuminosity());
		jsonDevice.put("TEMPERATURE", device.getTemperature());
		JSONArray jArr = new JSONArray();
		jArr.put(acc[0]);
		jArr.put(acc[1]);
		jArr.put(acc[2]);
		jsonDevice.put("ACCELERATION", jArr);

		Log.d(LOG_TAG, "Posting" + jsonDevice.toString());

		deviceClient.executePost(jsonDevice.toString());

		Log.d(LOG_TAG, "Updated device info.");
	}

	/**
	 * @return
	 * @throws	Exception
	 * @brief	Retrieves device information from WS
	 */
	public Device retrieveDevice() throws Exception {
		Log.d(LOG_TAG, "Retrieving device informations from web server.");
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
		if (jArr.length() != 3) {
			throw new RuntimeException("Invalid acceleration array length");
		}

		acceleration[0] = jArr.getDouble(0);
		acceleration[1] = jArr.getDouble(1);
		acceleration[2] = jArr.getDouble(2);

		return new Device(id, name, role, latitude, longitude, luminosity, temperature, acceleration);
	}

	/**
	 *
	 * @param	status
	 * @param	bitmap
	 * @throws	Exception
	 * @brief	Updates the treasure status on WS
	 */
	public void updateTreasureStatus(Boolean status, Bitmap bitmap) throws Exception {

		Log.d(LOG_TAG, " Updating treasure status ( Status = " + status.toString() + ")");
		byte[] pictureData = {};
		if (bitmap != null) {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
			pictureData = stream.toByteArray();
		}

		JSONObject endGame = new JSONObject();

		String encodedData = Base64.encodeToString(pictureData, Base64.DEFAULT);

		endGame.put("STATUS", status.toString());
		endGame.put("PIC_NAME", pathName + "/Winner.bmp");
		endGame.put("PIC_DATA", encodedData);

		endGameClient.executePost(endGame.toString());
	}

	/**
	 * @return
	 * @throws	Exception
	 * @brief	Retrieves treasure status informations
	 */
	public TreasureStatus retrieveTreasureStatus() throws Exception {
		Log.d(LOG_TAG, "Retrieving treasure status from web server.");

		JSONObject jsonEndgame = new JSONObject(endGameClient.executeGet());

		boolean status = Boolean.parseBoolean(jsonEndgame.getString("STATUS"));
		String picName = (String) jsonEndgame.get("PIC_NAME");
		byte[] picData = Base64.decode(jsonEndgame.getString("PIC_DATA"), Base64.DEFAULT);

		TreasureStatus res = new TreasureStatus(status);
		res.setWinner(new Media(picName, picData));

		return res;
	}

	/**
	 * @param	id
	 * @throws	Exception
	 * @brief	Deletes the device entry on WS
	 */
	public void deleteDevice(String id) throws Exception {
		Log.d(LOG_TAG, "Deleting device from web server.");
		deleteClient.executePost(id);
	}

	/**
	 * @param	mediaFile
	 * @throws	Exception
	 * @brief	Uploads audio file on WS
	 */
	public void uploadAudio(Media mediaFile) throws Exception {

		Log.d(LOG_TAG, "Updating audio on web server");

		JSONObject jsonAudio = new JSONObject();

		jsonAudio.put("AUDIO_NAME", mediaFile.getMediaName());

		JSONArray jArr = new JSONArray();
		byte[] audioData = mediaFile.getData();

		String encodedData = Base64.encodeToString(audioData, Base64.DEFAULT);

		jsonAudio.put("AUDIO_DATA", encodedData);

		audioClient.executePost(jsonAudio.toString());
	}

	/**
	 * @return
	 * @throws	Exception
	 * @brief	Retrieves audio file from WS
	 */
	public Media retrieveAudio() throws Exception {
		Log.d(LOG_TAG, "Retrieving audio file from web server.");

		JSONObject jsonAudio = new JSONObject(audioClient.executeGet());

		String name = (String) jsonAudio.get("AUDIO_NAME");
		byte[] audioData = Base64.decode(jsonAudio.getString("AUDIO_DATA"), Base64.DEFAULT);

		return new Media(name, audioData);
	}

	/**
	 * @param	mediaFile
	 * @throws	Exception
	 * @brief	Uploads pictures on WS
	 */
	public void uploadPicture(Media mediaFile) throws Exception {

		Log.d(LOG_TAG, "Uploading picture on web server.");

		JSONObject jsonPicture = new JSONObject();

		jsonPicture.put("PIC_NAME", mediaFile.getMediaName());

		byte[] pictureData = mediaFile.getData();

		String encodedData = Base64.encodeToString(pictureData, Base64.DEFAULT);

		jsonPicture.put("PIC_DATA", encodedData);

		pictureClient.executePost(jsonPicture.toString());
	}

	/**
	 * @return
	 * @throws	Exception
	 * @brief	Retrieves picture from WS
	 */
	public Media retrievePicture() throws Exception {
		Log.d(LOG_TAG, "Retrieving picture from web server.");

		JSONObject jsonPicture = new JSONObject(pictureClient.executeGet());

		String name = (String) jsonPicture.get("PIC_NAME");
		byte[] picData = Base64.decode(jsonPicture.getString("PIC_DATA"), Base64.DEFAULT);

		return new Media(name, picData);
	}
}
