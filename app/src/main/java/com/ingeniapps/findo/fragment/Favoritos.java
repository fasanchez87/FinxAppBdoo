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
import com.ingeniapps.findo.adapter.ConvenioAdapter;
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
public class Favoritos extends Fragment
{
    private com.ingeniapps.findo.sharedPreferences.gestionSharedPreferences sharedPreferences;
    private ArrayList<PuntoConvenio> listadoFavoritos;
    private RecyclerView recycler_view_favoritos;
    private ConvenioAdapter mAdapter;
    LinearLayoutManager mLayoutManager;

    LinearLayout linearHabilitarFavoritos;
    RelativeLayout layoutEspera;
    RelativeLayout layoutMacroEsperaFavoritos;
    RelativeLayout layoutNoFavoritos;
    private TextView editTextNumFavoritos;

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


    public Favoritos()
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
        context=getActivity();
        gestionSharedPreferences=new gestionSharedPreferences(Favoritos.this.getActivity());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favoritos, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        sharedPreferences=new gestionSharedPreferences(getActivity());
        listadoFavoritos=new ArrayList<PuntoConvenio>();
        vars=new vars();
        context = getActivity();

        not_found_convenios=(ImageView)getActivity().findViewById(R.id.not_found_convenios);
        editTextNumFavoritos=getActivity().findViewById(R.id.editTextNumFavoritos);
        layoutEspera=(RelativeLayout)getActivity().findViewById(R.id.layoutEsperaConvenios);
        layoutMacroEsperaFavoritos=(RelativeLayout)getActivity().findViewById(R.id.layoutMacroEsperaConveniosFavoritos);
        layoutNoFavoritos=(RelativeLayout)getActivity().findViewById(R.id.layoutNoFavoritos);
        linearHabilitarFavoritos=(LinearLayout)getActivity().findViewById(R.id.linearHabilitarConveniosFavoritos);

        recycler_view_favoritos=(RecyclerView) getActivity().findViewById(R.id.recycler_view_favoritos);
        mLayoutManager = new LinearLayoutManager(getActivity());

        mAdapter = new ConvenioAdapter(Favoritos.this.getActivity(),new ConvenioAdapter.OnItemClickListener()
        {
            @Override
            public void onItemClick(PuntoConvenio convenio)
            {
                Intent i=new Intent(Favoritos.this.getActivity(), DetalleMarkerConvenio.class);
                i.putExtra("codPunto",convenio.getCodPunto());
                startActivity(i);
            }
        });

       /* mAdapter = new ConvenioAdapter(getActivity(),listadoFavoritos,new ConvenioAdapter.OnItemClickListener()
        {
            @Override
            public void onItemClick(PuntoConvenio convenio)
            {
                Intent i=new Intent(getActivity(), DetalleMarkerConvenio.class);
                i.putExtra("codPunto",convenio.getCodPunto());
                startActivity(i);
            }
        });*/

        recycler_view_favoritos.setHasFixedSize(true);
        recycler_view_favoritos.setLayoutManager(mLayoutManager);
        recycler_view_favoritos.setItemAnimator(new DefaultItemAnimator());
        recycler_view_favoritos.setOverScrollMode(View.OVER_SCROLL_NEVER);
        recycler_view_favoritos.setAdapter(mAdapter);

