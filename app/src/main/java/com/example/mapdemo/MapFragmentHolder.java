package com.example.mapdemo;

import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by alecksjohansson on 4/24/16.
 */
public class MapFragmentHolder extends Fragment implements MyLocationListener,GoogleMap.OnInfoWindowClickListener {

    private SupportMapFragment mMapFragment;
    private GoogleMap mMap;
    private Firebase mDb;
    private ClusterManager<Police> mClusterManager;
    private LatLng Latlng;
    private LatLng mCurrentPosition;
    private ArrayList<Police> mPoliceList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.map_holder_fragment, container, false);
        mPoliceList = new ArrayList<>();
        mDb = new Firebase(Config.FIREBASE_URL);
        readDatabase();
        mCurrentPosition = new LatLng(0, 0);
        setupMap();
        return view;
    }

    void setupMap() {
        mMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mMapFragment != null) {
            mMapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap map) {
                    loadMap(map);
                }
            });
        }
    }
    public Police getPoliceInfo(Marker marker) {
        Police police = new Police();
        if (mPoliceList.size() > 0) {
            for (Police po : mPoliceList) {
                if (new LatLng(po.getLatitude(), po.getLongitude()).equals(marker.getPosition())) {
                    police = po;
                }
            }
        }
        return police;
    }

    protected void loadMap(GoogleMap googleMap) {
        mMap = googleMap;
        if (mMap != null) {
            // Map is ready
            Toast.makeText(getActivity(), "Map Fragment was loaded properly!", Toast.LENGTH_SHORT).show();
            MapActivityPermissionsDispatcher.getMyLocationWithCheck((MapActivity) getActivity());
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.setMyLocationEnabled(true);
            Log.d("DEBUG", "enabled location");
            mMap.setOnMapLongClickListener((GoogleMap.OnMapLongClickListener) getContext());
            googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Override
                public View getInfoWindow(Marker marker) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {
                    String dialog;
                    Police police = getPoliceInfo(marker);
                    dialog = ("     Seen: " + Police.getTimeAgo(police.getTimeInLong()) +
                            "\n Can be charged for: " +
                            "\n + Hit and run" +
                            "\n + Turn without turn indicator" +
                            "\n + Overspeed");
                    View v = getLayoutInflater(null).inflate(R.layout.info_window_layout, null);
                    TextView tvLat = (TextView) v.findViewById(R.id.tvLat);
                    TextView tvLog = (TextView) v.findViewById(R.id.tvLog);
                    tvLat.setText(dialog);
                    tvLog.setText(" Like: " + police.getLike() +
                            "\n Dislike: " + police.getDislike());
                    return v;
                }
            });
            setUpClusterer();
        } else {
            Toast.makeText(getContext(), "Error - Map was null!!", Toast.LENGTH_SHORT).show();
        }
    }

    public void setUpClusterer() {
        //nitialize the manager with the context and the map.
        mClusterManager = new ClusterManager<Police>(getContext(), mMap);
        mClusterManager.getClusterMarkerCollection().setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Log.d("123", "1");
                return false;
            }
        });
        mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<Police>() {
            @Override
            public boolean onClusterClick(Cluster<Police> cluster) {
                Log.d("123", "2");
                return false;
            }
        });
        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        mMap.setOnCameraChangeListener(mClusterManager);
        //mMap.setOnMarkerClickListener(mClusterManager);
        mClusterManager.getMarkerCollection().getMarkers();
        mMap.setOnMarkerClickListener((MapActivity) getActivity());

    }

    public void writeToCloud(Police police) {
        mDb.push().setValue(police);

    }

    @Override
    public void onLocationUpdate(LatLng newPos) {
        if (mMap != null) {
            mCurrentPosition = (newPos);
        }
    }

    public void dropMarker(Police police) {
        BitmapDescriptor defaultMarker = BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_RED);
        Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(police.getLatitude(), police.getLongitude()))
                .icon(defaultMarker).title("Police here").snippet("Seen " + Police.getTimeAgo(police.getTimeInLong())));
        Log.d("RUNME", "RUNME");
        Log.d("LAT ", String.valueOf(police.getLatitude()));
        Log.d("LONG", String.valueOf(police.getLongitude()));
        dropPinEffect(marker);
    }

    public void onLocationChanged(Location location) {
        // Report to the UI that the location was updated
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        mCurrentPosition = new LatLng(location.getLatitude(), location.getLongitude());
        //Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

    }

    private void dropPinEffect(final Marker marker) {
        // Handler allows us to repeat a code block after a specified delay
        final android.os.Handler handler = new android.os.Handler();
        final long start = SystemClock.uptimeMillis();
        final long duration = 1500;

        // Use the bounce interpolator
        final android.view.animation.Interpolator interpolator =
                new BounceInterpolator();

        // Animate marker with a bounce updating its position every 15ms
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                // Calculate t for bounce based on elapsed time
                float t = Math.max(
                        1 - interpolator.getInterpolation((float) elapsed
                                / duration), 0);
                // Set the anchor
                marker.setAnchor(0.5f, 1.0f + 14 * t);

                if (t > 0.0) {
                    // Post this event again 15ms from now.
                    handler.postDelayed(this, 15);
                } else { // done elapsing, show window
                    marker.showInfoWindow();
                }
                mClusterManager.cluster();
            }
        });
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        Police police;
        if (useCurrent(latLng)) {
            police = new Police(latLng);

        } else {
            police = new Police(mCurrentPosition);
        }
        writeToCloud(police);
        mClusterManager.addItem(police);
        mPoliceList.add(police);
        //      dropMarker(police);
        mClusterManager.cluster();
    }

    public boolean useCurrent(LatLng clickPos) {
        long distanceInMeter = Math.round(CalculationByDistance(mCurrentPosition, clickPos) * 1000);
        if (distanceInMeter > 80) {
            return true;
        } else {
            return false;
        }
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
        Log.i("Radius Value", "" + valueResult + "   KM  " + kmInDec
                + " Meter   " + meterInDec);
        return Radius * c;
    }

    public void readDatabase() {
        Query query = mDb.orderByChild("id");
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Police police = dataSnapshot.getValue(Police.class);
                mClusterManager.addItem(police);
                mPoliceList.add(police);
                getPolice(mPoliceList);
                mClusterManager.cluster();
                //dropMarker(police);
            }


            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void getPolice(ArrayList<Police> mPoliceList) {

        {
            for (int i = 0; i < mPoliceList.size(); i++) {
                mPoliceList.get(i).getPosition();
                Log.d("Police list", "ABC" + mPoliceList);
            }
        }
    }


    @Override
    public void onInfoWindowClick(Marker marker) {

    }
}
