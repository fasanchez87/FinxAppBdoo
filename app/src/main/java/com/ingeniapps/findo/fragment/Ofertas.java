package com.ingeniapps.findo.fragment;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.firebase.iid.FirebaseInstanceId;
import com.ingeniapps.findo.R;
import com.ingeniapps.findo.activity.DetalleMarkerConvenio;
import com.ingeniapps.findo.activity.DetalleOferta;
import com.ingeniapps.findo.adapter.ConvenioAdapter;
import com.ingeniapps.findo.adapter.OfertaAdapter;
import com.ingeniapps.findo.beans.Oferta;
import com.ingeniapps.findo.beans.PuntoConvenio;
import com.ingeniapps.findo.sharedPreferences.gestionSharedPreferences;
import com.ingeniapps.findo.vars.vars;
import com.ingeniapps.findo.volley.ControllerSingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN;

/**
 * A simple {@link Fragment} subclass.
 */
public class Ofertas extends Fragment
{
    private com.ingeniapps.findo.sharedPreferences.gestionSharedPreferences sharedPreferences;
    private ArrayList<Oferta> listadoOfertas;
    private RecyclerView recycler_view_ofertas;
    private OfertaAdapter mAdapter;
    LinearLayoutManager mLayoutManager;

    LinearLayout linearHabilitarOfertas;
    RelativeLayout layoutEsperaOfertas;
    RelativeLayout layoutMacroEsperaOfertas;
    RelativeLayout layoutNoFavoritos;

    Context context;
    private boolean solicitando=false;
    //VERSION DEL APP INSTALADA
    private String versionActualApp;
    private ImageView not_found_convenios;

    private ProgressDialog progressDialog;

    EditText editTextBusquedaConvenio;
    TextView editTextNumConvenios;
    private String idCategoria;
    private Double lat;
    private Double lon;
    public static String distanciaPunto;

    DividerItemDecoration mDividerItemDecoration;

    private BroadcastReceiver mRegistrationBroadcastReceiver;
    com.ingeniapps.findo.vars.vars vars;
    com.ingeniapps.findo.sharedPreferences.gestionSharedPreferences gestionSharedPreferences;
    private String cantidadKilometros;


    public Ofertas()
    {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        gestionSharedPreferences=new gestionSharedPreferences(getActivity().getApplicationContext());
        getActivity().getWindow().setSoftInputMode(SOFT_INPUT_ADJUST_PAN);
        vars=new vars();
        gestionSharedPreferences=new gestionSharedPreferences(Ofertas.this.getActivity());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ofertas, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        sharedPreferences=new gestionSharedPreferences(getActivity());
        listadoOfertas=new ArrayList<Oferta>();
        vars=new vars();
        context = getActivity();

        not_found_convenios=(ImageView)getActivity().findViewById(R.id.not_found_convenios);
        layoutEsperaOfertas=(RelativeLayout)getActivity().findViewById(R.id.layoutEsperaOfertas);
        layoutMacroEsperaOfertas=(RelativeLayout)getActivity().findViewById(R.id.layoutMacroEsperaOfertas);
        layoutNoFavoritos=(RelativeLayout)getActivity().findViewById(R.id.layoutNoFavoritos);
        linearHabilitarOfertas=(LinearLayout)getActivity().findViewById(R.id.linearHabilitarOfertas);

        recycler_view_ofertas=(RecyclerView) getActivity().findViewById(R.id.recycler_view_ofertas);
        mLayoutManager = new LinearLayoutManager(getActivity());

        mAdapter = new OfertaAdapter(getActivity(),listadoOfertas,new OfertaAdapter.OnItemClickListener()
        {
            @Override
            public void onItemClick(Oferta oferta)
            {
                Intent i=new Intent(getActivity(), DetalleOferta.class);
                i.putExtra("codOferta",oferta.getCodOferta());
                startActivity(i);
            }
        });

        recycler_view_ofertas.setHasFixedSize(true);
        recycler_view_ofertas.setLayoutManager(mLayoutManager);
        recycler_view_ofertas.setItemAnimator(new DefaultItemAnimator());
        recycler_view_ofertas.setOverScrollMode(View.OVER_SCROLL_NEVER);
        recycler_view_ofertas.setAdapter(mAdapter);

    }

