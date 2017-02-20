package com.vijay.jsonwizard.interfaces;

import android.view.View;

import com.vijay.jsonwizard.interactors.JsonFormInteractor;
import com.vijay.jsonwizard.views.JsonFormFragmentView;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by vijay on 5/16/15.
 */
public interface JsonApi {
    JSONObject getStep(String stepName);

    void writeValue(String stepName, String key, String value, String openMrsEntityParent,
                    String openMrsEntity, String openMrsEntityId) throws JSONException;

    void writeValue(String stepName, String prentKey, String childObjectKey, String childKey,
                    String value, String openMrsEntityParent, String openMrsEntity,
                    String openMrsEntityId)
            throws JSONException;

    String currentJsonState();

    String getCount();

    void onFormStart();

    void onFormFinish();

    void clearWatchedViews();

    void addWatchedView(View view);

    void refreshSkipLogic();
}
