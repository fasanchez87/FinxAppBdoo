package com.ingeniapps.findo.fragment;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firebase.iid.FirebaseInstanceId;
import com.ingeniapps.findo.R;
import com.ingeniapps.findo.volley.ControllerSingleton;
import com.ingeniapps.findo.vars.vars;
import com.ingeniapps.findo.sharedPreferences.gestionSharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class Contacto extends Fragment
{

    vars vars;
    gestionSharedPreferences sharedPreferences;

    EditText editTextMensajeContacto;
    EditText editTextEmailContacto;
    private ProgressDialog progressDialog;
    private InputMethodManager imm = null;
    private Button buttonEnviarComentarios;
    private RelativeLayout layoutMacroEsperaContacto;
    private LinearLayout llContacto;
    private Context context;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //setHasOptionsMenu(true);
        vars=new vars();
        context=getActivity();
        sharedPreferences=new gestionSharedPreferences(getActivity());
    }

    @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_contacto, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        editTextMensajeContacto=(EditText)getActivity().findViewById(R.id.editTextMensajeContacto);
        editTextEmailContacto=(EditText)getActivity().findViewById(R.id.editTextEmailContacto);
       // editTextEmailContacto.setText(TextUtils.isEmpty(sharedPreferences.getString("emaEmpleado").toString())?"":sharedPreferences.getString("emaEmpleado").toString());
        imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        layoutMacroEsperaContacto=(RelativeLayout) getActivity().findViewById(R.id.layoutMacroEsperaContacto);
        llContacto=(LinearLayout) getActivity().findViewById(R.id.llContacto);
        buttonEnviarComentarios=getActivity().findViewById(R.id.buttonEnviarComentario);
        buttonEnviarComentarios.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(TextUtils.isEmpty(editTextEmailContacto.getText().toString()) || !isValidEmail(editTextEmailContacto.getText().toString()))
                {
                    imm.hideSoftInputFromWindow(editTextEmailContacto.getWindowToken(), 0);
                    Snackbar.make(getActivity().findViewById(android.R.id.content),
                            "Necesitamos un email valido, por favor...", Snackbar.LENGTH_LONG).show();
                    return;
                }
                else
                if(TextUtils.isEmpty(editTextMensajeContacto.getText().toString()))
                {
                    imm.hideSoftInputFromWindow(editTextMensajeContacto.getWindowToken(), 0);
                    Snackbar.make(getActivity().findViewById(android.R.id.content),
                            "Déjanos saber tus sugerencias, por favor...", Snackbar.LENGTH_LONG).show();
                    return;
                }
                else
                {
                    imm.hideSoftInputFromWindow(editTextMensajeContacto.getWindowToken(), 0);
                    WebServiceContacto(editTextMensajeContacto.getText().toString(),editTextEmailContacto.getText().toString());
                    return;
                }
            }
        });




    }

   /* @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.menu_contacto, menu);  // Use filter.xml from step 1
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if(id == R.id.menu_enviar_contacto)
        {
            if(TextUtils.isEmpty(editTextEmailContacto.getText().toString()) || !isValidEmail(editTextEmailContacto.getText().toString()))
            {
                imm.hideSoftInputFromWindow(editTextEmailContacto.getWindowToken(), 0);
                Snackbar.make(getActivity().findViewById(android.R.id.content),
                        "Necesitamos un email valido, por favor...", Snackbar.LENGTH_LONG).show();
                return false;
            }
            else
            if(TextUtils.isEmpty(editTextMensajeContacto.getText().toString()))
            {
                imm.hideSoftInputFromWindow(editTextMensajeContacto.getWindowToken(), 0);
                Snackbar.make(getActivity().findViewById(android.R.id.content),
                        "Déjanos saber tus sugerencias, por favor...", Snackbar.LENGTH_LONG).show();
                return false;
            }
            else
            {
                imm.hideSoftInputFromWindow(editTextMensajeContacto.getWindowToken(), 0);
                WebServiceContacto(editTextMensajeContacto.getText().toString(),editTextEmailContacto.getText().toString());
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }*/

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        ControllerSingleton.getInstance().cancelPendingReq("requestContacto");
        ControllerSingleton.getInstance().cancelPendingReq("request_token_fcm_contacto");
    }

    private void WebServiceContacto(final String novedad, final String email)
    {
        progressDialog = new ProgressDialog(new ContextThemeWrapper(Contacto.this.getActivity(),R.style.AppCompatAlertDialogStyle));
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Enviando mensaje...");
        progressDialog.show();
        progressDialog.setCancelable(false);

        String _urlWebService = vars.ipServer.concat("/ws/EnviarContacto");

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, _urlWebService, null,
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
                                if (!((Activity) context).isFinishing())
                                {
                                    if (progressDialog.isShowing())
                                    {
                                        progressDialog.dismiss();

                                    }
                                }                                   Snackbar.make(getActivity().findViewById(android.R.id.content),
                                        ""+message, Snackbar.LENGTH_LONG).show();
                                editTextMensajeContacto.setText(null);
                                editTextEmailContacto.setText(null);
                            }

                            else
                            {
                                if (!((Activity) context).isFinishing())
                                {
                                    if (progressDialog.isShowing())
                                    {
                                        progressDialog.dismiss();

                                    }
                                }                                   Snackbar.make(getActivity().findViewById(android.R.id.content),
                                        ""+message, Snackbar.LENGTH_LONG).show();
                            }
                        }
                        catch (JSONException e)
                        {
                            //progressBar.setVisibility(View.GONE);
                            e.printStackTrace();
                        }
                    }
                },new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                if (error instanceof TimeoutError)
                {
                    if (!((Activity) context).isFinishing())
                    {
                        if (progressDialog.isShowing())
                        {
                            progressDialog.dismiss();

                        }
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Contacto.this.getActivity(),R.style.AlertDialogTheme));
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Contacto.this.getActivity(),R.style.AlertDialogTheme));
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Contacto.this.getActivity(),R.style.AlertDialogTheme));
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
                    if (!((Activity) context).isFinishing())
                    {
                        if (progressDialog.isShowing())
                        {
                            progressDialog.dismiss();

                        }
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Contacto.this.getActivity(),R.style.AlertDialogTheme));
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Contacto.this.getActivity(),R.style.AlertDialogTheme));
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Contacto.this.getActivity(),R.style.AlertDialogTheme));
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
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("WWW-Authenticate", "xBasic realm=".concat(""));
                headers.put("codEmpleado", ""+sharedPreferences.getString("codEmpleado"));
                headers.put("desMensaje", ""+novedad);
                headers.put("emaEmpleado", ""+email);
                return headers;
            }
        };

        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "requestContacto");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }



    @Override
    public void onResume()
    {
        super.onResume();
        WebServiceGetInfo();
        updateTokenFCMToServer();
        Log.i("Contacto","onResume");
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
                headers.put("codEmpleado", sharedPreferences.getString("codEmpleado"));
                headers.put("tokenFCM", ""+ FirebaseInstanceId.getInstance().getToken());
                return headers;
            }
        };
        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "request_token_fcm_contacto");
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
                                editTextEmailContacto.setText(""+response.getString("emaEmpleado"));
                                layoutMacroEsperaContacto.setVisibility(View.GONE);
                                llContacto.setVisibility(View.VISIBLE);
                                Log.i("Contacto","wservice");
                            }
                            else
                            {
                                layoutMacroEsperaContacto.setVisibility(View.GONE);
                                llContacto.setVisibility(View.VISIBLE);
                                Log.i("Contacto","wservice");
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

                    }
                })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("WWW-Authenticate", "xBasic realm=".concat(""));
                headers.put("codEmpleado", sharedPreferences.getString("codEmpleado"));
                return headers;
            }
        };

        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "getinfoempleado");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }
}
