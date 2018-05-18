package com.ingeniapps.findo.activity;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
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
import android.view.WindowManager;
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
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.firebase.iid.FirebaseInstanceId;
import com.ingeniapps.findo.R;
import com.ingeniapps.findo.beans.PuntoConvenio;
import com.ingeniapps.findo.fragment.MapaConvenios;
import com.ingeniapps.findo.service.LocationClientService;
import com.ingeniapps.findo.sharedPreferences.gestionSharedPreferences;
import com.ingeniapps.findo.volley.ControllerSingleton;
import com.ingeniapps.findo.vars.vars;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DetalleMarkerConvenio extends AppCompatActivity implements BaseSliderView.OnSliderClickListener, ViewPagerEx.OnPageChangeListener
{

    private SliderLayout mDemoSlider;
    DividerItemDecoration mDividerItemDecoration;
    ImageView favovitoOff;
    ImageView favovitoOn;

    private String codPunto;
    private String codTipo;//Cajero o Convenio
    private boolean isNotifyPush=false;
    public vars vars;
    private int meGusta;
    private RatingBar ratingBar;
    private Context context;

    private static final int MY_REQUEST_CODE = 12345; // Or whatever number you want

    private LinearLayout ll_espera_detalle,ll_ofertas_detalle_convenio;
    private NestedScrollView scrollDetallePunto;
    private ImageView imagenDetalleConvenio,imageViewCharedConvenio;
    private TextView textViewDescuento,nomConvenio,descConvenio,textViewDireccionConvenio,textViewTelefonoConvenio,textViewObservacionConvenio;
    com.ingeniapps.findo.sharedPreferences.gestionSharedPreferences gestionSharedPreferences;
    Button buttonCheckinEnable,buttonCheckinDisable;
    private ImageView iv_telefono,iv_horario;
    private LinearLayout ll_checkin_convenio;

    private long timeVisto;
    private long start;
    private Uri[] dynamicLinkUri;

    private boolean mailClientOpened = false;

    private HashMap<String,String> imagesSlider;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detalle_marker_convenio);
        context=this;

        dynamicLinkUri = new Uri[1];

        timeVisto=0;
        start = System.currentTimeMillis();

        gestionSharedPreferences=new gestionSharedPreferences(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //this line shows back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setElevation(0);

        // [START get_deep_link]
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>()
                {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData)
                    {
                        // Get deep link from result (may be null if no link is found)
                        Uri deepLink = null;
                        if (pendingDynamicLinkData != null)
                        {
                            deepLink = pendingDynamicLinkData.getLink();
                        }


                        // Handle the deep link. For example, open the linked
                        // content, or apply promotional credit to the user's
                        // account.
                        // ...

                        // [START_EXCLUDE]
                        // Display deep link in the UI
                        if (deepLink != null)
                        {
                            Uri uri = deepLink;//Uri.parse("https://graph.facebook.com/me/home?limit=25&since=1374196005");
                            String protocol = uri.getScheme();
                            String server = uri.getAuthority();
                            String path = uri.getPath();
                            Set<String> args = uri.getQueryParameterNames();
                            codPunto = uri.getQueryParameter("codPunto");
                            isNotifyPush = TextUtils.equals(uri.getQueryParameter("isNotifyPush"),"true")?true:false;
                            WebServiceGetDetallePunto(codPunto);
                        }
                        else
                        {
                            WebServiceGetDetallePunto(codPunto);
                        }

                        // [END_EXCLUDE]
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(findViewById(android.R.id.content),
                                "getDynamicLink: on fail", Snackbar.LENGTH_LONG).show();
                    }
                });
        // [END get_deep_link]



        if (savedInstanceState == null)
        {
            Bundle extras = getIntent().getExtras();
            if (extras == null)
            {
                codPunto=null;
                codTipo=null;
                isNotifyPush=false;
            }

            else
            {
                codPunto=extras.getString("codPunto");
                codTipo=extras.getString("codTipo");
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
                        Intent i=new Intent(DetalleMarkerConvenio.this, Principal.class);
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
        ll_espera_detalle=(LinearLayout)findViewById(R.id.ll_espera_detalle);
        ll_ofertas_detalle_convenio=(LinearLayout)findViewById(R.id.ll_ofertas_detalle_convenio);
        scrollDetallePunto=(NestedScrollView)findViewById(R.id.scrollDetallePunto);
        imagenDetalleConvenio=(ImageView)findViewById(R.id.imagenDetalleConvenio);
        imageViewCharedConvenio=(ImageView)findViewById(R.id.imageViewCharedConvenio);
        textViewDescuento=(TextView)findViewById(R.id.textViewDescuento);
        nomConvenio=(TextView)findViewById(R.id.nomConvenio);
        descConvenio=(TextView)findViewById(R.id.descConvenio);
        textViewDireccionConvenio=(TextView)findViewById(R.id.textViewDireccionConvenio);
        textViewTelefonoConvenio=(TextView)findViewById(R.id.textViewTelefonoConvenio);
        textViewObservacionConvenio=(TextView)findViewById(R.id.textViewObservacionConvenio);
        imagesSlider = new HashMap<String, String>();
        buttonCheckinEnable=(Button)findViewById(R.id.buttonCheckinEnable);
        buttonCheckinDisable=(Button)findViewById(R.id.buttonCheckinDisable);
        ll_checkin_convenio=(LinearLayout) findViewById(R.id.ll_checkin_convenio);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener()
        {
            public void onRatingChanged(RatingBar ratingBar, float rating,
                                        boolean fromUser)
            {
                int b=(int)(Math.round(rating));
                WebServiceSetCalificacion(codPunto,gestionSharedPreferences.getString("codEmpleado"),String.valueOf(b));
            }
        });

        iv_telefono = (ImageView) findViewById(R.id.iv_telefono);
        iv_horario = (ImageView) findViewById(R.id.iv_horario);


        if(isNotifyPush)//SI ES NOTIFICADO POR PUSH, HABILITAMOS EL BUTTON CHECKIN
        {
            buttonCheckinEnable.setVisibility(View.GONE);
            ll_checkin_convenio.setVisibility(View.GONE);
        }

        buttonCheckinEnable.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                buttonCheckinEnable.setVisibility(View.GONE);
                buttonCheckinDisable.setVisibility(View.VISIBLE);
                WebServiceCheckin(gestionSharedPreferences.getString("codEmpleado"), codPunto);
            }
        });

        mDemoSlider = (SliderLayout)findViewById(R.id.slider);

        favovitoOff=(ImageView) findViewById(R.id.imageViewMeGustaConvenioOff);
        favovitoOn=(ImageView) findViewById(R.id.imageViewMeGustaConvenioOn);







        if(gestionSharedPreferences.getInt("like_punto"+codPunto)!=0)
        {
            if(gestionSharedPreferences.getInt("like_punto"+codPunto)==1)
            {
                favovitoOff.setImageResource(R.drawable.ic_favorito);
            }
            else
            {
                favovitoOff.setImageResource(R.drawable.ic_favorito_off);
            }
        }

        imageViewCharedConvenio.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {




                    /*DynamicLink dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                            .setLink(Uri.parse("https://example.com/?codPunto="+codPunto+"&isNotifyPush="+true))
                            .setDynamicLinkDomain("y57ym.app.goo.gl")
                            // Open links with this app on Android
                            .setAndroidParameters(new DynamicLink.AndroidParameters.Builder().build())
                            // Open links with com.example.ios on iOS
                            .setIosParameters(new DynamicLink.IosParameters.Builder("com.example.ios").build())
                            .buildDynamicLink();

                    Uri dynamicLinkUri = dynamicLink.getUri();*/



                    //"longDynamicLink": "https://abc123.app.goo.gl/?link=https://example.com/&apn=com.example.android&ibi=com.example.ios"



                    Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                            .setLongLink(Uri.parse("y57ym.app.goo.gl/?link=https://example.com/?codPunto="+codPunto.concat("&isNotifyPush="+true+"&apn=com.example.android&ibn=com.example.ios")))
                            .buildShortDynamicLink()
                            .addOnCompleteListener(DetalleMarkerConvenio.this, new OnCompleteListener<ShortDynamicLink>()
                            {
                                @Override
                                public void onComplete(@NonNull Task<ShortDynamicLink> task)
                                {
                                    if (task.isSuccessful())
                                    {
                                        // Short link created
                                        DetalleMarkerConvenio.this.dynamicLinkUri[0] = task.getResult().getShortLink();
                                        Uri flowchartLink = task.getResult().getPreviewLink();
                                        Log.i("findo","ok: "+DetalleMarkerConvenio.this.dynamicLinkUri[0]);
                                       // Toast.makeText(DetalleMarkerConvenio.this,"lo envio",Toast.LENGTH_LONG).show();
                                    }
                                    else
                                    {
                                        // Error
                                        // ...
                                        Log.i("findo","fail: "+dynamicLinkUri[0]);
                                       // Toast.makeText(DetalleMarkerConvenio.this,"Lo cancelo o no lo envio",Toast.LENGTH_LONG).show();

                                    }
                                }
                            });


                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("text/plain");
                    i.putExtra(Intent.EXTRA_SUBJECT, "Findo - Tu bienestar es el nuestro");
                    String sAux = "Hola, descubre este y otros convenios ingresando a la aplicación Findo."+"\n"+nomConvenio.getText()+"\n"+textViewDireccionConvenio.getText()+"\n"+"Descuento del "+textViewDescuento.getText()+" justo ahora."+"\n";
                    //sAux = sAux + DetalleMarkerConvenio.this.dynamicLinkUri[0] +"\n\n";
                    sAux = sAux + "https://y57ym.app.goo.gl/V9Hh" +"\n\n";
                    i.putExtra(Intent.EXTRA_TEXT, sAux);
                    startActivityForResult(Intent.createChooser(i, "Comparte esta convenio con tus amigos justo ahora!"),MY_REQUEST_CODE);



                    //WebServiceCompartirMostrarOfertas(gestionSharedPreferences.getString("codEmpleado"));

                }
                catch(Exception e)
                {
                    //e.toString();
                }
            }
        });

        favovitoOff.setOnClickListener(new View.OnClickListener()
        {
            int meGusta = gestionSharedPreferences.getInt("like_punto"+codPunto);
            public void onClick(View v)
            {
                if (meGusta==0)
                {
                    favovitoOff.setImageResource(R.drawable.ic_favorito);
                    meGusta=1;
                    animateHeart(favovitoOff);
                    gestionSharedPreferences.putInt("like_punto"+codPunto,meGusta);
                    WebServiceSetMeGusta(""+meGusta);
                }
                else
                if (meGusta==1)
                {
                    favovitoOff.setImageResource(R.drawable.ic_favorito_off);
                    meGusta=0;
                    animateHeart(favovitoOff);
                    gestionSharedPreferences.putInt("like_punto"+codPunto,meGusta);
                    WebServiceSetMeGusta(""+meGusta);
                }
            }
        });





    // [END on_create]



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == MY_REQUEST_CODE && mailClientOpened )
        {
            WebServiceCompartirMostrarOfertas(gestionSharedPreferences.getString("codEmpleado"));
        }

       /* else
        {
            Toast.makeText(this,"cancelo",Toast.LENGTH_LONG).show();

        }*/
    }



    private void WebServiceCompartirMostrarOfertas(String codEmpleado) {
        String _urlWebServiceUpdateToken = vars.ipServer.concat("/ws/registroCompartirConvenio");

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, _urlWebServiceUpdateToken, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Boolean status = response.getBoolean("status");
                            String message = response.getString("message");

                            if (status)
                            {
                                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(DetalleMarkerConvenio.this, R.style.AlertDialogTheme));
                                builder
                                        .setTitle("Findo")
                                        .setMessage("" + message)
                                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int id) {

                                            }
                                        }).setCancelable(false).show().getButton(DialogInterface.BUTTON_POSITIVE).
                                        setTextColor(getResources().getColor(R.color.colorPrimary));

                            } else

                            {
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Toast.makeText(getActivity(), "Token FCM: " + "error"+error.getMessage(), Toast.LENGTH_LONG).show();

                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("WWW-Authenticate", "xBasic realm=".concat(""));
                headers.put("codEmpleado", gestionSharedPreferences.getString("codEmpleado"));
                headers.put("tokenFCM", "" + FirebaseInstanceId.getInstance().getToken());
                return headers;
            }
        };
        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "_requestupdatetokendetallermarker");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
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
                    Intent i=new Intent(DetalleMarkerConvenio.this, Principal.class);
                    startActivity(i);
                    isNotifyPush=false;
                    DetalleMarkerConvenio.this.finish();
                }
                else
                {
                    Log.i("notificado","notificado por push - principal activa");
                    DetalleMarkerConvenio.this.finish();
                }
            }
            else
            {
                DetalleMarkerConvenio.this.finish();
            }

            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    public void animateHeart(final ImageView view)
    {
        ScaleAnimation scaleAnimation = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        prepareAnimation(scaleAnimation);

        AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
        prepareAnimation(alphaAnimation);

        AnimationSet animation = new AnimationSet(true);
        animation.addAnimation(alphaAnimation);
        animation.addAnimation(scaleAnimation);
        animation.setDuration(300);
        //animation.setFillAfter(true);
        view.startAnimation(animation);

    }

    private Animation prepareAnimation(Animation animation)
    {
        animation.setRepeatCount(2);
        animation.setRepeatMode(Animation.REVERSE);
        return animation;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        updateTokenFCMToServer();
       Log.i("onresumewhats","onresumewhats");
        mailClientOpened = false;


        mDemoSlider.startAutoCycle();
    }



    @Override
    public void onPause()
    {
        super.onPause();
        mDemoSlider.stopAutoCycle();
        Log.i("estado","pause");
        timeVisto=((System.currentTimeMillis() - start) / 1000l);
        WebServiceSetVisto();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        timeVisto=((System.currentTimeMillis() - start) / 1000l);
        WebServiceSetVisto();
        Log.i("time",""+timeVisto);
        ControllerSingleton.getInstance().cancelPendingReq("_requestupdatetokendetallermarker");
        ControllerSingleton.getInstance().cancelPendingReq("_requestdetallemarker");
        ControllerSingleton.getInstance().cancelPendingReq("_requestdetallemarkermegusta");
        ControllerSingleton.getInstance().cancelPendingReq("_requestdetallecheckin");
        ControllerSingleton.getInstance().cancelPendingReq("_requestdetallevistomarker");
        ControllerSingleton.getInstance().cancelPendingReq("_requestdetallecalificacion");
    }

    @Override
    public void onStop()
    {
        // To prevent a memory leak on rotation, make sure to call stopAutoCycle() on the slider before activity or fragment is destroyed
        mDemoSlider.stopAutoCycle();
        mailClientOpened = true;

        super.onStop();
    }

    @Override
    public void onSliderClick(BaseSliderView slider)
    {
        //Toast.makeText(this,slider.getBundle().get("extra") + "",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
    {
    }

    @Override
    public void onPageSelected(int position)
    {
    }

    @Override
    public void onPageScrollStateChanged(int state)
    {
    }

    private void WebServiceGetDetallePunto(final String codPunto)
    {
        String _urlWebService= vars.ipServer.concat("/ws/getDetalleConvenio");

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
                                Log.i("numCalifica","numCalifica: "+response.getString("numCalifica"));

                                Glide.with(DetalleMarkerConvenio.this).
                                        load("" + response.getString("imaProveedor")).
                                        thumbnail(0.5f).into(imagenDetalleConvenio);

                                textViewDescuento.setText(response.getString("descPunto"));
                                nomConvenio.setText(response.getString("nomProveedor"));

                                if(TextUtils.equals(response.getString("descPunto"),"0"))
                                {
                                    textViewDescuento.setVisibility(View.GONE);
                                }

                                textViewDescuento.setText(response.getString("descPunto")+"%");
                                if(!TextUtils.isEmpty(response.getString("serPunto")))
                                {
                                    descConvenio.setText(response.getString("serPunto"));
                                    descConvenio.setVisibility(View.VISIBLE);
                                }

                                if(TextUtils.equals(response.getString("numCalifica").toString(),"null"))
                                {
                                    ratingBar.setRating(0);
                                }
                                else
                                {
                                    int b=(int)(Math.round(Float.parseFloat(response.getString("numCalifica").toString())));
                                    ratingBar.setRating(b);
                                }

                                textViewDireccionConvenio.setText(response.getString("dirPunto"));
                                textViewObservacionConvenio.setText(TextUtils.isEmpty(response.getString("obsPunto")) ? "Sin observaciones" : response.getString("obsPunto"));
                                textViewTelefonoConvenio.setText(response.getString("telPunto"));

                                JSONArray ofertas = response.getJSONArray("ofertas");

                                if (ofertas.length()==0)
                                {
                                    ll_ofertas_detalle_convenio.setVisibility(View.GONE);
                                }
                                else
                                {
                                    long timestamp;
                                    CharSequence timeAgo;

                                    for (int i=0; i<ofertas.length(); i++)
                                    {
                                        JSONObject ofertaObject = (JSONObject) ofertas.get(i);

                                        Log.i("oferta", "oferta: " + ofertaObject.getString("imgOferta"));

                                        timestamp = Long.parseLong(ofertaObject.getString("expOfertaTime")) * 1000L;
                                        timeAgo = DateUtils.getRelativeTimeSpanString(timestamp,
                                                System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);
                                        //imagesSlider.put("Oferta expira " + timeAgo, ofertaObject.getString("imgOferta"));



                                        TextSliderView textSliderView = new TextSliderView(DetalleMarkerConvenio.this);
                                        textSliderView
                                                .description("Oferta expira " + timeAgo)
                                                .image(ofertaObject.getString("imgOferta"))
                                                .setScaleType(BaseSliderView.ScaleType.Fit)
                                                .setOnSliderClickListener((BaseSliderView.OnSliderClickListener) DetalleMarkerConvenio.this);
                                        //add your extra information
                                        textSliderView.bundle(new Bundle());
                                        textSliderView.getBundle()
                                                .putString("extra", "");
                                        mDemoSlider.addSlider(textSliderView);

                                    }
                                    mDemoSlider.setPresetTransformer(SliderLayout.Transformer.Accordion);
                                    mDemoSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
                                    mDemoSlider.setCustomAnimation(new DescriptionAnimation());
                                    mDemoSlider.setDuration(5000);
                                    mDemoSlider.addOnPageChangeListener(DetalleMarkerConvenio.this);
                                    mDemoSlider.setPresetTransformer("ZoomOut");
                                }

                                if(TextUtils.equals(response.getString("codTipo"),"2"))//SI ES CAJERO
                                {
                                    textViewDescuento.setVisibility(View.GONE);
                                    nomConvenio.setText(null);
                                    nomConvenio.setText(response.getString("nomCajero"));
                                    iv_telefono.setVisibility(View.GONE);
                                    iv_horario.setVisibility(View.VISIBLE);
                                    textViewTelefonoConvenio.setText(null);
                                    textViewTelefonoConvenio.setText(response.getString("horPunto"));
                                    ll_checkin_convenio.setVisibility(View.GONE);
                                }

                                ll_espera_detalle.setVisibility(View.GONE);
                                scrollDetallePunto.setVisibility(View.VISIBLE);
                            }
                            else
                            {
                                ll_espera_detalle.setVisibility(View.GONE);
                                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(DetalleMarkerConvenio.this,R.style.AlertDialogTheme));
                                builder
                                        .setTitle(R.string.title)
                                        .setMessage("Error obteniendo detalle del punto convenio")
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
                            ll_espera_detalle.setVisibility(View.GONE);

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(DetalleMarkerConvenio.this,R.style.AlertDialogTheme));
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
                        ll_espera_detalle.setVisibility(View.GONE);

                        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(DetalleMarkerConvenio.this,R.style.AlertDialogTheme));
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
                            ll_espera_detalle.setVisibility(View.GONE);

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
                            ll_espera_detalle.setVisibility(View.GONE);

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
                            ll_espera_detalle.setVisibility(View.GONE);

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
                            ll_espera_detalle.setVisibility(View.GONE);

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
                            ll_espera_detalle.setVisibility(View.GONE);

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
                            ll_espera_detalle.setVisibility(View.GONE);

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
                headers.put("codPunto", codPunto);
                headers.put("codEmpleado", gestionSharedPreferences.getString("codEmpleado"));
                //headers.put("MyToken", sharedPreferences.getString("MyToken"));
                // headers.put("tokenFCM", ""+tokenFCM);
                return headers;
            }
        };

        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "_requestdetallemarker");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    private void WebServiceSetMeGusta(final String indLike)
    {
        String _urlWebService= vars.ipServer.concat("/ws/setLikePuntoConvenio");

        JsonObjectRequest jsonObjReq=new JsonObjectRequest(Request.Method.POST, _urlWebService, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                    }
                })
        {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                HashMap<String, String> headers = new HashMap <String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("WWW-Authenticate", "xBasic realm=".concat(""));
                headers.put("indLike", indLike);
                headers.put("codEmpleado",gestionSharedPreferences.getString("codEmpleado"));
                headers.put("codPunto", codPunto);
                headers.put("indLike", indLike);
                //headers.put("MyToken", sharedPreferences.getString("MyToken"));
                // headers.put("tokenFCM", ""+tokenFCM);
                return headers;
            }
        };

        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "_requestdetallemarkermegusta");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    private void WebServiceCheckin(final String codEmpleado, final String codPunto)
    {
        String _urlWebService= vars.ipServer.concat("/ws/CheckInEmpleadoConvenio");

        JsonObjectRequest jsonObjReq=new JsonObjectRequest(Request.Method.POST, _urlWebService, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                    }
                })
        {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                HashMap<String, String> headers = new HashMap <String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("WWW-Authenticate", "xBasic realm=".concat(""));
                headers.put("codEmpleado",codEmpleado);
                headers.put("codPunto", codPunto);
                //headers.put("MyToken", sharedPreferences.getString("MyToken"));
                headers.put("tokenFCM", ""+FirebaseInstanceId.getInstance().getToken());
                return headers;
            }
        };

        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "_requestdetallecheckin");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    private void WebServiceSetVisto()
    {
        String _urlWebService= vars.ipServer.concat("/ws/setVistoPuntoConvenio");
        Log.i("time","espera"+timeVisto);


        JsonObjectRequest jsonObjReq=new JsonObjectRequest(Request.Method.POST, _urlWebService, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        Log.i("time","bien"+timeVisto);

                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        Log.i("time","mal"+timeVisto);

                    }
                })
        {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                HashMap<String, String> headers = new HashMap <String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("WWW-Authenticate", "xBasic realm=".concat(""));
                headers.put("codEmpleado",gestionSharedPreferences.getString("codEmpleado"));
                headers.put("codPunto", codPunto);
                headers.put("tiempoVisto", ""+timeVisto);
                //headers.put("MyToken", sharedPreferences.getString("MyToken"));
                // headers.put("tokenFCM", ""+tokenFCM);
                return headers;
            }
        };

        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "_requestdetallevistomarker");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    private void WebServiceSetCalificacion(final String codPunto, final String codUsuario, final String rating)
    {
        String _urlWebService= vars.ipServer.concat("/ws/setCalificacion");
        Log.i("time","espera"+timeVisto);

        JsonObjectRequest jsonObjReq=new JsonObjectRequest(Request.Method.POST, _urlWebService, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        Log.i("time","bien"+timeVisto);

                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        Log.i("time","mal"+timeVisto);

                    }
                })
        {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                HashMap<String, String> headers = new HashMap <String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("WWW-Authenticate", "xBasic realm=".concat(""));
                headers.put("codPunto", ""+codPunto);
                headers.put("codEmpleado",""+codUsuario);
                headers.put("numCalifica", ""+rating);
                //headers.put("MyToken", sharedPreferences.getString("MyToken"));
                // headers.put("tokenFCM", ""+tokenFCM);
                return headers;
            }
        };

        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "_requestdetallecalificacion");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }
}
