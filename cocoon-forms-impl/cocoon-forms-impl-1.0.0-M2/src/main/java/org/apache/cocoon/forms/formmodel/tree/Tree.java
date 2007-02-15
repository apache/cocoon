/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.forms.formmodel.tree;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import org.apache.cocoon.environment.Request;
import org.apache.cocoon.forms.FormContext;
import org.apache.cocoon.forms.event.WidgetEvent;
import org.apache.cocoon.forms.event.WidgetEventMulticaster;
import org.apache.cocoon.forms.formmodel.AbstractWidget;
import org.apache.cocoon.forms.formmodel.Widget;
import org.apache.cocoon.forms.formmodel.WidgetDefinition;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * A tree widget, heavily inspired by Swing's <code>JTree</code>.
 *
 * @version $Id$
 */
public class Tree extends AbstractWidget {

    public static final int SINGLE_SELECTION = 0;
    public static final int MULTIPLE_SELECTION = 1;

    public interface ActionHandler {
        public void act(Tree tree, FormContext context);
    }

    private TreeDefinition treeDef;

    private TreeModel treeModel;

    private Set expandedPaths = new HashSet();

    private Set selectedPaths = new HashSet();

    private Set changedPaths = new HashSet();

    private HashMap pathWidgets = new HashMap();

    private boolean rootVisible = true;

    private boolean expandSelectedPath = false;

    private TreeSelectionListener selectionListener;

    private int selectionModel = MULTIPLE_SELECTION;

    private TreeModelListener modelListener = new TreeModelListener() {
        public void treeStructureChanged(TreeModelEvent event) {
            markForRefresh(event.getPath());
        }
    };

    protected Tree(TreeDefinition definition) {
        super(definition);
        this.treeDef = definition;
        this.rootVisible = definition.isRootVisible();
        if (!this.rootVisible) {
            // Expand it so that first-level children are visible
            this.expandedPaths.add(TreePath.ROOT_PATH);
        }
        this.treeModel = definition.createModel();
        this.treeModel.addTreeModelListener(modelListener);
        this.selectionListener = definition.getSelectionListener();
        this.selectionModel = definition.getSelectionModel();
    }

    public WidgetDefinition getDefinition() {
        return this.treeDef;
    }

    protected String getXMLElementName() {
        return "tree";
    }

    protected void generateItemSaxFragment(ContentHandler contentHandler, Locale locale) throws SAXException {
        throw new UnsupportedOperationException(this + " cannot be rendered using <ft:widget>. Please use <ft:tree>.");
    }

    public void readFromRequest(FormContext formContext) {
        //TODO: crawl open nodes, calling their widget's readFromRequest

        Request req = formContext.getRequest();
        String paramName = getRequestParameterName();

        //---------------------------------------------------------------------
        // Handle node selection using checkboxes named <id>$select
        //---------------------------------------------------------------------
        String[] selectValues = req.getParameterValues(paramName + ":select");
        if (selectValues != null) {
            // Create the set of paths given by the request
            Set newSelection = new HashSet();
            for (int i = 0; i < selectValues.length; i++) {
                newSelection.add(TreePath.valueOf(selectValues[i]));
            }

            // Check if all visible selections are in the new selection
            TreePath[] currentSelection = (TreePath[])this.selectedPaths.toArray(new TreePath[this.selectedPaths.size()]);
            for (int i = 0; i < currentSelection.length; i++) {
                TreePath p = currentSelection[i];
                if (!newSelection.contains(p) && isVisible(p)) {
                    removeSelectionPath(p);
                }
            }

            // Now add the currently selected items (may be selected already)
            Iterator iter = newSelection.iterator();
            while(iter.hasNext()) {
                addSelectionPath((TreePath)iter.next());
            }
        }

        //---------------------------------------------------------------------
        // Handle tree actions:
        // - action is in <name>$action
        // - path is in <name>$path
        //---------------------------------------------------------------------
        String action = req.getParameter(paramName + ":action");

        if (action == null || action.length() == 0) {
            // Nothing more to do here
            return;
        }

        getForm().setSubmitWidget(this);
        String pathValue = req.getParameter(paramName + ":path");

        if (pathValue == null || pathValue.length() == 0) {
            //this.treeDef.getLogger().warn("No tree path given");
            return;
        }

        // Parse the path
        TreePath path = TreePath.valueOf(pathValue);

        if ("expand".equals(action)) {
            this.expandPath(path);
        } else if ("collapse".equals(action)) {
            this.collapsePath(path);
        } else if ("toggle-collapse".equals(action)) {
            if (this.isExpanded(path)) {
                this.collapsePath(path);
            } else {
                this.expandPath(path);
            }
        } else if ("select".equals(action)) {
            this.addSelectionPath(path);
        } else if ("unselect".equals(action)) {
            this.removeSelectionPath(path);
        } else if ("toggle-select".equals(action)) {
            if (this.isPathSelected(path)) {
                this.removeSelectionPath(path);
            } else {
                this.addSelectionPath(path);
            }
        } else {
            // Unknown action
            //this.treeDef.getLogger().warn("Unknown action " + action);
        }
    }

