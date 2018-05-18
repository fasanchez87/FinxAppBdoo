package com.ingeniapps.findo.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.ingeniapps.findo.R;
import com.ingeniapps.findo.activity.CustomInfoWindow;
import com.ingeniapps.findo.activity.DetalleMarkerConvenio;
import com.ingeniapps.findo.beans.PuntoConvenio;
import com.ingeniapps.findo.sharedPreferences.gestionSharedPreferences;
import com.ingeniapps.findo.vars.vars;
import com.ingeniapps.findo.volley.ControllerSingleton;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RationaleListener;
import com.yanzhenjie.permission.SettingService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.LOCATION_SERVICE;
import static android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN;

public class MapaConvenios extends Fragment implements OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback, RationaleListener, LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMyLocationButtonClickListener
{
    private GoogleMap mGoogleMap;
    boolean mapaIniciado = false;
    private static final int REQUEST_CODE_PERMISSION_OTHER = 200;
    private static final int REQUEST_CODE_PERMISSION_GEOLOCATION = 101;
    private static final int REQUEST_CODE_SETTING = 300;

    LocationRequest mLocationRequest;
    private static final long INTERVAL=5000;
    private static final long FASTEST_INTERVAL=1000;
    private static final int DISPLACEMENT=1000;
    private Boolean mRequestingLocationUpdates;
    Location mCurrentLocation;
    GoogleApiClient mGoogleApiClient;
    LocationManager locationManager;
    private gestionSharedPreferences sharedPreferences;

    private LinearLayout ll_espera_convenios;
    private LinearLayout ll_map_convenios;
    private LinearLayout ll_sin_resu;

    DatabaseReference ref;
    GeoFire geoFire;
    vars vars;

    ArrayList<PuntoConvenio>listadoPuntosConvenio;
    FrameLayout flMapa;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        sharedPreferences=new gestionSharedPreferences(getActivity().getApplicationContext());
        listadoPuntosConvenio=new ArrayList<PuntoConvenio>();
        vars=new vars();
        getActivity().getWindow().setSoftInputMode(SOFT_INPUT_ADJUST_PAN);
        ref=FirebaseDatabase.getInstance().getReference("ubicaciones");//UBICACIONES EN FIREBASE DATABASE CUANDO ESTAMOS EN MODO FRONT
        geoFire=new GeoFire(ref);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_mapa_convenios, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        MapsInitializer.initialize(MapaConvenios.this.getActivity());

        sharedPreferences = new gestionSharedPreferences(this.getActivity().getApplicationContext());

        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager()
                .findFragmentById(R.id.map_convenios);
        mapFragment.getMapAsync(this);

        flMapa=(FrameLayout)getActivity().findViewById(R.id.flMapa);

