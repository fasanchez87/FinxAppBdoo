package com.ingeniapps.findo.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

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
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.iid.FirebaseInstanceId;
import com.ingeniapps.findo.R;
import com.ingeniapps.findo.adapter.CiudadAdapter;
import com.ingeniapps.findo.beans.Ciudad;
import com.ingeniapps.findo.sharedPreferences.gestionSharedPreferences;
import com.ingeniapps.findo.vars.vars;
import com.ingeniapps.findo.volley.ControllerSingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ConfigCiudad extends AppCompatActivity
{
    private gestionSharedPreferences sharedPreferences;
    private ArrayList<Ciudad> listadoCiudades;
    public com.ingeniapps.findo.vars.vars vars;
    private RecyclerView recycler_view_ciudades;
    private CheckBox checkAllCiudad;
    private CiudadAdapter mAdapter;
    LinearLayoutManager mLayoutManager;
    RelativeLayout layoutEsperaCiudades;
    RelativeLayout relativeLayoutCiudades;
    RelativeLayout layoutMacroEsperaCiudades;
    RelativeLayout rlConfigCiudad;
    LinearLayout ll_sin_resu;
    private int pagina;
    Context context;
    private boolean solicitando=false;
    //VERSION DEL APP INSTALADA
    private String versionActualApp;
    private Button btnFinConfigCiudad;
    private String ciudadesSelected;
    private boolean configInterna;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private String tokenFCM;
    private static String idDevice = null;
    public String currentVersion = null;
    private String ciudadesEscogidas;
    private Boolean guardarSesion;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null)
        {
            Bundle extras = getIntent().getExtras();
            if (extras == null)
            {
                configInterna = false;
            }
            else
            {
                configInterna=extras.getBoolean("configInterna");
            }
        }

        if(!configInterna)
        {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        idDevice=getIDDevice(this);



        try
        {
            currentVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        }
        catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }

        setContentView(R.layout.activity_config_ciudad);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        sharedPreferences=new gestionSharedPreferences(this);
        listadoCiudades=new ArrayList<Ciudad>();
        checkAllCiudad = (CheckBox) findViewById(R.id.checkAllCiudad);

        if(checkPlayServices())
        {
            if(!TextUtils.isEmpty(FirebaseInstanceId.getInstance().getToken()))
            {
                tokenFCM=FirebaseInstanceId.getInstance().getToken();
            }
        }

        //COMPROBAMOS LA SESION DEL USUARIO
        guardarSesion=sharedPreferences.getBoolean("GuardarSesion");
        if (guardarSesion==true)
        {
            if(!configInterna)
            {
                cargarActivityPrincipal();
            }
        }

        vars=new vars();
        context = this;
        pagina=0;
        ciudadesSelected="";

        btnFinConfigCiudad=(Button)findViewById(R.id.btnFinConfigCiudad);

        layoutEsperaCiudades=(RelativeLayout)findViewById(R.id.layoutEsperaCiudades);
        relativeLayoutCiudades=(RelativeLayout)findViewById(R.id.relativeLayoutCiudades);
        layoutMacroEsperaCiudades=(RelativeLayout)findViewById(R.id.layoutMacroEsperaCiudades);
        rlConfigCiudad=(RelativeLayout)findViewById(R.id.rlConfigCiudad);
        recycler_view_ciudades=(RecyclerView) findViewById(R.id.recycler_view_ciudades);
        //QUITAR EFECTO RECICLER VIEW
        recycler_view_ciudades.setOverScrollMode(View.OVER_SCROLL_NEVER);

        ll_sin_resu=(LinearLayout) findViewById(R.id.ll_sin_resu);

        mLayoutManager = new LinearLayoutManager(this);

        try
        {
            versionActualApp=getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        }
        catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }

        btnFinConfigCiudad.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                List<Ciudad> ciudadesSeleccionadas = ((CiudadAdapter) mAdapter).getCiudadList();

                for (int i=0; i<ciudadesSeleccionadas.size(); i++)
                {
                    Ciudad ciudad = ciudadesSeleccionadas.get(i);

                    if (ciudad.isSelected()==true)
                    {
                        ciudadesSelected=ciudadesSelected + "," + ciudad.getCodCiudad().toString();
                    }
                }

                ciudadesEscogidas=TextUtils.isEmpty(ciudadesSelected)?null:ciudadesSelected.substring(1);

                if(TextUtils.isEmpty(ciudadesEscogidas))
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(new android.support.v7.view.ContextThemeWrapper(ConfigCiudad.this, R.style.AlertDialogTheme));
                    builder
                            .setTitle(R.string.title)
                            .setMessage("Debes seleccionar mínimo (1) ciudad.")
                            .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int id)
                                {
                                }
                            }).show();
                }
                else
                {
                    if(TextUtils.isEmpty(idDevice))
                    {
                        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(new ContextThemeWrapper(ConfigCiudad.this,R.style.AlertDialogTheme));
                        builder
                                .setTitle(R.string.title)
                                .setMessage("No ha sido posible obtener el identificador unico UUID de su dispositivo, por favor contactenos o vuelve a intentar el ingreso.")
                                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id)
                                    {

                                    }
                                }).setCancelable(false).show().getButton(DialogInterface.BUTTON_POSITIVE).
                                setTextColor(getResources().getColor(R.color.colorPrimary));
                        return;
                    }

                    WebServiceRegistro();
                }

            }
        });

        WebServiceGetCiudades();

        checkAllCiudad.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (checkAllCiudad.isChecked())
                {
                    for (Ciudad model : listadoCiudades)
                    {
                        model.setSelected(true);
                    }
                }
                else
                {
                    for (Ciudad model : listadoCiudades)
                    {
                        model.setSelected(false);
                    }
                }

                mAdapter.notifyDataSetChanged();
            }
        });
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            finish();
        }

        return super.onKeyDown(keyCode, event);
    }

    public void cargarActivityPrincipal()
    {
        Intent intent = new Intent(ConfigCiudad.this, Principal.class);
        //intent.putExtra("indCambioClv",gestionSharedPreferences.getString("indCambioClv"));
        //intent.putExtra("nomEmpleado",gestionSharedPreferences.getString("nomEmpleado"));
        startActivity(intent);
        ConfigCiudad.this.finish();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        ControllerSingleton.getInstance().cancelPendingReq("_requestgetciudades");
        ControllerSingleton.getInstance().cancelPendingReq("registroUsuario");
    }

    private void WebServiceGetCiudades()
    {
        listadoCiudades.clear();
        String _urlWebService = vars.ipServer.concat("/ws/getCiudades");

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
                                JSONArray listaCiudades = response.getJSONArray("ciudades");
                                Log.i("findo","entro");

                                for (int i = 0; i < listaCiudades.length(); i++)
                                {
                                    JSONObject objectCiudad = (JSONObject) listaCiudades.get(i);
                                    Ciudad ciudad = new Ciudad();
                                    ciudad.setNomCiudad(objectCiudad.getString("nomCiudad"));
                                    ciudad.setCodCiudad(objectCiudad.getString("codCiudad"));
                                    ciudad.setType(objectCiudad.getString("type"));
                                    ciudad.setSelected(objectCiudad.getBoolean("indCheck"));
                                    listadoCiudades.add(ciudad);
                                }

                                mAdapter = new CiudadAdapter(ConfigCiudad.this,listadoCiudades);

                                recycler_view_ciudades.setHasFixedSize(true);
                                recycler_view_ciudades.setItemAnimator(new DefaultItemAnimator());
                                recycler_view_ciudades.setAdapter(mAdapter);
                                recycler_view_ciudades.setLayoutManager(mLayoutManager);

                                layoutMacroEsperaCiudades.setVisibility(View.GONE);
                                relativeLayoutCiudades.setVisibility(View.VISIBLE);
                                checkAllCiudad.setVisibility(View.VISIBLE);

                            }

                            else
                            {
                                layoutMacroEsperaCiudades.setVisibility(View.VISIBLE);
                                ll_sin_resu.setVisibility(View.VISIBLE);
                            }
                        }
                        catch (JSONException e)
                        {
                            //layoutMacroEsperaCiudades.setVisibility(View.VISIBLE);
                            layoutMacroEsperaCiudades.setVisibility(View.GONE);
                            ll_sin_resu.setVisibility(View.VISIBLE);

                            Snackbar.make(rlConfigCiudad, "Error al obtener puntos convenio. Por favor contactanos.", Snackbar.LENGTH_INDEFINITE)
                                    .setAction("Aceptar", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                        }
                                    })
                                    .show();
                        }
                        mAdapter.notifyDataSetChanged();
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        if (error instanceof TimeoutError)
                        {
                            //layoutMacroEsperaCiudades.setVisibility(View.VISIBLE);
                            layoutMacroEsperaCiudades.setVisibility(View.GONE);
                            ll_sin_resu.setVisibility(View.VISIBLE);

                            Snackbar.make(rlConfigCiudad, "Tiempo de espera agotado, por favor revisa tus datos.", Snackbar.LENGTH_INDEFINITE)
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
                            //layoutMacroEsperaCiudades.setVisibility(View.VISIBLE);
                            layoutMacroEsperaCiudades.setVisibility(View.GONE);
                            ll_sin_resu.setVisibility(View.VISIBLE);

                            Snackbar.make(rlConfigCiudad, "Sin conexión a internet, por favor revisa tus datos.", Snackbar.LENGTH_INDEFINITE)
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
                            //layoutMacroEsperaCiudades.setVisibility(View.VISIBLE);
                            layoutMacroEsperaCiudades.setVisibility(View.GONE);
                            ll_sin_resu.setVisibility(View.VISIBLE);


                            Snackbar.make(rlConfigCiudad, "Error token de acceso, cierra sesion e ingresa de nuevo.", Snackbar.LENGTH_INDEFINITE)
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
                            //layoutMacroEsperaCiudades.setVisibility(View.VISIBLE);
                            layoutMacroEsperaCiudades.setVisibility(View.GONE);
                            ll_sin_resu.setVisibility(View.VISIBLE);

                            Snackbar.make(rlConfigCiudad, "Existe una falla en el servidor. Intenta ingresar en un momento.", Snackbar.LENGTH_INDEFINITE)
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
                            //layoutMacroEsperaCiudades.setVisibility(View.VISIBLE);
                            layoutMacroEsperaCiudades.setVisibility(View.GONE);
                            ll_sin_resu.setVisibility(View.VISIBLE);

                            Snackbar.make(rlConfigCiudad, "Existe una falla en su red de internet. Revise su plan de datos.", Snackbar.LENGTH_INDEFINITE)
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
                            //layoutMacroEsperaCiudades.setVisibility(View.VISIBLE);
                            layoutMacroEsperaCiudades.setVisibility(View.GONE);
                            ll_sin_resu.setVisibility(View.VISIBLE);

                            Snackbar.make(rlConfigCiudad, "Existe una falla en la respuesta del servidor. Por favor contactanos.", Snackbar.LENGTH_INDEFINITE)
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
                headers.put("MyToken", sharedPreferences.getString("MyToken"));
                headers.put("codEmpleado", TextUtils.isEmpty(sharedPreferences.getString("codEmpleado"))?null:sharedPreferences.getString("codEmpleado"));
                headers.put("tokenFCM", ""+FirebaseInstanceId.getInstance().getToken());
                return headers;
            }
        };

        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "_requestgetciudades");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }

    public synchronized static String getIDDevice(Context context)
    {
        if (idDevice == null)
        {
            idDevice = UUID.randomUUID().toString();
        }

        return idDevice;
    }

    private void WebServiceRegistro()
    {
        String _urlWebService=vars.ipServer.concat("/ws/InsertDevice");

        JsonObjectRequest jsonObjReq=new JsonObjectRequest(Request.Method.POST, _urlWebService, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        try
                        {
                            boolean status=response.getBoolean("status");
                            String message=response.getString("message");
                            /*boolean status=response.getBoolean("status");
                            boolean sesionAbierta=response.getBoolean("sesionAbierta");
                            String message=response.getString("message");*/
                            if(status)//SI NO HA INICIADO SESION Y EXISTE
                            {
                                Intent i=new Intent(ConfigCiudad.this,ConfigCategoria.class);
                                i.putExtra("ciudadesEscogidas",ciudadesEscogidas);
                                i.putExtra("configInterna",configInterna);//SI ES PARA CONFIGURAR/MODIFICAR UNA VEZ ESTE IDENTIFICO E INGRESADO EN EL APP
                                sharedPreferences.putString("codEmpleado",""+response.getString("codEmpleado"));
                                sharedPreferences.putString("indConfigInicial",""+response.getString("indConfigInicial"));//CONFIGURACION INICIAL
                                startActivity(i);
                                finish();
                            }
                            else
                            {
                                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(new ContextThemeWrapper(ConfigCiudad.this,R.style.AlertDialogTheme));
                                builder
                                        .setTitle(R.string.title)
                                        .setMessage("Error registrando el usuario, intenta de nuevo o contactanos.")
                                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(DialogInterface dialog, int id)
                                            {

                                            }
                                        }).setCancelable(false).show().getButton(DialogInterface.BUTTON_POSITIVE).
                                        setTextColor(getResources().getColor(R.color.colorPrimary));
                            }
                        }
                        catch (JSONException e)
                        {
                            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(new ContextThemeWrapper(ConfigCiudad.this,R.style.AlertDialogTheme));
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
                        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(new ContextThemeWrapper(ConfigCiudad.this,R.style.AlertDialogTheme));
                        builder
                                .setTitle(R.string.title)
                                .setMessage(error.toString())
                                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id)
                                    {
                                    }
                                }).setCancelable(false).show().getButton(DialogInterface.BUTTON_POSITIVE).
                                setTextColor(getResources().getColor(R.color.colorPrimary));

                        if (error instanceof TimeoutError)
                        {
                            builder
                                    .setTitle(R.string.title)
                                    .setMessage(error.toString())
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).setCancelable(false).show().getButton(DialogInterface.BUTTON_POSITIVE).
                                    setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else
                        if (error instanceof NoConnectionError)
                        {
                            builder
                                    .setTitle(R.string.title)
                                    .setMessage(error.toString())
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).setCancelable(false).show().getButton(DialogInterface.BUTTON_POSITIVE).
                                    setTextColor(getResources().getColor(R.color.colorPrimary));
                        }

                        else

                        if (error instanceof AuthFailureError)
                        {
                            builder
                                    .setTitle(R.string.title)
                                    .setMessage(error.toString())
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).setCancelable(false).show().getButton(DialogInterface.BUTTON_POSITIVE).
                                    setTextColor(getResources().getColor(R.color.colorPrimary));
                        }

                        else

                        if (error instanceof ServerError)
                        {
                            builder
                                    .setTitle(R.string.title)
                                    .setMessage(error.toString())
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).setCancelable(false).show().getButton(DialogInterface.BUTTON_POSITIVE).
                                    setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else
                        if (error instanceof NetworkError)
                        {
                            builder
                                    .setTitle(R.string.title)
                                    .setMessage(error.toString())
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).setCancelable(false).show().getButton(DialogInterface.BUTTON_POSITIVE).
                                    setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else
                        if (error instanceof ParseError)
                        {
                            builder
                                    .setTitle(R.string.title)
                                    .setMessage(error.toString())
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).setCancelable(false).show().getButton(DialogInterface.BUTTON_POSITIVE).
                                    setTextColor(getResources().getColor(R.color.colorPrimary));
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
                headers.put("codDevice", ""+idDevice);
                headers.put("codSistema", "1");
                headers.put("tokenFCM", ""+tokenFCM);
                headers.put("versionApp", ""+currentVersion);

                return headers;
            }
        };

        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "registroUsuario");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

}
