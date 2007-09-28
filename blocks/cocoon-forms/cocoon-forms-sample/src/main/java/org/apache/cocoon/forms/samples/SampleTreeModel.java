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
package org.apache.cocoon.forms.samples;

import org.apache.cocoon.forms.formmodel.tree.DefaultTreeModel;

/**
 * The classical Swing sample tree model, that can be used for demonstration purposes.
 */
public class SampleTreeModel extends DefaultTreeModel {
    public SampleTreeModel() {
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
