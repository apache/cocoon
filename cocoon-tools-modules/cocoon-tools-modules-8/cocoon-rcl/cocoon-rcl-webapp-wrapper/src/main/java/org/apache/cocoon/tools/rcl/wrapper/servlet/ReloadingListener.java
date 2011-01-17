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
package org.apache.cocoon.tools.rcl.wrapper.servlet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpSessionActivationListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * This listener can be used as a wrapper around "real" listeners to support the reloading class loader.
 * 
 * @version $Id$
 */
public class ReloadingListener implements HttpSessionListener, ServletContextListener, HttpSessionActivationListener,
HttpSessionAttributeListener, HttpSessionBindingListener, ServletContextAttributeListener, ServletRequestListener {

    private static final String SERVLET_CONTEXT_CREATED = "CLC";

    private static final String SERVLET_CONTEXT_DESTROYED = "CLD";

    private static final String SESSION_CREATED = "SEC";

    private static final String SESSION_DESTROYED = "SED";

    private static final String SESSION_ACTIVATED = "SEAC";

    private static final String SESSION_PASSIVATE = "SEDE";

    private static final String VALUE_BOUND = "VB";

    private static final String VALUE_UNBOUND = "VUB";

    private static final String ATTR_REPLACED = "ARE";

    private static final String ATTR_REMOVED = "ADE";

    private static final String ATTR_ADDED = "AAD";

    private static final String CONTEXT_ATTR_REPLACED = "CARE";

    private static final String CONTEXT_ATTR_REMOVED = "CADE";

    private static final String CONTEXT_ATTR_ADDED = "CAAD";

    private static final String REQUEST_DESTROYED = "RD";

    private static final String REQUEST_INITIALIZED = "RI";

    protected List httpSessionListeners = new ArrayList();

    protected List servletContextListeners = new ArrayList();

    protected List httpSessionActivationListeners = new ArrayList();

    protected List httpSessionBindingListeners = new ArrayList();

    protected List servletContextAttributeListeners = new ArrayList();

    protected List httpSessionAttributeListeners = new ArrayList();

    protected List servletRequestListeners = new ArrayList();

    protected ServletContext context;

    /**
     * @see javax.servlet.http.HttpSessionAttributeListener#attributeAdded(javax.servlet.http.HttpSessionBindingEvent)
     */
    public void attributeAdded(HttpSessionBindingEvent event) {
        this.invoke(this.httpSessionAttributeListeners, ReloadingListener.ATTR_ADDED, event);
    }

    /**
     * @see javax.servlet.ServletContextAttributeListener#attributeAdded(javax.servlet.ServletContextAttributeEvent)
     */
    public void attributeAdded(ServletContextAttributeEvent event) {
        this.invoke(this.servletContextAttributeListeners, ReloadingListener.CONTEXT_ATTR_ADDED, event);
    }

    /**
     * @see javax.servlet.http.HttpSessionAttributeListener#attributeRemoved(javax.servlet.http.HttpSessionBindingEvent)
     */
    public void attributeRemoved(HttpSessionBindingEvent event) {
        this.invoke(this.httpSessionAttributeListeners, ReloadingListener.ATTR_REMOVED, event);
    }

    /**
     * @see javax.servlet.ServletContextAttributeListener#attributeRemoved(javax.servlet.ServletContextAttributeEvent)
     */
    public void attributeRemoved(ServletContextAttributeEvent event) {
        this.invoke(this.servletContextAttributeListeners, ReloadingListener.CONTEXT_ATTR_REMOVED, event);
    }

    /**
     * @see javax.servlet.http.HttpSessionAttributeListener#attributeReplaced(javax.servlet.http.HttpSessionBindingEvent)
     */
    public void attributeReplaced(HttpSessionBindingEvent event) {
        this.invoke(this.httpSessionAttributeListeners, ReloadingListener.ATTR_REPLACED, event);
    }

    /**
     * @see javax.servlet.ServletContextAttributeListener#attributeReplaced(javax.servlet.ServletContextAttributeEvent)
     */
    public void attributeReplaced(ServletContextAttributeEvent event) {
        this.invoke(this.servletContextAttributeListeners, ReloadingListener.CONTEXT_ATTR_REPLACED, event);
    }

    /**
     * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent contextEvent) {
        this.invoke(this.servletContextListeners, ReloadingListener.SERVLET_CONTEXT_DESTROYED, contextEvent);
    }

    /**
     * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent contextEvent) {
        final ServletContext context = contextEvent.getServletContext();
        this.init(context);

        this.invoke(this.servletContextListeners, ReloadingListener.SERVLET_CONTEXT_CREATED, contextEvent);
    }

    /**
     * @see javax.servlet.ServletRequestListener#requestDestroyed(javax.servlet.ServletRequestEvent)
     */
    public void requestDestroyed(ServletRequestEvent event) {
        this.invoke(this.servletRequestListeners, ReloadingListener.REQUEST_DESTROYED, event);
    }

    /**
     * @see javax.servlet.ServletRequestListener#requestInitialized(javax.servlet.ServletRequestEvent)
     */
    public void requestInitialized(ServletRequestEvent event) {
        this.invoke(this.servletRequestListeners, ReloadingListener.REQUEST_INITIALIZED, event);
    }

    /**
     * @see javax.servlet.http.HttpSessionListener#sessionCreated(javax.servlet.http.HttpSessionEvent)
     */
    public void sessionCreated(HttpSessionEvent event) {
        this.invoke(this.httpSessionListeners, ReloadingListener.SESSION_CREATED, event);
    }

    /**
     * @see javax.servlet.http.HttpSessionListener#sessionDestroyed(javax.servlet.http.HttpSessionEvent)
     */
    public void sessionDestroyed(HttpSessionEvent event) {
        this.invoke(this.httpSessionListeners, ReloadingListener.SESSION_DESTROYED, event);
    }

    /**
     * @see javax.servlet.http.HttpSessionActivationListener#sessionDidActivate(javax.servlet.http.HttpSessionEvent)
     */
    public void sessionDidActivate(HttpSessionEvent event) {
        this.invoke(this.httpSessionActivationListeners, ReloadingListener.SESSION_ACTIVATED, event);
    }

    /**
     * @see javax.servlet.http.HttpSessionActivationListener#sessionWillPassivate(javax.servlet.http.HttpSessionEvent)
     */
    public void sessionWillPassivate(HttpSessionEvent event) {
        this.invoke(this.httpSessionActivationListeners, ReloadingListener.SESSION_PASSIVATE, event);
    }

    /**
     * @see javax.servlet.http.HttpSessionBindingListener#valueBound(javax.servlet.http.HttpSessionBindingEvent)
     */
    public void valueBound(HttpSessionBindingEvent event) {
        this.invoke(this.httpSessionBindingListeners, ReloadingListener.VALUE_BOUND, event);
    }

    /**
     * @see javax.servlet.http.HttpSessionBindingListener#valueUnbound(javax.servlet.http.HttpSessionBindingEvent)
     */
    public void valueUnbound(HttpSessionBindingEvent event) {
        this.invoke(this.httpSessionBindingListeners, ReloadingListener.VALUE_UNBOUND, event);
    }

    protected void init(ServletContext context) {
        this.context = context;
        // Create the listeners
        final ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(ReloadingClassloaderManager.getClassLoader(this.context));
            final String listenersConfig = context.getInitParameter(ReloadingListener.class.getName());
            if (listenersConfig != null) {
                final StringTokenizer st = new StringTokenizer(listenersConfig, " \t\r\n\f;,", false);
                while (st.hasMoreTokens()) {
                    final String className = st.nextToken();
                    try {
                        ClassLoader cl = ReloadingClassloaderManager.getClassLoader(this.context);
                        Class listenerClass = cl.loadClass(className);
                        final Object listener = listenerClass.newInstance();
                        if (listener instanceof HttpSessionListener) {
                            this.httpSessionListeners.add(listener);
                        }
                        if (listener instanceof ServletContextListener) {
                            this.servletContextListeners.add(listener);
                        }
                        if (listener instanceof HttpSessionActivationListener) {
                            this.httpSessionActivationListeners.add(listener);
                        }
                        if (listener instanceof HttpSessionAttributeListener) {
                            this.httpSessionAttributeListeners.add(listener);
                        }
                        if (listener instanceof HttpSessionBindingListener) {
                            this.httpSessionBindingListeners.add(listener);
                        }
                        if (listener instanceof ServletContextAttributeListener) {
                            this.servletContextAttributeListeners.add(listener);
                        }
                        if (listener instanceof ServletRequestListener) {
                            this.servletRequestListeners.add(listener);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("Cannot load listener " + className, e);
                    }
                }
            }
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    protected void invoke(List listeners, String identifier, Object event) {
        if (ReloadingClassloaderManager.getClassLoader(this.context) != null) {
            final ClassLoader old = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(ReloadingClassloaderManager.getClassLoader(this.context));
                final Iterator i = listeners.iterator();
                while (i.hasNext()) {
                    final Object listener = i.next();
                    try {
                        if (ReloadingListener.SERVLET_CONTEXT_CREATED.equals(identifier)) {
                            ((ServletContextListener) listener).contextInitialized((ServletContextEvent) event);
                        } else if (ReloadingListener.SERVLET_CONTEXT_DESTROYED.equals(identifier)) {
                            ((ServletContextListener) listener).contextDestroyed((ServletContextEvent) event);
                        } else if (ReloadingListener.SESSION_CREATED.equals(identifier)) {
                            ((HttpSessionListener) listener).sessionCreated((HttpSessionEvent) event);
                        } else if (ReloadingListener.SESSION_DESTROYED.equals(identifier)) {
                            ((HttpSessionListener) listener).sessionDestroyed((HttpSessionEvent) event);
                        } else if (ReloadingListener.VALUE_BOUND.equals(identifier)) {
                            ((HttpSessionBindingListener) listener).valueBound((HttpSessionBindingEvent) event);
                        } else if (ReloadingListener.VALUE_UNBOUND.equals(identifier)) {
                            ((HttpSessionBindingListener) listener).valueUnbound((HttpSessionBindingEvent) event);
                        } else if (ReloadingListener.ATTR_ADDED.equals(identifier)) {
                            ((HttpSessionAttributeListener) listener).attributeAdded((HttpSessionBindingEvent) event);
                        } else if (ReloadingListener.ATTR_REMOVED.equals(identifier)) {
                            ((HttpSessionAttributeListener) listener).attributeRemoved((HttpSessionBindingEvent) event);
                        } else if (ReloadingListener.ATTR_REPLACED.equals(identifier)) {
                            ((HttpSessionAttributeListener) listener).attributeReplaced((HttpSessionBindingEvent) event);
                        } else if (ReloadingListener.CONTEXT_ATTR_ADDED.equals(identifier)) {
                            ((ServletContextAttributeListener) listener).attributeAdded((ServletContextAttributeEvent) event);
                        } else if (ReloadingListener.CONTEXT_ATTR_REMOVED.equals(identifier)) {
                            ((ServletContextAttributeListener) listener).attributeRemoved((ServletContextAttributeEvent) event);
                        } else if (ReloadingListener.CONTEXT_ATTR_REPLACED.equals(identifier)) {
                            ((ServletContextAttributeListener) listener).attributeReplaced((ServletContextAttributeEvent) event);
                        } else if (ReloadingListener.SESSION_ACTIVATED.equals(identifier)) {
                            ((HttpSessionActivationListener) listener).sessionDidActivate((HttpSessionEvent) event);
                        } else if (ReloadingListener.SESSION_PASSIVATE.equals(identifier)) {
                            ((HttpSessionActivationListener) listener).sessionWillPassivate((HttpSessionEvent) event);
                        } else if (ReloadingListener.REQUEST_DESTROYED.equals(identifier)) {
                            ((ServletRequestListener) listener).requestDestroyed((ServletRequestEvent) event);
                        } else if (ReloadingListener.REQUEST_INITIALIZED.equals(identifier)) {
                            ((ServletRequestListener) listener).requestInitialized((ServletRequestEvent) event);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("Cannot invoke listener " + listener, e);
                    }
                }
            } finally {
                Thread.currentThread().setContextClassLoader(old);
            }
        }
    }
}
