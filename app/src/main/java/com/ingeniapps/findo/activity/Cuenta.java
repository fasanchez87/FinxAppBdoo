package com.ingeniapps.findo.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.iid.FirebaseInstanceId;
import com.ingeniapps.findo.R;
import com.ingeniapps.findo.sharedPreferences.gestionSharedPreferences;
import com.ingeniapps.findo.vars.vars;
import com.ingeniapps.findo.volley.ControllerSingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN;

public class Cuenta extends AppCompatActivity
{
    EditText editTextNombreEmpleadoCuenta,editTextEmailEmpleadoCuenta;
    TextView textViewCerrarSesion;
    Switch switchCercaniaConveniosCuenta,switchPushOfertasConveniosCuenta;
    private String codEmpleado;
    private String nomEmpleado;
    private String emailEmpleado;
    com.ingeniapps.findo.vars.vars vars;
    com.ingeniapps.findo.sharedPreferences.gestionSharedPreferences gestionSharedPreferences;
    private String indSetUpUbicacion;
    private String indSetUpPush;
    private Button buttonSaveInfo;
    private ProgressDialog progressDialog;
    private InputMethodManager imm = null;
    private RelativeLayout layoutMacroEsperaCuenta;
    private RelativeLayout rlfragmentCuenta;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cuenta);
        gestionSharedPreferences=new gestionSharedPreferences(this);
        vars=new vars();
        context=this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //this line shows back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setElevation(0);

        toolbar.setNavigationOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                finish();
            }
        });


        editTextNombreEmpleadoCuenta=(EditText) findViewById(R.id.editTextNombreEmpleadoCuenta);
        editTextEmailEmpleadoCuenta=(EditText) findViewById(R.id.editTextEmailEmpleadoCuenta);
        editTextEmailEmpleadoCuenta.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        layoutMacroEsperaCuenta=(RelativeLayout) findViewById(R.id.layoutMacroEsperaCuenta);
        rlfragmentCuenta=(RelativeLayout) findViewById(R.id.rlfragmentCuenta);

        switchCercaniaConveniosCuenta=(Switch) findViewById(R.id.switchCercaniaConveniosCuenta);
        switchPushOfertasConveniosCuenta=(Switch) findViewById(R.id.switchPushOfertasConveniosCuenta);
        textViewCerrarSesion=(TextView) findViewById(R.id.textViewCerrarSesion);

        //editTextNombreEmpleadoCuenta.setText(""+gestionSharedPreferences.getString("codEmpleado"));
        //editTextEmailEmpleadoCuenta.setText(""+gestionSharedPreferences.getString("nomEmpleado"));

        textViewCerrarSesion.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Cuenta.this,R.style.AlertDialogTheme));
                builder
                        .setMessage("¿Esta seguro de cerrar sesión ahora?")
                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int id)
                            {
                                WebServiceCerrarSesion();
                            }
                        }).setNegativeButton("Cancelar", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {

                    }
                }).show();
            }
        });

        buttonSaveInfo=(Button)findViewById(R.id.buttonSaveInfo);
        buttonSaveInfo.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                nomEmpleado=editTextNombreEmpleadoCuenta.getText().toString();
                emailEmpleado=editTextEmailEmpleadoCuenta.getText().toString();

                imm.hideSoftInputFromWindow(editTextNombreEmpleadoCuenta.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(editTextEmailEmpleadoCuenta.getWindowToken(), 0);

                WebServiceSaveInfo(nomEmpleado,emailEmpleado);
            }
        });

        /*if(TextUtils.isEmpty(gestionSharedPreferences.getString("indAlertaConvenio")))
        {
            gestionSharedPreferences.putString("indAlertaConvenio","1");
        }*/

        if(TextUtils.isEmpty(gestionSharedPreferences.getString("indAlertaOferta")))
        {
            gestionSharedPreferences.putString("indAlertaOferta","1");
        }

        switchCercaniaConveniosCuenta.setChecked(TextUtils.equals(gestionSharedPreferences.getString("indAlertaConvenio"),"1")?true:false);
        switchCercaniaConveniosCuenta.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if(isChecked)
                {
                    gestionSharedPreferences.putString("indAlertaConvenio","1");
                }
                else
                {
                    gestionSharedPreferences.putString("indAlertaConvenio","0");
                }

                WebServiceSetUpUbicacionConvenio(""+gestionSharedPreferences.getString("indAlertaConvenio"));
            }
        });

        switchPushOfertasConveniosCuenta.setChecked(TextUtils.equals(gestionSharedPreferences.getString("indAlertaOferta"),"1")?true:false);
        switchPushOfertasConveniosCuenta.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if(isChecked)
                {
                    gestionSharedPreferences.putString("indAlertaOferta","1");
                }
                else
                {
                    gestionSharedPreferences.putString("indAlertaOferta","0");
                }

                WebServiceSetUpOferta(""+gestionSharedPreferences.getString("indAlertaOferta"));
            }
        });

    }

    public final static boolean isValidEmail(CharSequence target)
    {
        if (target == null)
        {
            return false;
        }
        else
        {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_config, menu);
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            // your code
            finish();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menu_config_params:
                Intent intent=new Intent(Cuenta.this, ConfigCiudad.class);
                intent.putExtra("configInterna",true);
                startActivity(intent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onPause()
    {
        super.onPause();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        WebServiceGetInfo();
        updateTokenFCMToServer();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        /*ControllerSingleton.getInstance().cancelPendingReq("cerrarSesionCuenta");
        ControllerSingleton.getInstance().cancelPendingReq("update_token_fcm_cuenta");
        ControllerSingleton.getInstance().cancelPendingReq("setUpNotificacionConveniosrequest");
        ControllerSingleton.getInstance().cancelPendingReq("setUpOfertaConveniosrequest");
        ControllerSingleton.getInstance().cancelPendingReq("saveInfo");
        ControllerSingleton.getInstance().cancelPendingReq("getinfoempleado");*/
    }

    private void WebServiceCerrarSesion()
    {
        progressDialog = new ProgressDialog(new ContextThemeWrapper(Cuenta.this,R.style.AppCompatAlertDialogStyle));
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Cerrando sesión, un momento...");
        progressDialog.show();
        progressDialog.setCancelable(false);

        String _urlWebServiceUpdateToken = vars.ipServer.concat("/ws/CerrarSesion");

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
                                gestionSharedPreferences.clear();
                                Intent i = new Intent(Cuenta.this, Login.class);
                                startActivity(i);
                                finish();
                                if (!((Activity) context).isFinishing())
                                {
                                    if (progressDialog.isShowing() )
                                    {
                                        progressDialog.dismiss();

                                    }
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
                        if (!((Activity) context).isFinishing())
                        {
                            if (progressDialog.isShowing() )
                            {
                                progressDialog.dismiss();

                            }
                        }

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
                return headers;
            }
        };
        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "cerrarSesionCuenta");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }


    private void WebServiceGetInfo()
    {

        String _urlWebServiceUpdateToken = vars.ipServer.concat("/ws/getInfo");

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET, _urlWebServiceUpdateToken, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        try
                        {
                            Boolean status = response.getBoolean("status");

                            if(status)
                            {
                                editTextNombreEmpleadoCuenta.setText(""+response.getString("nomEmpleado"));
                                editTextEmailEmpleadoCuenta.setText(""+response.getString("emaEmpleado"));
                                layoutMacroEsperaCuenta.setVisibility(View.GONE);
                                rlfragmentCuenta.setVisibility(View.VISIBLE);
                                Log.i("codigo","cofigo: "+gestionSharedPreferences.getString("codEmpleado"));
                            }
                            else
                            {
                                layoutMacroEsperaCuenta.setVisibility(View.GONE);
                                rlfragmentCuenta.setVisibility(View.VISIBLE);
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
                        layoutMacroEsperaCuenta.setVisibility(View.GONE);
                        rlfragmentCuenta.setVisibility(View.VISIBLE);
                        Snackbar.make(findViewById(android.R.id.content),
                                ""+error.getMessage().toString(), Snackbar.LENGTH_LONG).show();

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
                return headers;
            }
        };
        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "getinfoempleado");
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
        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "update_token_fcm_cuenta");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    private void WebServiceSetUpUbicacionConvenio(final String indSetUp)
    {

        progressDialog = new ProgressDialog(new ContextThemeWrapper(Cuenta.this,R.style.AppCompatAlertDialogStyle));
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Un momento...");
        progressDialog.show();
        progressDialog.setCancelable(false);

        String _urlWebServiceUpdateToken = vars.ipServer.concat("/ws/setUpNotificacionConvenios");

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, _urlWebServiceUpdateToken, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        try
                        {
                            Boolean status = response.getBoolean("status");
                            if(status)
                            {
                                if (!((Activity) context).isFinishing())
                                {
                                    if (progressDialog.isShowing() )
                                    {
                                        progressDialog.dismiss();

                                    }
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
                            }
                        }
                        catch (JSONException e)
                        {
                            //progressBar.setVisibility(View.GONE);
                            Log.e("Error", ""+ e.getMessage().toString());
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
                            if (progressDialog.isShowing() )
                            {
                                progressDialog.dismiss();

                            }
                        }
                        Snackbar.make(findViewById(android.R.id.content),
                                ""+error.getMessage().toString(), Snackbar.LENGTH_LONG).show();

                    }

                })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("WWW-Authenticate", "xBasic realm=".concat(""));
                headers.put("codEmpleado",gestionSharedPreferences.getString("codEmpleado"));
                headers.put("indSetUp",indSetUp);
                //headers.put("tokenFCM", refreshedToken);
                return headers;
            }
        };

        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "setUpNotificacionConveniosrequest");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    private void WebServiceSetUpOferta(final String indSetUp)
    {

        progressDialog = new ProgressDialog(new ContextThemeWrapper(Cuenta.this,R.style.AppCompatAlertDialogStyle));
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Un momento...");
        progressDialog.show();
        progressDialog.setCancelable(false);

        String _urlWebServiceUpdateToken = vars.ipServer.concat("/ws/setUpNotificacionOfertas");

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, _urlWebServiceUpdateToken, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        try
                        {
                            Boolean status = response.getBoolean("status");
                            if(status)
                            {
                                if (!((Activity) context).isFinishing())
                                {
                                    if (progressDialog.isShowing() )
                                    {
                                        progressDialog.dismiss();

                                    }
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
                            }
                        }
                        catch (JSONException e)
                        {
                            //progressBar.setVisibility(View.GONE);
                            Log.e("Error", ""+ e.getMessage().toString());
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
                            if (progressDialog.isShowing() )
                            {
                                progressDialog.dismiss();

                            }
                        }
                        Snackbar.make(findViewById(android.R.id.content),
                                ""+error.getMessage().toString(), Snackbar.LENGTH_LONG).show();

                    }

                })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("WWW-Authenticate", "xBasic realm=".concat(""));
                headers.put("codEmpleado",gestionSharedPreferences.getString("codEmpleado"));
                headers.put("indSetUp",indSetUp);
                //headers.put("tokenFCM", refreshedToken);
                return headers;
            }
        };

        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "setUpOfertaConveniosrequest");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    private void WebServiceSaveInfo(final String nombre, final String emailEmpleado)
    {
        progressDialog = new ProgressDialog(new ContextThemeWrapper(Cuenta.this,R.style.AppCompatAlertDialogStyle));
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Un momento...");
        progressDialog.show();
        progressDialog.setCancelable(false);

        String _urlWebServiceUpdateToken = vars.ipServer.concat("/ws/saveInfo");

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, _urlWebServiceUpdateToken, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        try
                        {
                            Boolean status = response.getBoolean("status");
                            if(status)
                            {

                                if (!((Activity) context).isFinishing())
                                {
                                    if (progressDialog.isShowing() )
                                    {
                                        gestionSharedPreferences.putString("emaEmpleado",TextUtils.isEmpty(emailEmpleado)?"":emailEmpleado);
                                        progressDialog.dismiss();
                                    }
                               }


                            }

                            else
                            {
                                if (!((Activity) context).isFinishing())
                                {
                                    if (progressDialog.isShowing())
                                    {
                                        progressDialog.dismiss();

                                    }
                                }
                            }
                        }
                        catch (JSONException e)
                        {
                            //progressBar.setVisibility(View.GONE);
                            Log.e("Error", ""+ e.getMessage().toString());
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
                        Snackbar.make(findViewById(android.R.id.content),
                                ""+error.getMessage().toString(), Snackbar.LENGTH_LONG).show();





                    }

                })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("WWW-Authenticate", "xBasic realm=".concat(""));
                headers.put("codEmpleado",gestionSharedPreferences.getString("codEmpleado"));
                headers.put("nomEmpleado",nombre);
                headers.put("corEmpleado",emailEmpleado);
                return headers;
            }
        };

        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "saveInfo");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }
}
