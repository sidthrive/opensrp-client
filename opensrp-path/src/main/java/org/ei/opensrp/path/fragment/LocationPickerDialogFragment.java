package org.ei.opensrp.path.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.common.base.Strings;

import org.ei.opensrp.path.toolbar.LocationSwitcherToolbar;
import org.opensrp.api.domain.Location;
import org.opensrp.api.util.EntityUtils;
import org.opensrp.api.util.LocationTree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import atv.holder.SelectableItemHolder;
import atv.model.TreeNode;
import atv.view.AndroidTreeView;

import static org.ei.opensrp.util.StringUtil.humanize;

/**
 * Created by Jason Rogena - jrogena@ona.io on 22/02/2017.
 */

@SuppressLint("ValidFragment")
public class LocationPickerDialogFragment extends DialogFragment implements TreeNode.TreeNodeClickListener {
    private final Activity parent;
    private final String locationJSONString;
    private AndroidTreeView tView;
    private ArrayList<String> value;
    private LocationSwitcherToolbar.OnLocationChangeListener onLocationChangeListener;

    public LocationPickerDialogFragment(Activity parent, String locationJSONString) {
        this.parent = parent;
        this.locationJSONString = locationJSONString;
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

        tView = new AndroidTreeView(getActivity(), root);
        tView.setDefaultContainerStyle(org.ei.opensrp.R.style.TreeNodeStyle);
        tView.setSelectionModeEnabled(false);

        // tView.getSelected().get(1).
        dialogView.addView(tView.getView());
        return dialogView;
    }

    public void setOnLocationChangeListener(LocationSwitcherToolbar.OnLocationChangeListener onLocationChangeListener) {
        this.onLocationChangeListener = onLocationChangeListener;
    }

    public TreeNode createNode(String locationLevel, String locationName) {
        TreeNode node = new TreeNode(locationName, locationLevel).setViewHolder(
                new SelectableItemHolder(getActivity(), locationLevel + ": "));
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
            if(this.onLocationChangeListener != null) {
                onLocationChangeListener.onLocationChanged(this.value);
            }
        }
    }

    private static void retrieveValue(TreeNode node, ArrayList<String> value) {
        if (node.getParent() != null) {
            value.add((String) node.getValue());
            retrieveValue(node.getParent(), value);
        }
    }
}
