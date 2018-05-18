package com.ingeniapps.findo.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;

/**
 * Created by Ingenia Applications on 29/12/2017.
 */

public class RecyclerViewDisabler implements RecyclerView.OnItemTouchListener {

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        return true;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }
}
