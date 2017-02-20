package com.vijay.jsonwizard.customviews;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.widget.LinearLayout;

import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;
import com.vijay.jsonwizard.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TreeViewDialog extends Dialog implements TreeNode.TreeNodeClickListener {
    private static final String KEY_NODES = "nodes";
    private static final String KEY_LEVEL = "level";
    private static final String KEY_NAME = "name";

    private final Context context;
    private ArrayList<String> value;
    private TreeNode rootNode;

    public TreeViewDialog(Context context, JSONArray structure) throws
            JSONException {
        super(context);
        this.context = context;
        init(structure);
    }

    public TreeViewDialog(Context context, int theme, JSONArray structure) throws JSONException {
        super(context, theme);
        this.context = context;
        init(structure);
    }

    protected TreeViewDialog(Context context, boolean cancelable, OnCancelListener
            cancelListener, JSONArray structure, ArrayList<String> value) throws JSONException {
        super(context, cancelable, cancelListener);
        this.context = context;
        init(structure);
    }

    private void init(JSONArray nodes) throws JSONException {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.setContentView(R.layout.dialog_tree_view);
        LinearLayout canvas = (LinearLayout) this.findViewById(R.id.canvas);


        this.value = new ArrayList<>();

        JSONObject rootObject = new JSONObject();
        rootObject.put(KEY_NAME, "");
        rootObject.put(KEY_LEVEL, "");
        rootObject.put(KEY_NODES, nodes);
        rootNode = constructTreeView(rootObject, null);

        AndroidTreeView androidTreeView = new AndroidTreeView(context, rootNode);
        androidTreeView.setDefaultContainerStyle(R.style.TreeNodeStyle);

        canvas.addView(androidTreeView.getView());
    }

    private TreeNode constructTreeView(JSONObject structure, TreeNode parent) throws
            JSONException {
        TreeNode curNode = new TreeNode(structure.getString(KEY_NAME));
        curNode.setClickListener(this);
        curNode.setViewHolder(new SelectableItemHolder(context, structure.getString(KEY_LEVEL)));
        if (parent == null) {
            curNode.setSelectable(false);
        }
        if (structure.has(KEY_NODES)) {
            JSONArray options = structure.getJSONArray(KEY_NODES);
            for (int i = 0; i < options.length(); i++) {
                constructTreeView(options.getJSONObject(i), curNode);
            }
        }

        if (parent != null) {
            parent.addChild(curNode);
        }

        return curNode;
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
        }
    }

    private static void retrieveValue(TreeNode node, ArrayList<String> value) {
        if (node.getParent() != null) {
            value.add((String) node.getValue());
            retrieveValue(node.getParent(), value);
        }
    }

    public ArrayList<String> getValue() {
        return this.value;
    }

    public void setValue(final ArrayList<String> value) {
        this.value = value;
    }

    private void setSelectedValue(TreeNode treeNode) {
        if (treeNode != null) {
            if (value != null) {
                int level = treeNode.getLevel() - 1;
                if (level >= 0 && level < value.size()) {
                    String levelValue = value.get(level);
                    String nodeValue = (String) treeNode.getValue();
                    if (nodeValue != null && nodeValue.equals(levelValue)) {
                        treeNode.setSelected(true);
                        treeNode.setExpanded(true);
                        List<TreeNode> children = treeNode.getChildren();
                        for (TreeNode curChild : children) {
                            setSelectedValue(curChild);
                        }
                        return;
                    }
                } else if (level < 0) {
                    treeNode.setSelected(true);
                    treeNode.setExpanded(true);
                }
            }
            treeNode.setSelected(false);
            treeNode.setExpanded(false);
        }
    }
}
