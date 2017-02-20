package com.vijay.jsonwizard.activities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.comparers.Comparer;
import com.vijay.jsonwizard.comparers.EqualToComparer;
import com.vijay.jsonwizard.comparers.GreaterThanComparer;
import com.vijay.jsonwizard.comparers.GreaterThanEqualToComparer;
import com.vijay.jsonwizard.comparers.LessThanComparer;
import com.vijay.jsonwizard.comparers.LessThanEqualToComparer;
import com.vijay.jsonwizard.comparers.NotEqualToComparer;
import com.vijay.jsonwizard.comparers.RegexComparer;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.interfaces.JsonApi;
import com.vijay.jsonwizard.utils.FormUtils;
import com.vijay.jsonwizard.utils.PropertyManager;
import com.vijay.jsonwizard.views.JsonFormFragmentView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonFormActivity extends AppCompatActivity implements JsonApi {

    private static final String TAG = "JsonFormActivity";

    private Toolbar             mToolbar;

    private JSONObject          mJSONObject;
    private PropertyManager propertyManager;
    private ArrayList<View> watchedViews;
    private String functionRegex;
    private HashMap<String, Comparer> comparers;

    public void init(String json) {
        try {
            mJSONObject = new JSONObject(json);
            if(!mJSONObject.has("encounter_type")) {
                mJSONObject = new JSONObject();
                throw new JSONException("Form encounter_type not set");
            }
        } catch (JSONException e) {
            Log.d(TAG, "Initialization error. Json passed is invalid : " + e.getMessage());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_json_form);
        mToolbar = (Toolbar) findViewById(R.id.tb_top);
        setSupportActionBar(mToolbar);
        watchedViews = new ArrayList<>();
        if (savedInstanceState == null) {
            init(getIntent().getStringExtra("json"));
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, JsonFormFragment.getFormFragment(JsonFormConstants.FIRST_STEP_NAME)).commit();
            onFormStart();
        } else {
            init(savedInstanceState.getString("jsonState"));
        }
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }

    @Override
    public synchronized JSONObject getStep(String name) {
        synchronized (mJSONObject) {
            try {
                return mJSONObject.getJSONObject(name);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void writeValue(String stepName, String key, String value, String openMrsEntityParent,
                           String openMrsEntity, String openMrsEntityId) throws JSONException {
        synchronized (mJSONObject) {
            JSONObject jsonObject = mJSONObject.getJSONObject(stepName);
            JSONArray fields = jsonObject.getJSONArray("fields");
            for (int i = 0; i < fields.length(); i++) {
                JSONObject item = fields.getJSONObject(i);
                String keyAtIndex = item.getString("key");
                if (key.equals(keyAtIndex)) {
                    item.put("value", value);
                    item.put("openmrs_entity_parent", openMrsEntityParent);
                    item.put("openmrs_entity", openMrsEntity);
                    item.put("openmrs_entity_id", openMrsEntityId);
                    refreshSkipLogic();
                    return;
                }
            }
        }
    }

    @Override
    public void writeValue(String stepName, String parentKey, String childObjectKey, String childKey,
                           String value, String openMrsEntityParent, String openMrsEntity,
                           String openMrsEntityId)
            throws JSONException {
        synchronized (mJSONObject) {
            JSONObject jsonObject = mJSONObject.getJSONObject(stepName);
            JSONArray fields = jsonObject.getJSONArray("fields");
            for (int i = 0; i < fields.length(); i++) {
                JSONObject item = fields.getJSONObject(i);
                String keyAtIndex = item.getString("key");
                if (parentKey.equals(keyAtIndex)) {
                    JSONArray jsonArray = item.getJSONArray(childObjectKey);
                    for (int j = 0; j < jsonArray.length(); j++) {
                        JSONObject innerItem = jsonArray.getJSONObject(j);
                        String anotherKeyAtIndex = innerItem.getString("key");
                        if (childKey.equals(anotherKeyAtIndex)) {
                            innerItem.put("value", value);
                            refreshSkipLogic();
                            return;
                        }
                    }
                }
            }
        }
    }

    @Override
    public String currentJsonState() {
        synchronized (mJSONObject) {
            return mJSONObject.toString();
        }
    }

    @Override
    public String getCount() {
        synchronized (mJSONObject) {
            return mJSONObject.optString("count");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("jsonState", mJSONObject.toString());
    }

    @Override
    public void onFormStart() {
        try {
            if (propertyManager == null) {
                propertyManager = new PropertyManager(this);
            }
            FormUtils.updateStartProperties(propertyManager, mJSONObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFormFinish() {
        try {
            if (propertyManager == null) {
                propertyManager = new PropertyManager(this);
            }
            FormUtils.updateEndProperties(propertyManager, mJSONObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void clearWatchedViews() {
        watchedViews = new ArrayList<>();
    }

    @Override
    public void addWatchedView(View view) {
        watchedViews.add(view);
    }

    @Override
    public void refreshSkipLogic() {
        initComparers();
        for (View curView : watchedViews) {
            String relevanceTag = (String) curView.getTag(R.id.relevance);
            if (relevanceTag != null && relevanceTag.length() > 0) {
                try {
                    JSONObject relevance = new JSONObject(relevanceTag);
                    Iterator<String> keys = relevance.keys();
                    boolean ok = true;
                    while (keys.hasNext()) {
                        String curKey = keys.next();
                        String[] address = curKey.split(":");
                        if (address.length == 2) {
                            JSONObject curRelevance = relevance.getJSONObject(curKey);
                            String curValue = getValueFromAddress(address);
                            try {
                                boolean comparison = doComparison(curValue, curRelevance);
                                ok = ok && comparison;
                                if (!ok) break;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    if (ok) {
                        curView.setEnabled(true);
                        curView.setVisibility(View.VISIBLE);
                    } else {
                        curView.setEnabled(false);
                        curView.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String getValueFromAddress(String[] address) throws Exception {
        String result = null;
        if(address != null && address.length == 2) {
            JSONArray fields = mJSONObject.getJSONObject(address[0]).getJSONArray("fields");
            for(int i = 0; i < fields.length(); i++) {
                if(fields.getJSONObject(i).getString("key").equals(address[1])) {
                    result = fields.getJSONObject(i).optString("value");
                }
            }
        }

        return result;
    }

    private void initComparers() {
        if (functionRegex == null || comparers == null) {
            functionRegex = "";
            comparers = new HashMap<>();

            LessThanComparer lessThanComparison = new LessThanComparer();
            functionRegex += lessThanComparison.getFunctionName();
            comparers.put(lessThanComparison.getFunctionName(), lessThanComparison);

            LessThanEqualToComparer lessThanEqualToComparison = new LessThanEqualToComparer();
            functionRegex += "|" + lessThanEqualToComparison.getFunctionName();
            comparers.put(lessThanEqualToComparison.getFunctionName(), lessThanEqualToComparison);

            EqualToComparer equalToComparison = new EqualToComparer();
            functionRegex += "|" + equalToComparison.getFunctionName();
            comparers.put(equalToComparison.getFunctionName(), equalToComparison);

            NotEqualToComparer notEqualToComparer = new NotEqualToComparer();
            functionRegex += "|" + notEqualToComparer.getFunctionName();
            comparers.put(notEqualToComparer.getFunctionName(), notEqualToComparer);

            GreaterThanComparer greaterThanComparison = new GreaterThanComparer();
            functionRegex += "|" + greaterThanComparison.getFunctionName();
            comparers.put(greaterThanComparison.getFunctionName(), greaterThanComparison);

            GreaterThanEqualToComparer greaterThanEqualToComparison = new GreaterThanEqualToComparer();
            functionRegex += "|" + greaterThanEqualToComparison.getFunctionName();
            comparers.put(greaterThanEqualToComparison.getFunctionName(), greaterThanEqualToComparison);

            RegexComparer regexComparison = new RegexComparer();
            functionRegex += "|" + regexComparison.getFunctionName();
            comparers.put(regexComparison.getFunctionName(), regexComparison);
        }
    }

    private boolean doComparison(String value, JSONObject comparison) throws Exception {
        String type = comparison.getString("type").toLowerCase();
        String ex = comparison.getString("ex");

        Pattern pattern = Pattern.compile("(" + functionRegex + ")\\((.*)\\)");
        Matcher matcher = pattern.matcher(ex);
        if (matcher.find()) {
            String functionName = matcher.group(1);
            String b = matcher.group(2);
            return comparers.get(functionName).compare(value, type, b);
        }

        return false;
    }
}
