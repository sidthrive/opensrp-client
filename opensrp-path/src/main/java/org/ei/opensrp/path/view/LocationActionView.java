package org.ei.opensrp.path.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.ei.opensrp.path.R;

/**
 * Created by Jason Rogena - jrogena@ona.io on 23/02/2017.
 */

public class LocationActionView extends LinearLayout {
    private Context context;
    private TextView itemText;

    public LocationActionView(Context context) {
        super(context);
        init(context);
    }

    public LocationActionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LocationActionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public LocationActionView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.action_location_switcher, this, true);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        setLayoutParams(layoutParams);

        itemText = (TextView) findViewById(R.id.item_text);
        setClickable(true);
    }

    public void setItemText(String text) {
        itemText.setText(text);
    }
}
