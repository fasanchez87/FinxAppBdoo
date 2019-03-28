package com.ingeniapps.findo.fragment;

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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.ingeniapps.findo.activity.Categorias;
import com.ingeniapps.findo.activity.DetalleConvenio;
import com.ingeniapps.findo.adapter.ConvenioAdapter;
import com.ingeniapps.findo.adapter.RecyclerViewDisabler;
import com.ingeniapps.findo.beans.PuntoConvenio;
import com.ingeniapps.findo.sharedPreferences.gestionSharedPreferences;
import com.ingeniapps.findo.util.PaginationScrollListener;
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


public class Buscar extends Fragment implements OnMapReadyCallback,
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
    vars vars;
    private InputMethodManager imm = null;
    private RelativeLayout layoutMacroEsperaConveniosFavoritos;
    private boolean isBuscando=false;

    private static final int PAGE_START = 0;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private int TOTAL_PAGES;
    private int currentPage = PAGE_START;

    public Buscar()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        vars=new vars();
        gestionSharedPreferences=new gestionSharedPreferences(Buscar.this.getActivity());
        getActivity().getWindow().setSoftInputMode(SOFT_INPUT_ADJUST_PAN);
        createLocationRequest();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_buscar, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
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

        progressDialog = new ProgressDialog(new android.support.v7.view.ContextThemeWrapper(Buscar.this.getActivity(),R.style.AppCompatAlertDialogStyle));
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Un momento...");

        listadoConvenios=new ArrayList<PuntoConvenio>();
        vars=new vars();
        context = Buscar.this.getActivity();
        pagina=1;

        layoutMacroEsperaConveniosFavoritos=getActivity().findViewById(R.id.layoutMacroEsperaConveniosFavoritos);

        imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        editTextNumConvenios=(TextView)getActivity().findViewById(R.id.editTextNumConvenios);
        editTextBusquedaConvenio=(EditText)getActivity().findViewById(R.id.editTextBusquedaConvenio);
        linearHabilitarConvenios=(LinearLayout)getActivity().findViewById(R.id.linearHabilitarConvenios);
        layoutNoFoundConvenios=(RelativeLayout)getActivity().findViewById(R.id.layoutNoFoundConvenios);

        recycler_view_convenios=(RecyclerView) getActivity().findViewById(R.id.recycler_view_convenios);
        mLayoutManager = new LinearLayoutManager(Buscar.this.getActivity(),LinearLayoutManager.VERTICAL, false);

        mAdapter = new ConvenioAdapter(Buscar.this.getActivity(),new ConvenioAdapter.OnItemClickListener()
        {
            @Override
            public void onItemClick(PuntoConvenio convenio)
            {
                Intent i=new Intent(Buscar.this.getActivity(), DetalleConvenio.class);
                i.putExtra("codProveedor",convenio.getCodProveedor());
                startActivityForResult(i, 1);
            }
        });

        recycler_view_convenios.setHasFixedSize(true);
        recycler_view_convenios.setLayoutManager(mLayoutManager);
        recycler_view_convenios.setItemAnimator(new DefaultItemAnimator());
        recycler_view_convenios.setOverScrollMode(View.OVER_SCROLL_NEVER);
        recycler_view_convenios.setAdapter(mAdapter);

        recycler_view_convenios.addOnScrollListener(new PaginationScrollListener(mLayoutManager)
        {
            @Override
            protected void loadMoreItems()
            {
                isLoading = true;
                currentPage += 1;
                Log.i("currentPage",""+currentPage);
                WebServiceGetPuntosConveniosMore(null);
            }

            @Override
            public int getTotalPageCount() {
                return TOTAL_PAGES;
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }


        });

        ImageView buttonBuscar = (ImageView) getActivity().findViewById(R.id.ivSearch);
        buttonBuscar.setClickable(true);
        buttonBuscar.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(editTextBusquedaConvenio.getText().length()>=1)
                {
                    imm.hideSoftInputFromWindow(editTextBusquedaConvenio.getWindowToken(), 0);
                    progressDialog.show();
                    progressDialog.setCancelable(false);
                    isBuscando=true;
                    mAdapter.clear();
                    categos="";
                    WebServiceFinderConvenio(editTextBusquedaConvenio.getText().toString());
                }
                else
                if(TextUtils.isEmpty(editTextBusquedaConvenio.getText()))
                {
                    isLastPage=false;
                    currentPage=0;
                    isLoad=false;
                    editTextNumConvenios.setVisibility(View.GONE);
                    imm.hideSoftInputFromWindow(editTextBusquedaConvenio.getWindowToken(), 0);
                    progressDialog.show();
                    progressDialog.setCancelable(false);
                    isBuscando=false;
                    mAdapter.clear();
                    WebServiceGetPuntosConvenios(null);
                }
            }
        });

        ImageView ivFiltro = (ImageView) getActivity().findViewById(R.id.ivFiltro);
        ivFiltro.setClickable(true);
        ivFiltro.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent i=new Intent(Buscar.this.getActivity(), Categorias.class);
                startActivityForResult(i,1);
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
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(new ContextThemeWrapper(Buscar.this.getActivity(), R.style.AlertDialogTheme));
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
                        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(new ContextThemeWrapper(Buscar.this.getActivity(), R.style.AlertDialogTheme));
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
                    break;
                }
                case REQUEST_CODE_PERMISSION_OTHER:
                {
                    break;
                }
            }

            if (AndPermission.hasAlwaysDeniedPermission(Buscar.this.getActivity(), deniedPermissions))
            {
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(new ContextThemeWrapper(Buscar.this.getActivity(),R.style.AlertDialogTheme));
                builder
                        .setTitle("Permiso de Geolocalización")
                        .setMessage("Vaya! parece que has denegado el acceso a tu ubicación. Presiona el botón Permitir, " +
                                "selecciona la opción Accesos y habilita la opción de Ubicación.")
                        .setPositiveButton("PERMITIR", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int id)
                            {
                                SettingService settingService = AndPermission.defineSettingDialog(Buscar.this.getActivity(), REQUEST_CODE_SETTING);
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

    private ArrayList<String> categorias;
    private String categos="";

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_CODE_SETTING)
        {
            enableMyLocation();
        }

        if (resultCode == 1)
        {
            categos="";
            categorias = data.getStringArrayListExtra("categorias");
            for(int i=0; i<categorias.size(); i++){
                categos+=categorias.get(i)+",";
            }
            categos=categos.replaceAll(",$", "");
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.detach(this).attach(this).commit();
            WebServiceFinderConvenio(null);
        }
        if (resultCode == Activity.RESULT_CANCELED)
        {
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
        if (ContextCompat.checkSelfPermission(Buscar.this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
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
        if (ActivityCompat.checkSelfPermission(Buscar.this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) !=
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
        String _urlWebService = vars.ipServer.concat("/ws/buscarConveniosGeneralPruebas");
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
                                TOTAL_PAGES= Integer.parseInt(response.getString("numPaginas"));

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
                                    editTextNumConvenios.setText(listaPuntosConvenios.length()+" Convenios encontrados");
                                    listadoConvenios.add(c);
                                }

                                layoutMacroEsperaConveniosFavoritos.setVisibility(View.GONE);
                                linearHabilitarConvenios.setVisibility(View.VISIBLE);

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
                                if (!((Activity) context).isFinishing())
                                {
                                    if(progressDialog.isShowing())
                                    {
                                        progressDialog.dismiss();
                                    }
                                }
                                Snackbar.make(getActivity().findViewById(android.R.id.content),
                                        "No se encontraron resultados para '"+busqueda+"'", Snackbar.LENGTH_LONG).show();
                            }

                            mAdapter.addAll(listadoConvenios);

                            if (currentPage <= TOTAL_PAGES)
                            {
                                if(!isBuscando)
                                {
                                    mAdapter.addLoadingFooter();
                                    Log.i("Finder","currentPage: "+currentPage);
                                    Log.i("Finder","TOTAL_PAGES: "+TOTAL_PAGES);
                                }
                            }
                            else
                            {
                                isLastPage = true;
                            }
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


                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Buscar.this.getActivity(), R.style.AlertDialogTheme));
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

                        Snackbar.make(getActivity().findViewById(android.R.id.content),
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


                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Buscar.this.getActivity(),R.style.AlertDialogTheme));
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


                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Buscar.this.getActivity(),R.style.AlertDialogTheme));
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


                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Buscar.this.getActivity(),R.style.AlertDialogTheme));
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


                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Buscar.this.getActivity(),R.style.AlertDialogTheme));
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


                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Buscar.this.getActivity(),R.style.AlertDialogTheme));
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


                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Buscar.this.getActivity(),R.style.AlertDialogTheme));
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
                //headers.put("numIndex", String.valueOf(pagina));
                headers.put("numIndex", String.valueOf(currentPage));
                headers.put("categos", ""+categos);
                return headers;
            }
        };

        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "_requestbuscar");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    private void WebServiceGetPuntosConveniosMore(final String busqueda)
    {
        isLoad=true;
        String _urlWebService = vars.ipServer.concat("/ws/buscarConveniosGeneralPruebas");//buscarConveniosGeneral es el produccion
        Log.i("isLoaderMotion - isBuscando", ""+isBuscando);
        Log.i("Paginador", ""+String.valueOf(pagina));

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
                                Log.i("resultado","resultado: mal");
                                if (!((Activity) context).isFinishing())
                                {
                                    if(progressDialog.isShowing())
                                    {
                                        progressDialog.dismiss();
                                    }
                                }
                                Snackbar.make(getActivity().findViewById(android.R.id.content),
                                        "No se encontraron resultados para '"+busqueda+"'", Snackbar.LENGTH_LONG).show();
                            }

                            mAdapter.removeLoadingFooter();
                            isLoading = false;
                            mAdapter.addAll(listadoConvenios);
                            if (currentPage != TOTAL_PAGES) mAdapter.addLoadingFooter();
                            else isLastPage = true;
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

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Buscar.this.getActivity(), R.style.AlertDialogTheme));
                            builder
                                    .setMessage(e.getMessage().toString())
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id) {
                                        }
                                    }).show();

                            e.printStackTrace();
                        }
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

                        Snackbar.make(getActivity().findViewById(android.R.id.content),
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

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Buscar.this.getActivity(),R.style.AlertDialogTheme));
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
                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Buscar.this.getActivity(),R.style.AlertDialogTheme));
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
                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Buscar.this.getActivity(),R.style.AlertDialogTheme));
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
                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Buscar.this.getActivity(),R.style.AlertDialogTheme));
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
                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Buscar.this.getActivity(),R.style.AlertDialogTheme));
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
                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Buscar.this.getActivity(),R.style.AlertDialogTheme));
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
                headers.put("numIndex", String.valueOf(currentPage));
                return headers;
            }
        };

        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "_requestbuscar");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    private void WebServiceFinderConvenio(final String busqueda)
    {
        String _urlWebService = vars.ipServer.concat("/ws/buscarConveniosGeneral");
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
                                    listadoConvenios.add(c);
                                }

                                if(listadoConvenios.size()==1)
                                {
                                    editTextNumConvenios.setText("Se ha encontrado ("+listaPuntosConvenios.length()+") coincidencia.");
                                }
                                else
                                {
                                    editTextNumConvenios.setText("Se han encontrado ("+listaPuntosConvenios.length()+") coincidencias.");
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

                                isLoading = false;
                                mAdapter.addAll(listadoConvenios);
                                isLastPage = true;
                            }
                            else
                            {
                                Log.i("resultado","resultado: mal");
                                if (!((Activity) context).isFinishing())
                                {
                                    if(progressDialog.isShowing())
                                    {
                                        progressDialog.dismiss();
                                    }
                                }
                                Snackbar.make(getActivity().findViewById(android.R.id.content),
                                        "No se encontraron resultados para '"+busqueda+"'", Snackbar.LENGTH_LONG).show();
                            }
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

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Buscar.this.getActivity(),R.style.AlertDialogTheme));
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

                        Snackbar.make(getActivity().findViewById(android.R.id.content),
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

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Buscar.this.getActivity(),R.style.AlertDialogTheme));
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

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Buscar.this.getActivity(),R.style.AlertDialogTheme));
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

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Buscar.this.getActivity(),R.style.AlertDialogTheme));
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

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Buscar.this.getActivity(),R.style.AlertDialogTheme));
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

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Buscar.this.getActivity(),R.style.AlertDialogTheme));
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

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Buscar.this.getActivity(),R.style.AlertDialogTheme));
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
                headers.put("categos", TextUtils.isEmpty(categos)?"":categos);
                headers.put("lat", String.valueOf(latitud));
                headers.put("lon", String.valueOf(longitud));
                headers.put("MyToken", gestionSharedPreferences.getString("MyToken"));
                headers.put("codEmpleado", gestionSharedPreferences.getString("codEmpleado"));
                return headers;
            }
        };

        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "_requestbuscar");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }
}
