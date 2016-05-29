/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pervasive.sth.entities;

/**
 *
 * @author Alex
 */
public class Device {
    private final String mac_address_;
    private String name_;
    private String role_;
    private double latitude_;
    private double longitude_;
    private double luminosity_;
    private double temperature_;
    private double[] acceleration_;
    private double[] rotation_;

    public Device(String mac_address, String name, String role, double lat, double lon, double lux, double temp, double[] acc, double[] rot) throws RuntimeException {
        if ( mac_address == null || name == null || acc == null || rot == null)  {
            throw new RuntimeException("Invalid mac address or name or acceleration or rotation.");
        }

        mac_address_ = mac_address;
        name_ = name;
        role_ = role;
        latitude_ = lat;
        longitude_ = lon;
        luminosity_ = lux;
        temperature_ = temp;
        acceleration_ = acc;
        rotation_ = rot;
    }

    public Device(String mac_address, String name, String role) {
        mac_address_ = mac_address;
        name_ = name;
        role_ = role;
        latitude_ = 0.0;
        longitude_ = 0.0;
        luminosity_ = 0.0;
        temperature_ = 0.0;
        acceleration_ = new double[3];
        rotation_ = new double[3];
    }

    public double getLatitude() {
        return latitude_;
    }

    public double getLongitude() {
        return longitude_;
    }

    public double getLuminosity() {
        return luminosity_;
    }

    public double getTemperature() {
        return temperature_;
    }

    public double[] getAcceleration() {
        return acceleration_;
    }

    public double[] getRotation() {
        return rotation_;
    }

    public void setLatitude(double lat) {
        latitude_ = lat;
    }

    public void setLongitude(double lon) {
        longitude_ = lon;
    }

    public void setLuminosity(double lux) {
        luminosity_ = lux;
    }

    public void setTemperature(double temp) {
        temperature_ = temp;
    }

    public void setAcceleration(float[] acc) {
        if ( acc == null || acc.length != 3 )
            throw new RuntimeException("Invalid acceleration array length");
        acceleration_[0] = acc[0];
        acceleration_[1] = acc[1];
        acceleration_[2] = acc[2];
    }

    public void setRotation(float[] rot) {
        if ( rot == null || rot.length != 3 )
            throw new RuntimeException("Invalid acceleration array length");
        rotation_[0] = rot[0];
        rotation_[1] = rot[1];
        rotation_[2] = rot[2];
    }

    public String getMACAddress() {
        return mac_address_;
    }

    public String getName() {
        return name_;
    }

    public String getRole() {
        return role_;
    }

    @Override
    public String toString() {
        return "Device{" + "mac_address_=" + mac_address_ + ", name_=" + name_ + ", role_=" + role_ + ", latitude_=" + latitude_ + ", longitude_=" + longitude_ + ", luminosity_=" + luminosity_ + ", temperature_=" + temperature_ + ", acceleration_=" + acceleration_ + ", rotation_=" + rotation_ + '}';
    }


}