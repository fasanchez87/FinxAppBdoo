package com.ingeniapps.findo.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.location.LocationRequest;

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ingeniapps.findo.R;
import com.ingeniapps.findo.vars.vars;
import com.ingeniapps.findo.sharedPreferences.gestionSharedPreferences;
import com.ingeniapps.findo.volley.ControllerSingleton;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RationaleListener;
import com.yanzhenjie.permission.SettingService;

import com.google.android.gms.location.LocationListener;


import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN;

public class UbicacionPunto extends AppCompatActivity implements OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback, RationaleListener, LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMyLocationButtonClickListener
{

    private String latitud, longitud;
    vars vars;
    private GoogleMap mGoogleMap;
    boolean mapaIniciado = false;
    private static final int REQUEST_CODE_PERMISSION_OTHER = 200;
    private static final int REQUEST_CODE_PERMISSION_GEOLOCATION = 101;
    private static final int REQUEST_CODE_SETTING = 300;
    LocationRequest mLocationRequest;
    private static final long INTERVAL=5000;
    private static final long FASTEST_INTERVAL=1000;
    private Boolean mRequestingLocationUpdates;
    Location mCurrentLocation;
    GoogleApiClient mGoogleApiClient;
    LocationManager locationManager;
    private gestionSharedPreferences sharedPreferences;
    LinearLayout flMapa;
    private EditText editTextDireccionRecogida;
    private ImageView imageButtonBuscarDireccionOrigen;
    private ImageView ivSearchDisabled;
    private String direccionTextoClienteOrigen;
    private Context context;
    private Button botonSolicitarServicioDisabled;
    private Button botonSolicitarServicio;
    private Button botonContinuar;
    private String type;
    private String nomEmpresa;
    private String dirEmpresa;
    private String imaEmpresa;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ubicacion_punto);
        MapsInitializer.initialize(this);
        vars = new vars();
        context=this;

        getWindow().setSoftInputMode(SOFT_INPUT_ADJUST_PAN);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_servicio);
        mapFragment.getMapAsync(this);

        mRequestingLocationUpdates = false;

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //this line shows back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setElevation(0);

        toolbar.setNavigationOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                finish();
            }
        });

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                latitud = null;
                longitud = null;
                nomEmpresa = null;
                dirEmpresa = null;
                imaEmpresa = null;
            } else {
                latitud = extras.getString("latPunto");
                longitud = extras.getString("lonPunto");
                nomEmpresa = extras.getString("nomPunto");
                dirEmpresa = extras.getString("dirPunto");
                imaEmpresa = extras.getString("imaPunto");
            }
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            showGPSDisabledAlertToUser();
        }

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            finish();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    private ProgressDialog progressDialog;
    private double lng, lat=0;

    private void showGPSDisabledAlertToUser()
    {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(new ContextThemeWrapper(UbicacionPunto.this, R.style.AlertDialogTheme));
        builder
                .setTitle(R.string.app_name)
                .setMessage("Su GPS esta apagado, para que Kupi funcione correctamente debe encenderlo, ¿desea hacerlo?")
                .setPositiveButton("Activar GPS", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {
                        Intent callGPSSettingIntent = new Intent(
                                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(callGPSSettingIntent);
                    }
                }).setNegativeButton("Cancelar", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int id)
            {
                dialog.cancel();
            }
        }).setCancelable(false).show().getButton(DialogInterface.BUTTON_POSITIVE).
                setTextColor(getResources().getColor(R.color.colorPrimary));
    }

    Marker marker;
    Map<String, String> images = new HashMap<>();



    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mGoogleMap = googleMap;
        mGoogleMap.setOnMyLocationButtonClickListener(this);
        createLocationRequest();
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);

        AndPermission.with(this)
                .requestCode(REQUEST_CODE_PERMISSION_GEOLOCATION)
                .permission(
                        // Multiple permissions, array form.
                        Manifest.permission.ACCESS_FINE_LOCATION
                )
                .callback(permissionListener)
                .rationale(new RationaleListener()
                {
                    @Override
                    public void showRequestPermissionRationale(int requestCode, final Rationale rationale)
                    {
                        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(new ContextThemeWrapper(UbicacionPunto.this, R.style.AlertDialogTheme));
                        builder
                                .setTitle("Permiso de Geolocalización")
                                .setMessage("Para reportar un daño efectivamente, es necesario que apruebes el permiso de ubicación, para ello presiona el botón ACEPTAR.")
                                .setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id)
                                    {
                                        rationale.resume();
                                    }
                                }).setNegativeButton("CANCELAR", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                rationale.cancel();
                            }
                        }).setCancelable(false).show().getButton(DialogInterface.BUTTON_POSITIVE).
                                setTextColor(getResources().getColor(R.color.colorPrimary));
                    }
                })
                .start();


        marker = mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(latitud.toString()),
                Double.parseDouble(longitud))).icon(BitmapDescriptorFactory.fromResource(R.mipmap.marker))
                .title(nomEmpresa).snippet(dirEmpresa+""));

        images.put(marker.getId(), imaEmpresa.toString());
        //marker.setTag(p);
        //eventMarkerMap.put(marker,p);
        mGoogleMap.setInfoWindowAdapter(new CustomInfoWindow(getLayoutInflater().inflate(R.layout.custom_info_window, null)
                , null, images));

        //EVENTO DEL MARKER
        mGoogleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener()
        {
            @Override
            public void onInfoWindowClick(Marker arg0)
            {
                // TODO Auto-generated method stub
               /* PuntoConvenio eventInfo = (PuntoConvenio) arg0.getTag();
                Intent i=new Intent(MapaConvenios.this.getActivity(), DetalleMarkerConvenio.class);
                i.putExtra("codPunto",eventInfo.getCodPunto());
                i.putExtra("codTipo",eventInfo.getCodTipo());
                startActivity(i);
                ///arg0.hideInfoWindow();*/
            }
        });


    }

    @Override
    public void showRequestPermissionRationale(int requestCode, Rationale rationale)
    {
    }

    private PermissionListener permissionListener = new PermissionListener()
    {
        @Override
        public void onSucceed(int requestCode, @NonNull List<String> grantPermissions)
        {
            switch (requestCode)
            {
                case REQUEST_CODE_PERMISSION_GEOLOCATION:
                {
                    enableMyLocation();
                    break;
                }
                case REQUEST_CODE_PERMISSION_OTHER:
                {
                    break;
                }
            }
        }

        @Override
        public void onFailed(int requestCode, @NonNull List<String> deniedPermissions)
        {
            switch (requestCode)
            {
                case REQUEST_CODE_PERMISSION_GEOLOCATION:
                {
                    //getActivity().finish();
                    break;
                }
                case REQUEST_CODE_PERMISSION_OTHER:
                {
                    //Toast.makeText(UbicacionDanoMap.this, R.string.message_post_failed, Toast.LENGTH_SHORT).show();
                    break;
                }
            }

            if (AndPermission.hasAlwaysDeniedPermission(UbicacionPunto.this, deniedPermissions))
            {
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(new ContextThemeWrapper(UbicacionPunto.this,R.style.AlertDialogTheme));
                builder
                        .setTitle("Permiso de Geolocalización")
                        .setMessage("Vaya! parece que has denegado el acceso a tu ubicación. Presiona el botón Permitir, " +
                                "selecciona la opción Accesos y habilita la opción de Ubicación.")
                        .setPositiveButton("PERMITIR", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int id)
                            {
                                SettingService settingService = AndPermission.defineSettingDialog(UbicacionPunto.this, REQUEST_CODE_SETTING);
                                settingService.execute();
                            }
                        }).setNegativeButton("CANCELAR", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {

                    }
                }).setCancelable(false).show().getButton(DialogInterface.BUTTON_POSITIVE).
                        setTextColor(getResources().getColor(R.color.colorPrimary));
            }
        }
    };




    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode)
        {
            case REQUEST_CODE_SETTING:
            {
                enableMyLocation();
                break;
            }
        }
    }


    @Override
    public void onPause()
    {
        // TODO Auto-generated method stub
        super.onPause();
        mGoogleApiClient.connect();
    }

    private boolean isMoving;
    private String latitudOrigen;
    private String longitudOrigen;




    private void enableMyLocation()
    {
        if (ContextCompat.checkSelfPermission(UbicacionPunto.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
        }

        if (mGoogleMap != null) {

            if (ContextCompat.checkSelfPermission(UbicacionPunto.this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
            }

            if (mGoogleMap != null)
            {
                mGoogleMap.getUiSettings().setZoomControlsEnabled(false);
                mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
                mGoogleMap.setMyLocationEnabled(true);

            }
        }
    }

    private Geocoder geocoder;
    List<android.location.Address> direcciones;
    private String direccionesObtenidas;


    protected void startLocationUpdates()
    {
        if (ActivityCompat.checkSelfPermission(UbicacionPunto.this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED)
        {
            return;
        }

        else
        {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
            Log.d("Daño", "Location update started ..............: ");
        }
    }

    private boolean haveNetworkConnection()
    {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;
        ConnectivityManager cm = (ConnectivityManager) UbicacionPunto.this.getSystemService(Context.CONNECTIVITY_SERVICE);
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
    public void onResume()
    {
        super.onResume();

        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates)
        {
            startLocationUpdates();
        }

        if(haveNetworkConnection())
        {
            //updateTokenFCMToServer();
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        ControllerSingleton.getInstance().cancelPendingReq("updatetokenmapa");
        ControllerSingleton.getInstance().cancelPendingReq("getpuntosmapa");
    }

    @Override
    public void onStart()
    {
        super.onStart();
        Log.d("Daño", "onStart fired ..............");
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop()
    {
        super.onStop();
        Log.d("Daño", "onStop fired ..............");
        mGoogleApiClient.disconnect();
        Log.d("Daño", "isConnected ...............: " + mGoogleApiClient.isConnected());
    }

    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
        Log.d("Daño", "onConnected - isConnected ...............: " + mGoogleApiClient.isConnected());
        startLocationUpdates();
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
        Log.d("Daño", "Firing onLocationChanged.............................................."+mapaIniciado);
        mCurrentLocation = location;
        final LatLng latLng = new LatLng(Double.parseDouble(latitud), Double.parseDouble(longitud));
        mRequestingLocationUpdates = true;

        if(!mapaIniciado)
        {
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,17));
            mapaIniciado=true;
        }
    }



    protected void createLocationRequest()
    {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    //private HashMap<Marker, PuntoConvenio> eventMarkerMap;
    @Override
    public boolean onMyLocationButtonClick()
    {
        /*if (mGoogleMap != null && !(mCurrentLocation == null))
        {
            LatLng latLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,19));
            return true;
        }
        else
        {
            return false;
        }*/
        return true;
    }
}
