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
package org.apache.cocoon.environment.wrapper;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Map;

import org.apache.cocoon.environment.Environment;

/**
 * Enviroment facade, whose delegate object can be changed. This class is required to handle internal redirects
 * in sitemap sources ("cocoon:"). This is because {@link org.apache.cocoon.components.source.SitemapSource} keeps
 * the environment in which the internal request should be processed. But internal redirects create a new
 * processing environment and there's no way to change the one held by the <code>SitemapSource</code>. So the
 * processing of internal redirects actually changes the delegate of this class, transparently for the 
 * <code>SitemapSource</code>.
 * 
 * @see org.apache.cocoon.components.source.impl.SitemapSource
 * @see org.apache.cocoon.components.treeprocessor.TreeProcessor#handleCocoonRedirect(String, Environment, org.apache.cocoon.components.treeprocessor.InvokeContext)
 *
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version CVS $Id: MutableEnvironmentFacade.java,v 1.10 2004/05/25 07:28:25 cziegeler Exp $
 */
public class MutableEnvironmentFacade implements Environment {

    private EnvironmentWrapper env;
    
    public MutableEnvironmentFacade(EnvironmentWrapper env) {
        this.env = env;
    }
    
    public EnvironmentWrapper getDelegate() {
        return this.env;
    }
    
    public void setDelegate(EnvironmentWrapper env) {
        this.env = env;
    }
    
    public void setURI(String prefix, String uri) {
        this.env.setURI(prefix, uri);
    }

    public void setOutputStream(OutputStream os) {
        this.env.setOutputStream(os);
    }

    // Move this to the Environment interface ?
    public String getRedirectURL() {
        return this.env.getRedirectURL();
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Environment#getURI()
     */
    public String getURI() {
        return env.getURI();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Environment#getURIPrefix()
     */
    public String getURIPrefix() {
        return env.getURIPrefix();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Environment#getView()
     */
    public String getView() {
        return env.getView();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Environment#getAction()
     */
    public String getAction() {
        return env.getAction();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Environment#redirect(java.lang.String, boolean, boolean)
     */
    public void redirect(String url, 
                         boolean global, 
                         boolean permanent) throws IOException {
        env.redirect(url, global, permanent);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Environment#setContentType(java.lang.String)
     */
    public void setContentType(String mimeType) {
        env.setContentType(mimeType);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Environment#getContentType()
     */
    public String getContentType() {
        return env.getContentType();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Environment#setContentLength(int)
     */
    public void setContentLength(int length) {
        env.setContentLength(length);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Environment#setStatus(int)
     */
    public void setStatus(int statusCode) {
        env.setStatus(statusCode);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Environment#getOutputStream(int)
     */
    public OutputStream getOutputStream(int bufferSize) throws IOException {
        return env.getOutputStream(bufferSize);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Environment#getObjectModel()
     */
    public Map getObjectModel() {
        return env.getObjectModel();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Environment#isResponseModified(long)
     */
    public boolean isResponseModified(long lastModified) {
        return env.isResponseModified(lastModified);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Environment#setResponseIsNotModified()
     */
    public void setResponseIsNotModified() {
        env.setResponseIsNotModified();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Environment#setAttribute(java.lang.String, java.lang.Object)
     */
    public void setAttribute(String name, Object value) {
        env.setAttribute(name, value);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Environment#getAttribute(java.lang.String)
     */
    public Object getAttribute(String name) {
        return env.getAttribute(name);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Environment#removeAttribute(java.lang.String)
     */
    public void removeAttribute(String name) {
        env.removeAttribute(name);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Environment#getAttributeNames()
     */
    public Enumeration getAttributeNames() {
        return env.getAttributeNames();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Environment#tryResetResponse()
     */
    public boolean tryResetResponse() throws IOException {
        return env.tryResetResponse();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Environment#commitResponse()
     */
    public void commitResponse() throws IOException {
        env.commitResponse();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Environment#startingProcessing()
     */
    public void startingProcessing() {
        env.startingProcessing();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Environment#finishingProcessing()
     */
    public void finishingProcessing() {
        env.finishingProcessing();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Environment#isExternal()
     */
    public boolean isExternal() {
        return env.isExternal();
    }
    
    public void reset() {
        this.env.reset();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Environment#isInternRedirect()
     */
    public boolean isInternalRedirect() {
        return env.isInternalRedirect();
    }

}
