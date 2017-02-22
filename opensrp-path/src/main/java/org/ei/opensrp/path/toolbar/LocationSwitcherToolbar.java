package org.ei.opensrp.path.toolbar;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MenuItem;

import org.ei.opensrp.path.R;

/**
 * To use this toolbar in your activity, include the following line as the first child in your
 * activity's main {@link android.support.design.widget.CoordinatorLayout}
 * <p>
 * <include layout="@layout/toolbar_location_switcher" />
 * <p>
 * Created by Jason Rogena - jrogena@ona.io on 17/02/2017.
 */

public class LocationSwitcherToolbar extends BaseToolbar {
    public static final int TOOLBAR_ID = R.id.location_switching_toolbar;
    private Context context;
    private OnLocationChangeListener onLocationChangeListener;

    public LocationSwitcherToolbar(Context context) {
        super(context);
        init(context);
    }

    public LocationSwitcherToolbar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LocationSwitcherToolbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        // TODO: Set the location to the current location
    }

    public void setOnLocationChangeListener(OnLocationChangeListener onLocationChangeListener) {
        this.onLocationChangeListener = onLocationChangeListener;
    }

    @Override
    public int getSupportedMenu() {
        return R.menu.menu_location_switcher;
    }

    @Override
    public MenuItem onMenuItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.location_switcher) {
            // TODO: Hookup the location switcher dialog here
        }
        return menuItem;
    }

    public static interface OnLocationChangeListener {
        void onLocationChanged(String newLocation);
    }
}
