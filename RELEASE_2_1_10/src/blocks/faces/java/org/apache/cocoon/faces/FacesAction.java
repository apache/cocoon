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
package org.apache.cocoon.faces;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.cocoon.CascadingIOException;
import org.apache.cocoon.Constants;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.acting.Action;
import org.apache.cocoon.environment.Context;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Response;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.environment.portlet.ActionResponse;
import org.apache.cocoon.environment.portlet.PortletResponse;

import javax.faces.FacesException;
import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import javax.faces.webapp.FacesServlet;
import java.io.IOException;
import java.util.Map;

/**
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id$
 */
public class FacesAction extends AbstractLogEnabled
                         implements Action, ThreadSafe, Contextualizable,
                                    Configurable, Initializable {

    public static final String REQUEST_REDIRECTOR_ATTRIBUTE = "org.apache.cocoon.faces.REDIRECTOR";

    private Context context;

    private String cutPrefix;
    private String cutSuffix;
    private String addPrefix;
    private String addSuffix;

    private FacesContextFactory facesContextFactory;
    private Application application;
    private Lifecycle lifecycle;


    /**
     *
     */
    class RedirectorImpl implements FacesRedirector {
        private Redirector redirector;
        private Request request;
        private Response response;

        RedirectorImpl (Redirector redirector, Request request, Response response) {
            this.redirector = redirector;
            this.request = request;
            this.response = response;
        }

        public void dispatch(String url) throws IOException {
            // System.err.println("INFO: Dispatching to " + url);
            try {
                // TODO: HACK: Dependency on ActionResponse
                if (response instanceof ActionResponse) {
                    // Can't render response. Redirect to another face.
                    redirector.redirect(true, url);
                } else {
                    // Need to render face. Convert face URL to view URL.
                    int begin = 0;
                    int end = url.length();

                    if (cutPrefix != null && url.startsWith(cutPrefix)) {
                        begin = cutPrefix.length();
                    }
                    if (cutSuffix != null && url.endsWith(cutSuffix)) {
                        end = end - cutSuffix.length();
                    }

                    StringBuffer buffer = new StringBuffer();
                    if (addPrefix != null) {
                        buffer.append(addPrefix);
                    }
                    if (begin < end) {
                        buffer.append(url.substring(begin, end));
                    }
                    if (addSuffix != null) {
                        buffer.append(addSuffix);
                    }
                    url = buffer.toString();

                    // System.err.println("INFO: Dispatching to view " + url);
                    redirector.redirect(true, "cocoon:/" + url);
                }
            } catch (Exception e) {
                throw new CascadingIOException(e);
            }
        }

        public void redirect(String url) throws IOException {
            // System.err.println("redirect: " + url);
            try {
                redirector.redirect(true, url);
            } catch (Exception e) {
                throw new CascadingIOException(e);
            }
        }

        public String encodeActionURL(String url) {
            // System.err.println("encodeActionURL: " + url);
            // TODO: HACK: Dependency on PortletResponse
            if (response instanceof PortletResponse) {
                final String context = request.getContextPath();

                if (url.startsWith(context)) {
                    url = url.substring(context.length());
                    // System.err.println("encodeActionURL: cut: " + url);
                }

                return "portlet:action:" + response.encodeURL(url);
            } else {
                return response.encodeURL(url);
            }
        }

        public String encodeResourceURL(String url) {
            // System.err.println("encodeResourceURL: " + url);
            return response.encodeURL(url);
        }
    }


    public void contextualize(org.apache.avalon.framework.context.Context avalonContext) throws ContextException {
        context = (Context) avalonContext.get(Constants.CONTEXT_ENVIRONMENT_CONTEXT);
    }

    public void configure(Configuration configuration) throws ConfigurationException {
        this.cutPrefix = configuration.getChild("cut-prefix").getValue(null);
        this.cutSuffix = configuration.getChild("cut-suffix").getValue(".faces");
        this.addPrefix = configuration.getChild("add-prefix").getValue(null);
        this.addSuffix = configuration.getChild("add-suffix").getValue(".view");
    }

    public void initialize() throws Exception {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Initializing FacesAction");
        }

        facesContextFactory = (FacesContextFactory) FactoryFinder.getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);
        // facesContextFactory = new FacesContextFactoryImpl();

        ApplicationFactory applicationFactory = (ApplicationFactory) FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY);
        application = applicationFactory.getApplication();
        // application.setDefaultRenderKitId("COCOON_BASIC_XHTML");

        LifecycleFactory lifecycleFactory = (LifecycleFactory) FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
        String lifecycleID = context.getInitParameter(FacesServlet.LIFECYCLE_ID_ATTR);
        if (lifecycleID == null) {
            lifecycleID = "DEFAULT";
        }
        lifecycle = lifecycleFactory.getLifecycle(lifecycleID);

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Faces context factory is " + facesContextFactory.getClass().getName());
            getLogger().debug("Faces application factory is " + applicationFactory.getClass().getName());
            getLogger().debug("Faces lifecycle factory is " + lifecycleFactory.getClass().getName());
        }
    }

    public Map act(Redirector redirector,
                   SourceResolver resolver,
                   Map objectModel,
                   String source,
                   Parameters parameters)
    throws Exception {
        Request request = ObjectModelHelper.getRequest(objectModel);
        Response response = ObjectModelHelper.getResponse(objectModel);

        // Pass FacesRedirector to the FacesContext implementation.
        request.setAttribute(REQUEST_REDIRECTOR_ATTRIBUTE, new RedirectorImpl(redirector, request, response));

        FacesContext context = facesContextFactory.getFacesContext(this.context, request, response, lifecycle);
        try {
            lifecycle.execute(context);
            lifecycle.render(context);

            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Request processed; View root ID: " + context.getViewRoot().getId());
            }
        } catch (FacesException e) {
            throw new ProcessingException("Failed to process faces request", e);
        } finally {
            context.release();
        }

        return null;
    }
}
