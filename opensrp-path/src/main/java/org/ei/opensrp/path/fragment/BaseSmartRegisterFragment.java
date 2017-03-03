package org.ei.opensrp.path.fragment;

import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import org.ei.opensrp.commonregistry.CommonPersonObject;
import org.ei.opensrp.commonregistry.CommonPersonObjectClient;
import org.ei.opensrp.cursoradapter.SecuredNativeSmartRegisterCursorAdapterFragment;
import org.ei.opensrp.cursoradapter.SmartRegisterQueryBuilder;
import org.ei.opensrp.path.activity.ChildImmunizationActivity;
import org.ei.opensrp.provider.SmartRegisterClientsProvider;
import org.ei.opensrp.view.activity.SecuredNativeSmartRegisterActivity;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class BaseSmartRegisterFragment extends SecuredNativeSmartRegisterCursorAdapterFragment {


    @Override
    protected SecuredNativeSmartRegisterActivity.DefaultOptionsProvider getDefaultOptionsProvider() {
        return null;
    }

    @Override
    protected SecuredNativeSmartRegisterActivity.NavBarOptionsProvider getNavBarOptionsProvider() {
        return null;
    }

    @Override
    protected SmartRegisterClientsProvider clientsProvider() {
        return null;
    }

    @Override
    protected void onInitialization() {

    }

    @Override
    protected void startRegistration() {

    }

    @Override
    protected void onCreation() {

    }

    protected TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override
        public void onTextChanged(final CharSequence cs, int start, int before, int count) {
            filter(cs.toString(), "", "");
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    public void openVaccineCard(final String filterString) {

        showProgressView();

        final Handler handler = new Handler(Looper.getMainLooper());

        new Thread(new Runnable() {
            @Override
            public void run() {

                final CommonPersonObjectClient client = filterForClient(filterString);
                if (client != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            hideProgressView();
                            ChildImmunizationActivity.launchActivity(getActivity(), client, null);
                        }
                    });
                } else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            hideProgressView();
                        }
                    });
                }

            }
        }).start();

    }

    private void filter(String filterString, String joinTableString, String mainConditionString) {
        filters = filterString;
        joinTable = joinTableString;
        mainCondition = mainConditionString;
        getSearchCancelView().setVisibility(isEmpty(filterString) ? INVISIBLE : VISIBLE);
        CountExecute();
        filterandSortExecute();
    }

    private CommonPersonObjectClient filterForClient(String filterString) {
        try {
            String query = "";

            SmartRegisterQueryBuilder sqb = new SmartRegisterQueryBuilder(mainSelect);
            query = sqb.addCondition(" WHERE " + getTablename() + ".zeir_id = " + filterString);
            query = sqb.Endquery(sqb.addlimitandOffset(query, 1, 0));

            Cursor cursor = commonRepository().RawCustomQueryForAdapter(query);
            cursor.moveToFirst();

            if (cursor.getCount() > 0) {
                CommonPersonObject personinlist = commonRepository().readAllcommonforCursorAdapter(cursor);
                final CommonPersonObjectClient client = new CommonPersonObjectClient(personinlist.getCaseId(), personinlist.getDetails(), personinlist.getDetails().get("FWHOHFNAME"));
                client.setColumnmaps(personinlist.getColumnmaps());
                return client;
            }
        } catch (Exception e) {
            Log.e(getClass().getName(), e.toString(), e);
        }
        return null;
    }
}