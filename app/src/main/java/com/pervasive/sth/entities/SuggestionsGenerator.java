package com.pervasive.sth.entities;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.pervasive.sth.rest.WSInterface;
import com.pervasive.sth.sensors.SensorsReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import com.pervasive.sth.entities.Media;

/**
 * @brief this class provides to generate random suggestions
 */
public class SuggestionsGenerator {

	private final String LOG_TAG = SuggestionsGenerator.class.getName();

	/*
	 * Suggestions IDs
	 */
	public final static int SUGGESTION_NUMBER = 5;
	public final static int ACCELEROMETER_SUGGESTION = 0;
	public final static int LUX_SUGGESTION = 1;
	public final static int TEMPERATURE_SUGGESTION = 2;
	public final static int PICTURE_SUGGESTION = 3;
	public final static int AUDIO_SUGGESTION = 4;

	/*
	 * array of suggestions probabilites
	 */
	private final double[] _suggestionProbs;

	/*
	 * The parent activity environment
	 */
	Context _context;

	/*
	 * Android sensors manager
	 */
	SensorsReader _sensorsReader;

	/*
	 * the hunter device
	 */
	Device _hunter;

	/*
	 * the web server interface
	 */
	WSInterface _webserver;

	int _lastSensorSuggestionType = SUGGESTION_NUMBER;

	/**
	 *
	 * @param context
	 * @param hunter
	 * @brief initialize the SuggestionsGenerator fields
	 */
	public SuggestionsGenerator(Context context, Device hunter) {
		_context = context;
		_sensorsReader = new SensorsReader(context);
		_hunter = hunter;
		_webserver = new WSInterface();
		_suggestionProbs = new double[SUGGESTION_NUMBER];
		initProbabilities();
	}

	/**
	 * @brief initialize suggestions probability values
	 */
	public void initProbabilities() {

		// the counter of all available sensors
		int sensorsCounter = 0;

		// counter of media adapters, We can fairly assume that a phone always has a camera and a microphone
		int mediaCounter = 2;

		if (_sensorsReader.isAccelerometerAvailable())
			++sensorsCounter;
		if (_sensorsReader.isThermometerAvailable())
			++sensorsCounter;
		if (_sensorsReader.isPhotoresistorAvailable()) {
			++sensorsCounter;
		}

		// probability of each media suggestion (Pm)
		double mediaProbs;

		// probability of each sensor suggestion (Ps)
		double sensorsProbs;

		// ratio between sensorsProbs and mediaProbs (R)
		double sensorsMediaRatio = 2;

		// Given that R=Ps/Pm, Ps = R*Pm and that N*Ps + M*Pm = 1, we have that
		sensorsProbs = 1.0/(sensorsCounter + mediaCounter / sensorsMediaRatio);
		mediaProbs = sensorsProbs / sensorsMediaRatio;

		//for the available sensors, set their probabilties equal to sensorProbs
		_suggestionProbs[ACCELEROMETER_SUGGESTION] = _sensorsReader.isAccelerometerAvailable() ? (sensorsProbs) : (0.0);
		_suggestionProbs[LUX_SUGGESTION] = _sensorsReader.isPhotoresistorAvailable() ? (sensorsProbs) : (0.0);
		_suggestionProbs[TEMPERATURE_SUGGESTION] = _sensorsReader.isThermometerAvailable() ? (sensorsProbs) : (0.0);

		//for each media (picture and microphone) set their probabilites equal to mediaProbs
		_suggestionProbs[PICTURE_SUGGESTION] = mediaProbs;
		_suggestionProbs[AUDIO_SUGGESTION] = mediaProbs;
	}

	/**
	 *
	 * @param treasure
	 * @return
	 * @throws Exception
	 * @brief this function creates a random suggestion among the available sensors/media
	 */
	public Suggestion createRandomSuggestion(Device treasure) throws Exception{

		boolean skip = true;

		Suggestion suggestion = null;
		while (skip) {
			int type = getRandomSuggestionType();

			//analizeSensors(treasure);
			if(_lastSensorSuggestionType == type)
				continue;
			switch (type) {
				case ACCELEROMETER_SUGGESTION:
					suggestion = new Suggestion(createAccelerometerMessage(treasure), 0.0, type);
					skip = false;
					break;
				case LUX_SUGGESTION:
					if(treasure.getLuminosity() != -Float.MAX_VALUE && _sensorsReader.isPhotoresistorAvailable()) {
						suggestion = new Suggestion(analizeLuxValues(treasure), 0.0, type);
						skip = false;
					}
					break;
				case TEMPERATURE_SUGGESTION:
					if(treasure.getTemperature() != -Float.MAX_VALUE && _sensorsReader.isThermometerAvailable()) {
						suggestion = new Suggestion(analizeTemperatureValues(treasure), 0.0, type);
						skip = false;
					}
					break;
				case PICTURE_SUGGESTION:
					Media picture;
					try {
						picture = _webserver.retrievePicture();
						if (picture == null) {
							// TODO
						}
						Log.d(this.getClass().getName(), "PICTURE RETRIEVED FROM WEBSERVER------------");

						File f = new File(picture.getMediaName());
						if (!f.exists())
							f.mkdir();
						FileOutputStream fo = new FileOutputStream(picture.getMediaName());
						fo.write(picture.getData());

						Log.d(this.getClass().getName(), "PHOTO WROTE ON SMARTPHONE------------");

						suggestion = new Suggestion(picture.getMediaName(), 0.0, type);
						_lastSensorSuggestionType = type;
						skip = false;

					} catch (Exception e) {
						Log.w(this.getClass().getName(), e.getMessage());
					}
					break;
				case AUDIO_SUGGESTION:
					Media audio;
					try {
						audio = _webserver.retrieveAudio();
						if (audio == null) {
							// TODO
						}
						Log.d(this.getClass().getName(), "AUDIO RETRIEVED FROM WEBSERVER------------");

						File f = new File(audio.getMediaName());
						if (!f.exists())
							f.mkdir();
						FileOutputStream fo = new FileOutputStream(audio.getMediaName());
						fo.write(audio.getData());

						Log.d(this.getClass().getName(), "AUDIO WROTE ON SMARTPHONE------------");

						suggestion = new Suggestion(audio.getMediaName(), 0.0, type);
						_lastSensorSuggestionType = type;
						skip = false;

					} catch (Exception e) {
						Log.w(this.getClass().getName(), e.getMessage());
					}
					break;
				default:
					break;
			}

			if(!skip)
				_lastSensorSuggestionType = type;
		}

		return suggestion;
	}

