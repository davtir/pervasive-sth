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
	 * The hunter device
	 */
	Device _hunter;

	/*
	 * The web server interface
	 */
	WSInterface _webserver;

	/*
	 *	The last generated suggestion. Used to avoid consecutive suggestions of the same type
	 */
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
		Log.d(LOG_TAG, "Initializing probabilities...");
		// the counter of all available sensors
		int sensorsCounter = 0;

		// counter of media adapters, we can fairly assume that a phone always has a camera and a microphone
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

		Log.d(LOG_TAG, "Sensors probability is Ps = " + sensorsProbs);
		Log.d(LOG_TAG, "Media probability is Pm = " + mediaProbs);
	}

	/**
	 *
	 * @param treasure
	 * @return
	 * @throws Exception
	 * @brief this function creates a random suggestion among the available sensors/media
	 */
	public Suggestion createRandomSuggestion(Device treasure) throws Exception {
		Suggestion suggestion = null;
		boolean skip = true;

		// Loop until a valid random suggestion is generated
		while ( skip ) {
			// Get suggestion type uniformly at random
			int type = getRandomSuggestionType();

			// If the generated suggestion is equal the previous one, then generate another type
			if ( _lastSensorSuggestionType == type )
				continue;

			switch ( type ) {

				// Accelerometer suggestion
				case ACCELEROMETER_SUGGESTION:
					Log.d(LOG_TAG, "Accelerometer suggestion selected (type = " + type + ")");
					suggestion = new Suggestion(createAccelerometerMessage(treasure), 0.0, type);
					skip = false;
					break;

				// Luminosity suggestion
				case LUX_SUGGESTION:
					if ( treasure.getLuminosity() != -Float.MAX_VALUE ) {
						Log.d(LOG_TAG, "Luminosity suggestion selected (type = " + type + ")");
						suggestion = new Suggestion(analizeLuxValues(treasure), 0.0, type);
						skip = false;
					}
					break;

				// Temperature suggestion
				case TEMPERATURE_SUGGESTION:
					if ( treasure.getTemperature() != -Float.MAX_VALUE ) {
						Log.d(LOG_TAG, "Temperature suggestion selected (type = " + type + ")");
						suggestion = new Suggestion(analizeTemperatureValues(treasure), 0.0, type);
						skip = false;
					}
					break;

				// Picture suggestion
				case PICTURE_SUGGESTION:
					Log.d(LOG_TAG, "Picture suggestion selected (type = " + type + "). Retrieving picture from web server...");
					Media picture = _webserver.retrievePicture();

					File picFile = new File(picture.getMediaName());
					File picDir = new File(picFile.getParent());
					if ( !picDir.exists() ) {
						picDir.mkdir();
					}
					FileOutputStream picOutStream = new FileOutputStream(picFile);
					picOutStream.write(picture.getData());

					suggestion = new Suggestion(picture.getMediaName(), 0.0, type);
					_lastSensorSuggestionType = type;
					skip = false;
					break;

				// Audio suggestion
				case AUDIO_SUGGESTION:
					Log.d(LOG_TAG, "Audio suggestion selected (type = " + type + "). Retrieving audio from web server...");
					Media audio = _webserver.retrieveAudio();

					File audioFile = new File(audio.getMediaName());
					File audioDir = new File(audioFile.getParent());
					if ( !audioDir.exists() ) {
						audioDir.mkdir();
					}
					FileOutputStream audioOutStream = new FileOutputStream(audio.getMediaName());
					audioOutStream.write(audio.getData());

					suggestion = new Suggestion(audio.getMediaName(), 0.0, type);
					_lastSensorSuggestionType = type;
					skip = false;
					break;

				// Unknown suggestion
				default:
					Log.w(LOG_TAG, "Unknown suggestion type. ( type = " + type + ").");
					break;
			}

			// If the suggestion have been correctly created, then update the last generated suggestion type
			if ( !skip ) {
				_lastSensorSuggestionType = type;
			}
		}
		return suggestion;
	}

	/**
	 * @return	The random generated suggestion type
	 * @brief	Generates suggestions type uniformly at random, according to the probabilities
	 * 			previously computed
	 */
	private int getRandomSuggestionType() {
		Log.d(LOG_TAG, "Generating a random suggestion type...");

		Random rndGen = new Random();

		double random = Math.abs(rndGen.nextInt()) / (double) Integer.MAX_VALUE;
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

		// An invalid suggestion
		return SUGGESTION_NUMBER;
	}

	/**
	 *
	 * @param treasure
	 * @return	The message string resulting from the comparison between treasure and hunter luminosity values
	 * @brief	This function compares luminosity values of treasure and hunter devices
	 * 			and returns a message string which depends on the result of the comparison
	 */
	public String analizeLuxValues(Device treasure) {
		Log.d(LOG_TAG, "Comparing luminosity values between treasure and hunter");
		double t_threshold = 0.0;
		double h_threshold = 0.0;
		double t_lux = treasure.getLuminosity();
		if (t_lux >= (t_threshold = SensorsReader.LUX_JOURNEY_ON_THE_SUN_THRESHOLD))
			Log.d(LOG_TAG, "Selected threshold " + t_threshold + " (LUX_JOURNEY_ON_THE_SUN_THRESHOLD) for treasure luminosity.");
		else if (t_lux >= (t_threshold = SensorsReader.LUX_DAYLIGHT_THRESHOLD))
			Log.d(LOG_TAG, "Selected threshold " + t_threshold + " (LUX_DAYLIGHT_THRESHOLD) for treasure luminosity.");
		else if (t_lux >= (t_threshold = SensorsReader.LUX_TWILIGHT_THRESHOLD))
			Log.d(LOG_TAG, "Selected threshold " + t_threshold + " (LUX_TWILIGHT_THRESHOLD) for treasure luminosity.");
		else if (t_lux >= (t_threshold = SensorsReader.LUX_DARK_THRESHOLD))
			Log.d(LOG_TAG, "Selected threshold " + t_threshold + " (LUX_DARK_THRESHOLD) for treasure luminosity.");

		h_threshold = SensorsReader.getLuxThreshold(_sensorsReader.getLuminosity());
		String msg;
		if ( h_threshold < t_threshold )
			msg = "Seems like the treasure is in a brighter place than you!";
		else if ( h_threshold > t_threshold )
			msg = "Seems like the treasure is in a darker place than you!";
		else msg = "Seems like you and the treasure are in a place with the same brightness!";

		return msg;

	}

	/**
	 *
	 * @param	treasure
	 * @return	The message string resulting from the comparison between treasure and hunter temperature values
	 * @brief	This function compares temperature values of treasure and hunter devices
	 * 			and returns a message string which depends on the result of the comparison
	 */
	public String analizeTemperatureValues(Device treasure) {
		double t_temp = treasure.getTemperature();
		double h_temp = _sensorsReader.getTemperature();
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

	/**
	 *
	 * @param	treasure
	 * @return	A message string which depends on the value of the resultant of the 3-axis accelerometer
	 * 			values of the treasure
	 * @brief	This function returns a message string which depends on the value of the resultant
	 * 			of the 3-axis accelerometer values of the treasure
	 */
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
