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
package org.apache.cocoon.environment.wrapper;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.cocoon.components.source.impl.SitemapSourceInfo;
import org.apache.cocoon.environment.AbstractEnvironment;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Response;
import org.apache.cocoon.util.BufferedOutputStream;

/**
 * This is a wrapper class for the <code>Environment</code> object.
 * It has the same properties except that the object model
 * contains a <code>RequestWrapper</code> object.
 *
 * @version $Id$
 */
public class EnvironmentWrapper extends AbstractEnvironment {

    /** The wrapped environment */
    protected final Environment environment;

    /** The request object */
    protected final Request request;

    protected String contentType;

    /** The redirect url */
    protected String redirectURL;

    protected boolean internalRedirect;


    /**
     * Construct a new environment.
     *
     * @param env    The origial Environment
     * @param info   A description of the uri for the new environment
     */
    public EnvironmentWrapper(Environment       env,
                              SitemapSourceInfo info) {
        this(env, info, true);
    }

    /**
     * Construct a new environment.
     *
     * @param env    The origial Environment
     * @param info   A description of the uri for the new environment
     * @param wrapResponse  Whether or not to wrap the Response object
     */
    public EnvironmentWrapper(Environment       env,
                              SitemapSourceInfo info,
                              boolean           wrapResponse) {
        super(env.getURI(), info.view, env.getAction());
        this.environment = env;

        // create new object model
        Map oldObjectModel = env.getObjectModel();
        this.objectModel = new HashMap(oldObjectModel);

        // replace the request object
        this.request = new RequestWrapper(ObjectModelHelper.getRequest(oldObjectModel),
                                          info.requestURI,
                                          info.queryString,
                                          this,
                                          info.rawMode);
        this.objectModel.put(ObjectModelHelper.REQUEST_OBJECT, this.request);

        if (wrapResponse) {
            Response response = new ResponseWrapper(ObjectModelHelper.getResponse(oldObjectModel));
            this.objectModel.put(ObjectModelHelper.RESPONSE_OBJECT, response);
        }

        setURI(info.prefix, info.uri);
    }

    /**
     * @see org.apache.cocoon.environment.Environment#redirect(String, boolean, boolean)
     */
    public void redirect(String newURL, boolean global, boolean permanent)
    throws IOException {
        if (!global && !this.internalRedirect) {
            this.redirectURL = newURL;
        } else {
            this.environment.redirect(newURL, global, permanent);
        }
    }

    /**
     * @see org.apache.cocoon.environment.Environment#getOutputStream(int)
     */
    public OutputStream getOutputStream(int bufferSize)
    throws IOException {
        return this.outputStream == null
                ? this.environment.getOutputStream(bufferSize)
                : this.outputStream;
    }

    /**
     * Set the output stream for this environment. It hides the one of the
     * wrapped environment.
     */
    public void setOutputStream(OutputStream stream) {
        this.outputStream = stream;
    }

    /**
     * @see org.apache.cocoon.environment.Environment#tryResetResponse()
     */
    public boolean tryResetResponse() throws IOException {
        final OutputStream os = getOutputStream(-1);
        if (os instanceof BufferedOutputStream && ((BufferedOutputStream) os).isResettable()) {
            ((BufferedOutputStream) os).reset();
            return true;
        }
        // return false
        return super.tryResetResponse();
    }

    /**
     * @see org.apache.cocoon.environment.Environment#commitResponse()
     */
    public void commitResponse() throws IOException {
        final OutputStream os = getOutputStream(-1);
        if (os != null) {
            os.flush();
        } else {
            // no action
            super.commitResponse();
        }
    }

    /**
     * if a redirect should happen this returns the url,
     * otherwise <code>null</code> is returned
     */
    public String getRedirectURL() {
        return this.redirectURL;
    }

    public void reset() {
        this.redirectURL = null;
    }

    /**
     * Set the StatusCode
     */
    public void setStatus(int statusCode) {
        // ignore this
    }

    public void setContentLength(int length) {
        // ignore this
    }

    /**
     * Set the ContentType
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Get the ContentType
     */
    public String getContentType() {
        return this.contentType;
    }

    /**
     * Lookup an attribute in this instance, and if not found search it
     * in the wrapped environment.
     *
     * @param name a <code>String</code>, the name of the attribute to
     * look for
     * @return an <code>Object</code>, the value of the attribute or
     * null if no such attribute was found.
     */
    public Object getAttribute(String name) {
        Object value = super.getAttribute(name);
        if (value == null)
            value = this.environment.getAttribute(name);

        return value;
    }

    /**
     * Remove attribute from the current instance, as well as from the
     * wrapped environment.
     *
     * @param name a <code>String</code> value
     */
    public void removeAttribute(String name) {
        super.removeAttribute(name);
        this.environment.removeAttribute(name);
    }

    /**
     * Always return <code>false</code>.
     */
    public boolean isExternal() {
        return false;
    }

    public void setInternalRedirect(boolean flag) {
        this.internalRedirect = flag;
        if ( flag ) {
            ((RequestWrapper)this.request).setRequestURI(this.prefix, this.uri);
        }
    }

    /**
     * @see org.apache.cocoon.environment.Environment#isInternalRedirect()
     */
    public boolean isInternalRedirect() {
        return this.internalRedirect;
    }
}
