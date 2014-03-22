package com.bearbusdriver;


import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

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
    private ImageView statusDrawable;

    private Handler mHandler;
    private Timer timer;

    private ParseQuery<ParseObject> query;

    private boolean isTransmittingLocation;

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
        isTransmittingLocation = true;
        mHandler = new Handler();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (timer == null) {
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
                            query = ParseQuery.getQuery("Bus");
                            ParseObject object = query.get(bus.id);
                            ParseGeoPoint geoPoint = new ParseGeoPoint(latitude, longitude);
                            object.put("location", geoPoint);
                            object.save();
                            isTransmittingLocation = true;
                        } catch (ParseException e) {
                            e.printStackTrace();
                            isTransmittingLocation = false;
                        }

                        updateStatusDrawable();
                    } else {
                        isTransmittingLocation = false;
                        updateStatusDrawable();
                    }
                }
            }, 0, GPS_UPDATE_INTERVAL);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRoot = inflater.inflate(R.layout.fragment_main, container, false);

        statusDrawable = (ImageView) mRoot.findViewById(R.id.status_image);
        updateStatusDrawable();

        return mRoot;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
        timer.purge();
    }

    private void updateStatusDrawable() {
        mHandler.post(updateStatusImage);
    }

    final Runnable updateStatusImage = new Runnable() {
        @Override
        public void run() {
            Drawable drawable = getActivity().getResources().getDrawable(R.drawable.colored_circle);
            if (isTransmittingLocation)
                drawable.setColorFilter(0xff00ff66  , PorterDuff.Mode.SRC_ATOP);
            else
                drawable.setColorFilter(0xffcc0000, PorterDuff.Mode.SRC_ATOP);
            statusDrawable.setImageDrawable(drawable);
        }
    };
}