    public TreeModel getModel() {
        return this.treeModel;
    }

    public void setModel(TreeModel model) {
        if (model == null) {
            model = DefaultTreeModel.UNSPECIFIED_MODEL;
        }
        this.treeModel.removeTreeModelListener(this.modelListener);
        this.treeModel = model;
        model.addTreeModelListener(this.modelListener);
    }

    private void markForRefresh(TreePath path) {
        this.changedPaths.add(path);
        getForm().addWidgetUpdate(this);
    }

    //---------------------------------------------------------------------------------------------
    // Selection
    //---------------------------------------------------------------------------------------------

    public void setSelectionModel(int model) {
        if (model < 0 || model > MULTIPLE_SELECTION) {
            throw new IllegalArgumentException("Illegal selection model " + model);
        }

        if (model == this.selectionModel) {
            return;
        }

        this.selectionModel = model;
        if (model == SINGLE_SELECTION && getSelectionCount() > 1) {
            clearSelection();
        }
    }

    public int getSelectionCount() {
        return this.selectedPaths.size();
    }

    public TreePath getSelectionPath() {
        if (this.selectedPaths.isEmpty()) {
            return null;
        } else {
            return (TreePath)this.selectedPaths.iterator().next();
        }
    }

    public TreePath[] getSelectionPaths() {
        return (TreePath[])this.selectedPaths.toArray(new TreePath[this.selectedPaths.size()]);
    }

    public boolean isPathSelected(TreePath path) {
        return this.selectedPaths.contains(path);
    }

    public boolean isSelectionEmpty() {
        return this.selectedPaths.isEmpty();
    }

    public void setSelectionPath(TreePath path) {
        clearSelection();
        addSelectionPath(path);
    }

    public void setSelectionPaths(TreePath paths[]) {
        clearSelection();
        addSelectionPaths(paths);
    }

    public void addSelectionPath(TreePath path) {
        if (this.selectionModel == SINGLE_SELECTION) {
            clearSelection();
        }

        if (this.selectedPaths.add(path)) {
            markForRefresh(path);
            if (this.expandSelectedPath) {
                expandPath(path);
            }
            this.getForm().addWidgetEvent(new TreeSelectionEvent(this, path, true));
        }
    }

    public void addSelectionPaths(TreePath paths[]) {
        if (this.selectionModel == SINGLE_SELECTION) {
            setSelectionPath(paths[0]);
        } else {
            for (int i = 0; i < paths.length; i++) {
                addSelectionPath(paths[i]);
                // FIXME: use array-based constructors of TreeSelectionEvent
            }
        }
    }

    public void removeSelectionPath(TreePath path) {
        if (this.selectedPaths.remove(path)) {
            // Need to redisplay the parent
            markForRefresh(path.getParentPath());
            this.getForm().addWidgetEvent(new TreeSelectionEvent(this, path, false));
        }
    }

    public void removeSelectionPaths(TreePath paths[]) {
        for (int i = 0; i < paths.length; i++) {
            removeSelectionPath(paths[i]);
            // FIXME: use array-based constructors of TreeSelectionEvent
        }
    }

    public void clearSelection() {
        if (this.isSelectionEmpty()) {
            return;
        }

        TreePath[] paths = (TreePath[])this.selectedPaths.toArray(new TreePath[this.selectedPaths.size()]);
        for (int i = 0; i < paths.length; i++) {
            // Need to redisplay the parent
            markForRefresh(paths[i].getParentPath());
        }
        this.selectedPaths.clear();
        this.getForm().addWidgetEvent(new TreeSelectionEvent(this, paths, false));
    }

