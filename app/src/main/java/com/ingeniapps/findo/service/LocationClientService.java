package com.ingeniapps.findo.service;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import com.ingeniapps.findo.volley.ControllerSingleton;
import com.ingeniapps.findo.sharedPreferences.gestionSharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.ingeniapps.findo.vars.vars;

public class LocationClientService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener
{
    private static final String TAG = LocationClientService.class.getSimpleName();
    public static GoogleApiClient googleApiClient;
    public static List<Geofence> geofenceLists;
    private PendingIntent geofencePendingIntent;
    private float GEOFENCE_RADIUS = 150f;
    vars vars;
    private int nivelBateria = 0;
    private Handler mHandler;
    public static gestionSharedPreferences sharedPreferences;

    @Override
    public void onCreate()
    {
        super.onCreate();
        createGoogleApi();
        sharedPreferences=new gestionSharedPreferences(LocationClientService.this);
        geofenceLists = new ArrayList<Geofence>();
        vars = new vars();
        //addGeofences();
        mHandler = new Handler();

        if(haveNetworkConnection())
        {
            WebServiceCargarConvenios();
            Log.i("marti","con conexion");
        }
        else
        {
            Log.i("marti","sin conexion");
            //geofencePendingIntent.cancel();//modificado
        }
    }

    private boolean haveNetworkConnection()
    {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();



        for (NetworkInfo ni : netInfo)
        {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.i(TAG, "onStartCommand");
        if (!googleApiClient.isConnected() || googleApiClient != null)
            googleApiClient.connect();
        mHandler.postDelayed(statusBatteryFindo, 10000);
        return START_STICKY;
    }

    final Runnable statusBatteryFindo = new Runnable()
    {
        public void run()
        {
            if (getBatteryLevel() < 30)
            {
                stopSelf();
            }
            mHandler.postDelayed(statusBatteryFindo, 5000);
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
        Log.i(TAG, "onConnected " + bundle);
        Location l = null;
        try
        {
            l = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        }
        catch (SecurityException e)
        {
            e.printStackTrace();
        }
        if (l != null)
        {
            Log.i(TAG, "lat " + l.getLatitude());
            Log.i(TAG, "lng " + l.getLongitude());
        }

        startLocationUpdate();
    }

    /*private void addGeofences()
    {
        if (!geofenceLists.isEmpty() && googleApiClient.isConnected() || !(googleApiClient == null))
        {

            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                return;
            }

            LocationServices.GeofencingApi.addGeofences(googleApiClient, getGeofencingRequest(),
                    getGeofencePendingIntent())
                    .setResultCallback(new ResultCallback<Status>()
                    {
                        @Override
                        public void onResult(Status status)
                        {
                            if (status.isSuccess())
                            {

                            }
                            // Remove notifiation here
                        }
                    });
        }
    }*/

    private void addGeofences()
    {
        try
        {
            if (!geofenceLists.isEmpty() && googleApiClient.isConnected() || !(googleApiClient == null))
            {
                new Handler().postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                        {
                            return;
                        }
                        LocationServices.GeofencingApi.addGeofences(googleApiClient, getGeofencingRequest(),
                                getGeofencePendingIntent())
                                .setResultCallback(new ResultCallback<Status>()
                                {
                                    @Override
                                    public void onResult(Status status)
                                    {
                                        if (status.isSuccess())
                                        {

                                        }
                                        // Remove notifiation here
                                    }
                                });

                    }
                }, 5000); //Damos tiempo a que se conecte googleApiClient.
            }

        }
        catch (SecurityException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionSuspended(int i)
    {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
    {
    }

    @Override
    public void onLocationChanged(Location location)
    {
       /* Log.i(TAG, "lat changed " + location.getLatitude());
        Log.i(TAG, "lng changed " + location.getLongitude());
        Log.i(TAG, "battery " + getBatteryLevel());
        Toast.makeText(getApplicationContext(), "Location changed service", Toast.LENGTH_SHORT).show();*/

       /* if(getBatteryLevel()<30.0)
        {
            stopSelf();
        }*/

        //Toast.makeText(getApplicationContext(), "Location changed service", Toast.LENGTH_SHORT).show();


    }



    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Log.i(TAG, "google api disconnect");
        mHandler.removeCallbacks(statusBatteryFindo);
        //googleApiClient.unregisterConnectionCallbacks(this);//modificado
        LocationServices.GeofencingApi.removeGeofences(googleApiClient, getGeofencePendingIntent());
        //googleApiClient.disconnect();//modificado
        ControllerSingleton.getInstance().cancelPendingReq("getpushlocationservice");
    }

    private void createGoogleApi()
    {
        Log.d(TAG, "createGoogleApi()");
        if (googleApiClient == null)
        {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    private void startLocationUpdate()
    {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        try
        {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, mLocationRequest, this);
        }
        catch (SecurityException e)
        {
            e.printStackTrace();
        }
    }

    GeofencingRequest.Builder builder;

    private GeofencingRequest getGeofencingRequest()
    {
        if (geofenceLists.isEmpty() || geofenceLists.size()==0)
        {
            throw new IllegalArgumentException("No geofence has been added to this request.");
        }
        else
        {
            builder = new GeofencingRequest.Builder();
            builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
            builder.addGeofences(geofenceLists);
            return builder.build();
        }
    }

    public float getBatteryLevel()
    {
        Intent batteryIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        // Error checking that probably isn't needed but I added just in case.
        if(level == -1 || scale == -1)
        {
            return 50.0f;
        }

        return ((float)level / (float)scale) * 100.0f;
    }



    private PendingIntent getGeofencePendingIntent()
    {
        // Reuse the PendingIntent if we already have it.
        if (geofencePendingIntent != null)
        {
            return geofencePendingIntent;
        }
        Intent intent = new Intent(this, TransitionIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        geofencePendingIntent = PendingIntent.getService(LocationClientService.this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return geofencePendingIntent;
    }

    private String a="";
    Geofence geofenceConvenio;

    private void WebServiceCargarConvenios()
    {
        String _urlWebService= vars.ipServer.concat("/ws/getPuntoPush");

        Log.i("llamadoWS","Llamado ok");

        geofenceLists.clear();
        geofenceConvenio=null;

        JsonObjectRequest jsonObjReq=new JsonObjectRequest(Request.Method.GET, _urlWebService, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        try
                        {
                            boolean status=response.getBoolean("status");

                            if(status)
                            {
                                final JSONArray puntos=response.getJSONArray("puntos");
                                JSONObject punto;

                                for (int i=0; i<puntos.length(); i++)
                                {
                                    JSONObject jsonObject = null;
                                    try
                                    {
                                        jsonObject = (JSONObject) puntos.get(i);
                                        a=jsonObject.getString("a").concat("|").concat(jsonObject.getString("d").concat("|").concat(jsonObject.getString("e")));

                                        geofenceConvenio=new Geofence.Builder()
                                                .setRequestId(""+a)
                                                .setCircularRegion(Double.parseDouble(jsonObject.getString("b")), Double.parseDouble(jsonObject.getString("c")), GEOFENCE_RADIUS)
                                                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                                                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER
                                                        | Geofence.GEOFENCE_TRANSITION_EXIT).build();

                                        geofenceLists.add(geofenceConvenio);
                                    }
                                    catch (JSONException e)
                                    {
                                        e.printStackTrace();
                                    }

                                    if(geofenceLists.isEmpty() || geofenceLists.size()==0)
                                    {
                                        Log.i("geofences","Vacio!!!");
                                    }
                                    else
                                    {
                                        Log.i("geofences","Lleno!!!");
                                    }



                                }

                                 addGeofences();
                            }
                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                    }
                })
        {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                HashMap<String, String> headers = new HashMap <String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("WWW-Authenticate", "xBasic realm=".concat(""));
                headers.put("codEmpleado", ""+sharedPreferences.getString("codEmpleado"));
                //headers.put("MyToken", sharedPreferences.getString("MyToken"));
                // headers.put("tokenFCM", ""+tokenFCM);
                return headers;
            }
        };

        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "getpushlocationservice");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }
}
