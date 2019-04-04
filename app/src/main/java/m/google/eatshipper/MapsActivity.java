package m.google.eatshipper;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import info.hoang8f.widget.FButton;
import m.google.eatshipper.Remote.IGeoCordinates;
import m.google.eatshipper.common.Common;
import m.google.eatshipper.common.DirectionJSONParser;
import m.google.eatshipper.model.Requests;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST = 500;
    private GoogleMap mMap;

    private FButton btncall, btnShipped;

    private LocationCallback mLastLocationCallback;
    private FusedLocationProviderClient apiClientProvider;
    private LocationRequest locationRequest;
    private Location mlocation;
    private Marker marker;
    private Polyline polyline;

    public String url;

    private IGeoCordinates mServices;

    private static int UPDATE_INTERVAL = 1000;
    private static int FATEST_INTERVAL = 5000;
    private static int DISPLACEMENT = 10;
    private String key = "AIzaSyBHJadPt3_CrSntFMAOf8qBtZkj8rhkPi4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        btncall = findViewById(R.id.IdbtnCall);
        btnShipped = findViewById(R.id.IdbtnShipped);

        btncall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel: " + Common.currentShipper.getPhone()));
                if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

                    return;
                }
                startActivity(intent);
            }
        });

        btnShipped.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OrderShipped();
            }
        });

        checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION);
        InitFused();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //inicializando retrofit
        mServices= new Retrofit.Builder().baseUrl("https://maps.googleapis.com")
                .addConverterFactory(ScalarsConverterFactory.create()).build().create(IGeoCordinates.class);
    }

    private void OrderShipped() {

        FirebaseDatabase.getInstance().getReference(Common.CURRENT_NEDD_SHIPPED)
                .child(Common.currentShipper.getPhone())
                .child(Common.currentKey)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Map<String,Object> update= new HashMap<>();
                        update.put("status","03");

                        FirebaseDatabase.getInstance().getReference("Requests")
                                .child(Common.currentKey)
                                .updateChildren(update)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        FirebaseDatabase.getInstance().getReference(Common.currentShipping)
                                                .child(Common.currentKey)
                                                .removeValue()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Toast.makeText(MapsActivity.this, "Shipped..", Toast.LENGTH_SHORT).show();
                                                        finish();
                                                    }
                                                });
                                    }
                                });
                    }
                });
    }

    private void InitFused() {

        apiClientProvider = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        builLocationRequest();
        builLocationCallback();
        apiClientProvider.requestLocationUpdates(locationRequest, mLastLocationCallback, Looper.myLooper());
    }

    private void builLocationCallback() {

        mLastLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                mlocation = locationResult.getLocations().get(locationResult.getLocations().size() - 1);

                if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    return;
                }
                apiClientProvider.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {

                        if (location != null) {
                            mlocation= location;

                            if (marker != null) {
                                marker.remove();
                            }
                            LatLng yourLocation = new LatLng(mlocation.getLatitude(), mlocation.getLongitude());
                            marker = mMap.addMarker(new MarkerOptions()
                                    .position(yourLocation)
                                    .title("YOUR LOCATION").icon(BitmapDescriptorFactory.defaultMarker()));
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(yourLocation,15.0f));
                            Common.UpdateShippingInformation(Common.currentKey,mlocation);

                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(yourLocation.latitude,yourLocation.longitude), 15.0f));
                            mMap.animateCamera(CameraUpdateFactory.zoomTo(16.0f));

                            drawRoute(yourLocation,Common.currentRequest,key);

                        }
                    }
                });
            }
        };
    }

    private void drawRoute(final LatLng yourLocation, Requests requests, final String key) {

        if (polyline!=null)
            polyline.remove();

        if (requests.getAddress() != null && !requests.getAddress().isEmpty() ) {
            mServices.getGeocode(requests.getAddress(), key).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {

                    try {
                        JSONObject jsonObject = new JSONObject(response.body().toString());

                        String lat = ((JSONArray) jsonObject.get("results"))
                                .getJSONObject(0)
                                .getJSONObject("geometry")
                                .getJSONObject("location")
                                .get("lat").toString();

                        String lng = ((JSONArray) jsonObject.get("results"))
                                .getJSONObject(0)
                                .getJSONObject("geometry")
                                .getJSONObject("location")
                                .get("lng").toString();


                        LatLng locationOrder = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
                        mMap.addMarker(new MarkerOptions().position(locationOrder).title("Order of: " + Common.currentRequest.getPhone()));

                        // Draw route

                            try {
                                url = "https://maps.googleapis.com/maps/api/directions/json?origin=" + yourLocation.latitude + "," + yourLocation.longitude + "&destination=" + locationOrder.latitude + "," + locationOrder.longitude + "&key=" + key + "";
                                Log.e("URL", url);

                                mServices.ObtenerRuta(url).enqueue(new Callback<String>() {
                                    @Override
                                    public void onResponse(Call<String> call, Response<String> response) {
                                        new ParserTask().execute(response.body());
                                    }

                                    @Override
                                    public void onFailure(Call<String> call, Throwable t) {

                                    }
                                });

                            } catch (Exception e) {

                            }



                        } catch(Exception e){

                        }



                }


                @Override
                public void onFailure(Call<String> call, Throwable t) {

                }
            });
        }
        else
        {
            if (requests.getLatLong()!=null && !requests.getLatLong().isEmpty())
            {
               String[] LatLng= requests.getLatLong().split(",");
               LatLng orderLocation= new LatLng(Double.parseDouble(LatLng[0]), Double.parseDouble(LatLng[1]));

                mMap.addMarker(new MarkerOptions().position(orderLocation).title("Order of: " + Common.currentRequest.getPhone()));

                try {
                    url = "https://maps.googleapis.com/maps/api/directions/json?origin=" + yourLocation.latitude+ "," + yourLocation.longitude+ "&destination=" + orderLocation.latitude + "," + orderLocation.longitude + "&key=" + key + "";
                    Log.e("URL", url);

                    mServices.ObtenerRuta(url).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            new ParserTask().execute(response.body());
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {

                        }
                    });

                } catch (Exception e) {

                }

            }
        }

    }

    private void builLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FATEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setSmallestDisplacement(DISPLACEMENT);
        
    }

    private void checkPermission(String accessFineLocation, String accessCoarseLocation) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                    != PackageManager.PERMISSION_GRANTED);
            {
                ActivityCompat.requestPermissions(this, new String[]
                        {
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.CALL_PHONE
                        }, LOCATION_PERMISSION_REQUEST);
                return;
            }
        }
        
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.clear();

        boolean istyle= mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,R.raw.uber_style));
        if (!istyle)
            Log.d("ERROR","Map Style Load Failed..");

        builLocationRequest();
        builLocationCallback();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        apiClientProvider.requestLocationUpdates(locationRequest, mLastLocationCallback, Looper.myLooper());

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case LOCATION_PERMISSION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    InitFused();
                } else {
                    Toast.makeText(this, "ES NECESARIO DAR PERMISO", Toast.LENGTH_SHORT).show();
                }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onStop() {
        apiClientProvider.removeLocationUpdates(mLastLocationCallback);
        super.onStop();
    }


    public class ParserTask extends AsyncTask<String,Integer, List<List<HashMap<String,String>>>> {

        ProgressDialog mDialog= new ProgressDialog(MapsActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.setMessage("Please waiting...");
            mDialog.show();
        }

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jsonObject;
            List<List<HashMap<String,String>>> routes= null;

            try {

                jsonObject= new JSONObject(strings[0]);

                DirectionJSONParser jsonParser= new DirectionJSONParser();

                routes = jsonParser.parse(jsonObject);

            } catch (JSONException e) {
                Log.e("Error doInBackground",e.toString());
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            mDialog.dismiss();

            ArrayList points = null;
            PolylineOptions polylineOptions= null;

            for (int i= 0; i<lists.size();i++)
            {
                points= new ArrayList();
                polylineOptions= new PolylineOptions();
                List<HashMap<String,String>> path= lists.get(i);

                for (int j=0; j<path.size();j++)
                {
                    HashMap<String,String> point= path.get(j);
                    double lat= Double.parseDouble((point.get("lat")));
                    double lng= Double.parseDouble(point.get("lng"));
                    LatLng position= new LatLng(lat,lng);

                    points.add(position);
                }

                polylineOptions.addAll(points);
                polylineOptions.width(12);
                polylineOptions.color(Color.BLUE);
                polylineOptions.geodesic(true);
            }
            if (polyline == null)
                polyline = mMap.addPolyline(polylineOptions);

            else {
                polyline.remove();
                polyline = mMap.addPolyline(polylineOptions);
            }
        }
    }
}
