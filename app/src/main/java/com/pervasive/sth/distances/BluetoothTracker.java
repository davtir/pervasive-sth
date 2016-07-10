package com.pervasive.sth.distances;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;

/**
 * Created by davtir on 30/04/16.
 */
public class BluetoothTracker {

	private BluetoothAdapter _adapter;
	private static final double LIGHT_VELOCITY = 299792458;

	public BluetoothTracker(final Context cnt, BroadcastReceiver receiver) {
		_adapter = BluetoothAdapter.getDefaultAdapter();
		if ( _adapter == null ) {
			throw new RuntimeException("Cannot create Bluetooth adapter");
		}
	}

	public void discover() {
		if ( _adapter.isDiscovering() ) {
			_adapter.cancelDiscovery();
		}
		_adapter.startDiscovery();
	}

	public static double calculateDistance(int rssi) {
		double transmitted_power = 4.0; // dbm
		double received_power = rssi; //dbm
		double path_loss_exp = 2.7;
		double frequency = 2412000000.0; // HZ
		double constant = -10 * Math.log10(4 * Math.PI / LIGHT_VELOCITY); // db
		double fade_margin = 10.0;

		return Math.pow(10, (transmitted_power - received_power - path_loss_exp * 10 * Math.log10(frequency) + (constant) * path_loss_exp - fade_margin) / (10.0 * path_loss_exp));
		//return Math.pow(10.0,((rssi-(-62.16))/-25.0));
	}
}