        mRequestingLocationUpdates = false;

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            showGPSDisabledAlertToUser();
        }

        ll_espera_convenios= (LinearLayout)getActivity().findViewById(R.id.ll_espera_convenios);
        ll_map_convenios= (LinearLayout)getActivity().findViewById(R.id.ll_map_convenios);
        ll_sin_resu= (LinearLayout)getActivity().findViewById(R.id.ll_sin_resu);
    }

    private void showGPSDisabledAlertToUser()
    {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(new ContextThemeWrapper(MapaConvenios.this.getActivity(), R.style.AlertDialogTheme));
        builder
                .setTitle(R.string.title)
                .setMessage("Su GPS esta apagado, para que Findo funcione correctamente debe encenderlo, ¿desea hacerlo?")
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

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mGoogleMap = googleMap;
        mGoogleMap.setOnMyLocationButtonClickListener(this);
        createLocationRequest();
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);

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
                        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(new ContextThemeWrapper(MapaConvenios.this.getActivity(), R.style.AlertDialogTheme));
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

            if (AndPermission.hasAlwaysDeniedPermission(MapaConvenios.this.getActivity(), deniedPermissions))
            {
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(new ContextThemeWrapper(MapaConvenios.this.getActivity(),R.style.AlertDialogTheme));
                builder
                        .setTitle("Permiso de Geolocalización")
                        .setMessage("Vaya! parece que has denegado el acceso a tu ubicación. Presiona el botón Permitir, " +
                                "selecciona la opción Accesos y habilita la opción de Ubicación.")
                        .setPositiveButton("PERMITIR", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int id)
                            {
                                SettingService settingService = AndPermission.defineSettingDialog(MapaConvenios.this.getActivity(), REQUEST_CODE_SETTING);
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

    /*@Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        switch (id)
        {
            case android.R.id.home:
            {
                MapaConvenios.this.getActivity().finish();
                enableMyLocation();
                break;
            }

            case R.id.menu_busqueda_convenio:
            {
                Intent buscar=new Intent(MapaConvenios.this.getActivity(),Buscar.class);
                buscar.putExtra("lat",mCurrentLocation.getLatitude());
                buscar.putExtra("lon",mCurrentLocation.getLongitude());
                startActivity(buscar);
                break;
            }
        }
        return true;
    }*/

    @Override
    public void onPause()
    {
        // TODO Auto-generated method stub
        super.onPause();
        mGoogleApiClient.connect();
    }


    private void enableMyLocation()
    {
        if (ContextCompat.checkSelfPermission(MapaConvenios.this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
        {
        }

        if (mGoogleMap != null)
        {
            WebServiceGetPuntos();
            mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
            mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
            mGoogleMap.setMyLocationEnabled(true);
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

    protected void startLocationUpdates()
    {
        if (ActivityCompat.checkSelfPermission(MapaConvenios.this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) !=
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
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
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
            updateTokenFCMToServer();
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        ControllerSingleton.getInstance().cancelPendingReq("updatetokenmapa");
        ControllerSingleton.getInstance().cancelPendingReq("getpuntosmapa");
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
                        //Toast.makeText(getActivity(), "Token FCM: " + "error"+error.getMessage(), Toast.LENGTH_LONG).show();

                    }
                })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("WWW-Authenticate", "xBasic realm=".concat(""));
                headers.put("codEmpleado", sharedPreferences.getString("codEmpleado"));
                headers.put("tokenFCM", ""+ FirebaseInstanceId.getInstance().getToken());
                return headers;
            }
        };
        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "updatetokenmapa");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
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
        LatLng latLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        mRequestingLocationUpdates = true;

        geoFire.setLocation(sharedPreferences.getString("codEmpleado"),new GeoLocation(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude()));

        if(!mapaIniciado)
        {
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(17));
            mapaIniciado=true;
        }
    }

    private HashMap<Marker, PuntoConvenio> eventMarkerMap;

    @Override
    public boolean onMyLocationButtonClick()
    {
        if (mGoogleMap != null && !(mCurrentLocation == null))
        {
            LatLng latLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(17));
            return true;
        }
        else
        {
            return false;
        }
    }

    Map<String, String> images = new HashMap<>();
    Marker marker;

    private void WebServiceGetPuntos()
    {
        String _urlWebService= vars.ipServer.concat("/ws/getPuntosConvenios");

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
                                JSONArray puntos=response.getJSONArray("puntos");
                                LatLng area;
                                GeoQuery geoQuery;

                                for (int i=0; i<puntos.length(); i++)
                                {
                                    JSONObject jsonObject = (JSONObject) puntos.get(i);
                                    eventMarkerMap = new HashMap<Marker, PuntoConvenio>();

                                    final PuntoConvenio p=new PuntoConvenio();
                                    p.setType(jsonObject.getString("type"));
                                    p.setCodTipo(jsonObject.getString("codTipo"));
                                    p.setImaProveedor(jsonObject.getString("imaProveedor"));

                                    if(TextUtils.equals(jsonObject.getString("codTipo"),"2"))//Cajero
                                    {
                                        p.setNomProveedor(jsonObject.getString("nomCajero"));
                                        p.setDescPunto(jsonObject.getString("horPunto"));
                                    }

                                    if(TextUtils.equals(jsonObject.getString("codTipo"),"1"))//Convenio
                                    {
                                        p.setNomProveedor(jsonObject.getString("nomProveedor"));
                                        p.setDescPunto(jsonObject.getString("descPunto"));
                                    }

                                    p.setCodPunto(jsonObject.getString("codPunto"));
                                    p.setCodProveedor(jsonObject.getString("codProveedor"));
                                    p.setCodCiudad(jsonObject.getString("codCiudad"));
                                    p.setSerPunto(jsonObject.getString("serPunto"));
                                    p.setDirPunto(jsonObject.getString("dirPunto"));
                                    p.setLatPunto(jsonObject.getString("latPunto"));

                                    p.setLonPunto(jsonObject.getString("lonPunto"));
                                    p.setConPunto(jsonObject.getString("conPunto"));
                                    p.setTelPunto(jsonObject.getString("telPunto"));
                                    p.setCorPunto(jsonObject.getString("corPunto"));
                                    p.setObsCiudad(jsonObject.getString("obsPunto"));

                                    if(TextUtils.equals(jsonObject.getString("codTipo"),"2"))//Cajero
                                    {
                                        marker = mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(p.getLatPunto().toString()),
                                                Double.parseDouble(p.getLonPunto()))).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_cajero))
                                                .title(p.getNomProveedor()).snippet(p.getDescPunto()+""));
                                    }

                                    if(TextUtils.equals(jsonObject.getString("codTipo"),"1"))//Convenio
                                    {
                                        marker = mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(p.getLatPunto().toString()),
                                                Double.parseDouble(p.getLonPunto()))).icon(BitmapDescriptorFactory.fromResource(R.mipmap.marker))
                                                .title(p.getNomProveedor()).snippet(p.getDescPunto()+"% Descuento"));
                                    }

                                    images.put(marker.getId(), p.getImaProveedor().toString());
                                    marker.setTag(p);
                                    eventMarkerMap.put(marker,p);
                                   /* area=new LatLng(Double.parseDouble(p.getLatPunto()),Double.parseDouble(p.getLonPunto()));
                                    mGoogleMap.addCircle(new CircleOptions().center(area).radius(50).strokeColor(Color.BLUE).fillColor(0x220000FF).strokeWidth(9.0f));

                                    geoQuery=geoFire.queryAtLocation(new GeoLocation(Double.parseDouble(p.getLatPunto()),Double.parseDouble(p.getLonPunto())),0.02f);

                                    geoQuery.addGeoQueryEventListener(new GeoQueryEventListener()
                                    {
                                        @Override
                                        public void onKeyEntered(String key, GeoLocation location)
                                        {
                                            Log.i("Move","Estas en: "+p.getNomProveedor()+"key: "+key+"latitud: "+location.latitude+" longitud: "+location.longitude);
                                        }

                                        @Override
                                        public void onKeyExited(String key)
                                        {
                                            Log.i("Move","Has salido de: "+p.getNomProveedor()+key);
                                        }

                                        @Override
                                        public void onKeyMoved(String key, GeoLocation location)
                                        {
                                            Log.i("Move","Estás moviendo en el area: ");
                                        }

                                        @Override
                                        public void onGeoQueryReady()
                                        {

                                        }

                                        @Override
                                        public void onGeoQueryError(DatabaseError error)
                                        {
                                            Log.i("Move","Error: "+error);

                                        }
                                    });*/
                                    listadoPuntosConvenio.add(p);

                                }

                                mGoogleMap.setInfoWindowAdapter(new CustomInfoWindow(getLayoutInflater().inflate(R.layout.custom_info_window, null)
                                        , null, images));

                                //EVENTO DEL MARKER
                                mGoogleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener()
                                {
                                    @Override
                                    public void onInfoWindowClick(Marker arg0)
                                    {
                                        // TODO Auto-generated method stub
                                        PuntoConvenio eventInfo = (PuntoConvenio) arg0.getTag();
                                        Intent i=new Intent(MapaConvenios.this.getActivity(), DetalleMarkerConvenio.class);
                                        i.putExtra("codPunto",eventInfo.getCodPunto());
                                        i.putExtra("codTipo",eventInfo.getCodTipo());
                                        startActivity(i);
                                        ///arg0.hideInfoWindow();
                                    }
                                });

                                ll_espera_convenios.setVisibility(View.GONE);
                                ll_map_convenios.setVisibility(View.VISIBLE);
                            }
                            else
                            {
                                ll_espera_convenios.setVisibility(View.GONE);
                                ll_map_convenios.setVisibility(View.VISIBLE);

                                Snackbar.make(flMapa, "No existen convenios en esta área. Revisa tu configuración", Snackbar.LENGTH_INDEFINITE)
                                        .setAction("Aceptar", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                            }
                                        })
                                        .show();
                            }
                        }
                        catch (JSONException e)
                        {
                            ll_espera_convenios.setVisibility(View.GONE);
                            ll_map_convenios.setVisibility(View.GONE);
                            ll_sin_resu.setVisibility(View.VISIBLE);

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(MapaConvenios.this.getActivity(),R.style.AlertDialogTheme));
                            builder
                                    .setTitle(R.string.title)
                                    .setMessage(e.getMessage().toString())
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).setCancelable(false).show().getButton(DialogInterface.BUTTON_POSITIVE).
                                    setTextColor(getResources().getColor(R.color.colorPrimary));
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        if (error instanceof TimeoutError)
                        {
                            ll_espera_convenios.setVisibility(View.GONE);
                            ll_map_convenios.setVisibility(View.GONE);
                            ll_sin_resu.setVisibility(View.VISIBLE);


                            Snackbar.make(flMapa, "Tiempo de espera agotado, por favor revisa tus datos.", Snackbar.LENGTH_INDEFINITE)
                                    .setAction("Aceptar", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                        }
                                    })
                                    .show();
                        }
                        else
                        if (error instanceof NoConnectionError)
                        {

                            ll_espera_convenios.setVisibility(View.GONE);
                            ll_map_convenios.setVisibility(View.GONE);
                            ll_sin_resu.setVisibility(View.VISIBLE);


                            Snackbar.make(flMapa, "Sin conexión a internet, por favor revisa tus datos.", Snackbar.LENGTH_INDEFINITE)
                                    .setAction("Aceptar", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                        }
                                    })
                                    .show();
                        }

                        else

                        if (error instanceof AuthFailureError)
                        {
                            ll_espera_convenios.setVisibility(View.GONE);
                            ll_map_convenios.setVisibility(View.GONE);
                            ll_sin_resu.setVisibility(View.VISIBLE);


                            Snackbar.make(flMapa, "Error token de acceso, cierra sesion e ingresa de nuevo.", Snackbar.LENGTH_INDEFINITE)
                                    .setAction("Aceptar", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                        }
                                    })
                                    .show();
                        }

                        else

                        if (error instanceof ServerError)
                        {
                            ll_espera_convenios.setVisibility(View.GONE);
                            ll_map_convenios.setVisibility(View.GONE);
                            ll_sin_resu.setVisibility(View.VISIBLE);

                            Snackbar.make(flMapa, "Existe una falla en el servidor. Intenta ingresar en un momento.", Snackbar.LENGTH_INDEFINITE)
                                    .setAction("Aceptar", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                        }
                                    })
                                    .show();
                        }
                        else
                        if (error instanceof NetworkError)
                        {
                            ll_espera_convenios.setVisibility(View.GONE);
                            ll_map_convenios.setVisibility(View.GONE);
                            ll_sin_resu.setVisibility(View.VISIBLE);

                            Snackbar.make(flMapa, "Existe una falla en su red de internet. Revise su plan de datos.", Snackbar.LENGTH_INDEFINITE)
                                    .setAction("Aceptar", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                        }
                                    })
                                    .show();
                        }
                        else
                        if (error instanceof ParseError)
                        {
                            ll_espera_convenios.setVisibility(View.GONE);
                            ll_map_convenios.setVisibility(View.GONE);
                            ll_sin_resu.setVisibility(View.VISIBLE);

                            Snackbar.make(flMapa, "Existe una falla en la respuesta del servidor. Por favor contactanos.", Snackbar.LENGTH_INDEFINITE)
                                    .setAction("Aceptar", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                        }
                                    })
                                    .show();
                        }
                    }
                })
        {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                HashMap<String, String> headers = new HashMap <String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("WWW-Authenticate", "xBasic realm=".concat(""));
                headers.put("codEmpleado", sharedPreferences.getString("codEmpleado"));
                //headers.put("MyToken", sharedPreferences.getString("MyToken"));
               // headers.put("tokenFCM", ""+tokenFCM);
                return headers;
            }
        };

        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "getpuntosmapa");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }
}
