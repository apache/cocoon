/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.components.repository.impl;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Properties;

import javax.xml.transform.OutputKeys;

import org.apache.avalon.excalibur.io.IOUtil;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.LifecycleHelper;
import org.apache.cocoon.components.repository.Repository;
import org.apache.cocoon.components.repository.helpers.CredentialsToken;
import org.apache.cocoon.components.repository.helpers.RepositoryTransactionHelper;
import org.apache.cocoon.components.repository.helpers.RepositoryPropertyHelper;
import org.apache.cocoon.components.repository.helpers.RepositoryVersioningHelper;
import org.apache.cocoon.components.webdav.WebDAVUtil;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.commons.httpclient.HttpException;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.xml.dom.DOMParser;
import org.apache.webdav.lib.WebdavResource;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * A repository implementation for WebDAV.
 */
public class WebDAVRepository extends AbstractLogEnabled
implements Repository, Serviceable, Configurable, Initializable, Disposable, Component {
    
    /** The name of the repository location configuration element */
    public static final String REPO_BASE_CONF = "repo-base";

    /* The ServiceManager */
    private ServiceManager manager;

    /* The RepositoryPropertyHelper */
    private WebDAVRepositoryPropertyHelper propertyHelper;

    /* The RepositoryTransactionHelper */
    private WebDAVRepositoryTransactionHelper transactionHelper;

    /* The RepositoryVersioningHelper */
    private WebDAVRepositoryVersioningHelper versioningHelper;

    /* The location of the repository */
    private String repoBaseUrl;

    /* The credentials to be used against the WebDAV repository */
    private CredentialsToken credentials;

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;

    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration configuration) throws ConfigurationException {
        this.repoBaseUrl = configuration.getChild(REPO_BASE_CONF).getValue();
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("configuring repository location " + this.repoBaseUrl);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize() throws Exception {
        this.propertyHelper = new WebDAVRepositoryPropertyHelper(this.credentials, this);
        this.transactionHelper = new WebDAVRepositoryTransactionHelper(this.credentials, this);
        this.versioningHelper = new WebDAVRepositoryVersioningHelper(this.credentials, this);
        LifecycleHelper lh = new LifecycleHelper(this.getLogger(),
                                                 null,
                                                 this.manager,
                                                 null,
                                                 null);
        lh.setupComponent(this.propertyHelper, true);
        lh.setupComponent(this.transactionHelper, true);
        lh.setupComponent(this.versioningHelper, true);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        this.manager = null;
        this.propertyHelper.dispose();
        this.transactionHelper.dispose();
        this.versioningHelper.dispose();
        this.propertyHelper = null;
        this.transactionHelper = null;
        this.versioningHelper = null;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.repository.Repository#getContentString(java.lang.String)
     */
    public String getContentString(String uri) throws ProcessingException {

        try {
            return IOUtil.toString(this.getContentStream(uri));

        } catch (IOException ioe) {
            throw new ProcessingException ("Error loading resource: " + this.repoBaseUrl + uri, ioe);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.repository.Repository#getContentStream(java.lang.String)
     */
    public InputStream getContentStream(String uri) throws ProcessingException {

        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("getting content of: " + uri);
        }

        try {

            WebdavResource resource = WebDAVUtil.getWebdavResource(this.getAbsoluteURI(uri));
            if (!resource.exists()) {
                throw new HttpException(uri + " does not exist");
            }
            return new BufferedInputStream(resource.getMethodData());

        } catch (MalformedURLException mue) {
            throw new ProcessingException ("Bad URL for resource: " + this.repoBaseUrl + uri, mue);
        } catch (IOException ioe) {
            throw new ProcessingException ("Error loading resource: " + this.repoBaseUrl + uri, ioe);
        }
   }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.repository.Repository#getContentDOM(java.lang.String)
     */
    public Document getContentDOM(String uri) throws ProcessingException {

        DOMParser parser = null;

        try {
            parser = (DOMParser)this.manager.lookup( DOMParser.ROLE);
            return parser.parseDocument(new InputSource(this.getContentStream(uri)));

        } catch (SAXException se) {
            throw new ProcessingException ("Error parsing: " + this.repoBaseUrl + uri, se);
        } catch (IOException ioe) {
            throw new ProcessingException ("Error getting: " + this.repoBaseUrl + uri, ioe);
        } catch (ServiceException se) {
            throw new ProcessingException ("Error getting DOMParser", se);

        } finally {
            this.manager.release(parser);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.repository.Repository#saveContent(java.lang.String, java.lang.String)
     */
    public boolean saveContent(String uri, String content) {

        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("save content to " + uri);
        }

        try {
            return WebDAVUtil.getWebdavResource(this.getAbsoluteURI(uri)).putMethod(content);
            
        } catch (HttpException he) {
            this.getLogger().error("Error saving: " + this.repoBaseUrl + uri, he);
        } catch (IOException ioe) {
            this.getLogger().error("Error saving: " + this.repoBaseUrl + uri, ioe);
        }

        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.repository.Repository#saveContent(java.lang.String, org.w3c.dom.Node)
     */
    public boolean saveContent(String uri, Node node) {

        try {
        Properties format = new Properties();
        format.put(OutputKeys.METHOD, "xml");
        format.put(OutputKeys.OMIT_XML_DECLARATION, "yes");
        return this.saveContent(uri, XMLUtils.serializeNode(node, format));

        } catch (ProcessingException pe) {
            this.getLogger().error("Error saving dom to: " + this.repoBaseUrl + uri, pe);
        }

        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.repository.Repository#saveContent(java.lang.String, org.apache.excalibur.source.Source)
     */
    public boolean saveContent(String uri, Source source) {

        try {
            return this.saveContent(uri, IOUtil.toString(source.getInputStream()));

        } catch (IOException ioe) {
            this.getLogger().error("Error saving source: " + source.getURI() +
                                           " to "+ this.repoBaseUrl + uri, ioe);
        }

        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.repository.Repository#createResource(java.lang.String, java.lang.String)
     */
    public boolean createResource(String uri, String content) {

        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("creating new resource " + uri);
        }

        try {
            WebDAVUtil.createResource(uri, content);
            return true;
            
        } catch (HttpException he) {
            this.getLogger().error("Error creating resource: " + this.repoBaseUrl + uri, he);
        } catch (IOException ioe) {
            this.getLogger().error("Error creating resource: " + this.repoBaseUrl + uri, ioe);
        }

        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.repository.Repository#exists(java.lang.String)
     */
    public boolean exists(String uri) {

        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("checking existance of " + uri);
        }

        try {
            return WebDAVUtil.getWebdavResource(this.getAbsoluteURI(uri)).exists();

        } catch (HttpException he) {
            this.getLogger().error("HTTP Error occurred while checking for existance of: " + uri, he);
        } catch (IOException ioe) {
            this.getLogger().error("IO Error occurred while checking for existance of: " + uri, ioe);
        }

        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.repository.Repository#copy(java.lang.String, java.lang.String, boolean)
     */
    public boolean copy(String uri, String dest, boolean recurse, boolean overwrite) {

        try {
            WebDAVUtil.copyResource(this.getAbsoluteURI(uri), this.getAbsoluteURI(dest), recurse, overwrite);
            return true;

        } catch (HttpException he) {
            this.getLogger().error("HTTP Error copying: " + this.repoBaseUrl + uri, he);
        } catch (IOException ioe) {
            this.getLogger().error("IO Error copying: " + this.repoBaseUrl + uri, ioe);
        }

        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.repository.Repository#move(java.lang.String, java.lang.String, boolean)
     */
    public boolean move(String uri, String dest, boolean recurse, boolean overwrite) {

        try {
            WebDAVUtil.moveResource(this.getAbsoluteURI(uri), this.getAbsoluteURI(dest), recurse, overwrite);
            return true;

        } catch (HttpException he) {
            this.getLogger().error("HTTP Error moving: " + this.repoBaseUrl + uri, he);
        } catch (IOException ioe) {
            this.getLogger().error("IO Error moving: " + this.repoBaseUrl + uri, ioe);
        }

        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.repository.Repository#remove(java.lang.String)
     */
    public boolean remove(String uri) {

        try {
            WebDAVUtil.getWebdavResource(this.getAbsoluteURI(uri)).deleteMethod();
            return true;

        } catch (HttpException he) {
            this.getLogger().error("HTTP Error removing: " + this.repoBaseUrl + uri, he);
        } catch (IOException ioe) {
            this.getLogger().error("IO Error removing: " + this.repoBaseUrl + uri, ioe);
        }

        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.repository.Repository#makeCollection(java.lang.String, boolean)
     */
    public boolean makeCollection(String uri, boolean recursive) {

        try {
            if (uri.endsWith("/")) uri = uri.substring(0, uri.length()-1);
            if (recursive) {
                WebDAVUtil.makePath(this.getAbsoluteURI(uri));
                return true;
            } else {
                String parent = uri.substring(0, uri.lastIndexOf("/"));
                String collection = uri.substring(uri.lastIndexOf("/")+1);
                WebDAVUtil.makeCollection(this.getAbsoluteURI(parent), collection);
                return true;
            }

        } catch (HttpException he) {
            this.getLogger().error("HTTP Error making collection: " + this.repoBaseUrl + uri, he);
        } catch (IOException ioe) {
            this.getLogger().error("IO Error making collection: " + this.repoBaseUrl + uri, ioe);
        }

        return false;
    }

    /**
     * get a WebDAV property helper
     * 
     * @return  a WebDAV property helper.
     */
    public RepositoryPropertyHelper getPropertyHelper() {
        return this.propertyHelper;
    }

    /**
     * get a WebDAV transaction helper
     * 
     * @return  a WebDAV transaction helper.
     */
    public RepositoryTransactionHelper getTransactionHelper() {
        return this.transactionHelper;
    }

    /**
     * get a WebDAV versioning helper
     * 
     * @return  a WebDAV versioning helper.
     */
    public RepositoryVersioningHelper getVersioningHelper() {
        return this.versioningHelper;
    }

    /**
     * get the absolute URI of a given path
     * 
     * @param uri  the uri to get a versioning helper for.
     * @return  the absolute URI.
     */
    public String getAbsoluteURI(String uri) {

        return "http://"+this.credentials.getPrincipal().getName()
                        +":"+this.credentials.getCredentials()
                        +"@"+this.repoBaseUrl + uri;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.repository.Repository#getCredentials()
     */
    public CredentialsToken getCredentials() {
        return this.credentials;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.repository.Repository#setCredentials(org.apache.cocoon.components.repository.helpers.CredentialsToken)
     */
    public void setCredentials(CredentialsToken credentials) {
        this.credentials = credentials;
    }

}