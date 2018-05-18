package com.ingeniapps.findo.service;

import android.app.ActivityManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;


import com.ingeniapps.findo.R;
import com.ingeniapps.findo.activity.DetalleMarkerConvenio;
import com.ingeniapps.findo.activity.Principal;
import com.ingeniapps.findo.sharedPreferences.gestionSharedPreferences;
import com.ingeniapps.findo.util.NotificationUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class TransitionIntentService extends IntentService
{
    private static final String TAG = TransitionIntentService.class.getSimpleName();
    public int GEOFENCE_NOTIFICATION_ID;

    gestionSharedPreferences gestionSharedPreferences;

    public TransitionIntentService()
    {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        // Retrieve the Geofencing intent
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        // Handling errors
        if ( geofencingEvent.hasError() )
        {
            String errorMsg = getErrorString(geofencingEvent.getErrorCode() );
            Log.e( TAG, errorMsg );
            return;
        }

        // Retrieve GeofenceTrasition
        PreferenceManager preferenceManager  = new PreferenceManager(this);
        int lastDirection = preferenceManager.getInt("LAST_DIRECTION", -1);
        String geofenceId = preferenceManager.getString("GEOFENCE_ID", "");


        int geoFenceTransition = geofencingEvent.getGeofenceTransition();
        List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
        Geofence geofence = triggeringGeofences.get(0);

        if(geofenceId.equals(geofence.getRequestId()))
        {
            if(lastDirection == geoFenceTransition)
            return;
            preferenceManager.putInt("LAST_DIRECTION", geoFenceTransition);
        }
        else
        {
            preferenceManager.putString("GEOFENCE_ID", geofence.getRequestId());
            preferenceManager.putInt("LAST_DIRECTION", geoFenceTransition);
        }

        // Check if the transition type
        if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER)
        {
            // Get the geofence that were triggered
            // Create a detail message with Geofences received
            String geofenceTransitionDetails = getGeofenceTrasitionDetails(geoFenceTransition, triggeringGeofences );
            //JSONObject proveedor= null;
            String[] result;
            String nomProveedor = null,descPunto=null,codPunto=null;
           /* try
            {*/
                //proveedor=new JSONObject(geofenceTransitionDetails);
               /* nomProveedor=proveedor.getString("a");
                descPunto=proveedor.getString("e");
                descPunto=proveedor.getString("e");
                codPunto=proveedor.getString("d")*/;

            Log.i("ServiceFindo","ENTRO");

            result=TextUtils.split(geofenceTransitionDetails,":");

            StringTokenizer st = new StringTokenizer(geofenceTransitionDetails, "\\|");
            nomProveedor = st.nextToken();
            codPunto = st.nextToken();
            descPunto = st.nextToken();




            /*Log.i("findopush",codPunto);
            Log.i("findopush",descPunto);
*/
/*
            }*/
           /* catch (JSONException e)
            {
                e.printStackTrace();
            }*/
            // Send notification details as a String
            sendNotification(nomProveedor, descPunto, codPunto);
        }

        else

        if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT)
        {
            Log.i("ServiceFindo","SALIO");
            String ns = Context.NOTIFICATION_SERVICE;
            NotificationManager nMgr = (NotificationManager)getSystemService(ns);
            nMgr.cancelAll();
            NotificationUtils.clearNotifications(this.getApplicationContext());
        }
    }

    // Create a detail message with Geofences received
    private String getGeofenceTrasitionDetails(int geoFenceTransition, List<Geofence> triggeringGeofences)
    {
        // get the ID of each geofence triggered
        String punto=null;
        String nomPunto=null;
        ArrayList<String> triggeringGeofencesList = new ArrayList<>();
        for ( Geofence geofence : triggeringGeofences )
        {
           // triggeringGeofencesList.add(geofence.getRequestId());
            punto=geofence.getRequestId().toString();
        }
       String status = null;
        if ( geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER )
            status = "Entrando ";
        else if ( geoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT )
            status = "Saliendo";
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancelAll();
        Log.i("ServiceFindo",""+status);
        return punto;
    }

    // Send a notification
    private void sendNotification(String msg, String descPunto, String codPunto)
    {
        Log.i("ServiceFindo", "sendNotification: " + msg);
        //String[] codPunto=msg.split(":");
//        Log.i(TAG, "sendNotification: " + msg+"codPunto: "+codPunto[1]);
        // Intent to start the main Activity

       /* String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager)getSystemService(ns);
        nMgr.cancelAll();*/

        final Uri alarmSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                + "://" + this.getApplicationContext().getPackageName() + "/raw/notification");


        Intent newIntent = new Intent(getApplicationContext(), DetalleMarkerConvenio.class);
        Intent intent = new Intent("finish_activity");
        sendBroadcast(intent);
        newIntent.putExtra("codPunto",codPunto);
        newIntent.putExtra("isNotifyPush",true);
        PendingIntent notificationPendingIntent = PendingIntent.getActivity(getApplicationContext(),
                0, newIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Creating and sending Notification
        NotificationManager notificatioMng =
                (NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE );
        notificatioMng.notify(generateId(),
                createNotification(msg, descPunto, notificationPendingIntent,alarmSound));
    }


    // Create a notification
    private Notification createNotification(String msg, String desc, PendingIntent notificationPendingIntent,Uri alarmSound)
    {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);

        notificationBuilder
                .setSmallIcon(R.drawable.icon_push_small)
                .setColor(Color.parseColor("#112548"))
                .setContentTitle(msg)
                .setContentText("Descuento del "+desc+"%")
                .setContentIntent(notificationPendingIntent)
                .setSound(alarmSound)
                .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                .setLights(Color.YELLOW, 1000, 1000);
        return notificationBuilder.build();
    }

    // Handle errors
    private static String getErrorString(int errorCode)
    {
        switch (errorCode)
        {
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                return "Google GeoFence no está disponible";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                return "Existen muchas instancias Google Geofences";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                return "Existen muchos Intents Pendings ahora";
            default:
                return "Error desconocido";
        }
    }

    private int generateId()
    {
        Date dNow = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("hhmmssMs");
        String datetime = ft.format(dNow);
        Log.i(TAG, datetime);
        GEOFENCE_NOTIFICATION_ID = Integer.parseInt(datetime);
        return GEOFENCE_NOTIFICATION_ID;
    }
}

