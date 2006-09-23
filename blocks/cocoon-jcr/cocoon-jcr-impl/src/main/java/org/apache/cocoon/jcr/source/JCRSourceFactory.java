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

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.LoginException;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceFactory;
import org.apache.excalibur.source.SourceUtil;

/**
 * JCRSourceFactory is an implementation of
 * <code>ModifiableTraversableSource</code> on top of a JCR (aka <a
 * href="http://www.jcp.org/en/jsr/detail?id=170">JSR-170</a>) repository.
 * <p>
 * Since JCR allows a repository to define its own node types, it is necessary
 * to configure this source factory with a description of what node types map to
 * "files" and "folders" and the properties used to store source-related data.
 * <p>
 * A typical configuration for a naked Jackrabbit repository is as follows:
 *
 * <pre>
 *
 *    &lt;source-factories&gt;
 *      &lt;component-instance class=&quot;org.apache.cocoon.jcr.source.JCRSourceFactory&quot; name=&quot;jcr&quot;&gt;
 *        &lt;folder-node type=&quot;rep:root&quot;  new-file=&quot;nt:file&quot; new-folder=&quot;nt:folder&quot;/&gt;
 *        &lt;folder-node type=&quot;nt:folder&quot; new-file=&quot;nt:file&quot;/&gt;
 *        &lt;file-node type=&quot;nt:file&quot; content-path=&quot;jcr:content&quot; content-type=&quot;nt:resource&quot;/&gt;
 *        &lt;file-node type=&quot;nt:linkedFile&quot; content-ref=&quot;jcr:content&quot;/&gt;
 *        &lt;content-node type=&quot;nt:resource&quot;
 *                      content-prop=&quot;jcr:data&quot;
 *                      mimetype-prop=&quot;jcr:mimeType&quot;
 *                      lastmodified-prop=&quot;jcr:lastModified&quot;
 *                      validity-prop=&quot;jcr:lastModified&quot;/&gt;
 *      &lt;/component-instance&gt;
 *    &lt;/source-factories&gt;
 *
 * </pre>
 *
 * A <code>&lt;folder-node&gt;</code> defines a node type that is mapped to a
 * non-terminal source (i.e. that can have children). The <code>new-file</code>
 * and <code>new-folder</code> attributes respectively define what node types
 * should be used to create a new terminal and non-terminal source.
 * <p>
 * A <code>&lt;file-node&gt;</code> defines a note type that is mapped to a
 * terminal source (i.e. that can have some content). The
 * <code>content-path</code> attribute defines the path to the node's child
 * that actually holds the content, and <code>content-type</code> defines the
 * type of this content node.
 * <p>
 * The <code>content-ref</code> attribute is used to comply with JCR's
 * <code>nt:linkedFile</code> definition where the content node is not a
 * direct child of the file node, but is referenced by a property of this file
 * node. Such node types are read-only as there's no way to indicate where the
 * referenced content node should be created.
 * <p>
 * A <code>&lt;content-node&gt;</code> defines a node type that actually holds
 * the content of a <code>file-node</code>. The <code>content-prop</code>
 * attribute must be present and gives the name of the node's binary property
 * that will hold the actual content. Other attributes are optional:
 * <ul>
 * <li><code>mimetype-prop</code> defines a string property holding the
 * content's MIME type, </li>
 * <li><code>lastmodified-prop</code> defines a date property holding the
 * node's last modification date. It is automatically updated when content is
 * written to the <code>content-node</code>. </li>
 * <li><code>validity-prop</code> defines a property that gives the validity
 * of the content, used by Cocoon's cache. If not specified,
 * <code>lastmodified-prop</code> is used, if present. Otherwise the source
 * has no validity and won't be cacheable. </li>
 * </ul>
 * <p>
 * The format of URIs for this source is a path in the repository, and it is
 * therefore currently limited to repository traversal. Further work will add
 * the ability to specify query strings.
 *
 * @version $Id$
 */
public class JCRSourceFactory implements ThreadSafe, SourceFactory, Configurable, Serviceable {

    protected static class NodeTypeInfo {
        // Empty base class
    }

    protected static class FolderTypeInfo extends NodeTypeInfo {
        public String newFileType;

        public String newFolderType;
    }

    protected static class FileTypeInfo extends NodeTypeInfo {
        public String contentPath;

        public String contentType;

        public String contentRef;
    }

    protected static class ContentTypeInfo extends NodeTypeInfo {
        public String contentProp;

        public String mimeTypeProp;

        public String lastModifiedProp;

        public String validityProp;
    }

    /**
     * The repository we use
     */
    protected Repository repo;

    /**
     * Scheme, lazily computed at the first call to getSource()
     */
    protected String scheme;

    /**
     * The NodeTypeInfo for each of the types described in the configuration
     */
    protected Map typeInfos;

