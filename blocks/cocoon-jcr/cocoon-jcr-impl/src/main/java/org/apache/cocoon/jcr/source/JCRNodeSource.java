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
package org.apache.cocoon.jcr.source;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.cocoon.CascadingIOException;
import org.apache.excalibur.source.ModifiableTraversableSource;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceNotFoundException;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.TraversableSource;

/**
 * A Source for a JCR node.
 *
 * @version $Id$
 */
public class JCRNodeSource implements Source, TraversableSource, ModifiableTraversableSource {

    /** The full URI */
    protected String computedURI;

    /** The node path */
    protected final String path;

    /** The factory that created this Source */
    protected final JCRSourceFactory factory;

    /** The session this source is bound to */
    protected final Session session;

    /** The node pointed to by this source (can be null) */
    protected Node node;

    public JCRNodeSource(JCRSourceFactory factory, Session session, String path) throws SourceException {
        this.factory = factory;
        this.session = session;
        this.path = path;

        try {
            Item item = session.getItem(path);
            if (!item.isNode()) {
                throw new SourceException("Path '" + path + "' is a property (should be a node)");
            } else {
                this.node = (Node) item;
            }
        } catch (PathNotFoundException e) {
            // Not found
            this.node = null;
        } catch (RepositoryException e) {
            throw new SourceException("Cannot lookup repository path " + path, e);
        }
    }

    public JCRNodeSource(JCRSourceFactory factory, Node node) throws SourceException {
        this.factory = factory;
        this.node = node;

        try {
          this.session = node.getSession();
          this.path = node.getPath();
        } catch (RepositoryException e) {
            throw new SourceException("Cannot get node's informations", e);
        }
    }

    public JCRNodeSource(JCRNodeSource parent, Node node) throws SourceException {
        this.factory = parent.factory;
        this.session = parent.session;
        this.node = node;

        try {
            this.path = getChildPath(parent.path, node.getName());

        } catch (RepositoryException e) {
            throw new SourceException("Cannot get name of child of " + parent.getURI(), e);
        }
    }

    private String getChildPath(String path, String name) {
        StringBuffer pathBuf = new StringBuffer(path);
        // Append '/' only if the parent isn't the root (it's path is "/" in
        // that case)
        if (pathBuf.length() > 1)
            pathBuf.append('/');
        pathBuf.append(name);
        return pathBuf.toString();
    }

    /**
     * Returns the JCR <code>Node</code> this source points to, or
     * <code>null</code> if it denotes a non-existing path.
     *
     * @return the JCR node.
     */
    public Node getNode() {
        return this.node;
    }

    /**
     * Returns the path within the repository this source points to.
     *
     * @return the path
     */
    public String getPath() {
        return this.path;
    }

    /**
     * Returns the JCR <code>Session</code> used by this source.
     *
     * @return the JCR session.
     */
    public Session getSession() {
        return this.session;
    }

    /**
     * Returns the JCR <code>Node</code> used to store the content of this
     * source.
     *
     * @return the JCR content node, or <code>null</code> if no such node
     *         exist, either because the source is a collection or doesn't
     *         currently contain data.
     */
    public Node getContentNode() {
        if (this.node == null) {
            return null;
        }

        if (isCollection()) {
            return null;
        }

        try {
            return this.factory.getContentNode(this.node);
        } catch (RepositoryException e) {
            return null;
        }
    }

    // =============================================================================================
    // Source interface
    // =============================================================================================

