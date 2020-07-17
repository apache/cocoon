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
package org.apache.cocoon.faces.context;

import org.apache.cocoon.environment.Context;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Response;
import org.apache.cocoon.faces.FacesAction;
import org.apache.cocoon.faces.FacesRedirector;

import org.apache.commons.collections.iterators.EnumerationIterator;

import javax.faces.context.ExternalContext;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Principal;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of the Java Server Faces ExternalContext
 *
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id$
 */
public class ExternalContextImpl extends ExternalContext {

    private Context context;
    private Request request;
    private Response response;


    public ExternalContextImpl(Context context, Request request, Response response) {
        this.context = context;
        this.request = request;
        this.response = response;
    }

    public void dispatch(String url) throws IOException {
        FacesRedirector redirector = (FacesRedirector)request.getAttribute(FacesAction.REQUEST_REDIRECTOR_ATTRIBUTE);
        if (redirector == null) {
            throw new IOException("Can not dispatch to <" + url + ">: Redirector missing.");
        }

        redirector.dispatch(url);
    }

    public String encodeActionURL(String url) {
        FacesRedirector redirector = (FacesRedirector)request.getAttribute(FacesAction.REQUEST_REDIRECTOR_ATTRIBUTE);
        if (redirector == null) {
            throw new RuntimeException("Can not encode action URL <" + url + ">: Redirector missing.");
        }

        return redirector.encodeActionURL(url);
    }

    public String encodeNamespace(String ns) {
        return ns;
    }

    public String encodeResourceURL(String url) {
        FacesRedirector redirector = (FacesRedirector)request.getAttribute(FacesAction.REQUEST_REDIRECTOR_ATTRIBUTE);
        if (redirector == null) {
            throw new RuntimeException("Can not encode resource URL <" + url + ">: Redirector missing.");
        }

        return redirector.encodeResourceURL(url);
    }

    public Map getApplicationMap() {
        return new ApplicationMap(this.context);
    }

    public String getAuthType() {
        return this.request.getAuthType();
    }

    public Object getContext() {
        return this.context;
    }

    public String getInitParameter(String parameter) {
        return this.context.getInitParameter(parameter);
    }

    public Map getInitParameterMap() {
        return new InitParameterMap(this.context);
    }

    public String getRemoteUser() {
        return this.request.getRemoteUser();
    }

    public Object getRequest() {
        return this.request;
    }

    public String getRequestContextPath() {
        return this.request.getContextPath();
    }

    public Map getRequestCookieMap() {
        // TODO: Implement getRequestCookieMap
        System.err.println("WARNING: getRequestCookieMap called.");
        return Collections.EMPTY_MAP;
    }

    public Map getRequestHeaderMap() {
        return new RequestHeaderMap(this.request);
    }

    public Map getRequestHeaderValuesMap() {
        return new RequestHeaderValuesMap(this.request);
    }

    public Locale getRequestLocale() {
        return this.request.getLocale();
    }

    public Iterator getRequestLocales() {
        return new EnumerationIterator(this.request.getLocales());
    }

    public Map getRequestMap() {
        return new RequestMap(this.request);
    }

    public Map getRequestParameterMap() {
        return new RequestParameterMap(this.request);
    }

    public Iterator getRequestParameterNames() {
        return new EnumerationIterator(this.request.getParameterNames());
    }

    public Map getRequestParameterValuesMap() {
        return new RequestParameterValuesMap(this.request);
    }

    public String getRequestPathInfo() {
        // HACK (VG):
        // Emulate servlet prefix mapping. This allows to bypass suffix mapping calculations,
        // as well as helps with WebSphere servlet container bugs (it treats default servlet
        // as prefix mapped servlet).

        // JSF Specification, 2.2.1 Restore View:
        //   o Derive the view identifier that corresponds to this request, as follows:
        //     o If prefix mapping (such as ?/faces/*?) is used for FacesServlet, the viewId
        //       is set from the extra path information of the request URI.

        StringBuffer path = new StringBuffer();

        boolean slash = false;
        String s = request.getServletPath();
        if (s != null) {
            path.append(s);
            slash = s.endsWith("/");
        }

        s = request.getPathInfo();
        if (s != null) {
            if (s.startsWith("/")) {
                if (slash){
                    s = s.substring(1);
                }
            } else {
                if (!slash) {
                    path.append('/');
                }
            }
            path.append(s);
        }

        return path.toString();
    }

    public String getRequestServletPath() {
        // See #getRequestPathInfo
        return "";
    }

    public URL getResource(String resource) throws MalformedURLException {
        return this.context.getResource(resource);
    }

    public InputStream getResourceAsStream(String resource) {
        return this.context.getResourceAsStream(resource);
    }

    public Set getResourcePaths(String path) {
        // TODO: Implement getResourcePaths
        System.err.println("WARNING: getResourcePaths(" + path + ") called.");
        throw new UnsupportedOperationException();
    }

    public Object getResponse() {
        return this.response;
    }

    public Object getSession(boolean create) {
        return this.request.getSession(create);
    }

    public Map getSessionMap() {
        return new SessionMap(request.getSession());
    }

    public Principal getUserPrincipal() {
        return this.request.getUserPrincipal();
    }

    public boolean isUserInRole(String role) {
        return this.request.isUserInRole(role);
    }

    public void log(String message) {
        // TODO: Implement
        System.err.println("WARNING: log(" + message + ") called.");
    }

    public void log(String message, Throwable e) {
        // TODO: Implement
        System.err.println("WARNING: log(" + message + ", " + e + ") called.");
    }

    public void redirect(String url) throws IOException {
        FacesRedirector redirector = (FacesRedirector)request.getAttribute(FacesAction.REQUEST_REDIRECTOR_ATTRIBUTE);
        if (redirector == null) {
            throw new IOException("Can not redirect to <" + url + ">: Redirector missing.");
        }

        redirector.redirect(url);
    }
}
