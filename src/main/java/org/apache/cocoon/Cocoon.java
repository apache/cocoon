/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
package org.apache.cocoon;

import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.core.Core;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.environment.internal.EnvironmentHelper;
import org.apache.commons.lang.SystemUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

/**
 * The Cocoon Object is the main Kernel for the entire Cocoon system.
 * 
 * @version $Id$
 */
public class Cocoon implements Processor, BeanFactoryAware {

    /** Active request count. */
    private volatile int activeRequestCount;

    /** The processor used to process the requests. */
    protected final Processor processor;

    /** The environment helper. */
    protected final EnvironmentHelper environmentHelper;

    /** Processor attributes. */
    protected final Map processorAttributes = new HashMap();

    /** The service manager. */
    protected final ServiceManager serviceManager;

    /** The logger. */
    protected final Logger logger;

    /**
     * An optional component that is called before and after processing all
     * requests.
     */
    protected RequestListener requestListener;

    /** Shared Cocoon instance - TODO: we should remove this. */
    static Cocoon instance;

    /**
     * Creates a new <code>Cocoon</code> instance.
     */
    public Cocoon(Processor processor,
                  ServiceManager serviceManager,
                  Context context,
                  Logger logger)
    throws Exception {
        // HACK: Provide a way to share an instance of Cocoon object between
        //       several servlets/portlets.
        Cocoon.instance = this;
        this.processor = processor;
        this.logger = logger;
        this.serviceManager = serviceManager;
        this.environmentHelper = new EnvironmentHelper((URL) context
                .get(ContextHelper.CONTEXT_ROOT_URL));
        ContainerUtil.enableLogging(this.environmentHelper, logger);
        ContainerUtil.service(this.environmentHelper, this.serviceManager);
    }

    /**
     * @see org.springframework.beans.factory.BeanFactoryAware#setBeanFactory(org.springframework.beans.factory.BeanFactory)
     */
    public void setBeanFactory(BeanFactory factory) throws BeansException {
        // get the optional request listener
        if (factory.containsBean(RequestListener.ROLE)) {
            this.requestListener = (RequestListener) factory.getBean(RequestListener.ROLE);
        }
    }

    /**
     * Log debug information about the current environment.
     * 
     * @param environment
     *            an <code>Environment</code> value
     */
    protected void debug(Environment environment, boolean internal) {
        String lineSeparator = SystemUtils.LINE_SEPARATOR;
        Map objectModel = environment.getObjectModel();
        Request request = ObjectModelHelper.getRequest(objectModel);
        Session session = request.getSession(false);
        StringBuffer msg = new StringBuffer();
        msg.append("DEBUGGING INFORMATION:").append(lineSeparator);
        if (internal) {
            msg.append("INTERNAL ");
        }
        msg.append("REQUEST: ").append(request.getRequestURI()).append(lineSeparator).append(
                lineSeparator);
        msg.append("CONTEXT PATH: ").append(request.getContextPath()).append(lineSeparator);
        msg.append("SERVLET PATH: ").append(request.getServletPath()).append(lineSeparator);
        msg.append("PATH INFO: ").append(request.getPathInfo()).append(lineSeparator).append(
                lineSeparator);

        msg.append("REMOTE HOST: ").append(request.getRemoteHost()).append(lineSeparator);
        msg.append("REMOTE ADDRESS: ").append(request.getRemoteAddr()).append(lineSeparator);
        msg.append("REMOTE USER: ").append(request.getRemoteUser()).append(lineSeparator);
        msg.append("REQUEST SESSION ID: ").append(request.getRequestedSessionId()).append(
                lineSeparator);
        msg.append("REQUEST PREFERRED LOCALE: ").append(request.getLocale().toString()).append(
                lineSeparator);
        msg.append("SERVER HOST: ").append(request.getServerName()).append(lineSeparator);
        msg.append("SERVER PORT: ").append(request.getServerPort()).append(lineSeparator).append(
                lineSeparator);

        msg.append("METHOD: ").append(request.getMethod()).append(lineSeparator);
        msg.append("CONTENT LENGTH: ").append(request.getContentLength()).append(lineSeparator);
        msg.append("PROTOCOL: ").append(request.getProtocol()).append(lineSeparator);
        msg.append("SCHEME: ").append(request.getScheme()).append(lineSeparator);
        msg.append("AUTH TYPE: ").append(request.getAuthType()).append(lineSeparator).append(
                lineSeparator);
        msg.append("CURRENT ACTIVE REQUESTS: ").append(activeRequestCount).append(lineSeparator);

        // log all of the request parameters
        Enumeration e = request.getParameterNames();

        msg.append("REQUEST PARAMETERS:").append(lineSeparator).append(lineSeparator);

        while (e.hasMoreElements()) {
            String p = (String) e.nextElement();

            msg.append("PARAM: '").append(p).append("' ").append("VALUES: '");
            String[] params = request.getParameterValues(p);
            for (int i = 0; i < params.length; i++) {
                msg.append("[" + params[i] + "]");
                if (i != (params.length - 1)) {
                    msg.append(", ");
                }
            }

            msg.append("'").append(lineSeparator);
        }

        // log all of the header parameters
        Enumeration e2 = request.getHeaderNames();

        msg.append("HEADER PARAMETERS:").append(lineSeparator).append(lineSeparator);

        while (e2.hasMoreElements()) {
            String p = (String) e2.nextElement();

            msg.append("PARAM: '").append(p).append("' ").append("VALUES: '");
            Enumeration e3 = request.getHeaders(p);
            while (e3.hasMoreElements()) {
                msg.append("[" + e3.nextElement() + "]");
                if (e3.hasMoreElements()) {
                    msg.append(", ");
                }
            }

            msg.append("'").append(lineSeparator);
        }

        msg.append(lineSeparator).append("SESSION ATTRIBUTES:").append(lineSeparator).append(
                lineSeparator);

        // log all of the session attributes
        if (session != null) {
            // Fix bug #12139: Session can be modified while still
            // being enumerated here
            synchronized (session) {
                e = session.getAttributeNames();
                while (e.hasMoreElements()) {
                    String p = (String) e.nextElement();
                    msg.append("PARAM: '").append(p).append("' ").append("VALUE: '").append(
                            session.getAttribute(p)).append("'").append(lineSeparator);
                }
            }
        }

        this.logger.debug(msg.toString());
    }