    /*
     * (non-Javadoc)
     *
     * @see org.apache.excalibur.source.Source#exists()
     */
    public boolean exists() {
        return this.node != null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.excalibur.source.Source#getInputStream()
     */
    public InputStream getInputStream() throws IOException, SourceNotFoundException {
        if (this.node == null) {
            throw new SourceNotFoundException("Path '" + this.getURI() + "' does not exist");
        }

        if (this.isCollection()) {
            throw new SourceException("Path '" + this.getURI() + "' is a collection");
        }

        try {
            Property contentProp = this.factory.getContentProperty(this.node);
            return contentProp.getStream();
        } catch (Exception e) {
            throw new SourceException("Error opening stream for '" + this.getURI() + "'", e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.excalibur.source.Source#getURI()
     */
    public String getURI() {
        if (this.computedURI == null) {
            this.computedURI = this.factory.getScheme() + ":/" + this.path;
        }
        return this.computedURI;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.excalibur.source.Source#getScheme()
     */
    public String getScheme() {
        return this.factory.getScheme();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.excalibur.source.Source#getValidity()
     */
    public SourceValidity getValidity() {
        if (!exists()) {
            return null;
        }
        try {
            Property prop = this.factory.getValidityProperty(this.node);
            return prop == null ? null : new JCRNodeSourceValidity(prop.getValue());
        } catch (RepositoryException re) {
            return null;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.excalibur.source.Source#refresh()
     */
    public void refresh() {
        // nothing to do here
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.excalibur.source.Source#getMimeType()
     */
    public String getMimeType() {
        if (!exists()) {
            return null;
        }
        try {
            Property prop = this.factory.getMimeTypeProperty(this.node);
            if (prop == null) {
                return null;
            } else {
                String value = prop.getString();
                return value.length() == 0 ? null : value;
            }
        } catch (RepositoryException re) {
            return null;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.excalibur.source.Source#getContentLength()
     */
    public long getContentLength() {
        if (!exists()) {
            return -1;
        }
        try {
            Property prop = this.factory.getContentProperty(this.node);
            return prop == null ? -1 : prop.getLength();
        } catch (RepositoryException re) {
            return -1;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.excalibur.source.Source#getLastModified()
     */
    public long getLastModified() {
        if (!exists()) {
            return 0;
        }
        try {
            Property prop = this.factory.getLastModifiedDateProperty(this.node);
            return prop == null ? 0 : prop.getDate().getTime().getTime();
        } catch (RepositoryException re) {
            return 0;
        }
    }

    // =============================================================================================
    // TraversableSource interface
    // =============================================================================================

    public boolean isCollection() {
        if (!exists())
            return false;

        try {
            return this.factory.isCollection(this.node);
        } catch (RepositoryException e) {
            return false;
        }
    }

    public Collection getChildren() throws SourceException {
        if (!isCollection()) {
            return Collections.EMPTY_LIST;
        } else {
            ArrayList children = new ArrayList();

            NodeIterator nodes;
            try {
                nodes = this.node.getNodes();
            } catch (RepositoryException e) {
                throw new SourceException("Cannot get child nodes for " + getURI(), e);
            }

            while (nodes.hasNext()) {
                children.add(this.factory.createSource(this, nodes.nextNode()));
            }
            return children;
        }
    }

    public Source getChild(String name) throws SourceException {
        if (this.isCollection()) {
            return this.factory.createSource(this.session, getChildPath(this.path, name));
        } else {
            throw new SourceException("Not a collection: " + getURI());
        }
    }

    public String getName() {
        return this.path.substring(this.path.lastIndexOf('/') + 1);
    }

    public Source getParent() throws SourceException {
        if (this.path.length() == 1) {
            // Root
            return null;
        }

        int lastPos = this.path.lastIndexOf('/');
        String parentPath = lastPos == 0 ? "/" : this.path.substring(0, lastPos);
        return this.factory.createSource(this.session, parentPath);
    }

    // =============================================================================================
    // ModifiableTraversableSource interface
    // =============================================================================================

    public OutputStream getOutputStream() throws IOException {
        if (isCollection()) {
            throw new SourceException("Cannot write to collection " + this.getURI());
        }

        try {
            Node contentNode;
            if (!exists()) {
                JCRNodeSource parent = (JCRNodeSource) getParent();

                // Create the path if it doesn't exist
                parent.makeCollection();

                // Create our node
                this.node = this.factory.createFileNode(parent.node, getName());
                contentNode = this.factory.createContentNode(this.node);
            } else {
                contentNode = this.factory.getContentNode(this.node);
            }

            return new JCRSourceOutputStream(contentNode);
        } catch (RepositoryException e) {
            throw new SourceException("Cannot create content node for " + getURI(), e);
        }
    }

    public void delete() throws SourceException {
        if (exists()) {
            try {
                this.node.remove();
                this.node = null;
                this.session.save();
            } catch (RepositoryException e) {
                throw new SourceException("Cannot delete " + getURI(), e);
            }
        }
    }

    public boolean canCancel(OutputStream os) {
        if (os instanceof JCRSourceOutputStream) {
            return ((JCRSourceOutputStream) os).canCancel();
        } else {
            return false;
        }
    }

    public void cancel(OutputStream os) throws IOException {
        if (canCancel(os)) {
            ((JCRSourceOutputStream) os).cancel();
        } else {
            throw new IllegalArgumentException("Stream cannot be cancelled");
        }
    }

    public void makeCollection() throws SourceException {
        if (exists()) {
            if (!isCollection()) {
                throw new SourceException("Cannot make a collection with existing node at " + getURI());
            }
        } else {
            try {
                // Ensure parent exists
                JCRNodeSource parent = (JCRNodeSource) getParent();
                if (parent == null) {
                    throw new RuntimeException("Problem: root node does not exist!!");
                }
                parent.makeCollection();
                Node parentNode = parent.node;

                String typeName = this.factory.getFolderNodeType(parentNode);

                this.node = parentNode.addNode(getName(), typeName);
                this.session.save();

            } catch (RepositoryException e) {
                throw new SourceException("Cannot make collection " + this.getURI(), e);
            }
        }
    }

    // ----------------------------------------------------------------------------------
    // Private helper class for ModifiableSource implementation
    // ----------------------------------------------------------------------------------

    /**
     * An outputStream that will save the session upon close, and discard it
     * upon cancel.
     */
    private class JCRSourceOutputStream extends ByteArrayOutputStream {
        private boolean isClosed = false;

        private final Node contentNode;

        public JCRSourceOutputStream(Node contentNode) {
            this.contentNode = contentNode;
        }

        public void close() throws IOException {
            if (!isClosed) {
                super.close();
                this.isClosed = true;
                try {
                    JCRSourceFactory.ContentTypeInfo info = (JCRSourceFactory.ContentTypeInfo) factory.getTypeInfo(contentNode);
                    contentNode.setProperty(info.contentProp, new ByteArrayInputStream(this.toByteArray()));
                    if (info.lastModifiedProp != null) {
                        contentNode.setProperty(info.lastModifiedProp, new GregorianCalendar());
                    }
                    if (info.mimeTypeProp != null) {
                        // FIXME: define mime type
                        contentNode.setProperty(info.mimeTypeProp, "");
                    }

                    JCRNodeSource.this.session.save();
                } catch (RepositoryException e) {
                    throw new CascadingIOException("Cannot save content to " + getURI(), e);
                }
            }
        }

        public boolean canCancel() {
            return !isClosed;
        }

        public void cancel() throws IOException {
            if (isClosed) {
                throw new IllegalStateException("Cannot cancel : outputstrem is already closed");
            }
        }
    }
}