    public void addTreeSelectionListener(TreeSelectionListener listener) {
        this.selectionListener = WidgetEventMulticaster.add(this.selectionListener, listener);
    }

    public void removeTreeSelectionListener(TreeSelectionListener listener) {
        this.selectionListener = WidgetEventMulticaster.remove(this.selectionListener, listener);
    }

    //---------------------------------------------------------------------------------------------
    // Visibility, expand & collapse
    //---------------------------------------------------------------------------------------------

    public boolean isCollapsed(TreePath path) {
        return !isExpanded(path);
    }

    public boolean isExpanded(TreePath path) {
        if (this.expandedPaths.contains(path)) {
            // Ensure all parents are expanded
            TreePath parent = path.getParentPath();
            return parent == null ? true : isExpanded(parent);
        } else {
            return false;
        }
    }

    /**
     * Returns true if the value identified by path is currently viewable,
     * which means it is either the root or all of its parents are expanded.
     * Otherwise, this method returns false.
     *
     * @return true if the node is viewable, otherwise false
     */
    public boolean isVisible(TreePath path) {
        if (path == TreePath.ROOT_PATH) {
            return true;
        }
        if (path != null) {
            TreePath parent = path.getParentPath();
            if (parent != null) {
                return isExpanded(parent);
            } else {
                // root node
                return true;
            }
        } else {
            return false;
        }
    }

    public void makeVisible(TreePath path) {
        if (path != null) {
            TreePath parent = path.getParentPath();
            if (parent != null) {
                expandPath(parent);
                // Make visible also all parent paths
                makeVisible(parent);
            }
        }
    }

    public boolean isRootVisible() {
        return this.rootVisible;
    }

    public void setRootVisible(boolean visible) {
        if (this.rootVisible != visible) {
            this.markForRefresh(TreePath.ROOT_PATH);
            this.rootVisible = visible;
            if (!visible) {
                // Expand it so that first-level children are visible
                this.expandPath(TreePath.ROOT_PATH);
            }
        }
    }

    public void collapsePath(TreePath path) {
        if (path != null) {
            if (this.expandedPaths.remove(path)) {
                markForRefresh(path);
            }
        }
    }

    public void expandPath(TreePath path) {
        if (path != null) {
            if (this.expandedPaths.add(path)) {
                markForRefresh(path);
            }
        }
    }
    
    public void collapseAll() {
    	this.expandedPaths.clear();
        if (!this.rootVisible) {
            this.expandedPaths.add(TreePath.ROOT_PATH);
        }    	
    }
    
    public void expandAll() {
    	collapseAll();
        this.expandedPaths.add(TreePath.ROOT_PATH);    	
    	TreeWalker tw = new TreeWalker(this);
    	tw.enterChildren();
    	while (tw.hasNext()) {
    		tw.next();
    		if (!tw.isLeaf()) {
    			expandPath(tw.getPath());
    			tw.enterChildren();
    		}
    		if (!tw.hasNext()) {
    			tw.leave();
    		}
    	}
    }
    

    public void setExpandsSelectedPath(boolean value) {
        this.expandSelectedPath  = value;
    }

    //---------------------------------------------------------------------------------------------
    // Widget management
    //---------------------------------------------------------------------------------------------

    public Widget getWidgetForPath(TreePath path) {
        Widget result = (Widget)this.pathWidgets.get(path);
        if (result == null && !this.pathWidgets.containsKey(path)) {
            result = createWidgetForPath(path);
            if (result != null) {
                result.setAttribute("TreePath", path);
            }
            this.pathWidgets.put(path, result);
        }

        return result;
    }

    private Widget createWidgetForPath(TreePath path) {
        //TODO
        return null;
    }

    public void broadcastEvent(WidgetEvent event) {
       if (event instanceof TreeSelectionEvent) {
           if (this.selectionListener != null) {
               this.selectionListener.selectionChanged((TreeSelectionEvent)event);
           }
       } else {
           super.broadcastEvent(event);
       }
   }
    //---------------------------------------------------------------------------------------------
    // TreeNode widget, which is the actual parent of widgets contained in a node
    //---------------------------------------------------------------------------------------------
    // TODO
}
