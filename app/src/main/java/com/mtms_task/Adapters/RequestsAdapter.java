package com.mtms_task.Adapters;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.maps.model.LatLng;
import com.mtms_task.POJO.Request;
import com.mtms_task.R;
import com.mtms_task.UI.RequestsActivity;
import com.mtms_task.UI.RequestsViewModel;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RequestsAdapter extends PagerAdapter {

    private Context context;
    private ArrayList<Request> requestsList = new ArrayList<>();
    private String Driver_Id;
    private RequestsViewModel viewModel;

    public RequestsAdapter(Context context, String Driver_Id) {
        this.context = context;
        this.Driver_Id = Driver_Id;
        this.viewModel = ViewModelProviders.of((FragmentActivity) context).get(RequestsViewModel.class);
        this.viewModel = ViewModelProviders.of((FragmentActivity) context).get(RequestsViewModel.class);
    }

    public void setRequestsList(ArrayList<Request> requestsList) {
        this.requestsList.addAll(requestsList);
    }

    @Override
    public int getCount() {
        return requestsList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return (view == (ConstraintLayout) object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.pager_item, container, false);

        //**************************************************************************************************
        //take reference of each view in the view pager item
        final Button accept_btn = view.findViewById(R.id.accept_btn);
        final Button reject_btn = view.findViewById(R.id.reject_btn);
        TextView clientName = view.findViewById(R.id.clientName);
        TextView dist_dur = view.findViewById(R.id.dist_duration);
        TextView fr_char = view.findViewById(R.id.frChar);
        TextView rating = view.findViewById(R.id.rating);
        final TextView destination_Addr = view.findViewById(R.id.destinationAddress);
        final TextView clientPhone = view.findViewById(R.id.phone);
        final TextView time = view.findViewById(R.id.time);

        //**************************************************************************************************
        //bind views with their values from requestsList depending on positions
        final Request request = requestsList.get(position);
        final LatLng latLng1 = new LatLng(request.getSourceLatitude(), request.getSourceLongitude());
        final LatLng latLng2 = new LatLng(request.getDestinationLatitude(), request.getDestinationLongitude());
        clientName.setText(request.getClientName());
        fr_char.setText("" + request.getClientName().charAt(0));
        rating.setText("" + request.getClientRating());
        clientPhone.setText("Phone:     " + request.getClientPhone());
        time.setText("Time:     " + extractTimeFromDateObj(request.getTime()));
        dist_dur.setText(calcualteDistance(latLng1, latLng2) + " km | 40 min");

        destination_Addr.setText("Pickup from\n" + extractAddressFromLt(latLng1.latitude, latLng1.longitude)); // get readable address from lat and long of the destination location

        //**************************************************************************************************
        // handle onClick events for the two buttons
        reject_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "Request is rejected", Toast.LENGTH_SHORT).show();
            }
        });
        accept_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // show client data only
                accept_btn.setVisibility(View.GONE);
                reject_btn.setVisibility(View.GONE);
                destination_Addr.setText("Destination: "+extractAddressFromLt(latLng2.latitude, latLng2.longitude));
                clientPhone.setVisibility(View.VISIBLE);
                time.setVisibility(View.VISIBLE);
                Toast.makeText(context, "Now you have full information about the client..", Toast.LENGTH_SHORT).show();
            }
        });
        //**************************************************************************************************

        ((ViewPager)container).addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        ((ViewPager)container).removeView((ConstraintLayout) object);
    }
    @Override
    public int getItemPosition(Object object){
        return PagerAdapter.POSITION_NONE;
    }

    //**************************************************************************************************
    //  extract readable time from date object
    private String extractTimeFromDateObj(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        return dateFormat.format(date);
    }

    //**************************************************************************************************
    // extract readable address from latitude and longitude
    private String extractAddressFromLt(double latitude, double longitude) {
        Geocoder geocoder;
        List<Address> addresses = null;
        geocoder = new Geocoder(context, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String address = addresses.get(0).getAddressLine(0);
        String state = addresses.get(0).getAdminArea();
        String street = "";
        if (address.contains(",")) {
            street = address.substring(0, address.indexOf(','));
            return street + ", " + state + ".";
        }
        else
        return address+ ".";
    }

    //**************************************************************************************************
    // Calculate distance by km between two points on the map
    public double calcualteDistance(LatLng StartP, LatLng EndP) {
        int Radius = 6371;// radius of earth in Km
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));
        Log.i("Radius Value", "" + valueResult + "   KM  " + kmInDec
                + " Meter   " + meterInDec);

        return kmInDec;
    }
    //**************************************************************************************************
}

