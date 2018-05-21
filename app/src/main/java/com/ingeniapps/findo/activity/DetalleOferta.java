package com.ingeniapps.findo.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
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
import com.bumptech.glide.Glide;
import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.firebase.iid.FirebaseInstanceId;
import com.ingeniapps.findo.R;
import com.ingeniapps.findo.sharedPreferences.gestionSharedPreferences;
import com.ingeniapps.findo.vars.vars;
import com.ingeniapps.findo.volley.ControllerSingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DetalleOferta extends AppCompatActivity
{

    private String codPunto;
    private String codOferta;
    private String codTipo;//Cajero o Convenio
    private boolean isNotifyPush=false;
    public com.ingeniapps.findo.vars.vars vars;
    private int meGusta;
    private RatingBar ratingBar;



    private LinearLayout ll_espera_detalleOferta,ll_ofertas_detalle_convenio;
    private NestedScrollView scrollDetalleOferta;
    private ImageView imagenDetalleConvenio,imageViewCharedConvenio;
    private TextView textViewDescuento,nomConvenio,nomOfertaDetalle,fecExpiraOfertaDetalle,descConvenio,textViewDireccionConvenio,textViewTelefonoConvenio,textViewObservacionConvenio;
    com.ingeniapps.findo.sharedPreferences.gestionSharedPreferences gestionSharedPreferences;
    Button buttonCheckinEnable,buttonCheckinDisable;
    private ImageView iv_telefono,iv_horario;
    private LinearLayout ll_checkin_convenio;

    private long timeVisto;
    private long start;
    private Uri[] dynamicLinkUri;

    private HashMap<String,String> imagesSlider;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detalle_oferta);

       gestionSharedPreferences=new gestionSharedPreferences(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //this line shows back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setElevation(0);


        if (savedInstanceState == null)
        {
            Bundle extras = getIntent().getExtras();
            if (extras == null)
            {
                codPunto=null;
                codOferta=null;
                isNotifyPush=false;
            }

            else
            {
                codPunto=extras.getString("codPunto");
                codOferta=extras.getString("codOferta");
                isNotifyPush=extras.getBoolean("isNotifyPush");
            }
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(isNotifyPush)
                {
                    if(gestionSharedPreferences.getBoolean("isActivePrincipal"))
                    {
                        finish();
                    }
                    else
                    {
                        Intent i=new Intent(DetalleOferta.this, Principal.class);
                        startActivity(i);
                        isNotifyPush=false;
                        finish();
                    }
                }
                else
                {
                    finish();
                }
            }
        });

        vars=new vars();
        ll_espera_detalleOferta=(LinearLayout)findViewById(R.id.ll_espera_detalleOferta);
        scrollDetalleOferta=(NestedScrollView)findViewById(R.id.scrollDetalleOferta);

        imagenDetalleConvenio=(ImageView)findViewById(R.id.imagenDetalleOfertaDetalle);
        textViewDescuento=(TextView)findViewById(R.id.textViewDescuentoOfertaDetalle);

        nomConvenio=(TextView)findViewById(R.id.nomConvenioOfertaDetalle);
        nomOfertaDetalle=(TextView)findViewById(R.id.nomOfertaDetalle);
        fecExpiraOfertaDetalle=(TextView)findViewById(R.id.fecExpiraOfertaDetalle);
        descConvenio=(TextView)findViewById(R.id.descLargaOfertaDetalle);

        WebServiceGetDetalleOferta(codOferta);

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
        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "_requestupdatetokendetallermarker");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            if(isNotifyPush)
            {
                if(!gestionSharedPreferences.getBoolean("isActivePrincipal"))
                {
                    Log.i("notificado","notificado por push - principal no esta activa");
                    Intent i=new Intent(DetalleOferta.this, Principal.class);
                    startActivity(i);
                    isNotifyPush=false;
                    DetalleOferta.this.finish();
                }
                else
                {
                    Log.i("notificado","notificado por push - principal activa");
                    DetalleOferta.this.finish();
                }
            }
            else
            {
                DetalleOferta.this.finish();
            }

            return true;
        }

        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void onResume()
    {
        super.onResume();
        updateTokenFCMToServer();
    }

    @Override
    public void onPause()
    {
        super.onPause();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        Log.i("time",""+timeVisto);
        ControllerSingleton.getInstance().cancelPendingReq("_requestupdatetokendetallermarker");
        ControllerSingleton.getInstance().cancelPendingReq("_requestdetallemarker");
        ControllerSingleton.getInstance().cancelPendingReq("_requestdetallemarkermegusta");
        ControllerSingleton.getInstance().cancelPendingReq("_requestdetallecheckin");
        ControllerSingleton.getInstance().cancelPendingReq("_requestdetallevistomarker");
        ControllerSingleton.getInstance().cancelPendingReq("_requestdetallecalificacion");
        ControllerSingleton.getInstance().cancelPendingReq("getDetalleOferta");
    }

    @Override
    public void onStop()
    {
        // To prevent a memory leak on rotation, make sure to call stopAutoCycle() on the slider before activity or fragment is destroyed
        super.onStop();
    }


    private void WebServiceGetDetalleOferta(final String codOferta)
    {
        String _urlWebService= vars.ipServer.concat("/ws/getDetalleOferta");

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
                                Glide.with(DetalleOferta.this).
                                        load("" + response.getString("imgOferta")).
                                        thumbnail(0.5f).into(imagenDetalleConvenio);

                                textViewDescuento.setText(response.getString("descOferta"));
                                nomConvenio.setText(response.getString("nomProveedor"));
                                nomOfertaDetalle.setText(response.getString("nomOferta"));
                                descConvenio.setText(response.getString("detOferta"));


                                long timestamp = Long.parseLong(response.getString("fecExpOferta")) * 1000L;
                                CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(timestamp,
                                        System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);
                                fecExpiraOfertaDetalle.setText("Expira "+timeAgo);

                                if(TextUtils.equals(response.getString("descOferta"),"0"))
                                {
                                    textViewDescuento.setVisibility(View.GONE);
                                }

                                textViewDescuento.setText(response.getString("descOferta")+"%");


                                ll_espera_detalleOferta.setVisibility(View.GONE);
                                scrollDetalleOferta.setVisibility(View.VISIBLE);
                            }
                            else
                            {
                                ll_espera_detalleOferta.setVisibility(View.GONE);
                                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(DetalleOferta.this,R.style.AlertDialogTheme));
                                builder
                                        .setTitle(R.string.title)
                                        .setMessage("Error obteniendo detalle de Oferta")
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
                            ll_espera_detalleOferta.setVisibility(View.GONE);

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(DetalleOferta.this,R.style.AlertDialogTheme));
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
                        ll_espera_detalleOferta.setVisibility(View.GONE);

                        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(DetalleOferta.this,R.style.AlertDialogTheme));
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
                            ll_espera_detalleOferta.setVisibility(View.GONE);

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
                            ll_espera_detalleOferta.setVisibility(View.GONE);

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
                            ll_espera_detalleOferta.setVisibility(View.GONE);

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
                            ll_espera_detalleOferta.setVisibility(View.GONE);

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
                            ll_espera_detalleOferta.setVisibility(View.GONE);

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
                            ll_espera_detalleOferta.setVisibility(View.GONE);

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
                headers.put("codOferta", codOferta);
                //headers.put("MyToken", sharedPreferences.getString("MyToken"));
                // headers.put("tokenFCM", ""+tokenFCM);
                return headers;
            }
        };

        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "getDetalleOferta");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }


}