    @Override
    public void onResume()
    {
        super.onResume();
        WebServiceGetOfertas();
        updateTokenFCMToServer();
        Log.i("codEmpleado",gestionSharedPreferences.getString("codEmpleado"));
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
        ControllerSingleton.getInstance().cancelPendingReq("_requestfavoritos");
        ControllerSingleton.getInstance().cancelPendingReq("getOfertas");
    }

    private void WebServiceGetOfertas()
    {
        String _urlWebService = vars.ipServer.concat("/ws/getOfertas");

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
                                listadoOfertas.clear();

                                JSONArray listaOfertas = response.getJSONArray("ofertas");

                                for (int i = 0; i < listaOfertas.length(); i++)
                                {
                                    JSONObject jsonObject = (JSONObject) listaOfertas.get(i);
                                    final Oferta oferta = new Oferta();
                                    oferta.setCodOferta(jsonObject.getString("codOferta"));
                                    oferta.setCodPunto(jsonObject.getString("codPunto"));
                                    oferta.setCodProveedor(jsonObject.getString("codProveedor"));
                                    oferta.setNomOferta(jsonObject.getString("nomOferta"));
                                    oferta.setDetOferta(jsonObject.getString("detOferta"));


                                    if(TextUtils.equals(jsonObject.getString("descOferta"),"0"))
                                    {
                                        oferta.setDescOferta("0");
                                    }
                                    else
                                    {
                                        oferta.setDescOferta("Descuento de "+jsonObject.getString("descOferta")+"%");
                                    }




                                    oferta.setImaProveedor(jsonObject.getString("imaProveedor"));
                                    oferta.setFecExpOferta(jsonObject.getString("fecExpOferta"));
                                    oferta.setNomProveedor(jsonObject.getString("nomProveedor"));
                                    oferta.setType(jsonObject.getString("type"));
                                    listadoOfertas.add(oferta);
                                }

                                layoutMacroEsperaOfertas.setVisibility(View.GONE);
                                linearHabilitarOfertas.setVisibility(View.VISIBLE);
                            }

                            else
                            {
                                mAdapter.notifyDataSetChanged();
                                layoutMacroEsperaOfertas.setVisibility(View.GONE);
                                linearHabilitarOfertas.setVisibility(View.GONE);
                                layoutNoFavoritos.setVisibility(View.VISIBLE);

                            }
                        }
                        catch (JSONException e)
                        {
                            layoutMacroEsperaOfertas.setVisibility(View.GONE);
                            linearHabilitarOfertas.setVisibility(View.GONE);
                            layoutNoFavoritos.setVisibility(View.VISIBLE);

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(),R.style.AlertDialogTheme));
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

                            layoutMacroEsperaOfertas.setVisibility(View.GONE);
                            linearHabilitarOfertas.setVisibility(View.GONE);
                            layoutNoFavoritos.setVisibility(View.VISIBLE);

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(),R.style.AlertDialogTheme));
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

                            layoutMacroEsperaOfertas.setVisibility(View.GONE);
                            linearHabilitarOfertas.setVisibility(View.GONE);
                            layoutNoFavoritos.setVisibility(View.VISIBLE);

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(),R.style.AlertDialogTheme));
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

                            layoutMacroEsperaOfertas.setVisibility(View.GONE);
                            linearHabilitarOfertas.setVisibility(View.GONE);
                            layoutNoFavoritos.setVisibility(View.VISIBLE);

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(),R.style.AlertDialogTheme));
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

                            layoutMacroEsperaOfertas.setVisibility(View.GONE);
                            linearHabilitarOfertas.setVisibility(View.GONE);
                            layoutNoFavoritos.setVisibility(View.VISIBLE);

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(),R.style.AlertDialogTheme));
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

                            layoutMacroEsperaOfertas.setVisibility(View.GONE);
                            linearHabilitarOfertas.setVisibility(View.GONE);
                            layoutNoFavoritos.setVisibility(View.VISIBLE);

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(),R.style.AlertDialogTheme));
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

                            layoutMacroEsperaOfertas.setVisibility(View.GONE);
                            linearHabilitarOfertas.setVisibility(View.GONE);
                            layoutNoFavoritos.setVisibility(View.VISIBLE);

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(),R.style.AlertDialogTheme));
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
                headers.put("MyToken", gestionSharedPreferences.getString("MyToken"));
                headers.put("codEmpleado", gestionSharedPreferences.getString("codEmpleado"));
                return headers;
            }
        };

        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "getOfertas");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    public interface Callback
    {
        void onSuccess(Oferta oferta);
    }

}