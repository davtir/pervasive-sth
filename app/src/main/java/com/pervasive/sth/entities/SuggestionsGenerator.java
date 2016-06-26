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

/**
 * Created by Alex on 25/06/2016.
 */
public class SuggestionsGenerator {

    private final String pathName = Environment.getExternalStorageDirectory().getAbsolutePath()+"/STH";

    public static int SUGGESTION_NUMBER = 6;
    public static int ACCELEROMETER_SUGGESTION = 0;
    public static int GYROSCOPE_SUGGESTION = 1;
    public static int LUX_SUGGESTION = 2;
    public static int TEMPERATURE_SUGGESTION = 3;
    public static int PICTURE_SUGGESTION = 4;
    public static int AUDIO_SUGGESTION = 5;

    private final double[] _suggestionProbs;

    Context _context;
    SensorsReader _sensorsReader;
    Device _hunter;
    WSInterface _webserver;

    public SuggestionsGenerator(Context context, Device hunter) {
        _context = context;
        _sensorsReader = new SensorsReader(context);
        _hunter = hunter;
        _webserver = new WSInterface();

        _suggestionProbs = new double[SUGGESTION_NUMBER];
        initProbabilities();
    }

    //public static int GPS_DISTANCE_SUGGESTION = 4;


    public void initProbabilities() {
        int sensorsCounter = 0;
        int mediaCounter = 2;           // We can FAIRLY assume that a phone always has a camera and a microphone!!!

        if ( _sensorsReader.isAccelerometerAvailable() )
            ++sensorsCounter;
        if ( _sensorsReader.isGyroscopeAvailable() )
            ++sensorsCounter;
        if ( _sensorsReader.isThermometerAvailable() )
            ++sensorsCounter;
        if ( _sensorsReader.isPhotoresistorAvailable() ) {
            ++sensorsCounter;
        }

        double mediaProbs = 1.0 / (2.0 * (sensorsCounter + 1) );
        double sensorsProbs = 2 * mediaProbs;

        _suggestionProbs[ACCELEROMETER_SUGGESTION] = _sensorsReader.isAccelerometerAvailable() ? (sensorsProbs) : (0.0);
        _suggestionProbs[GYROSCOPE_SUGGESTION] = _sensorsReader.isGyroscopeAvailable() ? (sensorsProbs) : (0.0);
        _suggestionProbs[LUX_SUGGESTION] = _sensorsReader.isPhotoresistorAvailable() ? (sensorsProbs) : (0.0);
        _suggestionProbs[TEMPERATURE_SUGGESTION] = _sensorsReader.isThermometerAvailable() ? (sensorsProbs) : (0.0);
        _suggestionProbs[PICTURE_SUGGESTION] = mediaProbs;
        _suggestionProbs[AUDIO_SUGGESTION] = mediaProbs;
    }

    // ESEGUIRE IN LOOP PER GESTIRE L' ASSENZA DI INFO PER LA SUGGESTION RICHIESTA
    public Suggestion createRandomSuggestion(Device treasure) {
        int type = getRandomSuggestionType();
        boolean skip = true;

        Suggestion suggestion = null;
        while ( skip ) {
           analizeSensors(treasure);               // Setta i valori sull' hunter ma non vengono usati. FORSE INUTILE!

           if (type == ACCELEROMETER_SUGGESTION) {
               suggestion = new Suggestion("Accelerometer da fare", 0.0, type);
               skip = false;
           }

           else if (type == GYROSCOPE_SUGGESTION) {
               suggestion = new Suggestion("Gyroscope", 0.0, type);
               skip = false;
           }

           else if (type == LUX_SUGGESTION) {
               suggestion = new Suggestion(analizeLuxValues(treasure), 0.0, type);
               skip = false;
           }
           else if (type == TEMPERATURE_SUGGESTION) {
               suggestion = new Suggestion(analizeTemperatureValues(treasure), 0.0, type);
               skip = false;
           }

           else if (type == PICTURE_SUGGESTION) {
               Media picture;
               try {
                   picture = _webserver.retrievePicture();
                   if (picture == null) {
                       // TODO
                   }
                   Log.d(this.getClass().getName(), "PICTURE RETRIEVED FROM WEBSERVER------------");

                   File f = new File(picture.get_mediaName());
                   if (!f.exists())
                       f.mkdir();
                   FileOutputStream fo = new FileOutputStream(picture.get_mediaName());
                   fo.write(picture.get_data());

                   Log.d(this.getClass().getName(), "PHOTO WROTE ON SMARTPHONE------------");

                   suggestion = new Suggestion(picture.get_mediaName(), 0.0, type);
                   skip = false;

               } catch (Exception e) {
                   Log.w(this.getClass().getName(), e.getMessage());
               }
           }

           else if (type == AUDIO_SUGGESTION) {
               Media audio;
               try {
                   audio = _webserver.retrieveAudio();
                   if (audio == null) {
                       // TODO
                   }
                   Log.d(this.getClass().getName(), "AUDIO RETRIEVED FROM WEBSERVER------------");

                   File f = new File(audio.get_mediaName());
                   if (!f.exists())
                       f.mkdir();
                   FileOutputStream fo = new FileOutputStream(audio.get_mediaName());
                   fo.write(audio.get_data());

                   Log.d(this.getClass().getName(), "AUDIO WROTE ON SMARTPHONE------------");

                   suggestion = new Suggestion(audio.get_mediaName(), 0.0, type);
                   skip = false;

               } catch (Exception e) {
                   Log.w(this.getClass().getName(), e.getMessage());
               }
           }
       }

        return suggestion;
    }

