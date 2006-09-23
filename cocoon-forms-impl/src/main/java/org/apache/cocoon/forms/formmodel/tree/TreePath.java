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

import java.util.StringTokenizer;

/**
 * A path in a {@link TreeModel}.
 * 
 * @version $Id$
 */
public class TreePath {
    
    public static final TreePath ROOT_PATH = new TreePath();
    /**
     * Path representing the parent, null if lastPathComponent represents the
     * root.
     */
    private TreePath parentPath;

    /** Last path component. */
    private String key;
    
    /** Cached result of toString() */
    private String cachedToString;
    
    /**
     * Builds a path representing the root node or a tree model.
     * Private to only be used by the ROOT_PATH constant.
     */
    private TreePath() {
        this.key = "";
        this.cachedToString = "/";
    }

    /**
     * Constructs a TreePath containing only a single element. This is usually
     * used to construct a TreePath for the the root of the TreeModel.
     */
    public TreePath(String key) {
        if (key == null || key.length() == 0) {
            throw new IllegalArgumentException("key must be non empty.");
        }
        
        if (key.indexOf('/') != -1) {
            throw new IllegalArgumentException("key cannot contain a '/'");
        }
        this.key = key;
        parentPath = ROOT_PATH;
    }

    /**
     * Constructs a new TreePath, which is the path identified by
     * <code>parent</code> ending in <code>lastElement</code>.
     */
    public TreePath(TreePath parent, String key) {
        this(key);
        if (parent == null) {
            throw new IllegalArgumentException("Parent path must be non null.");
        }
        this.parentPath = parent;
    }

    /**
     * Returns an ordered array of Objects containing the components of this
     * TreePath. The first element (index 0) is the root.
     * 
     * @return an array of Objects representing the TreePath
     * @see #TreePath()
     */
    public Object[] getObjectPath(TreeModel model) {
        int i = getPathCount();
        Object[] result = new Object[i--];

        for (TreePath path = this; path != null; path = path.parentPath) {
            result[i--] = path.getLastPathObject(model);
        }
        return result;
    }

    /**
     * Returns the last component of this path. For a path returned by
     * DefaultTreeModel this will return an instance of TreeNode.
     * 
     * @return the Object at the end of the path
     * @see #TreePath()
     */
    public Object getLastPathObject(TreeModel model) {
        Object parent;
        if (this.parentPath == ROOT_PATH) {
            parent = model.getRoot();
        } else {
            parent = this.parentPath.getLastPathObject(model);
        }
        return model.getChild(parent, this.key);
    }

    /**
     * Returns the number of elements in the path.
     * 
     * @return an int giving a count of items the path
     */
    public int getPathCount() {
        int result = 0;
        for (TreePath path = this; path != null; path = path.parentPath) {
            result++;
        }
        return result;
    }

//    /**
//     * Returns the path component at the specified index.
//     * 
//     * @param element
//     *            an int specifying an element in the path, where 0 is the first
//     *            element in the path
//     * @return the Object at that index location
//     * @throws IllegalArgumentException
//     *             if the index is beyond the length of the path
//     * @see #TreePath(Object[])
//     */
//    public Object getPathComponent(int element) {
//        int pathLength = getPathCount();
//
//        if (element < 0 || element >= pathLength)
//            throw new IllegalArgumentException("Index " + element + " is out of the specified range");
//
//        TreePath path = this;
//
//        for (int i = pathLength - 1; i != element; i--) {
//            path = path.parentPath;
//        }
//        return path.lastPathComponent;
//    }
//
    /**
     * Tests if two paths are equal. Two paths are considered equal if they are
     * of same length and contain the same keys.
     * 
     * @param obj the object ot compare
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof TreePath)) {
            return false;
        }
        
        TreePath otherPath = (TreePath)obj;

        if (getPathCount() != otherPath.getPathCount()) {
            return false;
        }
        
        TreePath path = this;
        do {
            if (otherPath == null || !path.key.equals(otherPath.key)) {
                return false;
            }
            path = path.parentPath;
            otherPath = otherPath.parentPath;
        } while(path != null);
        
        return true;
    }

    public int hashCode() {
        // Should be enough. We may also xor with parent's hashcode.
        return key.hashCode();
    }

    /**
     * Returns true if <code>aTreePath</code> is a descendant of this
     * TreePath. A TreePath P1 is a descendent of a TreePath P2 if P1 contains
     * all of the components that make up P2's path. For example, if this object
     * has the path [a, b], and <code>aTreePath</code> has the path [a, b, c],
     * then <code>aTreePath</code> is a descendant of this object. However, if
     * <code>aTreePath</code> has the path [a], then it is not a descendant of
     * this object.
     * 
     * @return true if <code>aTreePath</code> is a descendant of this path
     */
    public boolean isDescendant(TreePath aTreePath) {
        if (aTreePath == this)
            return true;

        if (aTreePath != null) {
            int pathLength = getPathCount();
            int oPathLength = aTreePath.getPathCount();

            if (oPathLength < pathLength)
                // Can't be a descendant, has fewer components in the path.
                return false;
            while (oPathLength-- > pathLength)
                aTreePath = aTreePath.getParentPath();
            return equals(aTreePath);
        }
        return false;
    }

//    /**
//     * Returns a new path containing all the elements of this object plus
//     * <code>child</code>. <code>child</code> will be the last element of
//     * the newly created TreePath. This will throw a NullPointerException if
//     * child is null.
//     */
//    public TreePath pathByAddingChild(Object child) {
//        if (child == null)
//            throw new NullPointerException("Null child not allowed");
//
//        return new TreePath(this, child);
//    }

    /**
     * Returns a path containing all the elements of this object, except the
     * last path component.
     */
    public TreePath getParentPath() {
        return parentPath;
    }
    
    /**
     * Returns the key of last element of this path.
     */
    public String getLastKey() {
        return this.key;
    }

    /**
     * Returns a string that displays and identifies this object's properties.
     * 
     * @return a String representation of this object
     */
    public String toString() {
        if (this.cachedToString == null) {
            StringBuffer buf = new StringBuffer();
            appendTo(buf);
            this.cachedToString = buf.toString();
        }
        return this.cachedToString;
    }
    
    /** Recursively build the text representation of a tree path */
    private void appendTo(StringBuffer buf) {
        if (this.parentPath != ROOT_PATH) {
            this.parentPath.appendTo(buf);
        }
        buf.append('/');
        buf.append(this.key);
    }
    
    /**
     * Returns the <code>TreePath</code> represented by a given String.
     * @param s the string representation of the path
     * @return a path object
     * 
     * @see #toString()
     */
    public static TreePath valueOf(String s) {
        // FIXME: see if some caching could be useful here.
        if (s == null || s.length() == 0) {
            throw new IllegalArgumentException("Invalid empty string");
        }
        StringTokenizer stok = new StringTokenizer(s, "/");
        TreePath current = ROOT_PATH;
        while (stok.hasMoreTokens()) {
            String tok = stok.nextToken();
            current = current == null ? new TreePath(tok) : new TreePath(current, tok);
        }

        return current;
    }
    
    public Object getObject(TreeModel model) {
        return this.parentPath == null ?
                model.getRoot() :
                model.getChild(this.parentPath.getObject(model), this.key);
    }
}
