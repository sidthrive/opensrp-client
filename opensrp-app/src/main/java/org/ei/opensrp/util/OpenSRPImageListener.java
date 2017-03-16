package org.ei.opensrp.util;

import android.widget.ImageView;
import android.widget.RemoteViews;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.android.volley.toolbox.ImageLoader.ImageListener;

public abstract class OpenSRPImageListener implements ImageListener {

    private final ImageView imageView;
    private final RemoteViews remoteView;
    private final int defaultImageResId;
    private final int errorImageResId;
    private final int imageViewId;
    private String absoluteFileName;
    private boolean hasImageViewTag;

    public OpenSRPImageListener(ImageView imageView, int defaultImageResId, int errorImageResId) {
        this.imageView = imageView;
        this.defaultImageResId = defaultImageResId;
        this.errorImageResId = errorImageResId;
        this.remoteView = null;
        this.imageViewId = 0;
    }

    public OpenSRPImageListener(RemoteViews remoteView, int imageViewId, int defaultImageResId, int errorImageResId) {
        this.remoteView = remoteView;
        this.defaultImageResId = defaultImageResId;
        this.errorImageResId = errorImageResId;
        this.imageView = null;
        this.imageViewId = imageViewId;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public int getDefaultImageResId() {
        return defaultImageResId;
    }

    public int getErrorImageResId() {
        return errorImageResId;
    }

    public void setAbsoluteFileName(String absoluteFileName) {
        this.absoluteFileName = absoluteFileName;
    }

    public String getAbsoluteFileName() {
        return absoluteFileName;
    }

    public void setHasImageTag(boolean hasTag) {
        this.hasImageViewTag = hasTag;
    }

    public boolean getHasImageViewTag() {
        return hasImageViewTag;
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(ImageContainer response, boolean isImmediate) {

    }
}