	private int getRandomSuggestionType() {
		Random rndGen = new Random();

		double random = Math.abs(rndGen.nextInt()) / (double) Integer.MAX_VALUE;
		Log.d(this.getClass().getName(), "*********The random number generated is: " + random);

		if (random <= _suggestionProbs[AUDIO_SUGGESTION]) {
			return AUDIO_SUGGESTION;
		}

		random -= _suggestionProbs[AUDIO_SUGGESTION];

		if (random <= _suggestionProbs[PICTURE_SUGGESTION]) {
			return PICTURE_SUGGESTION;
		}

		random -= _suggestionProbs[PICTURE_SUGGESTION];

		if (random <= _suggestionProbs[TEMPERATURE_SUGGESTION]) {
			return TEMPERATURE_SUGGESTION;
		}

		random -= _suggestionProbs[TEMPERATURE_SUGGESTION];

		if (random <= _suggestionProbs[LUX_SUGGESTION]) {
			return LUX_SUGGESTION;
		}

		random -= _suggestionProbs[LUX_SUGGESTION];

		if (random <= _suggestionProbs[ACCELEROMETER_SUGGESTION]) {
			return ACCELEROMETER_SUGGESTION;
		}

		return -1;
	}

	private void analizeSensors(Device treasure) throws Exception{
		setDeviceSensors();
		if (_sensorsReader.isPhotoresistorAvailable()) {
			analizeLuxValues(treasure);
		}

		if (_sensorsReader.isThermometerAvailable()) {
			analizeTemperatureValues(treasure);
		}
	}

	public void setDeviceSensors() throws Exception{

		float[] acc;

		if (_sensorsReader.isPhotoresistorAvailable()) {
			_hunter.setLuminosity(_sensorsReader.getLuminosity());
		} else _hunter.setLuminosity(-Float.MAX_VALUE);

		if (_sensorsReader.isThermometerAvailable()) {
			_hunter.setTemperature(_sensorsReader.getTemperature());
		} else _hunter.setTemperature(-Float.MAX_VALUE);

		acc = _sensorsReader.getAcceleration();
		if (_sensorsReader.isAccelerometerAvailable() && acc != null) {
			_hunter.setAcceleration(_sensorsReader.getAcceleration());
		} else {
			float[] a = {-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE};
			_hunter.setAcceleration(a);
		}
	}

	public String analizeLuxValues(Device treasure) {

		double t_threshold;
		double t_lux = treasure.getLuminosity();
		if (t_lux >= (t_threshold = SensorsReader.LUX_JOURNEY_ON_THE_SUN_THRESHOLD))
			Log.d(this.getClass().getName(), "Pija lo shuttle");
		else if (t_lux >= (t_threshold = SensorsReader.LUX_DAYLIGHT_THRESHOLD))
			Log.d(this.getClass().getName(), "Ben illuminato");
		else if (t_lux >= (t_threshold = SensorsReader.LUX_TWILIGHT_THRESHOLD))
			Log.d(this.getClass().getName(), "Non molto illuminato");
		else if (t_lux >= (t_threshold = SensorsReader.LUX_DARK_THRESHOLD))
			Log.d(this.getClass().getName(), "Scuro zi");

		double h_lux = _hunter.getLuminosity();
		double h_threshold = SensorsReader.getLuxThreshold(h_lux);
		String msg;
		if (h_threshold < t_threshold)
			msg = "Seems like the treasure is in a brighter place than you!";
		else if (h_threshold > t_threshold)
			msg = "Seems like the treasure is in a darker place than you!";
		else msg = "Seems like you and the treasure are in a place with the same brightness!";

		return msg;

	}

	public String analizeTemperatureValues(Device treasure) {
		double t_temp = treasure.getTemperature();
		double h_temp = _hunter.getTemperature();
		double deltaT = t_temp - h_temp;

		String msg;
		if (deltaT < 0)
			msg = "Seems like treasure temperature is " + (Math.round((Math.abs(deltaT) * 10)) / 10.0) + " degrees lower than you";
		else if (deltaT > 0)
			msg = "Seems like treasure temperature is " + (Math.round((Math.abs(deltaT) * 10)) / 10.0) + " degrees higher than you";
		else
			msg = "Seems like treasure temperature is equal to yours";

		return msg;
	}

	public String createAccelerometerMessage(Device treasure) {

		String msg;

		double[] meanAcc = treasure.getAcceleration();
		double resultant = 0.0;
		resultant = (float) Math.sqrt(Math.pow(meanAcc[0], 2) + Math.pow(meanAcc[1], 2) + Math.pow(meanAcc[2], 2));

		if (resultant >= 1)
			msg = "Watch out! The treasure is moving with acceleration equals to " + (Math.round(resultant * 10.0) / 10.0) + " m/s²";
		else
			msg = "Treasure is not moving at all! What are you waiting for?";

		return msg;
	}
}
