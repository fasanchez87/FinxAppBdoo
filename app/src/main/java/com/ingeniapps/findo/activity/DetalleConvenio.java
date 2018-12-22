package com.ingeniapps.findo.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.firebase.iid.FirebaseInstanceId;
import com.ingeniapps.findo.R;
import com.ingeniapps.findo.adapter.ConvenioAdapter;
import com.ingeniapps.findo.adapter.EndlessRecyclerViewScrollListener;
import com.ingeniapps.findo.adapter.RecyclerViewDisabler;
import com.ingeniapps.findo.beans.PuntoConvenio;
import com.ingeniapps.findo.fragment.Buscar;
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

import static android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN;

public class DetalleConvenio extends AppCompatActivity implements OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback, RationaleListener, LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMyLocationButtonClickListener
{

    private ArrayList<PuntoConvenio> listadoConvenios;
    private RecyclerView recycler_view_convenios;
    private ConvenioAdapter mAdapter;
    LinearLayoutManager mLayoutManager;
    LinearLayout linearHabilitarConvenios;
    RelativeLayout layoutNoFoundConvenios;
    private int pagina;
    Context context;
    private ProgressDialog progressDialog;
    EditText editTextBusquedaConvenio;
    TextView editTextNumConvenios;
    private Double latitud;
    private Double longitud;
    com.ingeniapps.findo.sharedPreferences.gestionSharedPreferences gestionSharedPreferences;
    private GoogleMap mGoogleMap;
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
    com.ingeniapps.findo.vars.vars vars;
    private InputMethodManager imm = null;
    private RelativeLayout layoutMacroEsperaConveniosFavoritos;
    private boolean solicitando=false;
    private boolean isLoaderMotion=false;
    private boolean isBuscando=false;
    private EndlessRecyclerViewScrollListener scrollListener;
    private RecyclerView.OnItemTouchListener disabler;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_convenio);

        super.onCreate(savedInstanceState);
        vars=new vars();
        gestionSharedPreferences=new gestionSharedPreferences(DetalleConvenio.this);
        getWindow().setSoftInputMode(SOFT_INPUT_ADJUST_PAN);
        createLocationRequest();
        disabler = new RecyclerViewDisabler();

        mRequestingLocationUpdates = false;


        mGoogleApiClient = new GoogleApiClient.Builder(DetalleConvenio.this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            showGPSDisabledAlertToUser();
        }

        progressDialog = new ProgressDialog(new android.support.v7.view.ContextThemeWrapper(DetalleConvenio.this,R.style.AppCompatAlertDialogStyle));
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Un momento...");

        listadoConvenios=new ArrayList<PuntoConvenio>();
        vars=new vars();
        context = DetalleConvenio.this;
        pagina=1;

        layoutMacroEsperaConveniosFavoritos=findViewById(R.id.layoutMacroEsperaConveniosFavoritos);

        imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

        editTextNumConvenios=(TextView)findViewById(R.id.editTextNumConvenios);
        editTextBusquedaConvenio=(EditText)findViewById(R.id.editTextBusquedaConvenio);
        linearHabilitarConvenios=(LinearLayout)findViewById(R.id.linearHabilitarConvenios);
        layoutNoFoundConvenios=(RelativeLayout)findViewById(R.id.layoutNoFoundConvenios);

        recycler_view_convenios=(RecyclerView) findViewById(R.id.recycler_view_convenios);
        mLayoutManager = new LinearLayoutManager(DetalleConvenio.this);

        mAdapter = new ConvenioAdapter(DetalleConvenio.this,listadoConvenios,new ConvenioAdapter.OnItemClickListener()
        {
            @Override
            public void onItemClick(PuntoConvenio convenio)
            {
                Intent i=new Intent(DetalleConvenio.this, DetalleMarkerConvenio.class);
                i.putExtra("codPunto",convenio.getCodPunto());
                startActivity(i);
            }
        });

        recycler_view_convenios.setHasFixedSize(true);
        recycler_view_convenios.setLayoutManager(mLayoutManager);
        recycler_view_convenios.setItemAnimator(new DefaultItemAnimator());
        recycler_view_convenios.setOverScrollMode(View.OVER_SCROLL_NEVER);
        recycler_view_convenios.setAdapter(mAdapter);

        scrollListener = new EndlessRecyclerViewScrollListener(mLayoutManager)
        {
            @Override
            public void onLoadMore(final int page, final int totalItemsCount, final RecyclerView view)
            {
                final int curSize = mAdapter.getItemCount();

                view.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if(!solicitando)
                        {
                            isLoaderMotion=true;
                            Log.i("onloadmore","cargando mas");
                            WebServiceGetPuntosConvenios(null);
                            //pagina+=1;
                        }
                    }
                });
            }
        };

        recycler_view_convenios.addOnScrollListener(scrollListener);

        ImageView buttonBuscar = (ImageView) findViewById(R.id.ivSearch);
        buttonBuscar.setClickable(true);
        buttonBuscar.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(editTextBusquedaConvenio.getText().length()>=1)
                {
                    recycler_view_convenios.removeOnScrollListener(scrollListener);
                    imm.hideSoftInputFromWindow(editTextBusquedaConvenio.getWindowToken(), 0);
                    progressDialog.show();
                    progressDialog.setCancelable(false);
                    //isBuscando=true;
                    //WebServiceGetPuntosConvenios(editTextBusquedaConvenio.getText().toString());
                    WebServiceGetPuntosConveniosP(editTextBusquedaConvenio.getText().toString());
                }
                else
                if(TextUtils.isEmpty(editTextBusquedaConvenio.getText()))
                {
                    editTextNumConvenios.setVisibility(View.GONE);
                    recycler_view_convenios.addOnScrollListener(scrollListener);
                    imm.hideSoftInputFromWindow(editTextBusquedaConvenio.getWindowToken(), 0);
                    progressDialog.show();
                    progressDialog.setCancelable(false);
                    isBuscando=false;
                    WebServiceGetPuntosConvenios(editTextBusquedaConvenio.getText().toString());
                }
               /* else
                if(editTextBusquedaConvenio.getText().length()<4)
                {
                    Snackbar.make(getActivity().findViewById(android.R.id.content),
                            "La busqueda debe tener mínimo (3) caracteres.", Snackbar.LENGTH_LONG).show();
                }*/
            }
        });

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

        updateTokenFCMToServer();

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
                headers.put("codEmpleado", gestionSharedPreferences.getString("codEmpleado"));
                headers.put("tokenFCM", ""+ FirebaseInstanceId.getInstance().getToken());
                return headers;
            }
        };
        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        ControllerSingleton.getInstance().cancelPendingReq("_requestbuscar");
    }

    private void showGPSDisabledAlertToUser()
    {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(new ContextThemeWrapper(DetalleConvenio.this, R.style.AlertDialogTheme));
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
                        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(new ContextThemeWrapper(DetalleConvenio.this, R.style.AlertDialogTheme));
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

            if (AndPermission.hasAlwaysDeniedPermission(DetalleConvenio.this, deniedPermissions))
            {
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(new ContextThemeWrapper(DetalleConvenio.this,R.style.AlertDialogTheme));
                builder
                        .setTitle("Permiso de Geolocalización")
                        .setMessage("Vaya! parece que has denegado el acceso a tu ubicación. Presiona el botón Permitir, " +
                                "selecciona la opción Accesos y habilita la opción de Ubicación.")
                        .setPositiveButton("PERMITIR", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int id)
                            {
                                SettingService settingService = AndPermission.defineSettingDialog(DetalleConvenio.this, REQUEST_CODE_SETTING);
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

    private void enableMyLocation()
    {
        if (ContextCompat.checkSelfPermission(DetalleConvenio.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
        {
        }

        if (mGoogleMap != null)
        {
            latitud=mCurrentLocation.getLatitude();
            longitud=mCurrentLocation.getLongitude();
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
    }

    protected void startLocationUpdates()
    {
        if (ActivityCompat.checkSelfPermission(DetalleConvenio.this, Manifest.permission.ACCESS_FINE_LOCATION) !=
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

    private boolean isLoad=false;

    @Override
    public void onLocationChanged(Location location)
    {
        mCurrentLocation = location;
        mRequestingLocationUpdates = true;
        latitud=mCurrentLocation.getLatitude();
        longitud=mCurrentLocation.getLongitude();

        if(!isLoad)
        {
            WebServiceGetPuntosConvenios(null);
        }
    }

    @Override
    public boolean onMyLocationButtonClick()
    {
        return false;
    }


    private void WebServiceGetPuntosConvenios(final String busqueda)
    {
        isLoad=true;
        String _urlWebService = vars.ipServer.concat("/ws/buscarConvenios");
        //Log.i("isLoaderMotion - loader", ""+isLoaderMotion);
        ///Log.i("isLoaderMotion - solicitando", ""+solicitando);
        Log.i("isLoaderMotion - isBuscando", ""+isBuscando);
        if(isBuscando)
        {
            mAdapter.setMoreDataAvailable(false);
        }


        if(isLoaderMotion && !isBuscando)
        {
            isLoaderMotion=false;
            solicitando=true;//PARA ESPERAR A QUE CARGUEN LOS DEMAS ITEMS Y LOGRAR OCULTAR EL PROGRESS.
            listadoConvenios.add(new PuntoConvenio("load"));
            mAdapter.notifyItemInserted(listadoConvenios.size()-1);
            pagina+=1;
        }


        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET, _urlWebService, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        try
                        {
                            if(response.getBoolean("status"))
                            {
                                listadoConvenios.clear();

                                JSONArray listaPuntosConvenios = response.getJSONArray("convenios");

                                double aux=999999999;
                                double distanciaActual=0;
                                double distancias[]=new double[listaPuntosConvenios.length()];

                                for (int i = 0; i < listaPuntosConvenios.length(); i++)
                                {
                                    JSONObject jsonObject = (JSONObject) listaPuntosConvenios.get(i);
                                    final PuntoConvenio c = new PuntoConvenio();
                                    c.setCodProveedor(jsonObject.getString("codProveedor"));
                                    c.setNomProveedor(jsonObject.getString("nomProveedor"));
                                    c.setImaProveedor(jsonObject.getString("imaProveedor"));
                                    c.setType(jsonObject.getString("type"));
                                    c.setNomCategoria(jsonObject.getString("nomCategoria"));
                                    c.setDirPunto(jsonObject.getString("dirPunto"));
                                    c.setDescPunto("Descuento del "+jsonObject.getString("descPunto")+"%");
                                    c.setLatPunto(jsonObject.getString("latPunto"));
                                    c.setLonPunto(jsonObject.getString("lonPunto"));
                                    c.setDistPunto(jsonObject.getString("dist"));
                                    c.setTimePunto(jsonObject.getString("time"));
                                    c.setNomCiudad(jsonObject.getString("nomCiudad"));
                                    c.setCalificacion(jsonObject.getString("numCalifica"));
                                    c.setCodTipo(jsonObject.getString("codTipo"));
                                    c.setNomCajero(jsonObject.getString("nomCajero"));
                                    c.setHorPunto(jsonObject.getString("horPunto"));
                                    editTextNumConvenios.setText(listaPuntosConvenios.length()+" Convenios encontrados");
                                    listadoConvenios.add(c);
                                }

                                layoutMacroEsperaConveniosFavoritos.setVisibility(View.GONE);
                                linearHabilitarConvenios.setVisibility(View.VISIBLE);
                                Log.i("resultado","resultado: bien");

                                if (!((Activity) context).isFinishing())
                                {
                                    if(progressDialog.isShowing())
                                    {
                                        progressDialog.dismiss();
                                    }
                                }

                            }
                            else
                            {
                                mAdapter.setMoreDataAvailable(false);
                                //layoutMacroEsperaConveniosFavoritos.setVisibility(View.GONE);
                                //linearHabilitarConvenios.setVisibility(View.VISIBLE);
                                Log.i("resultado","resultado: mal");
                                if (!((Activity) context).isFinishing())
                                {
                                    if(progressDialog.isShowing())
                                    {
                                        progressDialog.dismiss();
                                    }
                                }
                                Snackbar.make(findViewById(android.R.id.content),
                                        "No se encontraron resultados para '"+busqueda+"'", Snackbar.LENGTH_LONG).show();
                            }
                            solicitando=false;
                            mAdapter.notifyDataSetChanged();
                        }
                        catch (JSONException e)
                        {
                            if (!((Activity) context).isFinishing())
                            {
                                if (progressDialog.isShowing())
                                {
                                    progressDialog.dismiss();

                                }
                            }


                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(DetalleConvenio.this, R.style.AlertDialogTheme));
                            builder
                                    .setMessage(e.getMessage().toString())
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id) {
                                        }
                                    }).show();

                            e.printStackTrace();
                        }


                        //mAdapter.notifyDataSetChanged();
                        //recycler_view_convenios.removeOnItemTouchListener(disabler);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {


                        if (!((Activity) context).isFinishing())
                        {
                            if (progressDialog.isShowing())
                            {
                                progressDialog.dismiss();

                            }
                        }

                        Snackbar.make(findViewById(android.R.id.content),
                                ""+error.getMessage().toString(), Snackbar.LENGTH_LONG).show();

                        if (error instanceof TimeoutError)
                        {
                            if (!((Activity) context).isFinishing())
                            {
                                if (progressDialog.isShowing())
                                {
                                    progressDialog.dismiss();

                                }
                            }


                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(DetalleConvenio.this,R.style.AlertDialogTheme));
                            builder
                                    .setMessage("Error de conexión, sin respuesta del servidor.")
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).show();
                        }

                        else

                        if (error instanceof NoConnectionError)
                        {
                            if (!((Activity) context).isFinishing())
                            {
                                if (progressDialog.isShowing())
                                {
                                    progressDialog.dismiss();

                                }
                            }


                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(DetalleConvenio.this,R.style.AlertDialogTheme));
                            builder
                                    .setMessage("Por favor, conectese a la red.")
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).show();
                        }

                        else

                        if (error instanceof AuthFailureError)
                        {
                            if (!((Activity) context).isFinishing())
                            {
                                if (progressDialog.isShowing())
                                {
                                    progressDialog.dismiss();

                                }
                            }


                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(DetalleConvenio.this,R.style.AlertDialogTheme));
                            builder
                                    .setMessage("Error de autentificación en la red o el token de acceso no es valido. Cierra sesión e ingresa de nuevo por favor.")
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).show();
                        }

                        else

                        if (error instanceof ServerError)
                        {
                            if (!((Activity) context).isFinishing())
                            {
                                if (progressDialog.isShowing())
                                {
                                    progressDialog.dismiss();

                                }
                            }


                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(DetalleConvenio.this,R.style.AlertDialogTheme));
                            builder
                                    .setMessage("Error server, sin respuesta del servidor.")
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).show();
                        }

                        else

                        if (error instanceof NetworkError)
                        {
                            if (!((Activity) context).isFinishing())
                            {
                                if (progressDialog.isShowing())
                                {
                                    progressDialog.dismiss();

                                }
                            }


                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(DetalleConvenio.this,R.style.AlertDialogTheme));
                            builder
                                    .setMessage("Error de red, contacte a su proveedor de servicios.")
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).show();
                        }

                        else

                        if (error instanceof ParseError)
                        {
                            if (!((Activity) context).isFinishing())
                            {
                                if (progressDialog.isShowing())
                                {
                                    progressDialog.dismiss();

                                }
                            }


                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(DetalleConvenio.this,R.style.AlertDialogTheme));
                            builder
                                    .setMessage("Error de conversión Parser, contacte a su proveedor de servicios.")
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).show();
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
                headers.put("buscar", TextUtils.isEmpty(busqueda)?"":busqueda);
                headers.put("lat", String.valueOf(latitud));
                headers.put("lon", String.valueOf(longitud));
                headers.put("MyToken", gestionSharedPreferences.getString("MyToken"));
                headers.put("codEmpleado", gestionSharedPreferences.getString("codEmpleado"));
                headers.put("numIndex", String.valueOf(pagina));
                return headers;
            }
        };

        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "_requestbuscar");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    private void WebServiceGetPuntosConveniosP(final String busqueda)
    {

        String _urlWebService = vars.ipServer.concat("/ws/buscarConvenios");
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET, _urlWebService, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        try
                        {
                            if(response.getBoolean("status"))
                            {
                                listadoConvenios.clear();

                                JSONArray listaPuntosConvenios = response.getJSONArray("puntos");

                                double aux=999999999;
                                double distanciaActual=0;
                                double distancias[]=new double[listaPuntosConvenios.length()];

                                for (int i = 0; i < listaPuntosConvenios.length(); i++)
                                {
                                    JSONObject jsonObject = (JSONObject) listaPuntosConvenios.get(i);
                                    final PuntoConvenio c = new PuntoConvenio();
                                    c.setCodPunto(jsonObject.getString("codPunto"));
                                    c.setNomProveedor(jsonObject.getString("nomProveedor"));
                                    c.setImaProveedor(jsonObject.getString("imaProveedor"));
                                    c.setType(jsonObject.getString("type"));
                                    c.setDirPunto(jsonObject.getString("dirPunto"));

                                    if(TextUtils.equals(jsonObject.getString("descPunto"),"0"))
                                    {
                                        c.setDescPunto("0");
                                    }
                                    else
                                    {
                                        c.setDescPunto("Descuento del "+jsonObject.getString("descPunto")+"%");
                                    }



                                    c.setLatPunto(jsonObject.getString("latPunto"));
                                    c.setLonPunto(jsonObject.getString("lonPunto"));
                                    c.setDistPunto(jsonObject.getString("dist"));
                                    c.setTimePunto(jsonObject.getString("time"));
                                    c.setNomCiudad(jsonObject.getString("nomCiudad"));
                                    c.setCalificacion(jsonObject.getString("numCalifica"));
                                    c.setCodTipo(jsonObject.getString("codTipo"));
                                    c.setNomCajero(jsonObject.getString("nomCajero"));
                                    c.setHorPunto(jsonObject.getString("horPunto"));
                                    editTextNumConvenios.setText("Se han encontrado ("+listaPuntosConvenios.length()+") coincidencias.");
                                    listadoConvenios.add(c);
                                }

                                editTextNumConvenios.setVisibility(View.VISIBLE);

                                layoutMacroEsperaConveniosFavoritos.setVisibility(View.GONE);
                                linearHabilitarConvenios.setVisibility(View.VISIBLE);
                                Log.i("resultado","resultado: bien");

                                if (!((Activity) context).isFinishing())
                                {
                                    if(progressDialog.isShowing())
                                    {
                                        progressDialog.dismiss();
                                    }
                                }

                            }
                            else
                            {
                                //mAdapter.setMoreDataAvailable(false);
                                //layoutMacroEsperaConveniosFavoritos.setVisibility(View.GONE);
                                //linearHabilitarConvenios.setVisibility(View.VISIBLE);
                                Log.i("resultado","resultado: mal");
                                if (!((Activity) context).isFinishing())
                                {
                                    if(progressDialog.isShowing())
                                    {
                                        progressDialog.dismiss();
                                    }
                                }
                                Snackbar.make(findViewById(android.R.id.content),
                                        "No se encontraron resultados para '"+busqueda+"'", Snackbar.LENGTH_LONG).show();
                            }
                            solicitando=false;
                            mAdapter.notifyDataSetChanged();
                        }
                        catch (JSONException e)
                        {
                            if (!((Activity) context).isFinishing())
                            {
                                if (progressDialog.isShowing())
                                {
                                    progressDialog.dismiss();

                                }
                            }


                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(DetalleConvenio.this,R.style.AlertDialogTheme));
                            builder
                                    .setMessage(e.getMessage().toString())
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).show();

                            e.printStackTrace();
                        }
                        //mAdapter.notifyDataSetChanged();
                        //recycler_view_convenios.removeOnItemTouchListener(disabler);

                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {

                        if (!((Activity) context).isFinishing())
                        {
                            if (progressDialog.isShowing())
                            {
                                progressDialog.dismiss();

                            }
                        }

                        Snackbar.make(findViewById(android.R.id.content),
                                ""+error.getMessage().toString(), Snackbar.LENGTH_LONG).show();


                        if (error instanceof TimeoutError)
                        {
                            if (!((Activity) context).isFinishing())
                            {
                                if (progressDialog.isShowing())
                                {
                                    progressDialog.dismiss();

                                }
                            }


                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(DetalleConvenio.this,R.style.AlertDialogTheme));
                            builder
                                    .setMessage("Error de conexión, sin respuesta del servidor.")
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).show();
                        }

                        else

                        if (error instanceof NoConnectionError)
                        {
                            if (!((Activity) context).isFinishing())
                            {
                                if (progressDialog.isShowing())
                                {
                                    progressDialog.dismiss();

                                }
                            }


                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(DetalleConvenio.this,R.style.AlertDialogTheme));
                            builder
                                    .setMessage("Por favor, conectese a la red.")
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).show();
                        }

                        else

                        if (error instanceof AuthFailureError)
                        {
                            if (!((Activity) context).isFinishing())
                            {
                                if (progressDialog.isShowing())
                                {
                                    progressDialog.dismiss();

                                }
                            }


                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(DetalleConvenio.this,R.style.AlertDialogTheme));
                            builder
                                    .setMessage("Error de autentificación en la red o el token de acceso no es valido. Cierra sesión e ingresa de nuevo por favor.")
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).show();
                        }

                        else

                        if (error instanceof ServerError)
                        {
                            if (!((Activity) context).isFinishing())
                            {
                                if (progressDialog.isShowing())
                                {
                                    progressDialog.dismiss();

                                }
                            }


                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(DetalleConvenio.this,R.style.AlertDialogTheme));
                            builder
                                    .setMessage("Error server, sin respuesta del servidor.")
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).show();
                        }

                        else

                        if (error instanceof NetworkError)
                        {
                            if (!((Activity) context).isFinishing())
                            {
                                if (progressDialog.isShowing())
                                {
                                    progressDialog.dismiss();

                                }
                            }


                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(DetalleConvenio.this,R.style.AlertDialogTheme));
                            builder
                                    .setMessage("Error de red, contacte a su proveedor de servicios.")
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).show();
                        }

                        else

                        if (error instanceof ParseError)
                        {
                            if (!((Activity) context).isFinishing())
                            {
                                if (progressDialog.isShowing())
                                {
                                    progressDialog.dismiss();

                                }
                            }


                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(DetalleConvenio.this,R.style.AlertDialogTheme));
                            builder
                                    .setMessage("Error de conversión Parser, contacte a su proveedor de servicios.")
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).show();
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
                headers.put("buscar", TextUtils.isEmpty(busqueda)?"":busqueda);
                headers.put("lat", String.valueOf(latitud));
                headers.put("lon", String.valueOf(longitud));
                headers.put("MyToken", gestionSharedPreferences.getString("MyToken"));
                headers.put("codEmpleado", gestionSharedPreferences.getString("codEmpleado"));
                headers.put("numIndex", "0");
                return headers;
            }
        };

        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "_requestbuscar");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }
}
