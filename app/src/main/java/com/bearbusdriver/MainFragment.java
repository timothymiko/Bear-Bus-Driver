package com.bearbusdriver;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.Timer;
import java.util.TimerTask;


/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 *
 */
public class MainFragment extends Fragment {

    public static final String TAG = "mainFragment";

    public static final String ARG_BUS_INDEX = "busIndex";

    private GPSTracker gps;

    private Bus bus;

    private View mRoot;

    private Timer timer;

    private ParseQuery<ParseObject> query;

    private static final int GPS_UPDATE_INTERVAL = 10000;

    public static MainFragment newInstance(int busIndex) {
        MainFragment frag = new MainFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_BUS_INDEX, busIndex);
        frag.setArguments(args);
        return frag;
    }

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args;
        if (getArguments() != null)
            bus = ParseApplication.activeBusLines.get(getArguments().getInt(ARG_BUS_INDEX));
        gps = new GPSTracker(getActivity());
    }

    @Override
    public void onStart() {
        super.onStart();

        query = ParseQuery.getQuery("Bus");
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (gps.canGetLocation()) {

                    final double latitude = gps.getLatitude();
                    final double longitude = gps.getLongitude();

                    bus.latitude = gps.getLatitude();
                    bus.longitude = gps.getLongitude();

                    try {
                        ParseObject object = query.get(bus.id);
                        ParseGeoPoint geoPoint = new ParseGeoPoint(latitude, longitude);
                        object.put("location", geoPoint);
                        object.save();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                } else {
                    Log.w("updateGPSRunnable", "Error retrieving GPS coordinates");
                }
            }
        }, 0, GPS_UPDATE_INTERVAL);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRoot = inflater.inflate(R.layout.fragment_main, container, false);

        return mRoot;
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
