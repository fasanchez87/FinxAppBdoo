package com.ingeniapps.findo.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.bumptech.glide.Glide;
import com.ingeniapps.findo.R;
import com.ingeniapps.findo.beans.PuntoConvenio;
import com.ingeniapps.findo.sharedPreferences.gestionSharedPreferences;
import com.ingeniapps.findo.vars.vars;
import com.ingeniapps.findo.volley.ControllerSingleton;

import java.util.ArrayList;
import java.util.List;

public class ConvenioAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    private Context activity;
    private LayoutInflater inflater;
    private ArrayList<PuntoConvenio> listadoConvenios;

    private static final int ITEM = 0;
    private static final int LOADING = 1;

    private boolean isLoadingAdded = false;


    public final int TYPE_CONVENIO=0;
    public final int TYPE_LOAD=1;
    private gestionSharedPreferences sharedPreferences;
    private Context context;
/*
    OnLoadMoreListener loadMoreListener;
*/
    boolean isLoading=false, isMoreDataAvailable=true;
    vars vars;
    int previousPosition=0;


    public interface OnItemClickListener
    {
        void onItemClick(PuntoConvenio convenio);
    }

    private final ConvenioAdapter.OnItemClickListener listener;

    public ConvenioAdapter(Context context, ConvenioAdapter.OnItemClickListener listener)
    {
        this.activity=context;
        listadoConvenios=new ArrayList<>();
        vars=new vars();
        sharedPreferences=new gestionSharedPreferences(this.activity);
        this.listener=listener;

    }

   /* @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if(viewType==TYPE_CONVENIO)
        {
            return new ConvenioHolder(inflater.inflate(R.layout.convenio_row_layout,parent,false));
        }
        else
        {
            return new LoadHolder(inflater.inflate(R.layout.row_load,parent,false));
        }
    }*/

   /* public ArrayList<PuntoConvenio> getMovies() {
        return listadoConvenios;
    }

    public void setMovies(ArrayList<PuntoConvenio> movies) {
        this.listadoConvenios = movies;
    }*/


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case ITEM:
                viewHolder = getViewHolder(parent, inflater);
                break;
            case LOADING:
                View v2 = inflater.inflate(R.layout.row_load, parent, false);
                viewHolder = new LoadingVH(v2);
                break;
        }
        return viewHolder;
    }

    @NonNull
    private RecyclerView.ViewHolder getViewHolder(ViewGroup parent, LayoutInflater inflater) {
        RecyclerView.ViewHolder viewHolder;
        View v1 = inflater.inflate(R.layout.convenio_row_layout, parent, false);
        viewHolder = new MovieVH(v1);
        return viewHolder;
    }

    /*@Override
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
    }*/


  /*  @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
    {
        switch (getItemViewType(position))
        {
            case ITEM:
            ((ConvenioHolder)holder).bindData(listadoConvenios.get(position));
            break;
            case LOADING:
            //Do nothing
            break;
        }

    }
*/
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
    {

        final PuntoConvenio movie = listadoConvenios.get(position);

        switch (getItemViewType(position))
        {
            case ITEM:
                MovieVH movieVH = (MovieVH) holder;

               Glide.with(activity).
                        load(movie.getImaProveedor()).
                        thumbnail(0.5f).into(movieVH.logoConvenio);

                movieVH.nomCategoria.setText(movie.getNomCategoria());
                movieVH.nomCiudad.setText(movie.getNomCiudad());
                movieVH.nomConvenio.setText(movie.getNomProveedor());

                movieVH.itemView.setOnClickListener(new View.OnClickListener()
                {
                    @Override public void onClick(View v)
                    {
                        listener.onItemClick(movie);
                    }
                });

                break;
            case LOADING:
//                Do nothing
                break;
        }

    }

    @Override
    public int getItemCount() {
        return listadoConvenios == null ? 0 : listadoConvenios.size();
    }

    @Override
    public int getItemViewType(int position)
    {
        return (position == listadoConvenios.size() - 1 && isLoadingAdded) ? LOADING : ITEM;
    }

  /*  @Override
    public int getItemViewType(int position)
    {
        if(listadoConvenios.get(position).getType().equals("convenio")&& position == listadoConvenios.size() - 1 && isLoadingAdded)
        {
            return TYPE_CONVENIO;
        }
        else
        {
            return TYPE_LOAD;
        }
    }*/

   /* @Override
    public int getItemCount()
    {
        return listadoConvenios.size();
    }*/


     /*
   Helpers
   _________________________________________________________________________________________________
    */

    public void add(PuntoConvenio mc) {
        listadoConvenios.add(mc);
        notifyItemInserted(listadoConvenios.size() - 1);
    }

    public void addAll(ArrayList<PuntoConvenio> mcList) {
        for (PuntoConvenio mc : mcList) {
            add(mc);
        }
    }

    public void remove(PuntoConvenio city) {
        int position = listadoConvenios.indexOf(city);
        if (position > -1) {
            listadoConvenios.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void clear() {
        isLoadingAdded = false;
        while (getItemCount() > 0) {
            remove(getItem(0));
        }
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }


    public void addLoadingFooter() {
        isLoadingAdded = true;
        add(new PuntoConvenio());
    }

    public void removeLoadingFooter() {
        isLoadingAdded = false;

        int position = listadoConvenios.size() - 1;
        PuntoConvenio item = getItem(position);

        if (item != null) {
            listadoConvenios.remove(position);
            notifyItemRemoved(position);
        }
    }

    public PuntoConvenio getItem(int position) {
        return listadoConvenios.get(position);
    }

  /*  public class ConvenioHolder extends RecyclerView.ViewHolder
    {
        ImageView logoConvenio;
        public TextView nomConvenio;
        public TextView nomCiudad;
        public TextView nomCategoria;
        //public TextView dirConvenio;
        //public TextView distConvenio;
        //public TextView timeConvenio;
       // public TextView descuentoConvenio;
        //public RatingBar ratingBarListaConvenio;

        public ConvenioHolder(View view)
        {
            super(view);
            logoConvenio=(ImageView) view.findViewById(R.id.logoConvenio);
            nomConvenio=(TextView) view.findViewById(R.id.nomConvenio);
            nomCiudad=(TextView) view.findViewById(R.id.nomCiudad);
            nomCategoria=(TextView) view.findViewById(R.id.nomCategoria);
           *//* dirConvenio=(TextView) view.findViewById(R.id.dirConvenio);
            distConvenio=(TextView) view.findViewById(R.id.distConvenio);
            timeConvenio=(TextView) view.findViewById(R.id.timeConvenio);
            descuentoConvenio=(TextView) view.findViewById(R.id.descuentoConvenio);
            ratingBarListaConvenio=(RatingBar) view.findViewById(R.id.ratingBarListaConvenio);*//*
        }

        void bindData(final PuntoConvenio convenio)
        {
            Glide.with(activity).
                    load(convenio.getImaProveedor().toString()).
                    thumbnail(0.5f).into(logoConvenio);

            //dirConvenio.setText(convenio.getDirPunto());

            *//*if(TextUtils.equals(convenio.getCodTipo().toString(),"2"))//CAJERO
            {
                descuentoConvenio.setTextColor(Color.parseColor("#e6ad42"));
                ratingBarListaConvenio.setVisibility(View.GONE);
                descuentoConvenio.setText("Horario: "+convenio.getHorPunto());
                nomConvenio.setText(convenio.getNomCajero());
            }*//*

           *//* if(TextUtils.equals(convenio.getCodTipo().toString(),"1"))//CONVENIO
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
            }*//*

            nomConvenio.setText(convenio.getNomProveedor());
            nomCategoria.setText(convenio.getNomCategoria());

            *//*if(TextUtils.equals(convenio.getCalificacion().toString(),"null"))
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
            }*//*


            //nomCiudad.setText(convenio.getNomCiudad());
            //nomCategoria.setText(convenio.getNomCategoria());

            itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override public void onClick(View v)
                {
                    listener.onItemClick(convenio);
                }
            });
        }
    }*/

    /*static class LoadHolder extends RecyclerView.ViewHolder
    {
        public LoadHolder(View itemView)
        {
            super(itemView);
        }
    }*/

   /* public void setMoreDataAvailable(boolean moreDataAvailable)
    {
        isMoreDataAvailable = moreDataAvailable;
    }*/
    /* notifyDataSetChanged is final method so we can't override it
        call adapter.notifyDataChanged(); after update the list
        */
   /* public void notifyDataChanged()
    {
        notifyDataSetChanged();
        isLoading = false;
    }*/

   /* public interface OnLoadMoreListener
    {
        void onLoadMore();
    }*/

  /*  public void setLoadMoreListener(OnLoadMoreListener loadMoreListener)
    {
        this.loadMoreListener = loadMoreListener;
    }*/

   /* public List<PuntoConvenio> getNoticiasList()
    {
        return listadoConvenios;
    }*/



     /*
   View Holders
   _________________________________________________________________________________________________
    */

    /**
     * Main list's content ViewHolder
     */
    protected class MovieVH extends RecyclerView.ViewHolder
    {
        ImageView logoConvenio;
        public TextView nomConvenio;
        public TextView nomCiudad;
        public TextView nomCategoria;

        public MovieVH(View itemView)
        {
            super(itemView);
            logoConvenio=(ImageView) itemView.findViewById(R.id.logoConvenio);
            nomConvenio=(TextView) itemView.findViewById(R.id.nomConvenio);
            nomCiudad=(TextView) itemView.findViewById(R.id.nomCiudad);
            nomCategoria=(TextView) itemView.findViewById(R.id.nomCategoria);

            itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override public void onClick(View v)
                {
                    //listener.onItemClick(convenio);
                }
            });
        }
    }


    protected class LoadingVH extends RecyclerView.ViewHolder {

        public LoadingVH(View itemView) {
            super(itemView);
        }
    }

}
