package com.ingeniapps.findo.adapter;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.ingeniapps.findo.activity.GridItemView;
import com.ingeniapps.findo.beans.Categoria;

import java.util.ArrayList;
import java.util.List;

public class GridViewAdapter extends BaseAdapter {

    private Activity activity;
    private String[] strings;
    public List<Integer> selectedPositions;
    public ArrayList<Categoria> listadoCategorias;

    public GridViewAdapter(ArrayList<Categoria> listadoCategorias, Activity activity) {
        this.listadoCategorias = listadoCategorias;
        this.activity = activity;
        selectedPositions = new ArrayList<>();
        Log.i("info",""+listadoCategorias.get(0).getNomCategoria());

    }

    @Override
    public int getCount() {
        return listadoCategorias.size();
    }

    @Override
    public Object getItem(int position) {
        return listadoCategorias.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        GridItemView customView = (convertView == null) ? new GridItemView(activity) : (GridItemView) convertView;
        customView.display(listadoCategorias.get(position).getImaCategoria(),listadoCategorias.get(position).getNomCategoria(), selectedPositions.contains(position));
        return customView;
    }
}
