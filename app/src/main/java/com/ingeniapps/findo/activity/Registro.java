package com.ingeniapps.findo.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

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
import com.ingeniapps.findo.vars.vars;
import com.ingeniapps.findo.volley.ControllerSingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Registro extends AppCompatActivity
{

    public vars vars;
    private String tokenFCM="";
    private ProgressDialog progressDialog;
    EditText nombreUsuario,emailUsuario,claveUsuario,confirmaClaveUsuario;
    Button buttonRegistro;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_registro);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        vars=new vars();
        context=this;

        if(!TextUtils.isEmpty(FirebaseInstanceId.getInstance().getToken()))
        {
            tokenFCM=FirebaseInstanceId.getInstance().getToken();
        }

        nombreUsuario=(EditText) findViewById(R.id.nombreUsuario);
        emailUsuario=(EditText) findViewById(R.id.emailUsuario);
        claveUsuario=(EditText) findViewById(R.id.claveUsuario);
        confirmaClaveUsuario=(EditText) findViewById(R.id.confirmaClaveUsuario);
        buttonRegistro=(Button) findViewById(R.id.buttonRegistro);
        //EVENTO BOTON REGISTRO
        buttonRegistro.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                if (TextUtils.isEmpty(nombreUsuario.getText().toString()))
                {
                    Snackbar.make(findViewById(android.R.id.content),
                            "Digita tu nombre.", Snackbar.LENGTH_LONG).show();
                    return;
                }

                if (TextUtils.isEmpty(emailUsuario.getText().toString())||!(isValidEmail(emailUsuario.getText().toString())))
                {
                    Snackbar.make(findViewById(android.R.id.content),
                            "Digita un email valido.", Snackbar.LENGTH_LONG).show();
                    return;
                }

                if (TextUtils.isEmpty(claveUsuario.getText().toString()))
                {
                    Snackbar.make(findViewById(android.R.id.content),
                            "Digita tu contraseña.", Snackbar.LENGTH_LONG).show();
                    return;
                }

                if (claveUsuario.getText().toString().length()<=5)
                {
                    Snackbar.make(findViewById(android.R.id.content),
                            "Tu contraseña debe tener al menos (6) dígitos.", Snackbar.LENGTH_LONG).show();
                    return;
                }

                if (TextUtils.isEmpty(confirmaClaveUsuario.getText().toString()))
                {
                    Snackbar.make(findViewById(android.R.id.content),
                            "Confirmar tu contraseña.", Snackbar.LENGTH_LONG).show();
                    return;
                }

                if (!TextUtils.equals(claveUsuario.getText().toString(),confirmaClaveUsuario.getText().toString()))
                {
                    Snackbar.make(findViewById(android.R.id.content),
                            "Verifica de nuevo tu contraseña.", Snackbar.LENGTH_LONG).show();
                    return;
                }

                WebServiceRegistroUsuario();
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

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        ControllerSingleton.getInstance().cancelPendingReq("_requestRegistroUsuario");
    }

    private void WebServiceRegistroUsuario()
    {
        String _urlWebService=vars.ipServer.concat("/ws/RegistroUsuario");

        progressDialog = new ProgressDialog(new ContextThemeWrapper(Registro.this,R.style.AppCompatAlertDialogStyle));
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Registrando...");
        progressDialog.show();
        progressDialog.setCancelable(false);

        JsonObjectRequest jsonObjReq=new JsonObjectRequest(Request.Method.POST, _urlWebService, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        try
                        {
                            Boolean status=response.getBoolean("status");
                            String message=response.getString("message");

                            if(status)
                            {
                                if (!((Activity) context).isFinishing())
                                {
                                    if (progressDialog.isShowing())
                                    {
                                        progressDialog.dismiss();

                                    }
                                }                                   AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Registro.this,R.style.AlertDialogTheme));
                                builder
                                        .setTitle(R.string.title)
                                        .setMessage(message)
                                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(DialogInterface dialog, int id)
                                            {
                                                finish();
                                            }
                                        }).setCancelable(false).show().getButton(DialogInterface.BUTTON_POSITIVE).
                                        setTextColor(getResources().getColor(R.color.colorPrimary));
                            }
                            else
                            if (!status)
                            {
                                if (!((Activity) context).isFinishing())
                                {
                                    if (progressDialog.isShowing())
                                    {
                                        progressDialog.dismiss();

                                    }
                                }
                                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Registro.this,R.style.AlertDialogTheme));
                                builder
                                        .setTitle(R.string.title)
                                        .setMessage(message)
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
                            if (!((Activity) context).isFinishing())
                            {
                                if (progressDialog.isShowing())
                                {
                                    progressDialog.dismiss();

                                }
                            }
                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Registro.this,R.style.AlertDialogTheme));
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
                        if (!((Activity) context).isFinishing())
                        {
                            if (progressDialog.isShowing())
                            {
                                progressDialog.dismiss();

                            }
                        }                           AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Registro.this,R.style.AlertDialogTheme));
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
                            if (!((Activity) context).isFinishing())
                            {
                                if (progressDialog.isShowing())
                                {
                                    progressDialog.dismiss();

                                }
                            }                               builder
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
                            if (!((Activity) context).isFinishing())
                            {
                                if (progressDialog.isShowing())
                                {
                                    progressDialog.dismiss();

                                }
                            }                               builder
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
                            if (!((Activity) context).isFinishing())
                            {
                                if (progressDialog.isShowing())
                                {
                                    progressDialog.dismiss();

                                }
                            }                               builder
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
                            if (!((Activity) context).isFinishing())
                            {
                                if (progressDialog.isShowing())
                                {
                                    progressDialog.dismiss();

                                }
                            }                               builder
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
                            if (!((Activity) context).isFinishing())
                            {
                                if (progressDialog.isShowing())
                                {
                                    progressDialog.dismiss();

                                }
                            }                               builder
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
                            if (!((Activity) context).isFinishing())
                            {
                                if (progressDialog.isShowing())
                                {
                                    progressDialog.dismiss();

                                }
                            }                               builder
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
                //headers.put("ideDevice", idDevice);
                headers.put("tokenFCM", tokenFCM);
                headers.put("codSistema", "1");
                headers.put("corEmpleado", ""+emailUsuario.getText().toString());
                headers.put("clvEmpleado", ""+claveUsuario.getText().toString());
                headers.put("nomEmpleado", ""+nombreUsuario.getText().toString());
                return headers;
            }
        };

        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "_requestRegistroUsuario");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }
}
