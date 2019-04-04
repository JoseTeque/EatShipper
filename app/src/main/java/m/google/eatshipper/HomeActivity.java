package m.google.eatshipper;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import m.google.eatshipper.common.Common;
import m.google.eatshipper.model.Requests;
import m.google.eatshipper.model.Token;
import m.google.eatshipper.viewHolder.OrderViewHolder;

public class HomeActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST = 500;
    private static final int PLAY_SERVICES_REQUEST = 9997 ;
    private static final int UPDATE_INTERVAL = 1000;
    private static final int FATEST_INTERVAL =5000 ;
    private static final int DISPLACEMENT = 10 ;
    private LocationCallback mLastLocationCallback;
    private FusedLocationProviderClient apiClientProvider;
    private LocationRequest locationRequest;
    private Location location2;
    private LatLng yourLocation;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;

    private FirebaseDatabase database;
    private DatabaseReference reference;
    private FirebaseRecyclerOptions<Requests> adapterOptions;
    private FirebaseRecyclerAdapter<Requests, OrderViewHolder> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //view

        recyclerView= findViewById(R.id.IdRecyclerOrder);
        recyclerView.setHasFixedSize(true);
        layoutManager= new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        //firebase
        database= FirebaseDatabase.getInstance();
        reference= database.getReference(Common.CURRENT_NEDD_SHIPPED);

        checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (CheckPlayService()) {
            InitFused();
        }


       FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
           @Override
           public void onSuccess(InstanceIdResult instanceIdResult) {
                 String token= instanceIdResult.getToken();
                 updateTokens(token);
           }
       });


        loadAllOrder(Common.currentShipper.getPhone());


    }

    private void loadAllOrder(String phone) {

        DatabaseReference ShipperReference= reference.child(phone);

        adapterOptions= new FirebaseRecyclerOptions.Builder<Requests>()
                .setQuery(ShipperReference,Requests.class).build();

        adapter= new FirebaseRecyclerAdapter<Requests, OrderViewHolder>(adapterOptions) {
            @Override
            protected void onBindViewHolder(@NonNull OrderViewHolder holder, final int position, @NonNull final Requests model) {
                  holder.OrderName.setText(adapter.getRef(position).getKey());
                  holder.txtDate.setText(Common.getDate(Long.parseLong(adapter.getRef(position).getKey())));
                  holder.OrderAddress.setText(model.getAddress());
                  holder.OrderPhone.setText(model.getPhone());
                  holder.OrderStatus.setText(Common.converCodeToStatus(model.getStatus()));

                  holder.btnShipper.setOnClickListener(new View.OnClickListener() {
                      @Override
                      public void onClick(View v) {

                          Common.CreateShipingOrder(adapter.getRef(position).getKey(),
                                  Common.currentShipper.getPhone(),location2);

                          Common.currentRequest= model;
                          Common.currentKey= adapter.getRef(position).getKey();

                          startActivity(new Intent(HomeActivity.this,MapsActivity.class));


                      }
                  });
            }

            @NonNull
            @Override
            public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.order_item_layout,viewGroup,false);
                return new OrderViewHolder(view);
            }
        };

        adapter.startListening();
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);

    }

    private void updateTokens(String tokenrefresh) {
        FirebaseDatabase DB= FirebaseDatabase.getInstance();
        DatabaseReference tokens= DB.getReference("Tokens");

        Token token= new Token(tokenrefresh,false);//false because this tokensend from client app
        tokens.child(Common.currentShipper.getPhone()).setValue(token);

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
                location2 = locationResult.getLocations().get(locationResult.getLocations().size()-1);

                if (ActivityCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    return;
                }
                apiClientProvider.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {

                        if (location != null) {
                            location2= location;
                            yourLocation= new LatLng(location.getLatitude(),location.getLongitude());

                            Toast.makeText(HomeActivity.this, location.getLatitude() + "," + location.getLongitude(), Toast.LENGTH_SHORT).show();
                            Log.d("Location","Your Location : " + yourLocation.latitude + "," + yourLocation.longitude);
                        }
                    }
                });
            }
        };
    }

    private void builLocationRequest() {

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FATEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setSmallestDisplacement(DISPLACEMENT);
        
    }

    private boolean CheckPlayService() {
        int resulCode= GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resulCode!= ConnectionResult.SUCCESS)
        {
            if (GooglePlayServicesUtil.isUserRecoverableError(resulCode)) {
                GooglePlayServicesUtil.getErrorDialog(resulCode, this, PLAY_SERVICES_REQUEST).show();
            }else {
                Toast.makeText(this, "This Device Not Support", Toast.LENGTH_SHORT).show();
                finish();

            }
            return false;
        }
        return true;

    }

    private void checkPermission(String accessFineLocation, String accessCoarseLocation) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) ;
            {
                ActivityCompat.requestPermissions(this, new String[]
                        {
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION
                        }, LOCATION_PERMISSION_REQUEST);
                return;
            }
        }    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case LOCATION_PERMISSION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (CheckPlayService()) {
                        InitFused();
                    }
                } else {
                    Toast.makeText(this, "ES NECESARIO DAR PERMISO", Toast.LENGTH_SHORT).show();
                }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (adapter!=null)
            adapter.startListening();
    }

    @Override
    protected void onStop() {
        if (adapter!=null)
            adapter.stopListening();
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAllOrder(Common.currentShipper.getPhone());
    }
}
