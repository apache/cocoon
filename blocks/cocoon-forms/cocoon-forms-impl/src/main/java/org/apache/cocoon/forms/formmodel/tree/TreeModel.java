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

/**
 * Data model for the {@link Tree} widget, inspired by Swing's <code>TreeModel</code>, with
 * the difference that child nodes are accessed through keys rather than indices.
 * 
 * @version $Id$
 */
public interface TreeModel {

    /**
     * Returns the root of the tree.  Returns <code>null</code>
     * only if the tree has no nodes.
     *
     * @return  the root of the tree
     */
    public Object getRoot();


    public Collection getChildren(Object parent);

    /**
     * Returns <code>true</code> if <code>node</code> is a leaf.
     * It is possible for this method to return <code>false</code>
     * even if <code>node</code> has no children.
     * A directory in a filesystem, for example,
     * may contain no files; the node representing
     * the directory is not a leaf, but it also has no children.
     *
     * @param   node  a node in the tree, obtained from this data source
     * @return  true if <code>node</code> is a leaf
     */
    public boolean isLeaf(Object node);

    public String getChildKey(Object parent, Object child);
    
    public Object getChild(Object parent, String key);
    
    public Object getNode(TreePath path);

    /**
     * Adds a listener for the {@link TreeModelEvent} posted after the tree changes.
     *
     * @param   l       the listener to add
     */
    void addTreeModelListener(TreeModelListener l);

    /**
     * Removes a listener previously added with {@link #addTreeModelListener(TreeModelListener)}.
     *
     * @param   l       the listener to remove
     */  
    void removeTreeModelListener(TreeModelListener l);

}
