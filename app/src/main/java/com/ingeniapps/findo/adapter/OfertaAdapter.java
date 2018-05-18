package com.ingeniapps.findo.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ingeniapps.findo.R;
import com.ingeniapps.findo.beans.Oferta;
import com.ingeniapps.findo.beans.PuntoConvenio;
import com.ingeniapps.findo.sharedPreferences.gestionSharedPreferences;
import com.ingeniapps.findo.vars.vars;

import java.util.ArrayList;
import java.util.List;

public class OfertaAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    private Activity activity;
    private LayoutInflater inflater;
    private List<Oferta> listadoOfertas;

    public final int TYPE_OFERTA=0;
    public final int TYPE_LOAD=1;
    private gestionSharedPreferences sharedPreferences;
    private Context context;
    OnLoadMoreListener loadMoreListener;
    boolean isLoading=false, isMoreDataAvailable=true;
    vars vars;
    int previousPosition=0;

    public interface OnItemClickListener
    {
        void onItemClick(Oferta oferta);
    }

    private final OfertaAdapter.OnItemClickListener listener;

    public OfertaAdapter(Activity activity, ArrayList<Oferta> listadoOfertas, OfertaAdapter.OnItemClickListener listener)
    {
        this.activity=activity;
        this.listadoOfertas=listadoOfertas;
        vars=new vars();
        sharedPreferences=new gestionSharedPreferences(this.activity);
        this.listener=listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if(viewType==TYPE_OFERTA)
        {
            return new OfertaHolder(inflater.inflate(R.layout.oferta_row_layout,parent,false));
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

        if(getItemViewType(position)==TYPE_OFERTA)
        {
            ((OfertaHolder)holder).bindData(listadoOfertas.get(position));
        }
    }

    @Override
    public int getItemViewType(int position)
    {
        if(listadoOfertas.get(position).getType().equals("oferta"))
        {
            return TYPE_OFERTA;
        }
        else
        {
            return TYPE_LOAD;
        }
    }

    @Override
    public int getItemCount()
    {
        return listadoOfertas.size();
    }

    public class OfertaHolder extends RecyclerView.ViewHolder
    {
        ImageView logoConvenioOferta;
        public TextView nomConvenioOferta;
        public TextView nomOferta;
        public TextView descuentoOferta;
        public TextView fecExpOferta;

        public OfertaHolder(View view)
        {
            super(view);
            logoConvenioOferta=(ImageView) view.findViewById(R.id.logoConvenioOferta);
            nomConvenioOferta=(TextView) view.findViewById(R.id.nomConvenioOferta);
            nomOferta=(TextView) view.findViewById(R.id.nomOferta);
            descuentoOferta=(TextView) view.findViewById(R.id.descuentoOferta);
            fecExpOferta=(TextView) view.findViewById(R.id.fecExpOferta);
        }

        void bindData(final Oferta oferta)
        {
            Glide.with(activity).
                    load(oferta.getImaProveedor().toString()).
                    thumbnail(0.5f).into(logoConvenioOferta);

            long timestamp = Long.parseLong(oferta.getFecExpOferta()) * 1000L;
            CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(timestamp,
                    System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);
            fecExpOferta.setText("Expira "+timeAgo);

            nomConvenioOferta.setText(oferta.getNomProveedor());
            nomOferta.setText(oferta.getNomOferta());

            if(TextUtils.equals(oferta.getDescOferta(),"0"))
            {
                descuentoOferta.setText("Beneficio solo para ti");
            }
            else
            {
                descuentoOferta.setText(oferta.getDescOferta());
            }

            itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override public void onClick(View v)
                {
                    listener.onItemClick(oferta);
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

    public List<Oferta> getOfertasList()
    {
        return listadoOfertas;
    }

}
