package com.pervasive.sth.smarttreasurehunt;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.pervasive.sth.distances.BluetoothTracker;
import com.pervasive.sth.distances.GPSTracker;
import com.pervasive.sth.entities.Suggestion;
import com.pervasive.sth.entities.SuggestionsGenerator;
import com.pervasive.sth.exceptions.BluetoothCriticalException;
import com.pervasive.sth.exceptions.DeviceSensorCriticalException;
import com.pervasive.sth.exceptions.InvalidRESTClientParametersException;
import com.pervasive.sth.tasks.HunterDistanceTask;
import com.pervasive.sth.tasks.HunterTask;
import com.pervasive.sth.entities.Device;

import java.io.File;
import java.io.IOException;
import java.util.Random;

public class HunterActivity extends AppCompatActivity {

    private final String LOG_TAG = HunterActivity.class.getName();

    /*
     * Intent sent by HunterTask to notify that there is a winner
     */
    private static final int WINNER_REQUEST_CODE = 1;

    /*
     * Action codes for the interaction between the HunterActivity and the HunterTask
     */
    public static final String EXIT_ACTION = "com.pervasive.sth.smarttreasurehunt.EXIT_GAME";
    public static final String GPS_ACTION = "com.pervasive.sth.smarttreasurehunt.GPS_UPDATE";
    public static final String AUDIO_ACTION = "com.pervasive.sth.smarttreasurehunt.AUDIO_UPDATE";
    public static final String PICTURE_ACTION = "com.pervasive.sth.smarttreasurehunt.PICTURE_UPDATE";
    public static final String SUGGESTION_ACTION = "com.pervasive.sth.smarttreasurehunt.SUGGESTION_UPDATE";
    public static final String WINNER_ACTION = "com.pervasive.sth.smarttreasurehunt.WINNER_UPDATE";
    public static final String EXCEPTION_ACTION = "com.pervasive.sth.smarttreasurehunt.EXCEPTION_ACTION";

    /*
     * Gps handler for distance computations
     */
    private GPSTracker _gps;

    /*
     * Bluetooth handler for distance computations
     */
    private BluetoothTracker _bluetooth;

    /*
     * Asynchronous task that deals with sensors of the Hunter device
     */
    private HunterTask _task;

    /*
     * Asynchronous task that deals with distance computations of the Hunter device
     */
    private HunterDistanceTask _distance;

    /*
     * Hunter Device
     */
    private Device _hunter;

    /*
     * Progress bar for GPS distance computation:
     */
    private ProgressBar gpsProgressBar;

    /*
     * Progress bar for Bluetooth distance computation: [0-20] meters
     */
    private ProgressBar BLProgressBar;

    /*
     * Storage path for audio and picture retrieved from webserver
     */
    private String _audioPath, _picturePath;

    /*
     * Luminosity suggestion message received from the HunterTask
     */
    private String _luxMessageReceived;

    /*
     * Temperature suggestion message received from the HunterTask
     */
    private String _temperatureMessageReceived;

    /*
     * Acceleration suggestion message received from the HunterTask
     */
    private String _accelerationMessageReceived;


    /*
     * Animated Font
     */
    private Typewriter textualSuggestion;

    /*
     * flag for intent already registered in the broadcast receiver
     */
    private boolean _receiverRegistered;

    /*
     * GUI components
     */
    private ImageView satellite;
    private ImageView radar;
    private ImageView photoSuggestion;
    private ImageView _photoButton;
    private ImageView _audioButton;
    private ImageView _luxButton;
    private ImageView _temperatureButton;
    private ImageView _accelerometerButton;

