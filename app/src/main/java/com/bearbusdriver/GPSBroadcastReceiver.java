package com.bearbusdriver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by timothymiko on 4/11/14.
 */
public class GPSBroadcastReceiver extends BroadcastReceiver {

    public interface GPSChangeListener {
        void onEnabled();
        void onDisabled();
    }

    private GPSChangeListener mListener;

    public GPSBroadcastReceiver() {

    }

    public GPSBroadcastReceiver(GPSChangeListener listener) {
        this.mListener = listener;
    }

    public void setGPSChangeListener(GPSChangeListener listener) {
        this.mListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
    }

}
