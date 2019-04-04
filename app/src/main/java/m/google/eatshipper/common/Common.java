package m.google.eatshipper.common;

import android.location.Location;
import android.support.annotation.NonNull;
import android.text.format.DateFormat;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import m.google.eatshipper.model.Requests;
import m.google.eatshipper.model.Shipper;
import m.google.eatshipper.model.ShippingInformation;

public class Common {

    public static Shipper currentShipper;
    public static Requests currentRequest;
    public static String currentKey;
    public static String currentShipping= "ShippingOrders";

    public  static String CURRENT_NEDD_SHIPPED= "OrderNeedShip";

    public static String getDate(long time)
    {
        Calendar calendar= Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(time);
        StringBuilder builder= new StringBuilder(DateFormat.format("dd-MM-yyy HH:mm",calendar).toString());
        return builder.toString();
    }

    public static String converCodeToStatus(String status){

        if(status.equals("0"))
            return "Placed";
        else if (status.equals("1"))
            return "On my way";
        else if (status.equals("2"))
            return "Shipping";
        else
            return "Shipped";
    }

    public static void CreateShipingOrder(String key, String phone, Location yourLocation) {

       ShippingInformation shippingInformation= new ShippingInformation();

       shippingInformation.setOrderId(key);
       shippingInformation.setOrderPhone(phone);
       shippingInformation.setLat(yourLocation.getLatitude());
       shippingInformation.setLng(yourLocation.getLongitude());

       //create new item on information shipping table

        FirebaseDatabase.getInstance().getReference(currentShipping).child(key).setValue(shippingInformation)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("ERROR",e.getMessage());
                    }
                });
    }

    public static void UpdateShippingInformation(String currentKey, Location mlocation) {

        Map<String,Object> updateLocation= new HashMap<>();
        updateLocation.put("lat", mlocation.getLatitude());
        updateLocation.put("lng",mlocation.getLongitude());

        FirebaseDatabase.getInstance().getReference(currentShipping).child(currentKey).updateChildren(updateLocation)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                      Log.d("ERROR",e.getMessage());
                    }
                });
    }
}
