package com.pervasive.sth.entities;

/**
 * @brief this class represents an Android device
 */
public class Device {

	/*
	 * the unique id of the device in the bluetooth network
	 */
	private final String _bt_address;

	/*
	 * the name of the device
	 */
	private String _name;

	/*
	 * the role of the device
	 */
	private String _role;

	/*
	 * the latitude of the device
	 */
	private double _latitude;

	/*
	 * the longitude of the device
	 */
	private double _longitude;

	/*
	 * the luminosity around the device
	 */
	private double _luminosity;

	/*
	 * the external temperature of the device
	 */
	private double _temperature;

	/*
	 * the acceleration of the carried device
	 */
	private double[] _acceleration;

	/*
	 * true if the treasure has been found, false otherwise
	 */
	private boolean _found;

	/**
	 *
	 * @param bt_address
	 * @param name
	 * @param role
	 * @param lat
	 * @param lon
	 * @param lux
	 * @param temp
	 * @param acc
	 * @throws Exception
	 * @brief inizialite the device fields
	 */
	public Device(String bt_address, String name, String role, double lat, double lon, double lux, double temp, double[] acc) throws Exception{
		if (bt_address == null || name == null) {
			throw new Exception("Invalid bt address or name.");
		}

		_bt_address = bt_address;
		_name = name;
		_role = role;
		_latitude = lat;
		_longitude = lon;
		_luminosity = lux;
		_temperature = temp;
		if (acc == null)
			_acceleration = new double[3];
		else _acceleration = acc;
		_found = false;
	}

	/**
	 *
	 * @param bt_address
	 * @param name
	 * @param role
	 * @throws Exception
	 * @brief inizialite the device fields
	 */
	public Device(String bt_address, String name, String role) throws Exception{
		if (bt_address == null || name == null) {
			throw new Exception("Invalid bt address or name.");
		}
		_bt_address = bt_address;
		_name = name;
		_role = role;
		_latitude = 0.0;
		_longitude = 0.0;
		_luminosity = 0.0;
		_temperature = 0.0;
		_acceleration = new double[3];
	}

	public double getLatitude() {
		return _latitude;
	}

	public double getLongitude() {
		return _longitude;
	}

	public double getLuminosity() {
		return _luminosity;
	}

	public double getTemperature() {
		return _temperature;
	}

	public double[] getAcceleration() {
		return _acceleration;
	}

	public void setLatitude(double lat) {
		_latitude = lat;
	}

	public void setLongitude(double lon) {
		_longitude = lon;
	}

	public void setLuminosity(double lux) {
		_luminosity = lux;
	}

	public void setTemperature(double temp) {
		_temperature = temp;
	}

	public void setAcceleration(float[] acc) throws Exception{
		if (acc == null || acc.length != 3)
			throw new Exception("Invalid acceleration array length");
		_acceleration[0] = acc[0];
		_acceleration[1] = acc[1];
		_acceleration[2] = acc[2];
	}

	public String getBtAddress() {
		return _bt_address;
	}

	public String getName() {
		return _name;
	}

	public String getRole() {
		return _role;
	}

	public boolean isFound() {
		return _found;
	}

	public void setFound(boolean value) {
		_found = value;
	}

	@Override
	public String toString() {
		return "Device{" + "_bt_address=" + _bt_address + ", _name=" + _name + ", _role=" + _role + ", _latitude=" + _latitude + ", _longitude=" + _longitude + ", _luminosity=" + _luminosity + ", _temperature=" + _temperature + ", _acceleration=" + _acceleration + ", _found =" + _found + '}';
	}


}