    protected ServiceManager manager;

    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
        // this.repo is lazily initialized to avoid a circular dependency between SourceResolver
        // and JackrabbitRepository that leads to a StackOverflowError at initialization time
    }

    public void configure(Configuration config) throws ConfigurationException {
        this.typeInfos = new HashMap();

        Configuration[] children = config.getChildren();

        for (int i = 0; i < children.length; i++) {
            Configuration child = children[i];
            String name = child.getName();

            if ("folder-node".equals(name)) {
                FolderTypeInfo info = new FolderTypeInfo();
                String type = child.getAttribute("type");
                info.newFileType = child.getAttribute("new-file");
                info.newFolderType = child.getAttribute("new-folder", type);

                this.typeInfos.put(type, info);

            } else if ("file-node".equals(name)) {
                FileTypeInfo info = new FileTypeInfo();
                info.contentPath = child.getAttribute("content-path", null);
                info.contentType = child.getAttribute("content-type", null);
                info.contentRef = child.getAttribute("content-ref", null);
                if (info.contentPath == null && info.contentRef == null) {
                    throw new ConfigurationException("One of content-path or content-ref is required at " + child.getLocation());
                }
                if (info.contentPath != null && info.contentType == null) {
                    throw new ConfigurationException("content-type must be present with content-path at " + child.getLocation());
                }
                this.typeInfos.put(child.getAttribute("type"), info);

            } else if ("content-node".equals(name)) {
                ContentTypeInfo info = new ContentTypeInfo();
                info.contentProp = child.getAttribute("content-prop");
                info.lastModifiedProp = child.getAttribute("lastmodified-prop", null);
                info.mimeTypeProp = child.getAttribute("mimetype-prop", null);
                info.validityProp = child.getAttribute("validity-prop", info.lastModifiedProp);
                this.typeInfos.put(child.getAttribute("type"), info);

            } else {
                throw new ConfigurationException("Unknown configuration " + name + " at " + child.getLocation());
            }
        }

    }

    protected void lazyInit() {
        if (this.repo == null) {
            try {
                this.repo = (Repository)manager.lookup(Repository.class.getName());
            } catch (Exception e) {
                throw new CascadingRuntimeException("Cannot lookup repository", e);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.excalibur.source.SourceFactory#getSource(java.lang.String,
     *      java.util.Map)
     */
    public Source getSource(String uri, Map parameters) throws IOException, MalformedURLException {
        lazyInit();

        if (this.scheme == null) {
            this.scheme = SourceUtil.getScheme(uri);
        }

        Session session;
        try {
            // TODO: accept a different workspace?
            session = repo.login();
        } catch (LoginException e) {
            throw new SourceException("Login to repository failed", e);
        } catch (RepositoryException e) {
            throw new SourceException("Cannot access repository", e);
        }

        // Compute the path
        String path = SourceUtil.getSpecificPart(uri);
        if (!path.startsWith("//")) {
            throw new MalformedURLException("Expecting " + this.scheme + "://path and got " + uri);
        }
        // Remove first '/'
        path = path.substring(1);
        int pathLen = path.length();
        if (pathLen > 1) {
            // Not root: ensure there's no trailing '/'
            if (path.charAt(pathLen - 1) == '/') {
                path = path.substring(0, pathLen - 1);
            }
        }

        return createSource(session, path);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.excalibur.source.SourceFactory#release(org.apache.excalibur.source.Source)
     */
    public void release(Source source) {
        // nothing
    }

    public String getScheme() {
        return this.scheme;
    }

    /**
     * Get the type info for a node.
     *
     * @param node the node
     * @return the type info
     * @throws RepositoryException if node type couldn't be accessed or if no type info is found
     */
    public NodeTypeInfo getTypeInfo(Node node) throws RepositoryException {
        String typeName = node.getPrimaryNodeType().getName();
        NodeTypeInfo result = (NodeTypeInfo) this.typeInfos.get(typeName);
        if (result == null) {
            // TODO: build a NodeTypeInfo using introspection
            throw new RepositoryException("No type info found for node type '" + typeName + "' at " + node.getPath());
        }

        return result;
    }

    /**
     * Get the type info for a given node type name.
     * @param typeName the type name
     * @return the type info
     * @throws RepositoryException if no type info is found
     */
    public NodeTypeInfo getTypeInfo(String typeName) throws RepositoryException {
        NodeTypeInfo result = (NodeTypeInfo) this.typeInfos.get(typeName);
        if (result == null) {
            // TODO: build a NodeTypeInfo using introspection
            throw new RepositoryException("No type info found for node type '" + typeName + "'");
        }

        return result;
    }

    /**
     * Get the content node for a given node
     *
     * @param node the node for which we want the content node
     * @return the content node
     * @throws RepositoryException if some error occurs, or if the given node isn't a file node or a content node
     */
    public Node getContentNode(Node node) throws RepositoryException {
        NodeTypeInfo info = getTypeInfo(node);

        if (info instanceof ContentTypeInfo) {
            return node;

        } else if (info instanceof FileTypeInfo) {
            FileTypeInfo finfo = (FileTypeInfo) info;
            if (".".equals(finfo.contentPath)) {
                return node;
            } else if (finfo.contentPath != null) {
                return node.getNode(finfo.contentPath);
            } else {
                Property ref = node.getProperty(finfo.contentRef);
                return getContentNode(ref.getNode());
            }
        } else {
            // A folder
            throw new RepositoryException("Can't get content node for folder node at " + node.getPath());
        }
    }

    /**
     * Creates a new source given its parent and its node
     *
     * @param parent the parent
     * @param node the node
     * @return a new source
     * @throws SourceException
     */
    public JCRNodeSource createSource(JCRNodeSource parent, Node node) throws SourceException {
        return new JCRNodeSource(parent, node);
    }

    /**
     * Creates a new source given a session and a path
     *
     * @param session the session
     * @param path the absolute path
     * @return a new source
     * @throws SourceException
     */
    public JCRNodeSource createSource(Session session, String path) throws SourceException {
        return new JCRNodeSource(this, session, path);
    }

    /**
     * Create a child file node in a folder node.
     *
     * @param folderNode the folder node
     * @param name the child's name
     * @return the newly created child node
     * @throws RepositoryException if some error occurs
     */
    public Node createFileNode(Node folderNode, String name) throws RepositoryException {
        NodeTypeInfo info = getTypeInfo(folderNode);
        if (!(info instanceof FolderTypeInfo)) {
            throw new RepositoryException("Node type " + folderNode.getPrimaryNodeType().getName() + " is not a folder type");
        }

        FolderTypeInfo folderInfo = (FolderTypeInfo) info;
        return folderNode.addNode(name, folderInfo.newFileType);
    }

    /**
     * Create the content node for a file node.
     *
     * @param fileNode the file node
     * @return the content node for this file node
     * @throws RepositoryException if some error occurs
     */
    public Node createContentNode(Node fileNode) throws RepositoryException {

        NodeTypeInfo info = getTypeInfo(fileNode);
        if (!(info instanceof FileTypeInfo)) {
            throw new RepositoryException("Node type " + fileNode.getPrimaryNodeType().getName() + " is not a file type");
        }

        FileTypeInfo fileInfo = (FileTypeInfo) info;
        Node contentNode = fileNode.addNode(fileInfo.contentPath, fileInfo.contentType);

        return contentNode;
    }

    /**
     * Get the content property for a given node
     *
     * @param node a file or content node
     * @return the content property
     * @throws RepositoryException if some error occurs
     */
    public Property getContentProperty(Node node) throws RepositoryException {
        Node contentNode = getContentNode(node);
        ContentTypeInfo info = (ContentTypeInfo) getTypeInfo(contentNode);
        return contentNode.getProperty(info.contentProp);
    }

    /**
     * Get the mime-type property for a given node
     *
     * @param node a file or content node
     * @return the mime-type property, or <code>null</code> if no such property exists
     * @throws RepositoryException if some error occurs
     */
    public Property getMimeTypeProperty(Node node) throws RepositoryException {
        Node contentNode = getContentNode(node);
        ContentTypeInfo info = (ContentTypeInfo) getTypeInfo(contentNode);

        String propName = info.mimeTypeProp;
        if (propName != null && contentNode.hasProperty(propName)) {
            return contentNode.getProperty(propName);
        } else {
            return null;
        }
    }

    /**
     * Get the lastmodified property for a given node
     *
     * @param node a file or content node
     * @return the lastmodified property, or <code>null</code> if no such property exists
     * @throws RepositoryException if some error occurs
     */
    public Property getLastModifiedDateProperty(Node node) throws RepositoryException {
        Node contentNode = getContentNode(node);
        ContentTypeInfo info = (ContentTypeInfo) getTypeInfo(contentNode);

        String propName = info.lastModifiedProp;
        if (propName != null && contentNode.hasProperty(propName)) {
            return contentNode.getProperty(propName);
        } else {
            return null;
        }
    }

    /**
     * Get the validity property for a given node
     *
     * @param node a file or content node
     * @return the validity property, or <code>null</code> if no such property exists
     * @throws RepositoryException if some error occurs
     */
    public Property getValidityProperty(Node node) throws RepositoryException {
        Node contentNode = getContentNode(node);
        ContentTypeInfo info = (ContentTypeInfo) getTypeInfo(contentNode);

        String propName = info.validityProp;
        if (propName != null && contentNode.hasProperty(propName)) {
            return contentNode.getProperty(propName);
        } else {
            return null;
        }
    }

    /**
     * Does a node represent a collection (i.e. folder-node)?
     *
     * @param node the node
     * @return <code>true</code> if it's a collection
     * @throws RepositoryException if some error occurs
     */
    public boolean isCollection(Node node) throws RepositoryException {
        return getTypeInfo(node) instanceof FolderTypeInfo;
    }

    /**
     * Get the node type to create a new subfolder of a given folder node.
     *
     * @param folderNode
     * @return the child folder node type
     * @throws RepositoryException if some error occurs
     */
    public String getFolderNodeType(Node folderNode) throws RepositoryException {
        FolderTypeInfo info = (FolderTypeInfo) getTypeInfo(folderNode);
        return info.newFolderType;
    }
}
