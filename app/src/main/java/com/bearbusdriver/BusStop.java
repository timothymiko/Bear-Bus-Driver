package com.bearbusdriver;

/**
 * Created by timothymiko on 4/9/14.
 */
public class BusStop {

    public String id;
    public String name;
    public double latitude;
    public double longitude;

    public BusStop(String id, String name, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "ID: " + this.id + " Name: " + this.name + " Location: " + latitude + ", " + longitude;
    }
}
