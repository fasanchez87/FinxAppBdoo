package com.ingeniapps.findo.activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.iid.FirebaseInstanceId;
import com.ingeniapps.findo.R;
import com.ingeniapps.findo.app.Config;
import com.ingeniapps.findo.beans.Oferta;
import com.ingeniapps.findo.fragment.Contacto;
import com.ingeniapps.findo.fragment.Favoritos;
import com.ingeniapps.findo.fragment.MapaConvenios;
import com.ingeniapps.findo.fragment.Buscar;
import com.ingeniapps.findo.fragment.Ofertas;
import com.ingeniapps.findo.helper.BottomNavigationViewHelper;
import com.ingeniapps.findo.service.AlarmReceiver;
import com.ingeniapps.findo.service.LocationClientService;
import com.ingeniapps.findo.util.NotificationUtils;
import com.ingeniapps.findo.vars.vars;
import com.ingeniapps.findo.sharedPreferences.gestionSharedPreferences;
import com.ingeniapps.findo.volley.ControllerSingleton;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Principal extends AppCompatActivity
{
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    vars vars;
    private String tokenFCM;
    private String indCambioClv;
    private String nomColaborador;
    private String indicaPush;
    com.ingeniapps.findo.sharedPreferences.gestionSharedPreferences gestionSharedPreferences;
    public String currentVersion = null;
    private String html="";
    private String versionPlayStore="";
    Context context;
    Dialog dialog;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private static final String TAG = Principal.class.getSimpleName();
    private int nivelBateria=0;
    RelativeLayout activity_main;

    private int MULTIPLE_PERMISSION_REQUEST_CODE = 1;
    private BroadcastReceiver broadcast_reciever;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        gestionSharedPreferences=new gestionSharedPreferences(Principal.this);

        if(TextUtils.isEmpty(gestionSharedPreferences.getString("indAlertaConvenio")))
        {
            gestionSharedPreferences.putString("indAlertaConvenio","1");
        }

        final Snackbar snackBar = Snackbar.make(findViewById(R.id.activity_main), "asasas", Snackbar.LENGTH_LONG);

        activity_main=(RelativeLayout)findViewById(R.id.activity_main);

        tokenFCM="";
        vars=new vars();
        context = this;

        if (savedInstanceState == null)
        {
            Bundle extras = getIntent().getExtras();
            if (extras == null)
            {
                indCambioClv = null;
                nomColaborador = null;
                indicaPush = null;
            }
            else
            {
                indCambioClv = extras.getString("indCambioClv");
                nomColaborador = extras.getString("nomColaborador");
                indicaPush = extras.getString("indicaPush");
            }
        }

        try
        {
            currentVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        }
        catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }

        if(checkPlayServices())
        {
            if(!TextUtils.isEmpty(FirebaseInstanceId.getInstance().getToken()))
            {
                tokenFCM=FirebaseInstanceId.getInstance().getToken();
            }
        }
        else
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Principal.this,R.style.AlertDialogTheme));
            builder
                    .setTitle("GOOGLE PLAY SERVICES")
                    .setMessage("Se ha encontrado un error con los servicios de Google Play, actualizalo y vuelve a ingresar.")
                    .setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int id)
                        {
                            finish();
                        }
                    }).setCancelable(false).show().getButton(DialogInterface.BUTTON_POSITIVE).
                    setTextColor(getResources().getColor(R.color.colorPrimary));
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        final BottomNavigationView bottomNavigationView = (BottomNavigationView)
                findViewById(R.id.bottom_navigation);


        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);

        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, new MapaConvenios());
        fragmentTransaction.commit();

        bottomNavigationView.setOnNavigationItemSelectedListener
                (new BottomNavigationView.OnNavigationItemSelectedListener()
                {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item)
                    {
                        Fragment fragment = null;
                        Class fragmentClass;

                        switch (item.getItemId())
                        {
                            case R.id.action_convenios:
                                fragmentClass = MapaConvenios.class;
                                break;
                            case R.id.action_oferta:
                                fragmentClass = Ofertas.class;
                                break;
                            case R.id.action_buscar:
                                fragmentClass = Buscar.class;
                                break;
                            case R.id.action_favoritos:
                                fragmentClass = Favoritos.class;
                                break;
                            case R.id.action_contacto:
                                fragmentClass = Contacto.class;
                                break;
                            default:
                                fragmentClass = MapaConvenios.class;
                        }

                        try
                        {
                            fragment = (Fragment) fragmentClass.newInstance();
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }

                        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
                        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.frame_layout, fragment);
                        fragmentTransaction.commit();
                        return true;
                    }
                });

        mRegistrationBroadcastReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                // checking for type intent filter
                if (intent.getAction().equals(Config.PUSH_NOTIFICATION))//Ofertas
                {
                    if(!(Principal.this).isFinishing())
                    {
                        final String message = intent.getStringExtra("message");
                        String title = intent.getStringExtra("title");
                        final String codOferta = intent.getStringExtra("codOferta");

                        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Principal.this, R.style.AlertDialogTheme));
                        builder
                                .setTitle(title)
                                .setMessage(message)
                                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id)
                                    {
                                        Intent resultIntent = new Intent(Principal.this, DetalleOferta.class);
                                        /*Intent i = new Intent("finish_activity");
                                        sendBroadcast(i);*/
                                        resultIntent.putExtra("isNotifyPush",false);
                                        resultIntent.putExtra("codOferta", codOferta);
                                        startActivity(resultIntent);
                                    }
                                }).setCancelable(true).show().getButton(DialogInterface.BUTTON_POSITIVE).
                                setTextColor(getResources().getColor(R.color.colorPrimary));
                    }
                }
            }
        };


        broadcast_reciever = new BroadcastReceiver() {

            @Override
            public void onReceive(Context arg0, Intent intent) {
                String action = intent.getAction();
                if (action.equals("finish_activity")) {
                    finish();
                    // DO WHATEVER YOU WANT.
                }
            }
        };
        registerReceiver(broadcast_reciever, new IntentFilter("finish_activity"));


        //setUpAlarms();//Configuramos las alarmas
    }

    private boolean haveNetworkConnection()
    {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MULTIPLE_PERMISSION_REQUEST_CODE)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                //The External Storage Write Permission is granted to you... Continue your left job...

                if(!isMyServiceRunning(LocationClientService.class))
                {
                    startGeofenceService();
                    Log.d(TAG, "service started from broadcast");
                }
            }
            else
            {
                Log.d(TAG, "permission denied");
            }
        }
    }
    private void startGeofenceService()
    {
        Log.i(TAG, "GeoFence ha sido iniciado");
        Intent intent = new Intent(this, LocationClientService.class);
        startService(intent);
    }

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        ControllerSingleton.getInstance().cancelPendingReq("request_tokenfcm_principal");
        ControllerSingleton.getInstance().cancelPendingReq("version_playstore");
        unregisterReceiver(broadcast_reciever);
        Log.i("onDestroy","onDestroy");

    }

    private void setUpAlarms()
    {
        Log.i(TAG, "configured alarm");
        alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        //APAGADO AUTOMATICO DE GEOLOCATION POR MEDIO DE ALARMA
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("IntentType", 1);
        alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);

        if (calendar.getTimeInMillis() < System.currentTimeMillis())
        {
            calendar.setTimeInMillis(calendar.getTimeInMillis() + 24 * 60 * 60 * 1000);
            //calendar.add(Calendar.DATE, 1);
        }
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, alarmIntent);


        //ENCENDIDO AUTOMATICO DE GEOLOCATION POR MEDIO DE ALARMA
        intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("IntentType", 2);
        alarmIntent = PendingIntent.getBroadcast(this, 1, intent, 0);

        calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 7);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);

        if (calendar.getTimeInMillis() < System.currentTimeMillis())
        {
            calendar.setTimeInMillis(calendar.getTimeInMillis() + 24 * 60 * 60 * 1000);
            //calendar.add(Calendar.DATE, 1);
        }
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, alarmIntent);
    }

    private MenuItem menu_account;

    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_cuenta, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menu_account:
                Intent aboutIntent = new Intent(Principal.this, Cuenta.class);
                startActivity(aboutIntent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass)
    {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {
            if (serviceClass.getName().equals(service.service.getClassName()))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(new android.support.v7.view.ContextThemeWrapper(this, R.style.AlertDialogTheme));
            builder
                    .setTitle(R.string.title)
                    .setMessage("¿Deseas salir de la aplicación justo ahora?")
                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int id)
                        {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                            {
                                boolean hasPermissionAccesssFineLocation = ContextCompat.checkSelfPermission(getApplicationContext(),
                                        android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
                                boolean hasPermissionAccesssCoarseLocation = ContextCompat.checkSelfPermission(getApplicationContext(),
                                        android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
                                if (!hasPermissionAccesssFineLocation || !hasPermissionAccesssCoarseLocation)
                                    ActivityCompat.requestPermissions(Principal.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                                            android.Manifest.permission.ACCESS_COARSE_LOCATION}, MULTIPLE_PERMISSION_REQUEST_CODE);
                                else

                                if(!isMyServiceRunning(LocationClientService.class)&&TextUtils.equals(gestionSharedPreferences.getString("indAlertaConvenio"),"1")&&haveNetworkConnection())
                                {
                                    startGeofenceService();
                                    Log.d(TAG, "service started from broadcast");
                                }

                            }
                            else
                            {
                                if(!isMyServiceRunning(LocationClientService.class)&&TextUtils.equals(gestionSharedPreferences.getString("indAlertaConvenio"),"1")&&haveNetworkConnection())
                                {
                                    startGeofenceService();
                                    Log.d(TAG, "service started from broadcast");
                                }
                            }

                            finish();
                        }
                    }).setNegativeButton("Cancelar", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int id)
                {

                }
            }).show();
        }

        return false;
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



    @Override
    protected void onPause()
    {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);

       /* if (ActivityCompat.checkSelfPermission(Principal.this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                TextUtils.equals(gestionSharedPreferences.getString("indAlertaConvenio"),"1") && getBatteryLevel()>30.0)
        {
            *//*if(!isMyServiceRunning(LocationClientService.class)&&haveNetworkConnection())//modificado
            {*//*
                startGeofenceService();
                Log.d("ServiceFindo", "el servicio ha iniciadoxxxx");
           *//* }*//*
        }
        else
        if (ActivityCompat.checkSelfPermission(Principal.this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED && isMyServiceRunning(LocationClientService.class)&&TextUtils.equals(gestionSharedPreferences.getString("indAlertaConvenio"),"0"))
        {
            if(isMyServiceRunning(LocationClientService.class)&&haveNetworkConnection())
            {
                stopService(new Intent(context, LocationClientService.class));//PARAMOS SERVICIO PORQUE DE FRENTE AL APP
                Log.d("ServiceFindo", "el servicio ha parado");
            }
        }*/

        Log.d("Principal", "onPause");
        gestionSharedPreferences.putBoolean("isActivePrincipal",false);
    }

    private boolean notificaUpdate=false;

    @Override
    public void onResume()
    {
        super.onResume();
        updateTokenFCMToServer();

      /* if(getBatteryLevel()<30.0)
       {
           Snackbar.make(activity_main, "Bateria: "+getBatteryLevel()+"% Notificaciones apagadas.", Snackbar.LENGTH_INDEFINITE)
                   .setAction("Aceptar", new View.OnClickListener()
                   {
                       @Override
                       public void onClick(View v)
                       {
                       }
                   })
                   .show();
       }*/

        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.PUSH_NOTIFICATION));
        NotificationUtils.clearNotifications(this);

        gestionSharedPreferences.putBoolean("isActivePrincipal",true);

        //agregamos

       /*if (ActivityCompat.checkSelfPermission(Principal.this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&TextUtils.equals(gestionSharedPreferences.getString("indAlertaConvenio"),"1"))
        {
            if(isMyServiceRunning(LocationClientService.class)&&haveNetworkConnection())
            {
                stopService(new Intent(context, LocationClientService.class));//PARAMOS SERVICIO PORQUE DE FRENTE AL APP
                Log.d("ServiceFindo", "el servicio ha parado");
            }
        }*/

       /* if(!notificaUpdate)
        {
            new CheckUpdateAppPlayStore().execute();
            notificaUpdate=true;
        }*/
    }

    public static int compareVersions(String version1, String version2)//COMPARAR VERSIONES
    {
        String[] levels1 = version1.split("\\.");
        String[] levels2 = version2.split("\\.");

        int length = Math.max(levels1.length, levels2.length);
        for (int i = 0; i < length; i++){
            Integer v1 = i < levels1.length ? Integer.parseInt(levels1[i]) : 0;
            Integer v2 = i < levels2.length ? Integer.parseInt(levels2[i]) : 0;
            int compare = v1.compareTo(v2);
            if (compare != 0){
                return compare;
            }
        }
        return 0;
    }

    public class CheckUpdateAppPlayStore extends AsyncTask<String, Void, String>
    {
        protected String doInBackground(String... urls)
        {
            try
            {
                /*versionPlayStore=Jsoup.connect("https://play.google.com/store/apps/details?id=" + "com.ingeniapps.findo" + "&hl=es")
                        .timeout(10000)
                        .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                        .referrer("http://www.google.com")
                        .get()
                        .select("div[itemprop=softwareVersion]")
                        .first()
                        .ownText();*/

                versionPlayStore=Jsoup.connect("https://play.google.com/store/apps/details?id=" + "com.ingeniapps.findo" + "&hl=es")
                        .timeout(10000)
                        .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                        .referrer("http://www.google.com")
                        .get()
                        .select(".hAyfc .htlgb")
                        .get(2)
                        .ownText();


                return versionPlayStore;
            }
            catch (Exception e)
            {
                return "";
            }
        }

        protected void onPostExecute(String string)
        {
            versionPlayStore = string;

            Log.i("findoversion","update ok"+versionPlayStore);


            if(!TextUtils.isEmpty(versionPlayStore)&&!TextUtils.equals(versionPlayStore,""))
            {
                if (compareVersions(currentVersion, versionPlayStore) == -1)
                {
                    if (!((Activity) context).isFinishing())
                    {

                        dialog = new Dialog(Principal.this);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setCancelable(true);
                        dialog.setContentView(R.layout.custom_dialog);

                        Button dialogButton = (Button) dialog.findViewById(R.id.btn_dialog);
                        dialogButton.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse("market://details?id=com.ingeniapps.findo"));
                                startActivity(intent);
                            }
                        });

                        dialog.show();
                    }
                }
            }
        }
    }

    private boolean checkPlayServices()
    {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if(result != ConnectionResult.SUCCESS)
        {
            if(googleAPI.isUserResolvableError(result))
            {
                googleAPI.getErrorDialog(this, result,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            return false;
        }
        return true;
    }

    private void updateTokenFCMToServer()
    {
        String _urlWebServiceUpdateToken = vars.ipServer.concat("/ws/UpdateTokenFCM");

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, _urlWebServiceUpdateToken, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        try
                        {
                            Boolean status = response.getBoolean("status");
                            String message = response.getString("message");

                            if(status)
                            {
                            }
                            else
                            {
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
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("WWW-Authenticate", "xBasic realm=".concat(""));
                headers.put("codEmpleado", gestionSharedPreferences.getString("codEmpleado"));
                headers.put("tokenFCM", ""+FirebaseInstanceId.getInstance().getToken());
                headers.put("versionApp", ""+currentVersion);
                return headers;
            }
        };
        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "request_tokenfcm_principal");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

}
