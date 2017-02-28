package org.ei.opensrp.path.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.common.base.Strings;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;
import com.vijay.jsonwizard.customviews.SelectableItemHolder;

import org.ei.opensrp.Context;
import org.ei.opensrp.path.activity.BaseActivity;
import org.ei.opensrp.path.toolbar.LocationSwitcherToolbar;
import org.ei.opensrp.view.activity.SecuredActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.opensrp.api.domain.Location;
import org.opensrp.api.util.EntityUtils;
import org.opensrp.api.util.LocationTree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import util.JsonFormUtils;

import static org.ei.opensrp.util.StringUtil.humanize;

/**
 * Created by Jason Rogena - jrogena@ona.io on 22/02/2017.
 */

@SuppressLint("ValidFragment")
public class LocationPickerDialogFragment extends DialogFragment implements TreeNode.TreeNodeClickListener {
    private static final String TAG = "LocPickerFragment";
    private Context openSrpContext;
    private final Activity parent;
    private final String locationJSONString;
    private AndroidTreeView tView;
    private ArrayList<String> value;
    private ArrayList<String> defaultValue;
    private LocationSwitcherToolbar.OnLocationChangeListener onLocationChangeListener;
    private static final ArrayList<String> allowedLevels;
    static {
        allowedLevels = new ArrayList<>();
        allowedLevels.add("Country");
        allowedLevels.add("Province");
        allowedLevels.add("District");
        allowedLevels.add("Health Facility");
        allowedLevels.add("Zone");
    }

    public LocationPickerDialogFragment(Activity parent, Context context, String locationJSONString) {
        this.parent = parent;
        openSrpContext = context;
        this.locationJSONString = locationJSONString;
        defaultValue = new ArrayList<>();
        JSONArray rawDefaultLocation = JsonFormUtils
                .generateDefaultLocationHierarchy(openSrpContext, allowedLevels);

        if (rawDefaultLocation != null) {
            try {
                for (int i = 0; i < rawDefaultLocation.length(); i++) {
                    defaultValue.add(rawDefaultLocation.getString(i));
                }
            } catch (JSONException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }

        String currentLocality = openSrpContext.allSharedPreferences().fetchCurrentLocality();
        if (currentLocality != null) {
            try {
                JSONArray locationArray = new JSONArray(currentLocality);
                ArrayList<String> result = new ArrayList<>();
                for (int i = 0; i < locationArray.length(); i++) {
                    result.add(locationArray.getString(i));
                }
                value = result;
            } catch (JSONException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }

        if (value == null || value.size() == 0) {
            if (defaultValue != null) {
                value = new ArrayList<>(defaultValue);
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup dialogView = new LinearLayout(getActivity());
        TreeNode root = TreeNode.root();

        LocationTree locationTree = EntityUtils.fromJson(locationJSONString, LocationTree.class);

        Map<String, org.opensrp.api.util.TreeNode<String, Location>> locationMap =
                locationTree.getLocationsHierarchy();

        // creating the tree
        locationTreeToTreNode(root, locationMap);
        for (TreeNode curNode : root.getChildren()) {
            setSelectedValue(curNode, 0, value);
        }

        tView = new AndroidTreeView(getActivity(), root);
        tView.setDefaultContainerStyle(org.ei.opensrp.R.style.TreeNodeStyle);
        tView.setSelectionModeEnabled(false);

        // tView.getSelected().get(1).
        dialogView.addView(tView.getView());
        return dialogView;
    }

    private void saveCurrentLocation() {
        if (parent != null && parent instanceof BaseActivity) {
            String locality = null;
            if (this.value != null) {
                locality = new JSONArray(this.value).toString();
            }
            openSrpContext.allSharedPreferences().saveCurrentLocality(locality);
        }
    }

    public void setOnLocationChangeListener(LocationSwitcherToolbar.OnLocationChangeListener onLocationChangeListener) {
        this.onLocationChangeListener = onLocationChangeListener;
    }

    public TreeNode createNode(String locationLevel, String locationName) {
        TreeNode node = new TreeNode(locationName).setViewHolder(
                new SelectableItemHolder(getActivity(), ""));
        node.setSelectable(false);
        node.setClickListener(this);
        return node;
    }

    public void locationTreeToTreNode(TreeNode node, Map<String, org.opensrp.api.util.TreeNode<String, Location>> location) {

        for (Map.Entry<String, org.opensrp.api.util.TreeNode<String, Location>> entry : location.entrySet()) {
            String locationTag = entry.getValue().getNode().getTags().iterator().next();
            TreeNode tree = createNode(
                    Strings.isNullOrEmpty(locationTag) ? "-" : humanize(locationTag),
                    humanize(entry.getValue().getLabel()));
            node.addChild(tree);
            tree.setClickListener(this);
            if (entry.getValue().getChildren() != null) {
                locationTreeToTreNode(tree, entry.getValue().getChildren());
            }
        }
    }

    @Override
    public void onClick(TreeNode node, Object value) {
        value = new ArrayList<>();
        if (node.getChildren().size() == 0) {
            ArrayList<String> reversedValue = new ArrayList<>();
            retrieveValue(node, reversedValue);

            Collections.reverse(reversedValue);
            this.value = reversedValue;
            dismiss();
            saveCurrentLocation();
            if (this.onLocationChangeListener != null) {
                onLocationChangeListener.onLocationChanged(this.value);
            }
        }
    }

    public ArrayList<String> getValue() {
        if (this.value != null) {
            return new ArrayList<>(this.value);
        }

        return null;
    }

    private static void retrieveValue(TreeNode node, ArrayList<String> value) {
        if (node.getParent() != null) {
            value.add((String) node.getValue());
            retrieveValue(node.getParent(), value);
        }
    }

    private static void setSelectedValue(TreeNode treeNode, int level, ArrayList<String> defaultValue) {
        if (treeNode != null) {
            if (defaultValue != null) {
                if (level >= 0 && level < defaultValue.size()) {
                    String levelValue = defaultValue.get(level);
                    String nodeValue = (String) treeNode.getValue();
                    if (nodeValue != null && nodeValue.equals(levelValue)) {
                        treeNode.setExpanded(true);
                        List<TreeNode> children = treeNode.getChildren();
                        for (TreeNode curChild : children) {
                            setSelectedValue(curChild, level + 1, defaultValue);
                        }
                        return;
                    }
                }
            }

            treeNode.setExpanded(false);
        }
    }
}
