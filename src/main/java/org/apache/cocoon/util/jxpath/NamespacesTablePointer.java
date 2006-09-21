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
package org.apache.cocoon.util.jxpath;

import org.apache.cocoon.xml.NamespacesTable;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;

/**
 * A JXPath <code>Pointer</code> that tracks namespaces defined by a {@link NamespacesTable}.
 * This class is to be used to inform JXPath of the namespaces declared in a host environment
 * (e.g. JXTemplateGenerator) using
 * <a href="http://jakarta.apache.org/commons/jxpath/apidocs/org/apache/commons/jxpath/JXPathContext.html#setNamespaceContextPointer(org.apache.commons.jxpath.Pointer)">JXPathContext.setNamespaceContextPointer()</a>.
 * 
 * @since 2.1.8
 * @version $Id$
 */
public class NamespacesTablePointer extends NodePointer {
    
    private NamespacesTable namespaces;

    public NamespacesTablePointer(NamespacesTable namespaces) {
        super(null);
        this.namespaces = namespaces;
    }

    public String getNamespaceURI(String prefix) {
        return namespaces.getUri(prefix);
    }

    protected String getDefaultNamespaceURI() {
        return namespaces.getUri("");
    }

    public NodeIterator namespaceIterator() {
        return null;
    }
    
    //-------------------------------------------------------------------------
    // Dummy implementation of abstract methods
    //-------------------------------------------------------------------------

    public boolean isLeaf() {
        return true;
    }

    public boolean isCollection() {
        return false;
    }

    public int getLength() {
        return 0;
    }

    public QName getName() {
        return null;
    }

    public Object getBaseValue() {
        return null;
    }

    public Object getImmediateNode() {
        return null;
    }

    public void setValue(Object value) {
        // ignore
    }

    public int compareChildNodePointers(NodePointer arg0, NodePointer arg1) {
        return -1;
    }
}
