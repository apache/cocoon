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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A default tree model, implemented with {@link org.apache.cocoon.forms.formmodel.tree.DefaultTreeModel.TreeNode}s.
 * 
 * @version $Id$
 */
public class DefaultTreeModel implements TreeModel {
    
    /**
     * Default model that is used by a Tree when no model has been specified.
     */
    public static final TreeModel UNSPECIFIED_MODEL = buildUnspecifiedModel();
    
    private TreeModelHelper helper = new TreeModelHelper(this);
    
    TreeNode root;

    public interface TreeNode {
        Collection getChildren();

        boolean isLeaf();

        String getChildKey(Object child);

        Object getChild(String key);
    }
    
    public static class DefaultTreeNode implements TreeNode {
        
        private Object data;
        private Map children;

        public DefaultTreeNode(Object data) {
            this.data = data;
        }
        
        public DefaultTreeNode(Object data, Map children) {
            this.data = data;
            this.children = children;
        }
        
        public Object getData() {
            return this.data;
        }
        
        public void add(String key, TreeNode node) {
            if (this.children == null) {
                this.children = new HashMap();
            }
            children.put(key, node);
        }

        public Collection getChildren() {
            if (this.children == null) {
                return null;
            }
            
            return this.children.values();
        }

        public boolean isLeaf() {
            return this.children == null;
        }

        public String getChildKey(Object child) {
            Iterator iter = this.children.entrySet().iterator();
            while(iter.hasNext()) {
                Map.Entry entry = (Map.Entry)iter.next();
                if (child.equals(entry.getValue())) {
                    return (String)entry.getKey();
                }
            }
            return null;
        }

        public Object getChild(String key) {
            return this.children.get(key);
        }
    }

    public DefaultTreeModel(TreeNode root) {
        this.root = root;
    }

    public Object getRoot() {
        return this.root;
    }

    public Collection getChildren(Object parent) {
        return ((TreeNode)parent).getChildren();
    }

    public boolean isLeaf(Object node) {
        return ((TreeNode)node).isLeaf();
    }

    public String getChildKey(Object parent, Object child) {
        return ((TreeNode)parent).getChildKey(child);
    }

    public Object getChild(Object parent, String key) {
        return ((TreeNode)parent).getChild(key);
    }

    public Object getNode(TreePath path) {
        return helper.getNode(path);
    }

    public void addTreeModelListener(TreeModelListener l) {
        helper.addTreeModelListener(l);
    }

    public void removeTreeModelListener(TreeModelListener l) {
        helper.removeTreeModelListener(l);
    }

    private static TreeModel buildUnspecifiedModel() {
        DefaultTreeNode root = new DefaultTreeNode("This tree has no model.");
        DefaultTreeNode parent = new DefaultTreeNode("Tree model should be defined...");
        root.add("explanation", parent);
        parent.add("1", new DefaultTreeNode("in the form definition using <fd:tree-model>"));
        parent.add("2", new DefaultTreeNode("by the application using flowscript, event listeners, etc."));
        return new DefaultTreeModel(root);
    }

    /**
     * The classical Swing sample tree model, that can be used for demonstration purposes.
     */
    public static class Sample extends DefaultTreeModel {
        public Sample() {
            super(new DefaultTreeNode("root"));
            DefaultTreeNode root = (DefaultTreeNode)getRoot();

            DefaultTreeNode      parent;
            
            parent = new DefaultTreeNode("Colors");
            root.add("colors", parent);
            parent.add("blue", new DefaultTreeNode("Blue"));
            parent.add("violet", new DefaultTreeNode("Violet"));
            parent.add("red", new DefaultTreeNode("Red"));
            parent.add("yellow", new DefaultTreeNode("Yellow"));
    
            parent = new DefaultTreeNode("Sports");
            root.add("sports", parent);
            parent.add("basketball", new DefaultTreeNode("Basketball"));
            parent.add("soccer", new DefaultTreeNode("Soccer"));
            parent.add("football", new DefaultTreeNode("Football"));
            parent.add("hockey", new DefaultTreeNode("Hockey"));
    
            parent = new DefaultTreeNode("Food");
            root.add("food", parent);
            parent.add("hotdogs", new DefaultTreeNode("Hot Dogs"));
            parent.add("pizza", new DefaultTreeNode("Pizza"));
            parent.add("ravioli", new DefaultTreeNode("Ravioli"));
            parent.add("bananas", new DefaultTreeNode("Bananas"));
        }
    }
}
