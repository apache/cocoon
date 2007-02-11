/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.environment.wrapper;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.Enumeration;
import java.util.Map;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.Source;
import org.xml.sax.SAXException;

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
 * @version CVS $Id: MutableEnvironmentFacade.java,v 1.5 2004/02/24 10:33:32 joerg Exp $
 */
public class MutableEnvironmentFacade implements Environment {

    private EnvironmentWrapper env;
    
    // Track the first values set for prefix and uri
    private String prefix = null;
    private String uri = null;
    
    public MutableEnvironmentFacade(EnvironmentWrapper env) {
        this.env = env;
    }
    
    public EnvironmentWrapper getDelegate() {
        return this.env;
    }
    
    public void setDelegate(EnvironmentWrapper env) {
        this.env = env;
    }
    
    //----------------------------------
    // EnvironmentWrapper-specific method (SW:still have to understand why SitemapSource needs them)
    public void setURI(String prefix, String uri) {
        this.env.setURI(prefix, uri);
        
        if (this.uri == null) {
            // First call : keep the values to restore them on the wrapped
            // enviromnent in reset()
            this.prefix = prefix;
            this.uri = uri;
        }
    }

    public void setOutputStream(OutputStream os) {
        this.env.setOutputStream(os);
    }

    public void changeToLastContext() {
        this.env.changeToLastContext();
    }
    
    // Move this to the Environment interface ?
    public String getRedirectURL() {
        return this.env.getRedirectURL();
    }
    
    public void reset() {
        this.env.reset();
        // TODO - If we remove the line below, do we break something
        //        else again? If we leave it in, the SitemapSource
        //        object is unusable after a call to getInputStream()
        //        or toSAX() :(
        //this.env.setURI(this.uri, this.prefix);
    }
    //----------------------------------

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
     * @see org.apache.cocoon.environment.Environment#getRootContext()
     */
    public String getRootContext() {
        return env.getRootContext();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Environment#getContext()
     */
    public String getContext() {
        return env.getContext();
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
     * @see org.apache.cocoon.environment.Environment#setContext(java.lang.String, java.lang.String, java.lang.String)
     */
    public void setContext(String prefix, String uri, String context) {
        env.setContext(prefix, uri, context);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Environment#changeContext(java.lang.String, java.lang.String)
     */
    public void changeContext(String uriprefix, String context) throws Exception {
        env.changeContext(uriprefix, context);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Environment#redirect(boolean, java.lang.String)
     */
    public void redirect(boolean sessionmode, String url) throws IOException {
        env.redirect(sessionmode, url);
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
     * @see org.apache.cocoon.environment.Environment#getOutputStream()
     */
    public OutputStream getOutputStream() throws IOException {
        return env.getOutputStream();
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

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.SourceResolver#resolve(java.lang.String)
     */
    public Source resolve(String systemID) throws ProcessingException, SAXException, IOException {
        return env.resolve(systemID);
    }

    /* (non-Javadoc)
     * @see org.apache.excalibur.source.SourceResolver#resolveURI(java.lang.String)
     */
    public org.apache.excalibur.source.Source resolveURI(String arg0) throws MalformedURLException, IOException {
        return env.resolveURI(arg0);
    }

    /* (non-Javadoc)
     * @see org.apache.excalibur.source.SourceResolver#resolveURI(java.lang.String, java.lang.String, java.util.Map)
     */
    public org.apache.excalibur.source.Source resolveURI(String arg0, String arg1, Map arg2) throws MalformedURLException, IOException {
        return env.resolveURI(arg0, arg1, arg2);
    }

    /* (non-Javadoc)
     * @see org.apache.excalibur.source.SourceResolver#release(org.apache.excalibur.source.Source)
     */
    public void release(org.apache.excalibur.source.Source arg0) {
        env.release(arg0);
    }
}
