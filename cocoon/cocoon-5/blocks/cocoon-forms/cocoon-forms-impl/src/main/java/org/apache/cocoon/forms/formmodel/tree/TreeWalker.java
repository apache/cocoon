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

import java.util.Collections;
import java.util.Iterator;

import org.apache.commons.collections.ArrayStack;

/**
 * A helper to crawl a tree and quickly access important node-related information.
 * <p>
 * It's an <code>Iterator</code> on the "current level" of the tree. This level starts
 * at the root node (and therefore obviously contains only one element), and can then
 * be changed to children of the current node using {@link #enterChildren()} or popped
 * back to the parent level using {@link #leave()}.
 * <p>
 * The {@link #next()} method will return the next node in the iteration,
 * and set the current node used by many convenience methods giving information about
 * that node.
 * <p>
 * This class was primarily written for page templates containing {@link Tree}s (see
 * <code>org/apache/cocoon/forms/generation/jx-macros.xml</code>) but can of course be
 * used in other places as well.
 * 
 * @version $Id$
 */
public class TreeWalker implements Iterator {
    ArrayStack stack = new ArrayStack();
    Tree tree;
    Object node;
    TreePath path;
    Iterator iter;

    public TreeWalker(Tree tree) {
        // Root node has no siblings
        this.iter = Collections.EMPTY_LIST.iterator();
        this.tree = tree;
        this.node = tree.getModel().getRoot();
        this.path = TreePath.ROOT_PATH;
        
        stack.push(this.iter);
        stack.push(this.node);
    }
    
    /**
     * Starts iterating the children of the current node. The current iterator is pushed
     * on a stack and will be restored on {@link #leave()}.
     * <p>
     * Right after calling this method, there is no current node. Calling {@link #next()}
     * will move to the first child, if any.
     * 
     * @return the current tree walker (i.e. <code>this</code>).
     */
    public TreeWalker enterChildren() {
        Iterator newIter;
        if (isLeaf()) {
            newIter = Collections.EMPTY_LIST.iterator();
        } else {
            newIter = tree.getModel().getChildren(node).iterator();
        }
        this.stack.push(this.iter);
        this.stack.push(this.path);
        this.stack.push(this.node);
        this.iter = newIter;
        this.node = null;
        this.path = null;
        
        return this;
    }
    
    /**
     * Go back to the parent node, restoring the iterator at this node.
     */
    public void leave() {
        this.node = this.stack.pop();
        this.path = (TreePath)this.stack.pop();
        this.iter = (Iterator)this.stack.pop();
        this.path = this.path.getParentPath();
    }
    
    /**
     * Are there more nodes to iterate on at this level?
     */
    public boolean hasNext() {
        return this.iter.hasNext();
    }
    
    /**
     * Get the next node in the current iteration level.
     */
    public Object next() {
        this.node = iter.next();
        
        this.path = new TreePath(
             (TreePath)this.stack.peek(1),
             tree.getModel().getChildKey(stack.peek(), this.node));
        return this.node;
    }
    
    /**
     * Required by the <code>Iterator</code> interface, but not supported here.
     * 
     * @throws UnsupportedOperationException whenever called.
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Get the current depth of this walker (can be used e.g. to compute indentation margins
     * or CSS styles). If root node is visible (see {@link Tree#isRootVisible()}), depth 0 is
     * for the root. Otherwise, children of the root node are at depth 0.
     * 
     * @return the current depth
     */
    public int getDepth() {
        return path.getPathCount() - (this.tree.isRootVisible() ? 1 : 2);
    }
    
    /**
     * Get the current node, which is the result of the last call to {@link #next()} (except if
     * {@link #enterChildren()} or {@link #leave()} where called inbetween.
     * 
     * @return the current node.
     */
    public Object getNode() {
        return this.node;
    }
    
    /**
     * Get the path of the current node.
     * 
     * @return the path
     */
    public TreePath getPath() {
        return this.path;
    }
    
    /**
     * Is the current node a leaf?
     */
    public boolean isLeaf() {
        return this.tree.getModel().isLeaf(this.node);
    }
    
    /**
     * Is the current node expanded?
     */
    public boolean isExpanded() {
        return this.tree.isExpanded(this.path);
    }
    
    /**
     * Is the current node collapsed?
     */
    public boolean isCollapsed() {
        return this.tree.isCollapsed(this.path);
    }
    
    /**
     * Is the current node visible (i.e. its parent is expanded)?
     */
    public boolean isVisible() {
        return this.tree.isVisible(this.path);
    }
    
    /**
     * Is the current node selected?
     */
    public boolean isSelected() {
        return this.tree.isPathSelected(this.path);
    }
    
    /**
     * Get the "icon type" that should be used for this node, according to the common
     * visual paradigms used to render trees:
     * <ul>
     * <li>"<code>leaf</code>" for leaf nodes (will be e.g. a file icon),</li>
     * <li>"<code>expanded</code>" for non-leaf expanded nodes (will be e.g. a "minus" icon)</li>
     * <li>"<code>collapsed</code>" for non-leaf collapsed nodes (will be e.g. a "plus" icon)</li>
     * </ul>
     * 
     * @return the icon type
     */
    public String getIconType() {
        if (isLeaf()) {
            return "leaf";
        } else if (isExpanded()) {
            return "expanded";
        } else {
            return "collapsed";
        }
    }
    
    /**
     * Get the "selection type" that should be used for this node, that can be used e.g. as
     * a CSS class name:
     * <ul>
     * <li>"<code>selected</code>" for selected nodes,</li>
     * <li>"<code>unselected</code>" for unselected nodes.</li>
     * </ul>
     * 
     * @return the selection type
     */
    public String getSelectionType() {
        return this.tree.isPathSelected(this.path) ? "selected" : "unselected";
    }
}
