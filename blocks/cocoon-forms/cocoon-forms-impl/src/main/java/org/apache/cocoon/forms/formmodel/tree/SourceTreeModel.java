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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.cocoon.util.WildcardMatcherHelper;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.TraversableSource;

/**
 * A {@link TreeModel} that builds a hierarchy of <code>TraversableSource</code>s.
 * 
 * @version $Id$
 */
public class SourceTreeModel implements TreeModel {
    
    private TreeModelHelper helper = new TreeModelHelper(this);
    
    private List fileIncludePatterns;
    private List fileExcludePatterns;
    private List dirIncludePatterns;
    private List dirExcludePatterns;
    
    /** optimization hint: don't filter child collections if there are no patterns */
    private boolean hasPatterns = false;

    private TraversableSource rootSource;

    private String rootURL;
    private SourceResolver resolver;

    public SourceTreeModel(SourceResolver resolver, String rootURL) {
        this.resolver = resolver;
        this.rootURL = rootURL;
    }

    public SourceTreeModel(SourceTreeModelDefinition definition) {
        this.rootURL = definition.getRootURL();
        this.resolver = definition.getResolver();
        this.fileIncludePatterns = definition.getFileIncludePatterns();
        this.fileExcludePatterns = definition.getFileExcludePatterns();
        this.dirIncludePatterns =  definition.getDirectoryIncludePatterns();
        this.dirExcludePatterns =  definition.getDirectoryExcludePatterns();
        
        this.hasPatterns = this.fileIncludePatterns != null || this.fileExcludePatterns != null ||
            this.dirIncludePatterns != null || this.dirExcludePatterns != null;
    }

    public Object getRoot() {
        if (this.rootSource == null) {
            try {
                this.rootSource = (TraversableSource) this.resolver.resolveURI(this.rootURL);
            } catch (Exception e) {
                throw new CascadingRuntimeException("Cannot resolve " + this.rootURL, e);
            }
        }
        return this.rootSource;
    }

    public Collection getChildren(Object parent) {
        if (parent instanceof TraversableSource) {
            TraversableSource dir = (TraversableSource)parent;
            try {
                // Return children if it's a collection, null otherwise
                return dir.isCollection() ? filterChildren(dir.getChildren()) : null;
            } catch (SourceException e) {
                throw new CascadingRuntimeException("getChildren", e);
            }
        } else {
            return null;
        }
    }
    
    private Collection filterChildren(Collection coll) {
        if (!this.hasPatterns) {
            return coll;
        }
        
        ArrayList result = new ArrayList();
        Iterator iter = coll.iterator();
        while(iter.hasNext()) {
            TraversableSource src = (TraversableSource)iter.next();

            // Does it match the patterns?
            boolean matches = true;
            if (src.isCollection()) {
                matches = matches(src, this.dirIncludePatterns, this.dirExcludePatterns);
            } else {
                matches = matches(src, this.fileIncludePatterns, this.fileExcludePatterns);
            }

            if (matches) {
                result.add(src);
            }
        }
        
        return result;
    }
    
    private boolean matches(TraversableSource src, List includes, List excludes) {
        boolean matches = true;
        final String name = src.getName();
        
        // check include patterns
        if (includes != null && includes.size() > 0) {
            matches = false;
            check: for (int i = 0; i < includes.size(); i++) {
                if (WildcardMatcherHelper.match((String)includes.get(i), name) != null) {
                    matches = true;
                    break check;
                }
            }
        }
        
        // check exclude patterns
        if (matches && excludes != null && excludes.size() > 0) {
            check: for (int i = 0; i < excludes.size(); i++) {
                if (WildcardMatcherHelper.match((String)excludes.get(i), name) != null) {
                    matches = false;
                    break check;
                }
            }
        }
        return matches;
    }
    
    public boolean isLeaf(Object obj) {
        return !(obj instanceof TraversableSource) || !((TraversableSource)obj).isCollection();
    }

    public String getChildKey(Object parent, Object child) {
        return ((TraversableSource)child).getName();
    }

    public Object getChild(Object parent, String key) {
        try {
            return ((TraversableSource)parent).getChild(key);
        } catch (SourceException e) {
            throw new CascadingRuntimeException("getChild", e);
        }
    }
    
    public void setRootURL(String url) {
        if (this.rootSource != null) {
            this.resolver.release(this.rootSource);
            this.rootSource = null;
        }
        this.rootURL = url;
        helper.fireTreeStructureChanged(TreePath.ROOT_PATH);
    }

    public void setRootSource(TraversableSource src) {
        this.rootSource = src;
        helper.fireTreeStructureChanged(TreePath.ROOT_PATH);
    }

    public void addTreeModelListener(TreeModelListener l) {
        helper.addTreeModelListener(l);
    }

    public void removeTreeModelListener(TreeModelListener l) {
        helper.removeTreeModelListener(l);
    }

    public Object getNode(TreePath path) {
        // FIXME: can be heavily optimized by building a new URL from the path elements.
        return helper.getNode(path);
    }
}