    private int getRandomSuggestionType() {
        Random rndGen = new Random();

        double random = Math.abs(rndGen.nextInt()) / (double) Integer.MAX_VALUE;
        Log.d(this.getClass().getName(), "*********The random number generated is: " + random );

        if (random <= _suggestionProbs[AUDIO_SUGGESTION]) {
            return AUDIO_SUGGESTION;
        }

        random -=  _suggestionProbs[AUDIO_SUGGESTION];

        if (random <= _suggestionProbs[PICTURE_SUGGESTION]) {
            return PICTURE_SUGGESTION;
        }

        random -=  _suggestionProbs[PICTURE_SUGGESTION];

        if (random <= _suggestionProbs[TEMPERATURE_SUGGESTION]) {
            return TEMPERATURE_SUGGESTION;
        }

        random -=  _suggestionProbs[TEMPERATURE_SUGGESTION];

        if (random <= _suggestionProbs[LUX_SUGGESTION]) {
            return LUX_SUGGESTION;
        }

        random -=  _suggestionProbs[LUX_SUGGESTION];

        if (random <= _suggestionProbs[GYROSCOPE_SUGGESTION]) {
            return GYROSCOPE_SUGGESTION;
        }

        random -=  _suggestionProbs[GYROSCOPE_SUGGESTION];

        if (random <= _suggestionProbs[ACCELEROMETER_SUGGESTION]) {
            return ACCELEROMETER_SUGGESTION;
        }

        return -1;
    }

    private void analizeSensors(Device treasure) {
        setDeviceSensors();
        if ( _sensorsReader.isPhotoresistorAvailable() ) {
            analizeLuxValues(treasure);
        }

        if ( _sensorsReader.isThermometerAvailable() ) {
            analizeTemperatureValues(treasure);
        }
    }

    public void setDeviceSensors() {

        float[] acc;
        float[] rot;

        if ( _sensorsReader.isPhotoresistorAvailable() ) {
            _hunter.setLuminosity(_sensorsReader.getLuminosity());
        } else _hunter.setLuminosity(-Float.MAX_VALUE);

        if ( _sensorsReader.isThermometerAvailable() ) {
            _hunter.setTemperature(_sensorsReader.getTemperature());
        } else _hunter.setTemperature(-Float.MAX_VALUE);

        acc = _sensorsReader.getAcceleration();
        if ( _sensorsReader.isAccelerometerAvailable() && acc != null ) {
            _hunter.setAcceleration(_sensorsReader.getAcceleration());
        } else {
            float[] a = {-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE};
            _hunter.setAcceleration(a);
        }

        rot = _sensorsReader.getRotation();
        if ( _sensorsReader.isGyroscopeAvailable() && rot != null ) {
            _hunter.setRotation(_sensorsReader.getRotation());
        } else {
            float[] r = {-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE};
            _hunter.setRotation(r);
        }
    }

    public String analizeLuxValues(Device treasure) {
        double t_threshold;
        double t_lux = treasure.getLuminosity();
        if ( t_lux >= (t_threshold = SensorsReader.LUX_JOURNEY_ON_THE_SUN_THRESHOLD) )
            Log.d(this.getClass().getName(), "Pija lo shuttle");
        else if ( t_lux >= (t_threshold = SensorsReader.LUX_DAYLIGHT_THRESHOLD) )
            Log.d(this.getClass().getName(), "Ben illuminato");
        else if ( t_lux >= (t_threshold = SensorsReader.LUX_TWILIGHT_THRESHOLD) )
            Log.d(this.getClass().getName(), "Non molto illuminato");
        else if ( t_lux >= (t_threshold = SensorsReader.LUX_DARK_THRESHOLD) )
            Log.d(this.getClass().getName(), "Scuro zi");

        double h_lux = _hunter.getLuminosity();
        double h_threshold = SensorsReader.getLuxThreshold(h_lux);
        String msg;
        if ( h_threshold < t_threshold )
            msg = "Seems like the treasure is in a brighter place than you!";
        else if ( h_threshold > t_threshold )
            msg = "Seems like the treasure is in a darker place than you!";
        else  msg =  "Seems like you and the treasure are in a place with the same brightness!";

        return msg;

    }

    public String analizeTemperatureValues(Device treasure) {
        double t_temp = treasure.getTemperature();
        double h_temp = _hunter.getTemperature();
        double deltaT = t_temp - h_temp;

        String msg;
        if(deltaT < 0)
            msg = "Seems like treasure temperature is "+Math.abs(deltaT)+" degrees lower than you";
        else if(deltaT > 0)
            msg = "Seems like treasure temperature is "+Math.abs(deltaT)+" degrees higher than you";
        else
            msg = "Seems like treasure temperature is equal to yours";

        return msg;
    }
}
