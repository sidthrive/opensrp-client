package com.vijay.jsonwizard.widgets;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.rey.material.util.ViewUtil;
import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.customviews.GenericTextWatcher;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.interfaces.CommonListener;
import com.vijay.jsonwizard.interfaces.FormWidgetFactory;
import com.vijay.jsonwizard.interfaces.JsonApi;
import com.vijay.jsonwizard.interfaces.OnActivityResultListener;
import com.vijay.jsonwizard.utils.zxing.IntentIntegrator;
import com.vijay.jsonwizard.utils.zxing.IntentResult;
import com.vijay.jsonwizard.validators.edittext.RequiredValidator;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jason Rogena - jrogena@ona.io on 20/03/2017.
 */

public class BarcodeFactory implements FormWidgetFactory {
    private static final String SCAN_MODE = "SCAN_MODE";
    private static final String QR_MODE = "QR_MODE";
    private static final String TYPE_QR = "qrcode";
    private static final String TYPE_BARCODE = "barcode";
    private static final String DEFAULT_TYPE= TYPE_BARCODE;

    @Override
    public List<View> getViewsFromJson(String stepName, final Context context,
                                       JsonFormFragment formFragment, final JSONObject jsonObject,
                                       CommonListener listener) throws Exception {
        List<View> views = new ArrayList<>(1);
        try {
            String openMrsEntityParent = jsonObject.getString("openmrs_entity_parent");
            String openMrsEntity = jsonObject.getString("openmrs_entity");
            String openMrsEntityId = jsonObject.getString("openmrs_entity_id");
            String relevance = jsonObject.optString("relevance");
            final String constraints = jsonObject.optString("constraints");

            final MaterialEditText editText = (MaterialEditText) LayoutInflater.from(context).inflate(
                    R.layout.item_edit_text, null);
            editText.setHint(jsonObject.getString("hint"));
            editText.setFloatingLabelText(jsonObject.getString("hint"));
            editText.setId(ViewUtil.generateViewId());
            editText.setTag(R.id.key, jsonObject.getString("key"));
            editText.setTag(R.id.openmrs_entity_parent, openMrsEntityParent);
            editText.setTag(R.id.openmrs_entity, openMrsEntity);
            editText.setTag(R.id.openmrs_entity_id, openMrsEntityId);
            if (jsonObject.has("v_required")) {
                JSONObject requiredObject = jsonObject.optJSONObject("v_required");
                String requiredValue = requiredObject.getString("value");
                if (!TextUtils.isEmpty(requiredValue)) {
                    if (Boolean.TRUE.toString().equalsIgnoreCase(requiredValue)) {
                        editText.addValidator(new RequiredValidator(requiredObject.getString("err")));
                    }
                }
            }

            if (!TextUtils.isEmpty(jsonObject.optString("value"))) {
                editText.setText(jsonObject.optString("value"));
                if (jsonObject.has("read_only")) {
                    boolean readOnly = jsonObject.getBoolean("read_only");
                    editText.setEnabled(!readOnly);
                    editText.setFocusable(!readOnly);
                }
            }

            editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        launchBarcodeScanner((Activity) context, jsonObject.optString("barcode_type"));
                    }
                }
            });

            editText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    launchBarcodeScanner((Activity) context, jsonObject.optString("barcode_type"));
                }
            });

            editText.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    editText.setText("");
                    return true;
                }
            });

            if (context instanceof JsonApi) {
                JsonApi jsonApi = (JsonApi) context;
                jsonApi.addOnActivityResultListener(IntentIntegrator.REQUEST_CODE,
                        new OnActivityResultListener() {
                            @Override
                            public void onActivityResult(int requestCode,
                                                         int resultCode, Intent data) {
                                if(resultCode == Activity.RESULT_OK) {
                                    IntentResult res = IntentIntegrator.parseActivityResult(
                                            requestCode,
                                            resultCode,
                                            data);

                                    editText.setText(res.getContents());
                                }
                            }
                });
            }

            editText.addTextChangedListener(new GenericTextWatcher(stepName, formFragment, editText));
            if (relevance != null && context instanceof JsonApi) {
                editText.setTag(R.id.relevance, relevance);
                ((JsonApi) context).addSkipLogicView(editText);
            }
            if (constraints != null && context instanceof JsonApi) {
                editText.setTag(R.id.constraints, constraints);
                editText.setTag(R.id.address, stepName + ":" + jsonObject.getString("key"));
                ((JsonApi) context).addConstrainedView(editText);
            }

            views.add(editText);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return views;
    }

    private void launchBarcodeScanner(Activity activity, String barcodeType) {
        IntentIntegrator intentIntegrator = new IntentIntegrator(activity);
        if(barcodeType != null && barcodeType.equals(TYPE_QR)) {
            intentIntegrator.addExtra(SCAN_MODE, QR_MODE);
        }
        intentIntegrator.initiateScan();
    }

}
