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

import java.io.IOException;
import java.util.List;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.components.repository.helpers.CredentialsToken;
import org.apache.cocoon.components.repository.helpers.RepositoryVersioningHelper;
import org.apache.cocoon.components.webdav.WebDAVUtil;
import org.apache.commons.httpclient.HttpException;

/**
 * A versioning helper class
 * intended to be used by flowscripts or corresponding wrapper components.
 */
public class WebDAVRepositoryVersioningHelper extends AbstractLogEnabled
implements RepositoryVersioningHelper, Serviceable, Disposable, Component {
    
    /* The ServiceManager */
    private ServiceManager manager;

    /* The repository component */
    private WebDAVRepository repo;

    /* The credentials to be used against the WebDAV repository */
    private CredentialsToken credentials;

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        this.manager = null;
    }

    /**
     * create a WebDAVRepositoryVersioningHelper
     * 
     * @param credentials  the user credentials to be used against the WebDAV repository.
     * @param repo  a reference to the WebDAVRepository object.
     */
    public WebDAVRepositoryVersioningHelper (CredentialsToken credentials, WebDAVRepository repo) {
        this.credentials = credentials;
        this.repo = repo;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.repository.helpers.RepositoryVersioningHelper#checkout(java.lang.String)
     */
    public boolean checkout(String uri) {

        try {
            WebDAVUtil.getWebdavResource(this.repo.getAbsoluteURI(uri)).checkoutMethod();
            return true;

        } catch (HttpException he) {
            this.getLogger().error("HTTP Error checking out " + uri, he);
        } catch (IOException ioe) {
            this.getLogger().error("IO Error checking out " + uri, ioe);
        }

        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.repository.helpers.RepositoryVersioningHelper#checkin(java.lang.String)
     */
    public boolean checkin(String uri) {

        try {
            WebDAVUtil.getWebdavResource(this.repo.getAbsoluteURI(uri)).checkinMethod();
            return true;

        } catch (HttpException he) {
            this.getLogger().error("HTTP Error checking in " + uri, he);
        } catch (IOException ioe) {
            this.getLogger().error("IO Error checking in " + uri, ioe);
        }

        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.repository.helpers.RepositoryVersioningHelper#uncheckout(java.lang.String)
     */
    public boolean uncheckout(String uri) {

        try {
            WebDAVUtil.getWebdavResource(this.repo.getAbsoluteURI(uri)).uncheckoutMethod();
            return true;

        } catch (HttpException he) {
            this.getLogger().error("HTTP Error while uncheckout " + uri, he);
        } catch (IOException ioe) {
            this.getLogger().error("IO Error while uncheckout " + uri, ioe);
        }

        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.repository.helpers.RepositoryVersioningHelper#isVersioned(java.lang.String)
     */
    public boolean isVersioned(String uri) {
        //not yet implemented
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.repository.helpers.RepositoryVersioningHelper#setVersioned(java.lang.String, boolean)
     */
    public boolean setVersioned(final String uri, final boolean versioned) {            

        try {
            if(!versioned) {
                return false;

            } else {      
                return WebDAVUtil.getWebdavResource(this.repo.getAbsoluteURI(uri))
                                                    .versionControlMethod(this.repo.getAbsoluteURI(uri));
            }

        } catch (HttpException he) {
            this.getLogger().error("HTTP Error while versioncontrol " + uri, he);
        } catch (IOException ioe) {
            this.getLogger().error("IO Error while versioncontrol " + uri, ioe);
        }
        
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.repository.helpers.RepositoryVersioningHelper#getVersions(java.lang.String)
     */
    public List getVersions(String uri) {
        //not yet implemented
        throw new UnsupportedOperationException();
    }

}