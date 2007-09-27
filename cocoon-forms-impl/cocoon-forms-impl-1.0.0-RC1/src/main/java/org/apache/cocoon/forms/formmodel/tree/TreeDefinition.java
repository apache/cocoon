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

import org.apache.cocoon.forms.event.WidgetEventMulticaster;
import org.apache.cocoon.forms.formmodel.AbstractWidgetDefinition;
import org.apache.cocoon.forms.formmodel.Widget;

/**
 * Definition of a {@link Tree} widget.
 * 
 * @version $Id$
 */
public class TreeDefinition extends AbstractWidgetDefinition {
    
    private TreeModelDefinition modelDefinition;
    private boolean rootVisible = true;
    private TreeSelectionListener selectionListener;
    private int selectionModel = Tree.MULTIPLE_SELECTION;

    public Widget createInstance() {
        return new Tree(this);
    }
    
    public TreeModel createModel() {
        TreeModel model;
        if (this.modelDefinition == null) {
            model = DefaultTreeModel.UNSPECIFIED_MODEL;
        } else {
            model = modelDefinition.createInstance();
        }
        return model;
    }

    public void setModelDefinition(TreeModelDefinition definition) {
        checkMutable();
        this.modelDefinition = definition;
    }

    public void setRootVisible(boolean visible) {
        checkMutable();
        this.rootVisible = visible;
    }
    
    public boolean isRootVisible() {
        return this.rootVisible;
    }
    
    public void setSelectionModel(int model) {
        checkMutable();
        this.selectionModel = model;
    }
    
    public int getSelectionModel() {
        return this.selectionModel;
    }

    public void addSelectionListener(TreeSelectionListener listener) {
        checkMutable();
        this.selectionListener = WidgetEventMulticaster.add(this.selectionListener, listener);
    }
    
    public TreeSelectionListener getSelectionListener() {
        return this.selectionListener;
    }
}
