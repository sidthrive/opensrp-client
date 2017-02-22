package org.ei.opensrp.path.toolbar;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MenuItem;

import org.ei.opensrp.path.R;
import org.ei.opensrp.path.activity.BaseActivity;
import org.ei.opensrp.path.fragment.LocationPickerDialogFragment;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

/**
 * To use this toolbar in your activity, include the following line as the first child in your
 * activity's main {@link android.support.design.widget.CoordinatorLayout}
 * <p>
 * <include layout="@layout/toolbar_location_switcher" />
 * <p>
 * Created by Jason Rogena - jrogena@ona.io on 17/02/2017.
 */

public class LocationSwitcherToolbar extends BaseToolbar {
    private static final String TAG = "LocationSwitcherToolbar";
    public static final int TOOLBAR_ID = R.id.location_switching_toolbar;
    private BaseActivity baseActivity;
    private OnLocationChangeListener onLocationChangeListener;
    private LocationPickerDialogFragment locationPickerDialogFragment;
    private static final String LOCATION_DIALOG_TAG = "locationDialogTAG";

    public LocationSwitcherToolbar(Context context) {
        super(context);
    }

    public LocationSwitcherToolbar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public LocationSwitcherToolbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void initLocationPicker(BaseActivity baseActivity) {
        this.baseActivity = baseActivity;
        if (baseActivity != null) {
            locationPickerDialogFragment = new LocationPickerDialogFragment(baseActivity,
                    baseActivity.getOpenSRPContext().anmLocationController().get());
            locationPickerDialogFragment.setOnLocationChangeListener(new OnLocationChangeListener() {
                @Override
                public void onLocationChanged(ArrayList<String> newLocation) {
                    updateCurrentLocation(newLocation);
                    if (onLocationChangeListener != null) {
                        onLocationChangeListener.onLocationChanged(newLocation);
                    }
                }
            });
        }
    }

    public ArrayList<String> getCurrentLocation() {
        if (baseActivity != null) {
            String currentLocality = baseActivity.getOpenSRPContext().allSharedPreferences()
                    .fetchCurrentLocality();
            if (currentLocality != null) {
                try {
                    JSONArray locationArray = new JSONArray(currentLocality);
                    ArrayList<String> result = new ArrayList<>();
                    for (int i = 0; i < locationArray.length(); i++) {
                        result.add(locationArray.getString(i));
                    }

                    return result;
                } catch (JSONException e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                }
            }
        }
        return null;
    }

    private void updateMenu(ArrayList<String> selectedLocation) {
        if (baseActivity != null) {
            String name = baseActivity.getString(R.string.select_location);
            if (selectedLocation != null && selectedLocation.size() > 0) {
                name = selectedLocation.get(selectedLocation.size() - 1);
            }

            baseActivity.getMenu().findItem(R.id.location_switcher).setTitle(name);
        }
    }

    private void updateCurrentLocation(ArrayList<String> newLocation) {
        if (baseActivity != null) {
            String locality = null;
            if (newLocation != null) {
                locality = new JSONArray(newLocation).toString();
            }
            baseActivity.getOpenSRPContext().allSharedPreferences().saveCurrentLocality(locality);
            updateMenu(newLocation);
        }
    }

    public void setOnLocationChangeListener(OnLocationChangeListener onLocationChangeListener) {
        this.onLocationChangeListener = onLocationChangeListener;
    }

    @Override
    public int getSupportedMenu() {
        return R.menu.menu_location_switcher;
    }

    @Override
    public void prepareMenu() {
        updateMenu(getCurrentLocation());
    }

    @Override
    public MenuItem onMenuItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.location_switcher) {
            if (locationPickerDialogFragment != null && baseActivity != null) {
                FragmentTransaction ft = baseActivity.getFragmentManager().beginTransaction();
                Fragment prev = baseActivity.getFragmentManager()
                        .findFragmentByTag(LOCATION_DIALOG_TAG);

                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);

                locationPickerDialogFragment.show(ft, LOCATION_DIALOG_TAG);
            }
        }
        return menuItem;
    }

    public static interface OnLocationChangeListener {
        void onLocationChanged(final ArrayList<String> newLocation);
    }
}
