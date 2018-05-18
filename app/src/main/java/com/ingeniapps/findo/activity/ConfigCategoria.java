package com.ingeniapps.findo.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
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
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
import com.google.firebase.iid.FirebaseInstanceId;
import com.ingeniapps.findo.R;
import com.ingeniapps.findo.adapter.CategoriaAdapter;
import com.ingeniapps.findo.adapter.CiudadAdapter;
import com.ingeniapps.findo.beans.Categoria;
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

public class ConfigCategoria extends AppCompatActivity
{

    private gestionSharedPreferences sharedPreferences;
    private ArrayList<Categoria> listadoCategorias;
    public com.ingeniapps.findo.vars.vars vars;
    private RecyclerView recycler_view_categorias;
    private CategoriaAdapter mAdapter;
    LinearLayoutManager mLayoutManager;
    RelativeLayout layoutEsperaCategorias;
    RelativeLayout relativeLayoutCategorias;
    RelativeLayout layoutMacroEsperaCategorias;
    ImageView not_found_categorias;
    private int pagina;
    Context context;
    private boolean solicitando=false;
    //VERSION DEL APP INSTALADA
    private String versionActualApp;
    private Button btnFinConfigCategoria;
    private String ciudadesSelected;
    private boolean configInterna;
    private String categoriasSelected;
    private String categoriasEscogidas;
    private ProgressDialog progressDialog;
    private CheckBox checkAllCategorias;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_categoria);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        categoriasSelected="";


        if (savedInstanceState==null)
        {
            Bundle extras = getIntent().getExtras();

            if (extras == null)
            {
                ciudadesSelected=null;
                configInterna=false;
            }
            else
            {
                ciudadesSelected = extras.getString("ciudadesEscogidas");
                configInterna = extras.getBoolean("configInterna");
            }
        }

        if(!configInterna)
        {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        sharedPreferences=new gestionSharedPreferences(this);
        listadoCategorias=new ArrayList<Categoria>();
        checkAllCategorias = (CheckBox) findViewById(R.id.checkAllCategorias);


        vars=new vars();
        context = this;
        pagina=0;

        layoutEsperaCategorias=(RelativeLayout)findViewById(R.id.layoutEsperaCategorias);
        relativeLayoutCategorias=(RelativeLayout)findViewById(R.id.relativeLayoutCategorias);
        layoutMacroEsperaCategorias=(RelativeLayout)findViewById(R.id.layoutMacroEsperaCategorias);
        recycler_view_categorias=(RecyclerView) findViewById(R.id.recycler_view_categorias);

        mLayoutManager = new LinearLayoutManager(this);

        try
        {
            versionActualApp=getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        }
        catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }

        btnFinConfigCategoria=(Button)findViewById(R.id.btnFinConfigCategoria);
        btnFinConfigCategoria.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                categoriasEscogidas=null;
                categoriasSelected=null;
                List<Categoria> categoriasSeleccionadas = ((CategoriaAdapter) mAdapter).getCategoriaList();

                for (int i=0; i<categoriasSeleccionadas.size(); i++)
                {
                    Categoria categoria = categoriasSeleccionadas.get(i);

                    if (categoria.isSelected()==true)
                    {
                        categoriasSelected=categoriasSelected + "," + categoria.getCodCategoria().toString();
                    }
                }

                categoriasEscogidas=TextUtils.isEmpty(categoriasSelected)?null:categoriasSelected.substring(5);

                if(TextUtils.isEmpty(categoriasEscogidas))
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(new android.support.v7.view.ContextThemeWrapper(ConfigCategoria.this, R.style.AlertDialogTheme));
                    builder
                            .setTitle(R.string.title)
                            .setMessage("Debes seleccionar mínimo (1) categoria.")
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
                    Log.i("categos",categoriasEscogidas);
                    WebServiceRegistroConfiguracion(ciudadesSelected,categoriasEscogidas,sharedPreferences.getString("codEmpleado"));
                }

            }
        });

        //recycler_view_notificaciones.addOnScrollListener(scrollListener);
        WebServiceGetCategorias();

        checkAllCategorias.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (checkAllCategorias.isChecked())
                {
                    for (Categoria model : listadoCategorias)
                    {
                        model.setSelected(true);
                    }
                }
                else
                {
                    for (Categoria model : listadoCategorias)
                    {
                        model.setSelected(false);
                    }
                }

                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            Intent i=new Intent(ConfigCategoria.this,ConfigCiudad.class);
            i.putExtra("configInterna",true);
            startActivity(i);
            finish();
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        ControllerSingleton.getInstance().cancelPendingReq("_requestbuscarcategoria");
        ControllerSingleton.getInstance().cancelPendingReq("_requestregistroconf");

        if (!((Activity) context).isFinishing())
        {
            if (progressDialog.isShowing() )
            {
                progressDialog.dismiss();

            }
        }


    }

    private void WebServiceGetCategorias()
    {
        listadoCategorias.clear();
        String _urlWebService = vars.ipServer.concat("/ws/getCategorias");

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
                                JSONArray listaCategorias = response.getJSONArray("categorias");

                                for (int i = 0; i < listaCategorias.length(); i++)
                                {
                                    JSONObject objectCategoria = (JSONObject) listaCategorias.get(i);
                                    Categoria categoria = new Categoria();
                                    categoria.setNomCategoria(objectCategoria.getString("nomCategoria"));
                                    categoria.setCodCategoria(objectCategoria.getString("codCategoria"));
                                    categoria.setType(objectCategoria.getString("type"));
                                    categoria.setSelected(objectCategoria.getBoolean("indCheck"));
                                    listadoCategorias.add(categoria);
                                }

                                mAdapter = new CategoriaAdapter(ConfigCategoria.this,listadoCategorias);

                                recycler_view_categorias.setHasFixedSize(true);
                                recycler_view_categorias.setItemAnimator(new DefaultItemAnimator());
                                recycler_view_categorias.setOverScrollMode(View.OVER_SCROLL_NEVER);

                                recycler_view_categorias.setAdapter(mAdapter);
                                recycler_view_categorias.setLayoutManager(mLayoutManager);

                                layoutMacroEsperaCategorias.setVisibility(View.GONE);
                                relativeLayoutCategorias.setVisibility(View.VISIBLE);
                                checkAllCategorias.setVisibility(View.VISIBLE);
                            }

                            else
                            {
                                layoutMacroEsperaCategorias.setVisibility(View.VISIBLE);
                                not_found_categorias.setVisibility(View.VISIBLE);
                            }
                        }
                        catch (JSONException e)
                        {
                            //layoutMacroEsperaCiudades.setVisibility(View.VISIBLE);
                            not_found_categorias.setVisibility(View.VISIBLE);

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(ConfigCategoria.this,R.style.AlertDialogTheme));
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
                            not_found_categorias.setVisibility(View.VISIBLE);

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(ConfigCategoria.this,R.style.AlertDialogTheme));
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
                            //layoutMacroEsperaCiudades.setVisibility(View.VISIBLE);
                            not_found_categorias.setVisibility(View.VISIBLE);

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(ConfigCategoria.this,R.style.AlertDialogTheme));
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
                            //layoutMacroEsperaCiudades.setVisibility(View.VISIBLE);
                            not_found_categorias.setVisibility(View.VISIBLE);

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(ConfigCategoria.this,R.style.AlertDialogTheme));
                            builder
                                    .setMessage("Error de autentificación en la red, favor contacte a su proveedor de servicios.")
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
                            //layoutMacroEsperaCiudades.setVisibility(View.VISIBLE);
                            //not_found_ciudades.setVisibility(View.VISIBLE);

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(ConfigCategoria.this,R.style.AlertDialogTheme));
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
                            //layoutMacroEsperaCiudades.setVisibility(View.VISIBLE);
                            not_found_categorias.setVisibility(View.VISIBLE);

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(ConfigCategoria.this,R.style.AlertDialogTheme));
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
                            //layoutMacroEsperaCiudades.setVisibility(View.VISIBLE);
                            not_found_categorias.setVisibility(View.VISIBLE);

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(ConfigCategoria.this,R.style.AlertDialogTheme));
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
                headers.put("MyToken", sharedPreferences.getString("MyToken"));
                headers.put("codEmpleado", TextUtils.isEmpty(sharedPreferences.getString("codEmpleado"))?null:sharedPreferences.getString("codEmpleado"));
                return headers;
            }
        };

        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "_requestbuscarcategoria");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    private void WebServiceRegistroConfiguracion(final String ciudades, final String categorias, final String codEmpleado)
    {
        String _urlWebService = vars.ipServer.concat("/ws/RegistroConfiguracion");

        progressDialog = new ProgressDialog(new ContextThemeWrapper(ConfigCategoria.this,R.style.AppCompatAlertDialogStyle));
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Guardando configuración...");
        progressDialog.show();
        progressDialog.setCancelable(false);

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, _urlWebService, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        try
                        {
                            if(response.getBoolean("status"))
                            {
                                if(!configInterna)
                                {
                                    sharedPreferences.putBoolean("GuardarSesion", true);
                                    Intent i=new Intent(ConfigCategoria.this,Principal.class);
                                    i.putExtra("categoriasEscogidas",categoriasEscogidas);
                                    startActivity(i);
                                    finish();
                                }
                                else
                                {
                                    finish();
                                }
                            }

                            else
                            {
                                if (!((Activity) context).isFinishing())
                                {
                                    if (progressDialog.isShowing() )
                                    {
                                        progressDialog.dismiss();

                                    }
                                }




                                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(ConfigCategoria.this,R.style.AlertDialogTheme));
                                builder
                                        .setTitle(R.string.title)
                                        .setMessage("Error registrando la configuración.")
                                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(DialogInterface dialog, int id)
                                            {

                                            }
                                        }).setCancelable(false).show();
                            }
                        }
                        catch (JSONException e)
                        {
                            //layoutMacroEsperaCiudades.setVisibility(View.VISIBLE);
                            if (!((Activity) context).isFinishing())
                            {
                                if (progressDialog.isShowing() )
                                {
                                    progressDialog.dismiss();

                                }
                            }
                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(ConfigCategoria.this,R.style.AlertDialogTheme));
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
                            if (!((Activity) context).isFinishing())
                            {
                                if (progressDialog.isShowing() )
                                {
                                    progressDialog.dismiss();

                                }
                            }
                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(ConfigCategoria.this,R.style.AlertDialogTheme));
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
                            //layoutMacroEsperaCiudades.setVisibility(View.VISIBLE);
                            if (!((Activity) context).isFinishing())
                            {
                                if (progressDialog.isShowing() )
                                {
                                    progressDialog.dismiss();

                                }
                            }
                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(ConfigCategoria.this,R.style.AlertDialogTheme));
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
                            //layoutMacroEsperaCiudades.setVisibility(View.VISIBLE);
                            if (!((Activity) context).isFinishing())
                            {
                                if (progressDialog.isShowing() )
                                {
                                    progressDialog.dismiss();

                                }
                            }
                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(ConfigCategoria.this,R.style.AlertDialogTheme));
                            builder
                                    .setMessage("Error de autentificación en la red, favor contacte a su proveedor de servicios.")
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
                            //layoutMacroEsperaCiudades.setVisibility(View.VISIBLE);
                            if (!((Activity) context).isFinishing())
                            {
                                if (progressDialog.isShowing() )
                                {
                                    progressDialog.dismiss();

                                }
                            }
                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(ConfigCategoria.this,R.style.AlertDialogTheme));
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
                            //layoutMacroEsperaCiudades.setVisibility(View.VISIBLE);
                            if (!((Activity) context).isFinishing())
                            {
                                if (progressDialog.isShowing() )
                                {
                                    progressDialog.dismiss();

                                }
                            }
                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(ConfigCategoria.this,R.style.AlertDialogTheme));
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
                            //layoutMacroEsperaCiudades.setVisibility(View.VISIBLE);
                            if (!((Activity) context).isFinishing())
                            {
                                if (progressDialog.isShowing() )
                                {
                                    progressDialog.dismiss();

                                }
                            }
                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(ConfigCategoria.this,R.style.AlertDialogTheme));
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
                headers.put("MyToken", sharedPreferences.getString("MyToken"));
                headers.put("tokenFCM", FirebaseInstanceId.getInstance().getToken());
                headers.put("ciudades", ciudades);
                headers.put("categorias", categorias);
                headers.put("codEmpleado", sharedPreferences.getString("codEmpleado"));
                return headers;
            }
        };

        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "_requestregistroconf");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }
}
