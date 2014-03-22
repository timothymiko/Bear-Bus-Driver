package com.bearbusdriver;


import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

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

    private int runCount;

    private int selectedBusLine;

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

        if (!ParseApplication.haveInternet(getActivity())) {
            final Handler handler = new Handler();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    runCount++;
                    if (!ParseApplication.haveInternet(getActivity())) {
                        if (runCount > 4) {
                            Toast.makeText(getActivity(), "No Internet. Quiting Application.", Toast.LENGTH_SHORT).show();
                            getActivity().finish();
                        } else {
                            Toast.makeText(getActivity(), "No Internet. Retrying...", Toast.LENGTH_SHORT).show();
                            handler.postDelayed(this, 5000);
                        }
                    }
                }
            });
        }

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Bus");
        query.findInBackground(new FindCallback<ParseObject>() {

            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null) {
                    Bus bus;
                    ParseObject object;
                    ParseGeoPoint geoPoint;
                    spinnerTitles = new String[parseObjects.size()];
                    for (int i = 0; i < parseObjects.size(); i++) {

                        bus = new Bus();
                        object = parseObjects.get(i);
                        geoPoint = object.getParseGeoPoint("location");

                        bus.id = object.getObjectId();
                        bus.name = object.getString("name");
                        if (geoPoint != null) {
                            bus.latitude = geoPoint.getLatitude();
                            bus.longitude = geoPoint.getLongitude();
                        }

                        ParseApplication.activeBusLines.add(bus);

                        spinnerTitles[i] = object.getString("name");
                    }

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
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedBusLine = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        goButton = (Button) mRoot.findViewById(R.id.go_button);
        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                transaction.remove(SetupFragment.this);
                transaction.add(R.id.container, MainFragment.newInstance(selectedBusLine), MainFragment.TAG);
                transaction.commit();
            }
        });

        return mRoot;
    }


}
