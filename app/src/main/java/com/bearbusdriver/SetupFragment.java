package com.bearbusdriver;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.Arrays;
import java.util.List;


/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Use the {@link SetupFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class SetupFragment extends Fragment {

    private View mRoot;
    private Spinner spinner;
    private String[] spinnerTitles;
    private ArrayAdapter<String> adapter;
    private Button goButton;

    public static SetupFragment newInstance(String param1) {
        SetupFragment fragment = new SetupFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }
    public SetupFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onStart() {
        super.onStart();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Bus");
        query.findInBackground(new FindCallback<ParseObject>() {

            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                Log.d("test", "callback!");
                if (e == null) {
                    Bus bus;
                    ParseObject object;
                    ParseGeoPoint geoPoint;
                    spinnerTitles = new String[parseObjects.size()];
                    Log.d("test", "Parse Objects Size: " + parseObjects.size());
                    for (int i = 0; i < parseObjects.size(); i++) {

                        bus = new Bus();
                        object = parseObjects.get(i);
                        geoPoint = object.getParseGeoPoint("location");

                        bus.id = object.getString("objectId");
                        bus.name = object.getString("name");
                        if (geoPoint != null) {
                            bus.latitude = geoPoint.getLatitude();
                            bus.longitude = geoPoint.getLongitude();
                        }

                        ParseApplication.activeBusLines.add(bus);

                        spinnerTitles[i] = object.getString("name");
                    }

                    Arrays.sort(spinnerTitles);
                    adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, spinnerTitles);
                    spinner.setAdapter(adapter);
                    mRoot.findViewById(R.id.loadingIndicator).setVisibility(View.GONE);
                    spinner.setVisibility(View.VISIBLE);
                    goButton.setVisibility(View.VISIBLE);
                } else {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRoot = inflater.inflate(R.layout.fragment_setup, container, false);

        spinner = (Spinner) mRoot.findViewById(R.id.spinner);

        goButton = (Button) mRoot.findViewById(R.id.go_button);

        return mRoot;
    }


}