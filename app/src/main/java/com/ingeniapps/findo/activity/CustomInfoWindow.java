
package com.ingeniapps.findo.activity;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;
import com.ingeniapps.findo.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Custom implementation of {@link InfoWindowAdapter} to show a profile photo, title and snippet.
 * @author mitali@nextdoor.com (Mitali Gala)
 */
public class CustomInfoWindow implements InfoWindowAdapter {

    private View view;
    private Handler handler;
    private Set markerIdIconBlackList = Collections.emptySet();
    private Map<String, String> photos = Collections.emptyMap();

    public CustomInfoWindow(View view) {
        this.view = view;
    }

    public CustomInfoWindow(View view, Set markerIdIconBlackList) {
        this.view = view;
        this.markerIdIconBlackList = markerIdIconBlackList == null ? this.markerIdIconBlackList : markerIdIconBlackList;
    }

    public CustomInfoWindow(View view, Set markerIdIconBlackList, Map<String, String> photos) {
        this.view = view;
        this.markerIdIconBlackList = markerIdIconBlackList == null ? this.markerIdIconBlackList : markerIdIconBlackList;
        this.photos = photos == null ? this.photos : photos;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        if (viewHolder == null) {
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        }
        renderInfoWindow(marker, viewHolder);
        return view;
    }

    private void renderInfoWindow(Marker marker, ViewHolder viewHolder) {
        if (photos.containsKey(marker.getId())) {
//            whyWontThisImageShowUp(viewHolder.profileIcon, photos.get(marker.getId()));
            updateImageViewWithRemoteImage(marker, viewHolder.profileIcon, photos.get(marker.getId()));
        } else if (markerIdIconBlackList.contains(marker.getId())) {
            viewHolder.profileIcon.setImageResource(R.drawable.placeholder_cat);
        } else {
            viewHolder.profileIcon.setImageResource(R.drawable.placeholder_dog);
        }
        viewHolder.textNDTextTitle.setText(marker.getTitle());
        viewHolder.textNDTextSnippet.setText(marker.getSnippet());
    }

    /**
     * Try loading a photo url using Picasso without a callback.
     */
    public void whyWontThisImageShowUp(ImageView imageView,
                                       String imageUri) {
        Picasso.with(imageView.getContext())
                .load(imageUri)
                .resize(100, 100)
                .into(imageView);
    }

    /**
     * Try loading a photo url using picasso and add a callback.
     */
    public void updateImageViewWithRemoteImage(final Marker marker,
                                               ImageView imageView,
                                               final String imageUri)
    {
        Picasso.with(view.getContext())
                .load(imageUri)
                .into(imageView, new Callback()
                {
                    @Override
                    public void onSuccess()
                    {
                        if (marker != null && marker.isInfoWindowShown())
                        {
                            updateImageWithDelay(marker, 200);
                        }
                    }

                    @Override
                    public void onError() {
                        Log.e("Boo", String.format("Error loading image at %s", imageUri));
                    }
                });
    }

    private void updateImageWithDelay(final Marker marker, int delay)
    {
        if (handler == null)
        {
            handler = new Handler();
        }
        handler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                marker.hideInfoWindow();
                marker.showInfoWindow();
            }
        }, delay);
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    private static class ViewHolder
    {
        private ImageView profileIcon;
        private TextView textNDTextTitle;
        private TextView textNDTextSnippet;

        public ViewHolder(View view) {
            profileIcon = (ImageView) view.findViewById(R.id.profileIcon);
            textNDTextTitle = (TextView) view.findViewById(R.id.textNDTextTitle);
            textNDTextSnippet = (TextView) view.findViewById(R.id.textNDTextHeading);
        }
    }

    static class MarkerCallback implements Callback {
        Marker marker=null;

        MarkerCallback(Marker marker) {
            this.marker=marker;
        }

        @Override
        public void onError() {
            Log.e(getClass().getSimpleName(), "Error loading thumbnail!");
        }

        @Override
        public void onSuccess() {
            if (marker != null && marker.isInfoWindowShown()) {
                marker.showInfoWindow();
            }
        }
    }
}