    /**
     * @see org.apache.cocoon.Processor#process(org.apache.cocoon.environment.Environment)
     */
    public boolean process(Environment environment) throws Exception {
        environment.startingProcessing();
        final int environmentDepth = EnvironmentHelper.markEnvironment();
        EnvironmentHelper.enterProcessor(this, this.serviceManager, environment);
        try {
            boolean result;
            if (this.logger.isDebugEnabled()) {
                ++activeRequestCount;
                this.debug(environment, false);
            }

            if (this.requestListener != null) {
                try {
                    requestListener.onRequestStart(environment);
                } catch (Exception e) {
                    this.logger.error("Error encountered monitoring request start: "
                            + e.getMessage());
                }
            }

            result = this.processor.process(environment);

            if (this.requestListener != null) {
                try {
                    requestListener.onRequestEnd(environment);
                } catch (Exception e) {
                    this.logger.error("Error encountered monitoring request start: "
                            + e.getMessage());
                }
            }

            // commit response on success
            environment.commitResponse();

            return result;
        } catch (Exception any) {
            if (this.requestListener != null) {
                try {
                    requestListener.onRequestException(environment, any);
                } catch (Exception e) {
                    this.logger.error("Error encountered monitoring request start: "
                            + e.getMessage());
                }
            }
            // reset response on error
            environment.tryResetResponse();
            throw any;
        } finally {
            EnvironmentHelper.leaveProcessor();
            environment.finishingProcessing();
            if (this.logger.isDebugEnabled()) {
                --activeRequestCount;
            }
            Core.cleanup();

            EnvironmentHelper.checkEnvironment(environmentDepth, this.logger);
        }
    }

    /**
     * @see org.apache.cocoon.Processor#buildPipeline(org.apache.cocoon.environment.Environment)
     */
    public InternalPipelineDescription buildPipeline(Environment environment) throws Exception {
        try {
            if (this.logger.isDebugEnabled()) {
                ++activeRequestCount;
                debug(environment, true);
            }

            return this.processor.buildPipeline(environment);

        } finally {
            if (this.logger.isDebugEnabled()) {
                --activeRequestCount;
            }
        }
    }

    /**
     * @see org.apache.cocoon.Processor#getComponentConfigurations()
     */
    public Map getComponentConfigurations() {
        return null;
    }

    /**
     * Return this (Cocoon is always at the root of the processing chain).
     * 
     * @since 2.1.1
     */
    public Processor getRootProcessor() {
        return this;
    }

    /**
     * Accessor for active request count
     */
    public int getActiveRequestCount() {
        return activeRequestCount;
    }

    /**
     * @see org.apache.cocoon.Processor#getSourceResolver()
     */
    public org.apache.cocoon.environment.SourceResolver getSourceResolver() {
        return this.environmentHelper;
    }

    /**
     * @see org.apache.cocoon.Processor#getContext()
     */
    public String getContext() {
        return this.environmentHelper.getContext();
    }

    /**
     * @see org.apache.cocoon.Processor#getAttribute(java.lang.String)
     */
    public Object getAttribute(String name) {
        return this.processorAttributes.get(name);
    }

    /**
     * @see org.apache.cocoon.Processor#removeAttribute(java.lang.String)
     */
    public Object removeAttribute(String name) {
        return this.processorAttributes.remove(name);
    }

    /**
     * @see org.apache.cocoon.Processor#setAttribute(java.lang.String,
     *      java.lang.Object)
     */
    public void setAttribute(String name, Object value) {
        this.processorAttributes.put(name, value);
    }
}
