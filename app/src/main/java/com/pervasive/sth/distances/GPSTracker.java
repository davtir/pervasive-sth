package com.pervasive.sth.distances;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.pervasive.sth.smarttreasurehunt.HunterActivity;

/**
 * Created by Alex on 30/04/2016.
 */
public class GPSTracker extends Service implements LocationListener {

	Context context;

	Criteria mFineCriteria;

	boolean isGPSEnabled = false;
	boolean isNetworkEnabled = false;

	double latitude;
	double longitude;

	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0;
	private static final long MIN_TIME_BW_UPDRATES = 10000;

	protected LocationManager locationManager;

	public GPSTracker(Context context) {
		this.context = context;
		mFineCriteria = new Criteria();
		mFineCriteria.setAccuracy(Criteria.ACCURACY_FINE);
		mFineCriteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
		mFineCriteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);
		mFineCriteria.setBearingAccuracy(Criteria.ACCURACY_HIGH);
		mFineCriteria.setPowerRequirement(Criteria.ACCURACY_HIGH);
		mFineCriteria.setAltitudeRequired(true);
		mFineCriteria.setBearingRequired(true);
	}

	public boolean isReacheable() {
		locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
		isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

		if (!isGPSEnabled && !isNetworkEnabled) {
			return false;
		} else {
			return true;
		}
	}

	public void getLocation() {

		//If it doesn't work, comment this if
		if (Build.VERSION.SDK_INT >= 23 &&
				ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
				ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
			return;

		try {
			// Acquire a reference to the system Location Manager
			locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);

			isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
			isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);


			if (isNetworkEnabled || isGPSEnabled) {
				locationManager.requestLocationUpdates(MIN_TIME_BW_UPDRATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, mFineCriteria, this, null);
			}

			Log.d("HunterTask", "getLocation done");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		latitude = location.getLatitude();
		longitude = location.getLongitude();
		//Log.d("HunterTask", "LISTENER - Lat: " +  getLatitude() + " Lon: " + getLongitude());
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	@Override
	public void onProviderEnabled(String provider) {

	}

	@Override
	public void onProviderDisabled(String provider) {

	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public double gpsDistance(double from_lat, double from_lon) {
		double earthRadius = 6371000.0; //meters
		/*double dLat = Math.toRadians(from_lat-latitude);
		double dLng = Math.toRadians(from_lon-longitude);
        double a = Math.sin(dLat/2.0) * Math.sin(dLat/2.0) +
                Math.cos(Math.toRadians(latitude)) * Math.cos(Math.toRadians(from_lat)) *
                        Math.sin(dLng/2.0) * Math.sin(dLng/2.0);
        double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double dist = (double) (earthRadius * c);*/
		double Lat1 = Math.toRadians(from_lat);
		double Lat2 = Math.toRadians(latitude);
		double dLat = Math.toRadians(from_lat - latitude);
		double dLng = Math.toRadians(from_lon - longitude);
		double a = Math.sin(dLat / 2.0) * Math.sin(dLat / 2.0) +
				Math.cos(Lat1) * Math.cos(Lat2) *
						Math.sin(dLng / 2.0) * Math.sin(dLng / 2.0);
		double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double dist = (double) (earthRadius * c);
		Log.d("GPSTracker", "T_lat/lon = (" + from_lat + "," + from_lon + ")" + "H_Lat/Lon = (" + latitude + "," + longitude + ")");
		Log.d("GPSTracker", "Distance = " + dist);

		return dist;
	}
}