	/**
	 * @brief initialization of Hunter activity components
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_hunter);

        satellite = (ImageView) this.findViewById(R.id.satellite_pic);
        satellite.setImageResource(R.drawable.app_th_iconsatellite);

        radar = (ImageView) this.findViewById(R.id.radar_pic);
        radar.setImageResource(R.drawable.app_th_iconradar);

        gpsProgressBar = (ProgressBar) this.findViewById(R.id.gps_progress_bar);
        BLProgressBar = (ProgressBar) this.findViewById(R.id.bl_progress_bar);

        gpsProgressBar.getProgressDrawable().setColorFilter(Color.rgb(30,122,186), android.graphics.PorterDuff.Mode.SRC_IN);
        BLProgressBar.getProgressDrawable().setColorFilter(Color.rgb(51,177,71), android.graphics.PorterDuff.Mode.SRC_IN);

        gpsProgressBar.setScaleY(10f);
        BLProgressBar.setScaleY(10f);

        gpsProgressBar.setProgress(0);
        BLProgressBar.setProgress(0);

        photoSuggestion = (ImageView) this.findViewById(R.id.photo_suggestion);
        textualSuggestion = (Typewriter) this.findViewById(R.id.textual_suggestion);

        Typeface type = Typeface.createFromAsset(getAssets(),"fonts/TravelingTypewriter.ttf");
        textualSuggestion.setTypeface(type);
        textualSuggestion.setTextSize(20);
        textualSuggestion.setCharacterDelay(50);

        _photoButton = (ImageButton) findViewById(R.id.photo_button);
        _photoButton.setImageResource(R.drawable.appth_photo_off);
        _photoButton.setEnabled(false);

        _audioButton = (ImageButton) findViewById(R.id.audio_button);
        _audioButton.setImageResource(R.drawable.appth_sound_off);
        _audioButton.setEnabled(false);

        _luxButton = (ImageButton) findViewById(R.id.lux_button);
        _luxButton.setImageResource(R.drawable.appth_light_off);
        _luxButton.setEnabled(false);

        _temperatureButton = (ImageButton) findViewById(R.id.temperature_button);
        _temperatureButton.setImageResource(R.drawable.appth_temp_off);
        _temperatureButton.setEnabled(false);

        _accelerometerButton = (ImageButton) findViewById(R.id.accelerometer_button);
        _accelerometerButton.setImageResource(R.drawable.appth_movement_off);
        _accelerometerButton.setEnabled(false);
        _accelerometerButton.setClickable(false);

        _gps = new GPSTracker(this);
    }

    /*
     * Initialization on Hunter activity while resumed
     */
    protected void onResume() {
        Log.d(LOG_TAG, "onResume() invoked.");
        super.onResume();

		try {
			_bluetooth = new BluetoothTracker();
		} catch (BluetoothCriticalException e) {
			Log.e(LOG_TAG, e.toString());
			showErrorDialog("An internal error occurs:\n" + e.getMessage());
		}

		try {
			_gps.getLocation();
		} catch ( Exception e ) {
			Log.e(LOG_TAG, e.toString());
			showErrorDialog("An internal error occurs:\n" + e.getMessage());
		}

		_receiverRegistered = false;

		try {
			_hunter = new Device(BluetoothAdapter.getDefaultAdapter().getAddress(), BluetoothAdapter.getDefaultAdapter().getName(), "H");
		} catch (Exception e) {
			Log.e(LOG_TAG, e.toString());
			showErrorDialog("An internal error occurs:\n" + e.getMessage());
		}

        if ( !_receiverRegistered ) {

            // Register for broadcasts when a device is discovered
            registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));

