package com.example.mapdemo;

import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.google.android.gms.maps.model.LatLng;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by alecksjohansson on 4/24/16.
 */
public class EasyMode extends Fragment implements MyLocationListener {

    TextView tvDistance;
    TextView tvZone;
    ArrayList<Police> mPoliceList;
    private Firebase mDb;
    private static final String TAG = "CompassActivity";
    private Compass compass;
    private LatLng mCurrentPosition;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.easy_fragment, container, false);
        tvDistance = (TextView) view.findViewById(R.id.tvDistance);
        tvZone = (TextView) view.findViewById(R.id.tvZone);
        compass = new Compass(getContext());
        compass.arrowView = (ImageView) view.findViewById(R.id.main_image_hands);
        mDb = new Firebase(Config.FIREBASE_URL);
        mCurrentPosition = new LatLng(0, 0);
        mPoliceList = new ArrayList<>();
        readDatabase();
        compass.start();
        return view;
    }

    public void RunningBackgroud() {
        final Handler h = new Handler();
        h.post(new Runnable() {
            @Override
            public void run() {
                long distance = FindNearestPin(mCurrentPosition);
                if (distance > 1000) {
                    tvDistance.setTextColor(getResources().getColor(R.color.green_text));
                    tvZone.setText("");
                    tvZone.setVisibility(View.GONE);
                    tvDistance.setText("There're no police nearby");
                } else {
                    if( distance <100) {
                        tvZone.setVisibility(View.VISIBLE);
                        tvDistance.setTextColor(getResources().getColor(R.color.red_text));
                        tvZone.setTextColor(getResources().getColor(R.color.red_text));
                        tvZone.setText("KILL ZONE");
                        tvDistance.setText(distance + " meters");
                    }
                    else {
                        tvZone.setVisibility(View.VISIBLE);
                        tvDistance.setTextColor(getResources().getColor(R.color.yellow_text));
                        tvZone.setTextColor(getResources().getColor(R.color.yellow_text));
                        tvZone.setText("DANGEROUS ZONE\n");
                        tvDistance.setText(distance+" meters");
                    }
                }
                h.postDelayed(this, 3000);
            }
        });
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    public double CalculationByDistance(LatLng StartP, LatLng EndP) {
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
        //Log.i("Radius Value", "" + valueResult + "   KM  " + kmInDec
        //       + " Meter   " + meterInDec);
        return Radius * c;
    }


    public void onLocationUpdate(LatLng newPos) {
        mCurrentPosition = newPos;
    }

    public void writeToCloud(Police police) {
    }

    public void onMapLongClick(LatLng latLng) {
    }

    public void setUpClusterer() {
    }

    public void onLocationChanged(Location location) {
    }

    public void readDatabase() {
        Query query = mDb.orderByChild("id");
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Police police = dataSnapshot.getValue(Police.class);
                mPoliceList.add(police);
                RunningBackgroud();
                //dropMarker(police);
            }


            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Police police = dataSnapshot.getValue(Police.class);
                mPoliceList.add(police);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Police police = dataSnapshot.getValue(Police.class);
                mPoliceList.remove(police);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public long FindNearestPin(LatLng latLng) {
        long distance = 10000000;
        for (int i = 0; i < mPoliceList.size(); i++) {
            long distances = Math.round(CalculationByDistance(latLng, mPoliceList.get(i).getPosition()) * 1000);
            if (distances<distance) {
                distance = distances;
            }
        }
        Log.d("Distance", distance + "m");
        return distance;

    }



}
