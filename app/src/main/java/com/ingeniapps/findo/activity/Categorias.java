package com.ingeniapps.findo.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
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
import com.ingeniapps.findo.R;
import com.ingeniapps.findo.adapter.CategoriaAdapter;
import com.ingeniapps.findo.adapter.GridViewAdapter;
import com.ingeniapps.findo.beans.Categoria;
import com.ingeniapps.findo.vars.vars;
import com.ingeniapps.findo.volley.ControllerSingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Categorias extends AppCompatActivity {

    private ArrayList<Categoria>listadoCategorias;
    private vars vars;
    private GridViewAdapter adapter;
    private GridView gridView;
    private Button buttonAplicarFiltro, buttonAplicarFiltroDisable;
    private ArrayList<String> selectedCategorias;
    private LinearLayout ll_espera_categorias;
    private NestedScrollView scrollDetalleCategorias;
    private LinearLayout linearHabilitarCategorias;
    private int count=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categorias);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //this line shows back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setElevation(0);

        buttonAplicarFiltro=findViewById(R.id.buttonAplicarFiltro);
        buttonAplicarFiltroDisable=findViewById(R.id.buttonAplicarFiltroDisable);


        ll_espera_categorias=findViewById(R.id.ll_espera_categorias);
        linearHabilitarCategorias=findViewById(R.id.linearHabilitarCategorias);

        toolbar.setNavigationOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                finish();
            }
        });

        count=0;


        vars=new vars();
        listadoCategorias=new ArrayList<Categoria>();
        gridView=findViewById(R.id.gridCategorias);
        selectedCategorias = new ArrayList<>();

         buttonAplicarFiltro.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
             Intent returnIntent = new Intent();
             returnIntent.putStringArrayListExtra("categorias", selectedCategorias);
             setResult(1,returnIntent);
             finish();
         }
       });






        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                int selectedIndex = adapter.selectedPositions.indexOf(position);
                if (selectedIndex > -1) {
                    count=count+1;
                    adapter.selectedPositions.remove(selectedIndex);
                    ((GridItemView) v).display(false);
                    selectedCategorias.remove(listadoCategorias.get(position).getCodCategoria());
                } else {
                    count=count-1;
                    adapter.selectedPositions.add(position);
                    ((GridItemView) v).display(true);
                    selectedCategorias.add(listadoCategorias.get(position).getCodCategoria());
                }
                if(count==0){

                    buttonAplicarFiltro.setVisibility(View.GONE);
                    buttonAplicarFiltroDisable.setVisibility(View.VISIBLE);
                }
                else{

                    buttonAplicarFiltro.setVisibility(View.VISIBLE);
                    buttonAplicarFiltroDisable.setVisibility(View.GONE);
                }
            }
        });
        WebServiceGetCategorias();
    }

    private void WebServiceGetCategorias()
    {
        listadoCategorias.clear();
        String _urlWebService = vars.ipServer.concat("/ws/getCategoriasGeneral");

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
                                    categoria.setImaCategoria(objectCategoria.getString("imgCategoria"));
                                    categoria.setType(objectCategoria.getString("type"));
                                    listadoCategorias.add(categoria);
                                }

                                adapter = new GridViewAdapter(listadoCategorias, Categorias.this);
                                gridView.setAdapter(adapter);


                                /*recycler_view_categorias.setHasFixedSize(true);
                                recycler_view_categorias.setItemAnimator(new DefaultItemAnimator());
                                recycler_view_categorias.setOverScrollMode(View.OVER_SCROLL_NEVER);

                                recycler_view_categorias.setAdapter(mAdapter);
                                recycler_view_categorias.setLayoutManager(mLayoutManager);

                                layoutMacroEsperaCategorias.setVisibility(View.GONE);
                                relativeLayoutCategorias.setVisibility(View.VISIBLE);
                                checkAllCategorias.setVisibility(View.VISIBLE);*/
                            }

                            else
                            {
                              /*  layoutMacroEsperaCategorias.setVisibility(View.VISIBLE);
                                not_found_categorias.setVisibility(View.VISIBLE);*/
                            }
                        }
                        catch (JSONException e)
                        {
                            //layoutMacroEsperaCiudades.setVisibility(View.VISIBLE);
                           // not_found_categorias.setVisibility(View.VISIBLE);

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Categorias.this,R.style.AlertDialogTheme));
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
                        adapter.notifyDataSetChanged();
                        ll_espera_categorias.setVisibility(View.GONE);
                        linearHabilitarCategorias.setVisibility(View.VISIBLE);
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

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Categorias.this,R.style.AlertDialogTheme));
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

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Categorias.this,R.style.AlertDialogTheme));
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

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Categorias.this,R.style.AlertDialogTheme));
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

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Categorias.this,R.style.AlertDialogTheme));
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

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Categorias.this,R.style.AlertDialogTheme));
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

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Categorias.this,R.style.AlertDialogTheme));
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
                return headers;
            }
        };

        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "_requestbuscarcategoria");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }
}
