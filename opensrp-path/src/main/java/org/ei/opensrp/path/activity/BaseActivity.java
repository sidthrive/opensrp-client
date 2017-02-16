package org.ei.opensrp.path.activity;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import org.ei.opensrp.path.R;

/**
 * Base activity class for all other PATH activity classes. Implements:
 * - A uniform navigation bar that is launched by swiping from the left
 * - Support of custom toolbars
 * <p>
 * This activity requires that the base view for any child activity be {@link DrawerLayout}
 * Make sure include the navigation view as the last element in the activity's root DrawerLayout
 * like this:
 * <p>
 * <include layout="@layout/nav_view_base"/>
 * <p>
 * Created by Jason Rogena - jrogena@ona.io on 16/02/2017.
 */
public abstract class BaseActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    /**
     * The layout resource file to user for this activity
     *
     * @return The resource id for the layout file to use
     */
    protected abstract int getContentView();

    /**
     * The id for the base {@link DrawerLayout} for the activity
     *
     * @return
     */
    protected abstract int getDrawerLayoutId();

    /**
     * The id for the toolbar used in this activity
     *
     * @return The id for the toolbar used
     */
    protected abstract int getToolbarId();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentView());
        Toolbar toolbar = getToolbar();
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(getDrawerLayoutId());
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(getDrawerLayoutId());
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_register) {

        } else if (id == R.id.nav_record_vaccination_out_catchment) {

        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_sync) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(getDrawerLayoutId());
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    protected Toolbar getToolbar() {
        return (Toolbar) findViewById(getToolbarId());
    }
}
