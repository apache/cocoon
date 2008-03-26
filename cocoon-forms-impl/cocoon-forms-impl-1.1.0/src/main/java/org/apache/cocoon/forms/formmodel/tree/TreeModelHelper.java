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

import java.util.EventListener;

import org.apache.cocoon.forms.event.WidgetEventMulticaster;

/**
 * A helper class to ease the implementation of {@link TreeModel}s
 * 
 * @version $Id$
 */
public class TreeModelHelper {
    
    private TreeModel model;
    
    private TreeModelListener listener;

    public TreeModelHelper(TreeModel model) {
        this.model = model;
    }
    
    public Object getNode(TreePath path) {
        if (path == TreePath.ROOT_PATH) {
            return model.getRoot();
        }
        
        Object parent = getNode(path.getParentPath());
        if (parent == null) {
            return null;
        }
        
        return model.getChild(parent, path.getLastKey());
    }

    public void addTreeModelListener(TreeModelListener listener) {
        this.listener = EventMulticaster.add(this.listener, listener);
    }

    public void removeTreeModelListener(TreeModelListener listener) {
        this.listener = EventMulticaster.remove(this.listener, listener);
    }

    boolean hasListeners() {
        return this.listener != null;
    }

    public void fireTreeStructureChanged(TreePath path) {
        if (hasListeners()) {
            TreeModelEvent event = new TreeModelEvent(model, path);
            this.listener.treeStructureChanged(event);
        }
    }

    private static class EventMulticaster extends WidgetEventMulticaster implements TreeModelListener {

        protected EventMulticaster(EventListener a, EventListener b) {
            super(a, b);
        }

        protected static EventListener addInternal(EventListener a, EventListener b) {
            if (a == null)  return b;
            if (b == null)  return a;
            return new EventMulticaster(a, b);
        }
                
        public static TreeModelListener add(TreeModelListener a, TreeModelListener b) {
            return (TreeModelListener)addInternal(a, b);
        }
        
        public static TreeModelListener remove(TreeModelListener l, TreeModelListener oldl) {
            return (TreeModelListener)removeInternal(l, oldl);
        }

        protected EventListener remove(EventListener oldl) {
            if (oldl == a)  return b;
            if (oldl == b)  return a;
            EventListener a2 = removeInternal(a, oldl);
            EventListener b2 = removeInternal(b, oldl);
            if (a2 == a && b2 == b) {
                return this;        // it's not here
            }
            return addInternal(a2, b2);
        }
        
        public void treeStructureChanged(TreeModelEvent event) {
            ((TreeModelListener)a).treeStructureChanged(event);
            ((TreeModelListener)b).treeStructureChanged(event);
        }
    }
}