        ImageView buttonBuscar = (ImageView) getActivity().findViewById(R.id.ivSearch);
        buttonBuscar.setClickable(true);

    }

    @Override
    public void onResume()
    {
        super.onResume();
        WebServiceGetPuntosFavoritos();
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
    }

    private void WebServiceGetPuntosFavoritos()
    {
        String _urlWebService = vars.ipServer.concat("/ws/getPuntosLike");
        mAdapter.clear();

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
                                listadoFavoritos.clear();


                                JSONArray listaPuntosConvenios = response.getJSONArray("puntos");
                                editTextNumFavoritos.setText(listaPuntosConvenios.length()+" Convenios Favoritos");

                                double aux=999999999;
                                double distanciaActual=0;
                                double distancias[]=new double[listaPuntosConvenios.length()];

                                for (int i = 0; i < listaPuntosConvenios.length(); i++)
                                {
                                    JSONObject jsonObject = (JSONObject) listaPuntosConvenios.get(i);
                                    final PuntoConvenio c = new PuntoConvenio();
                                    c.setCodPunto(jsonObject.getString("codPunto"));
                                    c.setNomCategoria(jsonObject.getString("nomCategoria"));
                                    c.setNomProveedor(jsonObject.getString("nomProveedor"));
                                    c.setImaProveedor(jsonObject.getString("imaProveedor"));
                                    c.setType(jsonObject.getString("type"));
                                    c.setDirPunto(jsonObject.getString("dirPunto"));
                                    c.setDescPunto("Descuento del "+jsonObject.getString("descPunto")+"%");
                                    c.setCalificacion(jsonObject.getString("numCalifica"));
                                    c.setNomCiudad(jsonObject.getString("nomCiudad"));
                                    c.setCodTipo(jsonObject.getString("codTipo"));
                                    c.setNomCajero(jsonObject.getString("nomCajero"));
                                    c.setHorPunto(jsonObject.getString("horPunto"));

                                    if(TextUtils.equals(jsonObject.getString("descPunto"),"0"))
                                    {
                                        c.setDescPunto("0");
                                    }
                                    else
                                    {
                                        c.setDescPunto("Descuento del "+jsonObject.getString("descPunto")+"%");
                                    }

                                    if(TextUtils.isEmpty(jsonObject.getString("latPunto").toString()))
                                    {
                                        c.setLatPunto(null);
                                    }

                                    if(TextUtils.isEmpty(jsonObject.getString("lonPunto").toString()))
                                    {
                                        c.setLonPunto(null);
                                    }

                                    if(TextUtils.isEmpty(jsonObject.getString("lonPunto").toString()))
                                    {
                                        c.setLonPunto(null);
                                    }

                                    if(TextUtils.isEmpty(jsonObject.getString("dist").toString()))
                                    {
                                        c.setDistPunto(null);
                                    }

                                    if(TextUtils.isEmpty(jsonObject.getString("time").toString()))
                                    {
                                        c.setTimePunto(null);
                                    }

                                    listadoFavoritos.add(c);
                                }

                                layoutMacroEsperaFavoritos.setVisibility(View.GONE);
                                linearHabilitarFavoritos.setVisibility(View.VISIBLE);
                            }

                            else
                            {
                                mAdapter.notifyDataSetChanged();
                                layoutMacroEsperaFavoritos.setVisibility(View.GONE);
                                linearHabilitarFavoritos.setVisibility(View.GONE);
                                layoutNoFavoritos.setVisibility(View.VISIBLE);

                            }

                            mAdapter.addAll(listadoFavoritos);
                        }
                        catch (JSONException e)
                        {
                            layoutMacroEsperaFavoritos.setVisibility(View.GONE);
                            linearHabilitarFavoritos.setVisibility(View.GONE);
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

                            layoutMacroEsperaFavoritos.setVisibility(View.GONE);
                            linearHabilitarFavoritos.setVisibility(View.GONE);
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

                            layoutMacroEsperaFavoritos.setVisibility(View.GONE);
                            linearHabilitarFavoritos.setVisibility(View.GONE);
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

                            layoutMacroEsperaFavoritos.setVisibility(View.GONE);
                            linearHabilitarFavoritos.setVisibility(View.GONE);
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

                            layoutMacroEsperaFavoritos.setVisibility(View.GONE);
                            linearHabilitarFavoritos.setVisibility(View.GONE);
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

                            layoutMacroEsperaFavoritos.setVisibility(View.GONE);
                            linearHabilitarFavoritos.setVisibility(View.GONE);
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

                            layoutMacroEsperaFavoritos.setVisibility(View.GONE);
                            linearHabilitarFavoritos.setVisibility(View.GONE);
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

        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "_requestfavoritos");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    public interface Callback
    {
        void onSuccess(PuntoConvenio convenio);
    }

}
