package com.bearbusdriver;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by timothymiko on 4/9/14.
 */
public class RequestsAdapter extends ArrayAdapter<Request> {

    private OnApproveDissaproveListener mListener;

    public interface OnApproveDissaproveListener {
        void OnClick(int position, boolean approve);
    }

    public RequestsAdapter(Context context, int resource) {
        super(context, resource);
    }

    public RequestsAdapter(Context context, int resource, List<Request> items ) {
        super(context, resource, items);
    }

    public void setOnApproveDisapproveListener(OnApproveDissaproveListener listener) {
        this.mListener = listener;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View v = convertView;
        final Request request = getItem(position);

        if ( v == null )
            v = View.inflate(getContext(), R.layout.request_list_item, null);

        ((TextView) v.findViewById(R.id.pickup)).setText(request.pickupLocation);
        ((TextView) v.findViewById(R.id.dropoff)).setText(request.dropoffLocation);

        v.findViewById(R.id.approve).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mListener.OnClick(position, true);

            }
        });

        v.findViewById(R.id.disapprove).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mListener.OnClick(position, false);
            }
        });

        return v;
    }
}