            // Register for broadcasts when discovery has finished
            registerReceiver(receiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));

            // Register for broadcast when discovery has started
            registerReceiver( receiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));

            // Register for broadcast when the game is ended
            registerReceiver ( receiver, new IntentFilter(EXIT_ACTION));

            // Register for broadcasts when GPS distance is updated
            registerReceiver ( receiver, new IntentFilter(GPS_ACTION));

            // Register for broadcasts when audio file is received
            registerReceiver( receiver, new IntentFilter(AUDIO_ACTION));

            // Register for broadcasts when picture file is received
            registerReceiver( receiver, new IntentFilter(PICTURE_ACTION));

            // Register for broadcasts when a suggestions is received
            registerReceiver( receiver, new IntentFilter(SUGGESTION_ACTION));

            // Register for broadcasts when a winner is declared
            registerReceiver(receiver, new IntentFilter(WINNER_ACTION));

            // Register for broadcasts when a winner is declared
            registerReceiver(receiver, new IntentFilter(EXCEPTION_ACTION));

            _receiverRegistered = true;
        }

        // Start tasks
        Log.d(LOG_TAG, "Starting Hunter Task");
        try {
            if ( _task == null || _task.isCancelled() ) {
                _task = new HunterTask(this, _hunter);
                _task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }

            Log.d(LOG_TAG, "Starting Distance Task");
            if ( _distance == null || _distance.isCancelled() ) {
                _distance = new HunterDistanceTask(this, _gps, _bluetooth, _hunter);
                _distance.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        } catch ( InvalidRESTClientParametersException e) {
            Log.e(LOG_TAG, e.toString());
			showErrorDialog("An internal error occurs:\n" + e.getMessage());
        } catch ( DeviceSensorCriticalException e ) {
            Log.e(LOG_TAG, e.toString());
			showErrorDialog("An internal error occurs:\n" + e.getMessage());
        }
    }

	/**
     * @brief stop tasks and unregister intent actions
     */
    protected void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "onPause() invoked.");

        if ( _receiverRegistered ) {
            unregisterReceiver(receiver);
            _receiverRegistered = false;
        }

        // Stop treasure task
        if ( _task != null && !_task.isCancelled() )
            _task.cancel(false);
        if ( _distance != null && !_distance.isCancelled() )
            _distance.cancel(false);
    }

    protected void onStop() {
        super.onStop();
    }

    protected void onDestroy() {
        super.onDestroy();
    }


    private void showErrorDialog(String message) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Internal Error");
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        alertDialog.show();
    }


	/**
     * @brief lux button handler
     */
    public void onLuxButtonClick(View v) {

        _luxButton.setEnabled(false);
        _luxButton.setClickable(false);
        _luxButton.setImageResource(R.drawable.appth_light_off);

        textualSuggestion.setText(_luxMessageReceived);
        photoSuggestion.setVisibility(View.INVISIBLE);
        textualSuggestion.setVisibility(View.VISIBLE);
        textualSuggestion.animateText(_luxMessageReceived);
    }

    /**
     * @brief Temperature button handler
     */
    public void onTemperatureButtonClick(View v) {
        _temperatureButton.setEnabled(false);
        _temperatureButton.setClickable(false);
        _temperatureButton.setImageResource(R.drawable.appth_temp_off);

        textualSuggestion.setText(_temperatureMessageReceived);
        photoSuggestion.setVisibility(View.INVISIBLE);
        textualSuggestion.setVisibility(View.VISIBLE);
        textualSuggestion.animateText(_temperatureMessageReceived);
    }

    /**
     * @brief Accelerometer button handler
     */
    public void onAccelerometerButtonClick(View v) {
        _accelerometerButton.setEnabled(false);
        _accelerometerButton.setClickable(false);
        _accelerometerButton.setImageResource(R.drawable.appth_movement_off);

        textualSuggestion.setText(_accelerationMessageReceived);
        photoSuggestion.setVisibility(View.INVISIBLE);
        textualSuggestion.setVisibility(View.VISIBLE);
        textualSuggestion.animateText(_accelerationMessageReceived);
    }

    /**
     * @brief Audio button handler
     */
    public void onAudioButtonClick(View v) {

        _audioButton.setEnabled(false);
        _audioButton.setClickable(false);
        _audioButton.setImageResource(R.drawable.appth_sound_off);

        MediaPlayer mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(_audioPath);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(this.getClass().getName(), "media player prepare() failed");
        }
    }

    /**
     * @brief Picture button handler
     */
    public void onPicButtonClick(View v) {

        _photoButton.setEnabled(false);
        _photoButton.setClickable(false);
        _photoButton.setImageResource(R.drawable.appth_photo_off);

        textualSuggestion.setVisibility(View.INVISIBLE);
        Log.d(this.getClass().getName(), "*************" + _picturePath);

        File picFile = new File(_picturePath);

        if (picFile.exists()) {
            Bitmap bmap = BitmapFactory.decodeFile(picFile.getAbsolutePath());
            Matrix rotation = new Matrix();
            rotation.postRotate((float) -90.0);
            Bitmap bmapRotated = Bitmap.createBitmap(bmap, 0, 0, bmap.getWidth(), bmap.getHeight(), rotation, true);
            photoSuggestion.setImageBitmap(bmapRotated);
        }


        photoSuggestion.setVisibility(View.VISIBLE);
    }

    /**
     * @brief Update the bluetooth progress bar
     */
    public void updateBLProximityBars(double dist) {
        int progress = 100 - ((int) Math.round((dist * 100.0 / 20.0)));
        if(android.os.Build.VERSION.SDK_INT >= 11){
            // will update the "progress" propriety of seekbar until it reaches progress
            ObjectAnimator animation = ObjectAnimator.ofInt(BLProgressBar, "progress", progress);
            animation.setDuration(500); // 0.5 second
            animation.setInterpolator(new DecelerateInterpolator());
            animation.start();
        }
        else
            BLProgressBar.setProgress(progress);
    }

    /**
     * @brief Update the GPS progress bar
     */
    public void updateGPSProximityBars(double dist) {
        int progress = 100 - ((int) Math.round((dist * 100.0 / 100.0)));
        if(android.os.Build.VERSION.SDK_INT >= 11){
            // will update the "progress" propriety of seekbar until it reaches progress
            ObjectAnimator animation = ObjectAnimator.ofInt(gpsProgressBar, "progress", progress);
            animation.setDuration(500); // 0.5 second
            animation.setInterpolator(new DecelerateInterpolator());
            animation.start();
        }
        else
            gpsProgressBar.setProgress(progress);
    }


    /**
     * @brief Close the activity when a winner is declared
     */
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        if (reqCode == WINNER_REQUEST_CODE) {
            finish();
        }
    }


    /**
     * @brief Handles the incoming intents
     */
    private final BroadcastReceiver receiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {

            String mIntentAction = intent.getAction();

            // Handle the bluetooth distance update
            if(mIntentAction.equals(BluetoothDevice.ACTION_FOUND)) {
                int RSSI = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceAddress = device.getAddress();
                Log.d("Receiver", "Received ID: " + deviceAddress + " TreasureID " + _task.getTreasureID());
                if( deviceAddress.equals(_task.getTreasureID()) ) {
                    updateBLProximityBars(BluetoothTracker.calculateDistance(RSSI));
                    //Toast.makeText(context, "BLUETOOTH: " + deviceName + " : " + RSSI + " (dBm) ->" + BluetoothTracker.calculateDistance(RSSI) + " m", Toast.LENGTH_LONG).show();
                    //"\nGPS Distance: " + _task.getDistance(), Toast.LENGTH_LONG).show();
                }
            }

            // Handle the exit intent request
            else if(mIntentAction.equals(EXIT_ACTION)) {
                    if ( intent.getBooleanExtra("EXIT_GAME", false) ) {
                        finish();
                    }
            }

            // Handle the GPS distance update
            else if(mIntentAction.equals(GPS_ACTION)) {
                updateGPSProximityBars(intent.getDoubleExtra("GPS_DISTANCE", 0.0));
               // Toast.makeText(context, "GPS Distance: " + intent.getDoubleExtra("GPS_DISTANCE", 0.0), Toast.LENGTH_LONG).show();
            }

            // Handle the winner intent request, sending the intent to the winner activity
            else if (mIntentAction.equals(WINNER_ACTION)) {
                String pathName = intent.getStringExtra("WINNER_UPDATE");
                Intent winnerIntent = new Intent(context, TreasureCaught.class);
                winnerIntent.putExtra("WINNER_PICTURE", pathName);
                startActivityForResult( winnerIntent, WINNER_REQUEST_CODE);

            }

            // Handle the suggestion received action
            else if ( mIntentAction.equals(SUGGESTION_ACTION)) {
                Suggestion received = (Suggestion)(intent.getSerializableExtra("SUGGESTION"));
                Log.d(LOG_TAG, "TYPE: " + received.getType());

                if( received.getType() == SuggestionsGenerator.LUX_SUGGESTION) {
                    _luxMessageReceived = received.getMessage();
                    _luxButton.setEnabled(true);
                    _luxButton.setClickable(true);
                    _luxButton.setImageResource(R.drawable.appth_light_on);

                } else if (received.getType() == SuggestionsGenerator.TEMPERATURE_SUGGESTION ) {
                    _temperatureMessageReceived = received.getMessage();
                    _temperatureButton.setEnabled(true);
                    _temperatureButton.setClickable(true);
                    _temperatureButton.setImageResource(R.drawable.appth_temp_on);

                } else if ( received.getType() == SuggestionsGenerator.PICTURE_SUGGESTION) {
                    Log.d(LOG_TAG, "Media received");
                    Log.d(LOG_TAG, received.getMessage());
                    _picturePath = received.getMessage();
                    _photoButton.setEnabled(true);
                    _photoButton.setClickable(true);
                    _photoButton.setImageResource(R.drawable.appth_photo_on);

                } else if ( received.getType() == SuggestionsGenerator.AUDIO_SUGGESTION){
                    Log.d(this.getClass().getName(), "Media received");
                    _audioPath = received.getMessage();
                    _audioButton.setEnabled(true);
                    _audioButton.setClickable(true);
                    _audioButton.setImageResource(R.drawable.appth_sound_on);

                } else if ( received.getType() == SuggestionsGenerator.ACCELEROMETER_SUGGESTION) {
                    _accelerationMessageReceived = received.getMessage();
                    _accelerometerButton.setEnabled(true);
                    _accelerometerButton.setClickable(true);
                    _accelerometerButton.setImageResource(R.drawable.appth_movement_on);
                }
            } else if ( mIntentAction.equals(EXCEPTION_ACTION) ) {
                Log.e(LOG_TAG, intent.getStringExtra(TreasureActivity.EXCEPTION_NAME));
                showErrorDialog(intent.getStringExtra(TreasureActivity.EXCEPTION_NAME));
            }
        }
    };




}

/**
 * Font Animation Thread
 */
class Typewriter extends TextView {

    private CharSequence mText;
    private int mIndex;
    private long mDelay = 500; //Default 500ms delay


    public Typewriter(Context context) {
        super(context);
    }

    public Typewriter(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private Handler mHandler = new Handler();
    private Runnable characterAdder = new Runnable() {
        @Override
        public void run() {
            setText(mText.subSequence(0, mIndex++));
            if(mIndex <= mText.length()) {
                mHandler.postDelayed(characterAdder, mDelay);
            }
        }
    };

    public void animateText(CharSequence text) {
        mText = text;
        mIndex = 0;

        setText("");
        mHandler.removeCallbacks(characterAdder);
        mHandler.postDelayed(characterAdder, mDelay);
    }

    public void setCharacterDelay(long millis) {
        mDelay = millis;
    }
}