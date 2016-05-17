package com.pervasive.sth.smarttreasurehunt;

import android.app.AlertDialog;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

public class StartupActivity extends AppCompatActivity {

    boolean gps_enabled;
    boolean bluetooth_enabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("StartupActivity", "StartupActivity launched.");

        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //set content view AFTER ABOVE sequence (to avoid crash)
        setContentView(R.layout.activity_startup);

        TextView blinkingText = (TextView) findViewById(R.id.blinking );
        if ( blinkingText != null ) {
            Animation anim = new AlphaAnimation(0.0f, 1.0f);
            anim.setDuration(1000); //You can manage the blinking time with this parameter
            anim.setStartOffset(20);
            anim.setRepeatMode(Animation.REVERSE);
            anim.setRepeatCount(Animation.INFINITE);
            blinkingText.startAnimation(anim);
        }

        LocationManager gps_manager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        BluetoothManager ble_manager = (BluetoothManager) this.getSystemService(BLUETOOTH_SERVICE);
        gps_enabled = gps_manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        bluetooth_enabled = ble_manager.getAdapter().isEnabled();

        if ( !bluetooth_enabled ) {
            getBluetoothPermissions(this);
            bluetooth_enabled = ble_manager.getAdapter().isEnabled();
        }

        if ( !gps_enabled ) {
            getGPSPermissions(this);
            gps_enabled = gps_manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }

        //if ( !bluetooth_enabled || !gps_enabled )
         //   finish();
    }

    public void getGPSPermissions(final Context context) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

        alertDialog.setTitle("GPS is settings");
        alertDialog.setMessage("GPS is not enabled. Do you want to go on settings menu?");
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(intent);
            }
        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog.show();
    }

    public void getBluetoothPermissions(final Context context) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

        alertDialog.setTitle("Bluetooth is settings");
        alertDialog.setMessage("Bluetooth is not enabled. Do you want to go on settings menu?");
        alertDialog.setPositiveButton("Activate", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
                context.startActivity(intent);
            }
        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog.show();
    }

    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch ( action ) {
            case MotionEvent.ACTION_DOWN:
                //if ( !bluetooth_enabled || !gps_enabled )
                  // finish();

                startActivity(new Intent(this, MainActivity.class));
                break;
            default:
                return false;
        }

        return true;
    }

}
