package com.bearbusdriver;


import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ListView;

import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 *
 */
public class MainFragment extends Fragment {

    public static final String TAG = "mainFragment";

    public static final String ARG_BUS_INDEX = "busIndex";
    private static final int GPS_UPDATE_INTERVAL = 2500;
    private static final int REQUEST_UPDATE_INTERVAL = 5000;

    private GPSTracker gps;

    private Bus bus;

    private View mRoot;
    private ListView list;
    private View listHeader;
    private ArrayList<Request> currentRequests = new ArrayList<Request>();
    private RequestsAdapter adapter;
    private ImageView statusDrawable;

    private Handler mHandler;
    private Timer timer;

    private ParseQuery<ParseObject> query;

    private boolean isTransmittingLocation;

    private int status;
    private Request request;

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

            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    try {
                        ParseQuery query = ParseQuery.getQuery("Request");
                        query.whereEqualTo("targetBusID", ParseApplication.CURRENT_BUS_ID);

                        List<ParseObject> parseObjects = query.find();
                        currentRequests.clear();

                        ParseObject object;
                        for ( int i = 0; i < parseObjects.size(); i++ ) {
                            object = parseObjects.get(i);

                            if (object.getInt("status") != 1 && object.getInt("status") != -1) {
                                Request request = new Request();
                                request.id = object.getObjectId();
                                Log.d("request", "ID: " + request.id);

                                request.pickupLocation = ParseApplication.stops.get(object.getString("pickupLocation")).name;
                                request.dropoffLocation = ParseApplication.stops.get(object.getString("dropoffLocation")).name;

                                request.approved = false;
                                request.status = object.getInt("status");

                                boolean doesAlreadyExist = false;
                                for (int j = 0; j < currentRequests.size(); j++)
                                    doesAlreadyExist = currentRequests.get(j).id == request.id;

                                if (!doesAlreadyExist)
                                    currentRequests.add(request);
                            }
                        }

                        updateAdapter();

                    } catch (ParseException e) {

                    }
                }
            }, 0, REQUEST_UPDATE_INTERVAL);
        }

        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {
                statusDrawable.setColorFilter(0xff00ff66, PorterDuff.Mode.SRC_ATOP);
            }

            @Override
            public void onProviderDisabled(String provider) {
                statusDrawable.setColorFilter(0xffcc0000, PorterDuff.Mode.SRC_ATOP);
            }
        };
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRoot = inflater.inflate(R.layout.fragment_main, container, false);

        list = (ListView) mRoot.findViewById(R.id.requests_list);

        listHeader = View.inflate(getActivity(), R.layout.requests_header, null);
        statusDrawable = (ImageView) listHeader.findViewById(R.id.status_image);

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(statusDrawable, "alpha", 0f, 1f);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        fadeIn.setDuration(2500l);

        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(statusDrawable, "alpha", 1f, 0f);
        fadeOut.setInterpolator(new DecelerateInterpolator());
        fadeOut.setDuration(2500l);

        ObjectAnimator growX = ObjectAnimator.ofFloat(statusDrawable, "scaleX", 0.75f, 1f);
        growX.setDuration(2500l);
        growX.setInterpolator(new DecelerateInterpolator());

        ObjectAnimator growY = ObjectAnimator.ofFloat(statusDrawable, "scaleY", 0.75f, 1f);
        growY.setDuration(2500l);
        growY.setInterpolator(new DecelerateInterpolator());

        ObjectAnimator shrinkX = ObjectAnimator.ofFloat(statusDrawable, "scaleX", 1f, 0.75f);
        shrinkX.setDuration(2500l);
        shrinkX.setInterpolator(new DecelerateInterpolator());

        ObjectAnimator shrinkY = ObjectAnimator.ofFloat(statusDrawable, "scaleY", 1f, 0.75f);
        shrinkY.setDuration(2500l);
        shrinkY.setInterpolator(new DecelerateInterpolator());

        final AnimatorSet fadeGrowIn = new AnimatorSet();
        fadeGrowIn.playTogether(fadeIn, growX, growY);

        final AnimatorSet fadeGrowOut = new AnimatorSet();
        fadeGrowOut.playTogether(fadeOut, shrinkX, shrinkY);
        fadeGrowOut.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                fadeGrowIn.start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        fadeGrowIn.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                fadeGrowOut.start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        fadeGrowIn.start();

        list.addHeaderView(listHeader);

        adapter = new RequestsAdapter(getActivity(), R.layout.request_list_item, currentRequests);
        adapter.setOnApproveDisapproveListener(new RequestsAdapter.OnApproveDissaproveListener() {
            @Override
            public void OnClick(int position, boolean approve) {
                request = currentRequests.get(position);
                status = approve ? 1 : -1;
                currentRequests.remove(position);
                adapter.notifyDataSetChanged();
                new UpdateRequestTask().execute();
            }
        });
        list.setAdapter(adapter);

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
                drawable.setColorFilter(0xff00ff66, PorterDuff.Mode.SRC_ATOP);
            else {
                Log.e(TAG, "Not transmitting location!");
                drawable.setColorFilter(0xffcc0000, PorterDuff.Mode.SRC_ATOP);
            }
            statusDrawable.setImageDrawable(drawable);
        }
    };

    private void updateAdapter() { mHandler.post(updateAdapterRunnable); };

    final Runnable updateAdapterRunnable = new Runnable() {
        @Override
        public void run() {
            adapter.notifyDataSetChanged();
        }
    };


    public class UpdateRequestTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {

            ParseQuery query = ParseQuery.getQuery("Request");
            try {
                ParseObject parseObject = query.get(request.id);
                parseObject.put("status", status);
                Log.d("request", "Status: " + parseObject.get("status"));
                parseObject.save();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }
}
