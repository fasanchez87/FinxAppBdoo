package com.ingeniapps.findo.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.ingeniapps.findo.R;
import com.ingeniapps.findo.beans.Categoria;
import com.ingeniapps.findo.beans.Ciudad;
import com.ingeniapps.findo.sharedPreferences.gestionSharedPreferences;
import com.ingeniapps.findo.vars.vars;
import com.ingeniapps.findo.volley.ControllerSingleton;

import java.util.ArrayList;


public class CategoriaAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    private Activity activity;
    private LayoutInflater inflater;
    private ArrayList<Categoria> listadoCategoria;
    public final int TYPE_CATEGORIA=0;
    public final int TYPE_LOAD=1;
    private gestionSharedPreferences sharedPreferences;
    OnLoadMoreListener loadMoreListener;
    boolean isLoading=false, isMoreDataAvailable=true;
    vars vars;

    ImageLoader imageLoader = ControllerSingleton.getInstance().getImageLoader();

    public CategoriaAdapter(Activity activity, ArrayList<Categoria> listadoCategoria)
    {
        this.activity=activity;
        this.listadoCategoria=listadoCategoria;
        vars=new vars();
        sharedPreferences=new gestionSharedPreferences(this.activity);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if(viewType==TYPE_CATEGORIA)
        {
            return new CategoriaHolder(inflater.inflate(R.layout.categorias_row,parent,false));
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

        if(getItemViewType(position)==TYPE_CATEGORIA)
        {
            ((CategoriaHolder)holder).bindData(listadoCategoria.get(position));
        }
    }

    @Override
    public int getItemViewType(int position)
    {
        if(listadoCategoria.get(position).getType().equals("categoria"))
        {
            return TYPE_CATEGORIA;
        }
        else
        {
            return TYPE_LOAD;
        }
    }

    @Override
    public int getItemCount()
    {
        return listadoCategoria.size();
    }

    public class CategoriaHolder extends RecyclerView.ViewHolder
    {
        public TextView nombreCategoria;
        public CheckBox checkCategoria;


        public CategoriaHolder(View view)
        {
            super(view);
            nombreCategoria=(TextView) view.findViewById(R.id.nombreCategoria);
            checkCategoria=(CheckBox) view.findViewById(R.id.checkCategoria);
        }

        void bindData(final Categoria categoria)
        {
            nombreCategoria.setText(categoria.getNomCategoria());
            //in some cases, it will prevent unwanted situations
            checkCategoria.setOnCheckedChangeListener(null);
            //if true, your checkbox will be selected, else unselected
            //checkCategoria.setChecked(checkCategoria.isSelected());
            checkCategoria.setChecked(categoria.isSelected());
            checkCategoria.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                {
                    //set your object's last status
                    categoria.setSelected(isChecked);
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

    public ArrayList<Categoria> getCategoriaList()
    {
        return listadoCategoria;
    }

}
