package m.google.eatshipper.Service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Random;

import m.google.eatshipper.Helper.NotificationHelper;
import m.google.eatshipper.HomeActivity;
import m.google.eatshipper.MainActivity;
import m.google.eatshipper.R;
import m.google.eatshipper.common.Common;
import m.google.eatshipper.model.Token;

public class MyFirebaseMessageService  extends FirebaseMessagingService {

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {

                String token = instanceIdResult.getToken();
                if (Common.currentShipper!= null)
                    UpdateTokenToFirebase(token);

            }
        });
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage!=null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                sendNotificationApi26(remoteMessage);
            } else {
                sendNotification(remoteMessage);
            }
        }
    }

    private void sendNotification(RemoteMessage remoteMessage) {

        Map<String,String> data= remoteMessage.getData();
        String title= data.get("Title");
        String message= data.get("Message");

        Intent intent= new Intent(this, HomeActivity.class);
        intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent= PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT);

        Uri defaultsoudUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder= new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_local_shipping_black_24dp)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultsoudUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager= (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0,builder.build());
    }

    private void sendNotificationApi26(RemoteMessage remoteMessage) {

        Map<String,String> data= remoteMessage.getData();
        String title= data.get("Title");
        String message= data.get("Message");


        //here we will fix to click to notification -> go to order list
        PendingIntent pendingIntent;
        NotificationHelper notificationHelper;
        Notification.Builder builder;

        if (Common.currentShipper !=null) {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
            pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

            Uri defaultsoudUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            notificationHelper = new NotificationHelper(this);
            builder = notificationHelper.getEatChannelNotification(title, message, pendingIntent, defaultsoudUri);

//get random id for notification to show all notification
            notificationHelper.getManager().notify(new Random().nextInt(), builder.build());
        }
        else
        {
            Uri defaultsoudUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            notificationHelper = new NotificationHelper(this);
            builder = notificationHelper.getEatChannelNotification(title, message,  defaultsoudUri);

//get random id for notification to show all notification
            notificationHelper.getManager().notify(new Random().nextInt(), builder.build());
        }
    }

    private void UpdateTokenToFirebase(String tokenrefresh) {

        if (Common.currentShipper!= null) {
            FirebaseDatabase DB = FirebaseDatabase.getInstance();
            DatabaseReference tokens = DB.getReference("Tokens");

            Token token = new Token(tokenrefresh, true);//TRUE because this tokensend from SERVER app
            tokens.child(Common.currentShipper.getPhone()).setValue(token);
        }
    }
}
