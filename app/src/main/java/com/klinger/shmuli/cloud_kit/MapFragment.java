package com.klinger.shmuli.cloud_kit;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import org.json.JSONObject;

public class MapFragment extends SupportMapFragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Marker issMarker;
    private Handler handler = new Handler();
    Context thiscontext;

    public MapFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        thiscontext = container.getContext();
        return inflater.inflate(R.layout.map_fragment, container, false);
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);


    }

    @Override
    public void onDetach() {
        super.onDetach();


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera

        if (ContextCompat.checkSelfPermission(thiscontext, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);

            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            handler.post(runnableCode);
        } else {
            // No explanation needed; request the permission
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }
    }

    private Runnable runnableCode = new Runnable() {
        @Override
        public void run() {

            getISSLocationAndPutItOnMap(); // Volley Request

            // Repeat this the same runnable code block again another 2 seconds
            handler.postDelayed(runnableCode, 2000);
        }
    };

    private void getISSLocationAndPutItOnMap() {

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(thiscontext);
        String url = "http://api.open-notify.org/iss-now.json";

        // Request a string response from the provided URL.
        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Double lat = null;
                        Double lon = null;

                        try {
                            lat = response.getJSONObject("iss_position").getDouble("latitude");
                            lon = response.getJSONObject("iss_position").getDouble("longitude");
                            LatLng issLocation = new LatLng(lat, lon);

                            if(issMarker == null) {
                                issMarker = mMap.addMarker(new MarkerOptions().position(issLocation));
                                // Changing marker icon
                                issMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.iss));
                                mMap.animateCamera(CameraUpdateFactory.newLatLng(issMarker.getPosition()));
                            } else {
                                issMarker.setPosition(issLocation);
                                // Changing marker icon
                                issMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.iss));
                                mMap.animateCamera(CameraUpdateFactory.newLatLng(issMarker.getPosition()));
                            }

                        } catch (Exception e) {

                        }
                        //Toast.makeText(getApplicationContext(), lat.toString() + " , " + lon.toString(), Toast.LENGTH_SHORT).show();

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

// Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
}