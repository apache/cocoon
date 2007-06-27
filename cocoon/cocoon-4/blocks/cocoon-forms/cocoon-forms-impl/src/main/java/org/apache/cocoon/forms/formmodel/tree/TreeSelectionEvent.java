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

import org.apache.cocoon.forms.event.WidgetEvent;

/**
 * An event fired when the selection of a {@link Tree} changes.
 * 
 * @version $Id$
 */
public class TreeSelectionEvent extends WidgetEvent {
    
    TreePath[] paths;
    boolean [] isNew;

    public TreeSelectionEvent(Tree source, TreePath path, boolean isNew) {
        super(source);
        this.paths = new TreePath[] { path };
        this.isNew = new boolean[] { isNew };
    }
    
    public TreeSelectionEvent(Tree source, TreePath paths[], boolean areNew[]) {
        super(source);
        this.paths = paths;
        this.isNew = areNew;
    }
    
    public TreeSelectionEvent(Tree source, TreePath paths[], boolean allNew) {
        super(source);
        this.paths = paths;
        
        // Fill isNew with allNew
        this.isNew = new boolean[paths.length];
        for (int i = 0; i < isNew.length; i++) {
            this.isNew[i] = allNew;
        }
    }
    
    public Tree getTree() {
        return (Tree)super.getSource();
    }
    
    /**
     * Get the first path element.
     */
    public TreePath getPath() {
        return this.paths[0];
    }
    
    /**
     * Is the first path a new addition to the selection?
     */
    public boolean isAddedPath() {
        return this.isNew[0];
    }

    /**
     * Get paths that have been added or removed from the selection.
     */
    public TreePath[] getPaths() {
        return this.paths;
    }

    /**
     * Was the <code>index</code>th path added to the selection?
     */
    public boolean isAddedPath(int index) {
        return this.isNew[index];
    }
}
