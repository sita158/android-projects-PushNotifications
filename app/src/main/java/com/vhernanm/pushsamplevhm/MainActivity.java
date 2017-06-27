package com.vhernanm.pushsamplevhm;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.ibm.mobilefirstplatform.clientsdk.android.analytics.api.Analytics;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.BMSClient;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.Response;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.ResponseListener;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPPush;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPPushException;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPPushNotificationListener;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPPushResponseListener;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPSimplePushNotification;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    MFPPush push = null;
    MFPPushNotificationListener notificationListener = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the SDK for Android
        BMSClient.getInstance().initialize(this, BMSClient.REGION_US_SOUTH);

        //Initialize client Push SDK for Java
        push = MFPPush.getInstance();
        push.initialize(getApplicationContext(), "97479c24-523b-422d-9739-24428676e7ed", "6f549945-a112-47ef-afeb-08f0bccc35f3");


        // In this code example, Analytics is configured to record lifecycle events.
        Analytics.init(getApplication(), "PushSampleVHM", "9441d0e6-7d91-419b-ae0c-c406f9327532", true, Analytics.DeviceEvent.ALL);
        Analytics.setUserIdentity("sita");

        // Enable recording of usage analytics
        Analytics.enable();

        //Handles the notification when it arrives
        notificationListener = new MFPPushNotificationListener() {
            @Override
            public void onReceive (final MFPSimplePushNotification message){
                // Handle Push Notification
                Log.d("App", "Mensaje recibido: " + message);
                runOnUiThread(new Runnable() {
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("Notificación Push Recibida");
                        builder.setMessage(message.toString());

                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        });
                        builder.show();
                    }
                });
            }
        };
    }

    public void subscribe (View view){
        // Log a custom analytics event
        JSONObject eventJSONObject = new JSONObject();

        try{
            eventJSONObject.put("buttonPressed" , "subscribe");
        } catch(JSONException e){
            Log.d("App", "Excepción: " + e.toString());
        }

        Analytics.log(eventJSONObject);

        //Register Android devices
        push.registerDevice(new MFPPushResponseListener<String>() {
            @Override
            public void onSuccess(String response) {
                //handle success here
                Log.d("App", "Suscripción exitosa: " + response);
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(MainActivity.this, "Suscripción exitosa", Toast.LENGTH_LONG).show();
                    }
                });
            }
            @Override
            public void onFailure(MFPPushException ex) {
                //handle failure here
                Log.d("App", "Suscripción fallida: " + ex.toString());
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(MainActivity.this, "Suscripción fallida", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    public void unsubscribe (View view){
        // Log a custom analytics event
        JSONObject eventJSONObject = new JSONObject();

        try{
            eventJSONObject.put("buttonPressed" , "unsubscribe");
        } catch(JSONException e){
            Log.d("App", "Excepción: " + e.toString());
        }

        Analytics.log(eventJSONObject);

        push.unregister(new MFPPushResponseListener<String>() {
            @Override
            public void onSuccess(String response) {
                Log.d("App", "Remoción exitosa: " + response);
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(MainActivity.this, "Remoción exitosa", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onFailure(final MFPPushException exception) {
                Log.d("App", "Remoción fallida: " + exception.toString());
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(MainActivity.this, "Remoción fallida", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }


    @Override
    protected void onResume(){
        super.onResume();
        Log.d("App", "onResume");
        if(push != null) {
            Log.d("App", "Agregando listener");
            push.listen(notificationListener);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("App", "onPause");
        if (push != null) {
            push.hold();
        }

        // Send recorded usage analytics to the Mobile Analytics Service
        Analytics.send(new ResponseListener() {
            @Override
            public void onSuccess(Response response) {
                // Handle Analytics send success here.
                Log.d("App", "Analíticos enviados");
            }

            @Override
            public void onFailure(Response response, Throwable throwable, JSONObject jsonObject) {
                // Handle Analytics send failure here.
                Log.d("App", "Error al enviar analíticos");
            }
        });
    }

}
