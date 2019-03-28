package com.ingeniapps.findo.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ingeniapps.findo.R;
import com.ingeniapps.findo.beans.PuntoConvenio;
import com.ingeniapps.findo.sharedPreferences.gestionSharedPreferences;
import com.ingeniapps.findo.vars.vars;

import java.util.ArrayList;
import java.util.List;

public class ConvenioDetalleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    private Activity activity;
    private LayoutInflater inflater;
    private List<PuntoConvenio> listadoConvenios;

    public final int TYPE_CONVENIO=0;
    public final int TYPE_LOAD=1;
    private gestionSharedPreferences sharedPreferences;
    private Context context;
    OnLoadMoreListener loadMoreListener;
    boolean isLoading=false, isMoreDataAvailable=true;
    vars vars;
    int previousPosition=0;


    public interface OnItemClickListener
    {
        void onItemClick(PuntoConvenio convenio);
    }

    private final ConvenioDetalleAdapter.OnItemClickListener listener;

    public ConvenioDetalleAdapter(Activity activity, ArrayList<PuntoConvenio> listadoConvenios, ConvenioDetalleAdapter.OnItemClickListener listener)
    {
        this.activity=activity;
        this.listadoConvenios=listadoConvenios;
        vars=new vars();
        sharedPreferences=new gestionSharedPreferences(this.activity);
        this.listener=listener;

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if(viewType==TYPE_CONVENIO)
        {
            return new ConvenioHolder(inflater.inflate(R.layout.convenio_detalle_row_layout,parent,false));
        }
        else
        {
            return new LoadHolder(inflater.inflate(R.layout.row_load,parent,false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
    {
        if(position >= getItemCount()-1 && isMoreDataAvailable && !isLoading && loadMoreListener!=null)
        {
            isLoading = true;
            loadMoreListener.onLoadMore();
        }

        if(getItemViewType(position)==TYPE_CONVENIO)
        {
            ((ConvenioHolder)holder).bindData(listadoConvenios.get(position));
        }
    }

    @Override
    public int getItemViewType(int position)
    {
        if(listadoConvenios.get(position).getType().equals("punto"))
        {
            return TYPE_CONVENIO;
        }
        else
        {
            return TYPE_LOAD;
        }
    }

    @Override
    public int getItemCount()
    {
        return listadoConvenios.size();
    }

    public class ConvenioHolder extends RecyclerView.ViewHolder
    {
        ImageView logoConvenio;
        public TextView nomConvenio;
        public TextView nomCiudad;
        public TextView dirConvenio;
        public TextView distConvenio;
        public TextView timeConvenio;
        public TextView descuentoConvenio;
        public RatingBar ratingBarListaConvenio;

        public ConvenioHolder(View view)
        {
            super(view);
            logoConvenio=(ImageView) view.findViewById(R.id.logoConvenio);
            nomConvenio=(TextView) view.findViewById(R.id.nomConvenio);
            nomCiudad=(TextView) view.findViewById(R.id.nomCiudad);
            dirConvenio=(TextView) view.findViewById(R.id.dirConvenio);
            distConvenio=(TextView) view.findViewById(R.id.distConvenio);
            timeConvenio=(TextView) view.findViewById(R.id.timeConvenio);
            descuentoConvenio=(TextView) view.findViewById(R.id.descuentoConvenio);
            ratingBarListaConvenio=(RatingBar) view.findViewById(R.id.ratingBarListaConvenio);
        }

        void bindData(final PuntoConvenio convenio)
        {
            Glide.with(activity).
                    load(convenio.getImaProveedor().toString()).
                    thumbnail(0.5f).into(logoConvenio);

            dirConvenio.setText(convenio.getDirPunto());

            if(TextUtils.equals(convenio.getCodTipo().toString(),"2"))//CAJERO
            {
                descuentoConvenio.setTextColor(Color.parseColor("#e6ad42"));
                ratingBarListaConvenio.setVisibility(View.GONE);
                descuentoConvenio.setText("Horario: "+convenio.getHorPunto());
                nomConvenio.setText(convenio.getNomCajero());
            }

            if(TextUtils.equals(convenio.getCodTipo().toString(),"1"))//CONVENIO
            {

                if(TextUtils.equals(convenio.getDescPunto(),"0"))
                {
                    descuentoConvenio.setTextColor(Color.parseColor("#e6ad42"));
                    descuentoConvenio.setText("Beneficio solo para ti");
                }
                else
                {
                    descuentoConvenio.setTextColor(Color.parseColor("#e6ad42"));
                    descuentoConvenio.setText(convenio.getDescPunto());
                }


                nomConvenio.setText(convenio.getNomProveedor());
            }

            nomConvenio.setText(convenio.getNomProveedor());

            if(TextUtils.equals(convenio.getCalificacion().toString(),"null"))
            {
                ratingBarListaConvenio.setRating(0);
            }
            else
            {
                int b=(int)(Math.round(Float.parseFloat(convenio.getCalificacion())));
                ratingBarListaConvenio.setRating(b);
            }

            //VALIDACION DE DISTANCIAS
            if(TextUtils.isEmpty(convenio.getDistPunto()))
            {
                distConvenio.setVisibility(View.GONE);
            }
            else
            if(TextUtils.equals(convenio.getDistPunto(),"null"))
            {
                distConvenio.setText("Distancia no disponible");
            }
            else
            {
                distConvenio.setText(convenio.getDistPunto());
            }
            //VALIDACION DE TIEMPOS
            if(TextUtils.isEmpty(convenio.getTimePunto()))
            {
                timeConvenio.setVisibility(View.GONE);
            }
            else
            if(TextUtils.equals(convenio.getTimePunto(),"null"))
            {
                timeConvenio.setText("Tiempo no disponible");
            }
            else
            {
                timeConvenio.setText(convenio.getTimePunto());
            }


            nomCiudad.setText(convenio.getNomCiudad());

            itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override public void onClick(View v)
                {
                    listener.onItemClick(convenio);
                }
            });
        }
    }

    static class LoadHolder extends RecyclerView.ViewHolder
    {
        public LoadHolder(View itemView)
        {
            super(itemView);
        }
    }

    public void setMoreDataAvailable(boolean moreDataAvailable)
    {
        isMoreDataAvailable = moreDataAvailable;
    }
    /* notifyDataSetChanged is final method so we can't override it
        call adapter.notifyDataChanged(); after update the list
        */
    public void notifyDataChanged()
    {
        notifyDataSetChanged();
        isLoading = false;
    }

    public interface OnLoadMoreListener
    {
        void onLoadMore();
    }

    public void setLoadMoreListener(OnLoadMoreListener loadMoreListener)
    {
        this.loadMoreListener = loadMoreListener;
    }

    public List<PuntoConvenio> getNoticiasList()
    {
        return listadoConvenios;
    }

}
