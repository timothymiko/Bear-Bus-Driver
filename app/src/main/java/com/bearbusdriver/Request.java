package com.bearbusdriver;

/**
 * Created by timothymiko on 4/9/14.
 */
public class Request {

    public String id;
    public String pickupLocation;
    public String dropoffLocation;
    public int status;
    public boolean approved;

    public Request() {

    }

    public Request(String id, String pickup, String dropoff, int status) {
        this.id = id;
        this.pickupLocation = pickup;
        this.dropoffLocation = dropoff;
        this.status = status;
        approved = false;
    }
}
