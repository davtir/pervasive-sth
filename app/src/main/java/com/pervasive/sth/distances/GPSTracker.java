package com.pervasive.sth.distances;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;

/**
 * @brief	This class implements the GPS funtionalities for distance computation
 */
public class GPSTracker extends Service implements LocationListener {

	private final String LOG_TAG = GPSTracker.class.getName();

	/*
	 * The parent activity environment
	 */
	Context _context;

	/*
	 * Criteria used to define GPS accuracy
	 */
	Criteria _mFineCriteria;

	/*
	 * Variables used to understand the level of GPS accuracy set by the user
	 */
	boolean _isGPSEnabled = false;
	boolean _isNetworkEnabled = false;

	double _latitude;
	double _longitude;

	/*
	 * Minimum distance required to update GPS latitude and longitude
	 */
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0;

	/*
	 * Minimum time required to update GPS latitude and longitude
	 */
	private static final long MIN_TIME_BW_UPDRATES = 10000;

	/*
	 * Android location manager used to get the GPS location utilities
	 */
	protected LocationManager _locationManager;

	/**
	 *
	 * @param context
	 * @brief	Initialize class fields
	 */
	public GPSTracker(Context context) {
		_context = context;
		_mFineCriteria = new Criteria();
		_mFineCriteria.setAccuracy(Criteria.ACCURACY_FINE);
		_mFineCriteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
		_mFineCriteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);
		_mFineCriteria.setBearingAccuracy(Criteria.ACCURACY_HIGH);
		_mFineCriteria.setPowerRequirement(Criteria.ACCURACY_HIGH);
		_mFineCriteria.setAltitudeRequired(true);
		_mFineCriteria.setBearingRequired(true);
	}

	/**
	 * @brief	Return true if the device is reachable by the GPS signal,
	 * 			false otherwise
	 */
	public boolean isReacheable() {
		_locationManager = (LocationManager) _context.getSystemService(LOCATION_SERVICE);
		_isGPSEnabled = _locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		_isNetworkEnabled = _locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

		return (_isGPSEnabled || _isNetworkEnabled);
	}

	/**
	 * @brief	This function requests to the system the coordinates values of the current device position
	 */
	public void getLocation() throws Exception {

		// This is for Android API >0 =23
		if (Build.VERSION.SDK_INT >= 23 &&
				ContextCompat.checkSelfPermission(_context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
				ContextCompat.checkSelfPermission(_context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
			return;

		// Acquire a reference to the system Location Manager
		_locationManager = (LocationManager) _context.getSystemService(LOCATION_SERVICE);

		_isGPSEnabled = _locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		_isNetworkEnabled = _locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

		// If GPS is enabled then get location update
		if ( _isNetworkEnabled || _isGPSEnabled ) {
			_locationManager.requestLocationUpdates(MIN_TIME_BW_UPDRATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, _mFineCriteria, this, null);
		}

		Log.d(LOG_TAG, "getLocation done");
	}

	@Override
	public void onLocationChanged(Location location) {
		_latitude = location.getLatitude();
		_longitude = location.getLongitude();
	}

	public double getLatitude() {
		return _latitude;
	}

	public double getLongitude() {
		return _longitude;
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

	/**
	 *
	 * @param from_lat:	Treasure latitude
	 * @param from_lon:	Treasure longitude
	 * @return	Returns the distance computed between hunter and treasure coordinates.
	 * @brief	This function returns the distance computed between hunter and treasure coordinates.
	 */
	public double gpsDistance(double from_lat, double from_lon) {
		double earthRadius = 6371000.0; //meters
		double Lat1 = Math.toRadians(from_lat);
		double Lat2 = Math.toRadians(_latitude);
		double dLat = Math.toRadians(from_lat - _latitude);
		double dLng = Math.toRadians(from_lon - _longitude);
		double a = Math.sin(dLat / 2.0) * Math.sin(dLat / 2.0) +
				Math.cos(Lat1) * Math.cos(Lat2) *
						Math.sin(dLng / 2.0) * Math.sin(dLng / 2.0);
		double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double dist = (double) (earthRadius * c);
		Log.d(LOG_TAG, "T_lat/lon = (" + from_lat + "," + from_lon + ")" + "H_Lat/Lon = (" + _latitude + "," + _longitude + ")");
		Log.d(LOG_TAG, "Computed distance = " + dist);

		return dist;
	}
}