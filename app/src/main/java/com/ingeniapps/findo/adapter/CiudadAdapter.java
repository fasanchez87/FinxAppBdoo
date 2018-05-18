package com.ingeniapps.findo.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.ingeniapps.findo.beans.Ciudad;
import com.ingeniapps.findo.R;
import com.ingeniapps.findo.sharedPreferences.gestionSharedPreferences;
import com.ingeniapps.findo.vars.vars;
import com.ingeniapps.findo.volley.ControllerSingleton;

import java.util.ArrayList;


public class CiudadAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    private Activity activity;
    private LayoutInflater inflater;
    private ArrayList<Ciudad> listadoCiudades;
    public final int TYPE_CIUDAD=0;
    public final int TYPE_LOAD=1;
    private gestionSharedPreferences sharedPreferences;
    OnLoadMoreListener loadMoreListener;
    boolean isLoading=false, isMoreDataAvailable=true;
    vars vars;

    ImageLoader imageLoader = ControllerSingleton.getInstance().getImageLoader();

    public CiudadAdapter(Activity activity, ArrayList<Ciudad> listadoCiudades)
    {
        this.activity=activity;
        this.listadoCiudades=listadoCiudades;
        vars=new vars();
        sharedPreferences=new gestionSharedPreferences(this.activity);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if(viewType==TYPE_CIUDAD)
        {
            return new CiudadHolder(inflater.inflate(R.layout.ciudades_row,parent,false));
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

        if(getItemViewType(position)==TYPE_CIUDAD)
        {
            ((CiudadHolder)holder).bindData(listadoCiudades.get(position));
        }
    }

    @Override
    public int getItemViewType(int position)
    {
        if(listadoCiudades.get(position).getType().equals("ciudad"))
        {
            return TYPE_CIUDAD;
        }
        else
        {
            return TYPE_LOAD;
        }
    }

    @Override
    public int getItemCount()
    {
        return listadoCiudades.size();
    }

    public class CiudadHolder extends RecyclerView.ViewHolder
    {
        public TextView nombreCiudad;
        public CheckBox checkCiudad;

        public CiudadHolder(View view)
        {
            super(view);
            nombreCiudad=(TextView) view.findViewById(R.id.nombreCiudad);
            checkCiudad=(CheckBox) view.findViewById(R.id.checkCiudad);
        }

        void bindData(final Ciudad ciudad)
        {
           nombreCiudad.setText(ciudad.getNomCiudad());
            //in some cases, it will prevent unwanted situations
           checkCiudad.setOnCheckedChangeListener(null);
            //if true, your checkbox will be selected, else unselected
           //checkCiudad.setChecked(checkCiudad.isSelected());
           checkCiudad.setChecked(ciudad.isSelected());
           checkCiudad.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
           {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                {
                    //set your object's last status
                    ciudad.setSelected(isChecked);
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

    public ArrayList<Ciudad> getCiudadList()
    {
        return listadoCiudades;
    }

}
