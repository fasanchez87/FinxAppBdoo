package com.ingeniapps.findo.volley;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.ingeniapps.findo.helper.MyPreferenceManager;

/**
 * Created by FABiO on 23/01/2016.
 */
public class ControllerSingleton extends Application
{
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private static ControllerSingleton mInstance;

    public static final String TAG = ControllerSingleton.class.getSimpleName();

    private MyPreferenceManager pref;

    @Override
    public void onCreate()
    {
        super.onCreate();
        mInstance = this;
    }

    @Override
    protected void attachBaseContext(Context base)
    {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static synchronized ControllerSingleton getInstance()
    {
        return mInstance;
    }

    public MyPreferenceManager getPrefManager()
    {
        if (pref == null)
        {
            pref = new MyPreferenceManager(this);
        }

        return pref;
    }

    public RequestQueue getReqQueue()
    {
        if (mRequestQueue == null)
        {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    public <T> void addToReqQueue(Request<T> req, String tag)
    {

        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getReqQueue().add(req);
    }

    public <T> void addToReqQueue(Request<T> req)
    {
        req.setTag(TAG);
        getReqQueue().add(req);
    }

    public ImageLoader getImageLoader()
    {
        getReqQueue();
        if (mImageLoader == null)
        {
            mImageLoader = new ImageLoader(this.mRequestQueue,
                    new BitmapLruCache());
        }
        return this.mImageLoader;
    }

    public void cancelPendingReq(Object tag)
    {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }
}