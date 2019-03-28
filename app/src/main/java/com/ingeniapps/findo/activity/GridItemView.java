package com.ingeniapps.findo.activity;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ingeniapps.findo.R;

public class GridItemView extends FrameLayout {

    private TextView textView;
    private ImageView imaCategoria;
    private LinearLayout layout;

    public GridItemView(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.item_grid, this);
        textView = (TextView) getRootView().findViewById(R.id.text);
        imaCategoria = (ImageView) getRootView().findViewById(R.id.imaCategoria);
        layout = (LinearLayout) getRootView().findViewById(R.id.layout);
    }

    public void display(String imagen, String text, boolean isSelected) {
        textView.setText(text);
        Glide.with(GridItemView.this).
                load(imagen).
                thumbnail(0.5f).into(imaCategoria);
        display(isSelected);
    }

    public void display(boolean isSelected)
    {
        if(isSelected){
            layout.setBackgroundColor(Color.parseColor("#FFB4B4B4"));
            textView.setTextColor(Color.parseColor("#FFFFFF"));
        }
        else{
            layout.setBackgroundColor(Color.parseColor("#FFFFFF"));
            textView.setTextColor(Color.parseColor("#3F3E3E"));
        }
    }
}