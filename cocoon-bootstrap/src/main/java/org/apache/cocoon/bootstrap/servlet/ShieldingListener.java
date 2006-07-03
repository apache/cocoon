/*
 * Copyright 1999-2006 The Apache Software Foundation.
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
package org.apache.cocoon.bootstrap.servlet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSessionActivationListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * This listener can be used as a wrapper around "real" listeners to
 * support the paranoid class loader.
 *
 * @version $Id$
 */
public class ShieldingListener
    implements HttpSessionListener,
               ServletContextListener,
               HttpSessionActivationListener,
               HttpSessionAttributeListener,
               HttpSessionBindingListener,
               ServletContextAttributeListener {

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

    protected ClassLoader classloader;

    protected List httpSessionListeners = new ArrayList();

    protected List servletContextListeners = new ArrayList();

    protected List httpSessionActivationListeners = new ArrayList();

    protected List httpSessionBindingListeners = new ArrayList();

    protected List servletContextAttributeListeners = new ArrayList();

    protected List httpSessionAttributeListeners = new ArrayList();

    protected void init(ServletContext context) {
        // Get the classloader
        try {
            this.classloader = BootstrapClassLoaderManager.getClassLoader(context);
        } catch (ServletException se) {
            throw new RuntimeException("Unable to create bootstrap classloader.", se);
        }

        // Create the listeners
        final ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(this.classloader);
            final String listenersConfig = context.getInitParameter(ShieldingListener.class.getName());
            if ( listenersConfig != null ) {
                final StringTokenizer st = new StringTokenizer(listenersConfig, " \t\r\n\f;,", false);
                while ( st.hasMoreTokens() ) {
                    final String className = st.nextToken();
                    try {
                        context.log("ShieldingListener: Loading listener class " + className);
                        Class listenerClass = this.classloader.loadClass(className);
                        final Object listener = listenerClass.newInstance();
                        if ( listener instanceof HttpSessionListener ) {
                            this.httpSessionListeners.add(listener);
                        }
                        if ( listener instanceof ServletContextListener ) {
                            this.servletContextListeners.add(listener);
                        }
                        if ( listener instanceof HttpSessionActivationListener ) {
                            this.httpSessionActivationListeners.add(listener);
                        }
                        if ( listener instanceof HttpSessionAttributeListener ) {
                            this.httpSessionAttributeListeners.add(listener);
                        }
                        if ( listener instanceof HttpSessionBindingListener ) {
                            this.httpSessionBindingListeners.add(listener);
                        }
                        if ( listener instanceof ServletContextAttributeListener ) {
                            this.servletContextAttributeListeners.add(listener);
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
        if ( this.classloader != null ) {
            final ClassLoader old = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(this.classloader);
                final Iterator i = listeners.iterator();
                while ( i.hasNext() ) {
                    final Object listener = i.next();
                    try {
                        if ( ShieldingListener.SERVLET_CONTEXT_CREATED.equals(identifier) ) {
                            ((ServletContextListener)listener).contextInitialized((ServletContextEvent)event);
                        } else if ( ShieldingListener.SERVLET_CONTEXT_DESTROYED.equals(identifier) ) {
                            ((ServletContextListener)listener).contextDestroyed((ServletContextEvent)event);                            
                        } else if ( ShieldingListener.SESSION_CREATED.equals(identifier) ) {
                            ((HttpSessionListener)listener).sessionCreated((HttpSessionEvent)event);                            
                        } else if ( ShieldingListener.SESSION_DESTROYED.equals(identifier) ) {
                            ((HttpSessionListener)listener).sessionDestroyed((HttpSessionEvent)event);                            
                        } else if ( ShieldingListener.VALUE_BOUND.equals(identifier) ) {
                            ((HttpSessionBindingListener)listener).valueBound((HttpSessionBindingEvent)event);                            
                        } else if ( ShieldingListener.VALUE_UNBOUND.equals(identifier) ) {
                            ((HttpSessionBindingListener)listener).valueUnbound((HttpSessionBindingEvent)event);                            
                        } else if ( ShieldingListener.ATTR_ADDED.equals(identifier) ) {
                            ((HttpSessionAttributeListener)listener).attributeAdded((HttpSessionBindingEvent)event);                            
                        } else if ( ShieldingListener.ATTR_REMOVED.equals(identifier) ) {
                            ((HttpSessionAttributeListener)listener).attributeRemoved((HttpSessionBindingEvent)event);                            
                        } else if ( ShieldingListener.ATTR_REPLACED.equals(identifier) ) {
                            ((HttpSessionAttributeListener)listener).attributeReplaced((HttpSessionBindingEvent)event);                            
                        } else if ( ShieldingListener.CONTEXT_ATTR_ADDED.equals(identifier) ) {
                            ((ServletContextAttributeListener)listener).attributeAdded((ServletContextAttributeEvent)event);                            
                        } else if ( ShieldingListener.CONTEXT_ATTR_REMOVED.equals(identifier) ) {
                            ((ServletContextAttributeListener)listener).attributeRemoved((ServletContextAttributeEvent)event);                            
                        } else if ( ShieldingListener.CONTEXT_ATTR_REPLACED.equals(identifier) ) {
                            ((ServletContextAttributeListener)listener).attributeReplaced((ServletContextAttributeEvent)event);                            
                        } else if ( ShieldingListener.SESSION_ACTIVATED.equals(identifier) ) {
                            ((HttpSessionActivationListener)listener).sessionDidActivate((HttpSessionEvent)event);                            
                        } else if ( ShieldingListener.SESSION_PASSIVATE.equals(identifier) ) {
                            ((HttpSessionActivationListener)listener).sessionWillPassivate((HttpSessionEvent)event);                            
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

    /**
     * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent contextEvent) {
        this.invoke(this.servletContextListeners, ShieldingListener.SERVLET_CONTEXT_DESTROYED, contextEvent);
    }

    /**
     * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent contextEvent) {
        final ServletContext context = contextEvent.getServletContext();
        this.init(context);

        this.invoke(this.servletContextListeners, ShieldingListener.SERVLET_CONTEXT_CREATED, contextEvent);
    }
    
    /**
     * @see javax.servlet.http.HttpSessionListener#sessionCreated(javax.servlet.http.HttpSessionEvent)
     */
    public void sessionCreated(HttpSessionEvent event) {
        this.invoke(this.httpSessionListeners, ShieldingListener.SESSION_CREATED, event);
    }

    /**
     * @see javax.servlet.http.HttpSessionListener#sessionDestroyed(javax.servlet.http.HttpSessionEvent)
     */
    public void sessionDestroyed(HttpSessionEvent event) {
        this.invoke(this.httpSessionListeners, ShieldingListener.SESSION_DESTROYED, event);
    }

    /**
     * @see javax.servlet.http.HttpSessionBindingListener#valueBound(javax.servlet.http.HttpSessionBindingEvent)
     */
    public void valueBound(HttpSessionBindingEvent event) {
        this.invoke(this.httpSessionBindingListeners, ShieldingListener.VALUE_BOUND, event);
    }

    /**
     * @see javax.servlet.http.HttpSessionBindingListener#valueUnbound(javax.servlet.http.HttpSessionBindingEvent)
     */
    public void valueUnbound(HttpSessionBindingEvent event) {
        this.invoke(this.httpSessionBindingListeners, ShieldingListener.VALUE_UNBOUND, event);
    }

    /**
     * @see javax.servlet.http.HttpSessionAttributeListener#attributeAdded(javax.servlet.http.HttpSessionBindingEvent)
     */
    public void attributeAdded(HttpSessionBindingEvent event) {
        this.invoke(this.httpSessionAttributeListeners, ShieldingListener.ATTR_ADDED, event);
    }

    /**
     * @see javax.servlet.http.HttpSessionAttributeListener#attributeRemoved(javax.servlet.http.HttpSessionBindingEvent)
     */
    public void attributeRemoved(HttpSessionBindingEvent event) {
        this.invoke(this.httpSessionAttributeListeners, ShieldingListener.ATTR_REMOVED, event);
    }

    /**
     * @see javax.servlet.http.HttpSessionAttributeListener#attributeReplaced(javax.servlet.http.HttpSessionBindingEvent)
     */
    public void attributeReplaced(HttpSessionBindingEvent event) {
        this.invoke(this.httpSessionAttributeListeners, ShieldingListener.ATTR_REPLACED, event);
    }

    /**
     * @see javax.servlet.http.HttpSessionActivationListener#sessionDidActivate(javax.servlet.http.HttpSessionEvent)
     */
    public void sessionDidActivate(HttpSessionEvent event) {
        this.invoke(this.httpSessionActivationListeners, ShieldingListener.SESSION_ACTIVATED, event);
    }

    /**
     * @see javax.servlet.http.HttpSessionActivationListener#sessionWillPassivate(javax.servlet.http.HttpSessionEvent)
     */
    public void sessionWillPassivate(HttpSessionEvent event) {
        this.invoke(this.httpSessionActivationListeners, ShieldingListener.SESSION_PASSIVATE, event);
    }

    /**
     * @see javax.servlet.ServletContextAttributeListener#attributeAdded(javax.servlet.ServletContextAttributeEvent)
     */
    public void attributeAdded(ServletContextAttributeEvent event) {
        this.invoke(this.servletContextAttributeListeners, ShieldingListener.CONTEXT_ATTR_ADDED, event);
    }

    /**
     * @see javax.servlet.ServletContextAttributeListener#attributeRemoved(javax.servlet.ServletContextAttributeEvent)
     */
    public void attributeRemoved(ServletContextAttributeEvent event) {
        this.invoke(this.servletContextAttributeListeners, ShieldingListener.CONTEXT_ATTR_REMOVED, event);
    }

    /**
     * @see javax.servlet.ServletContextAttributeListener#attributeReplaced(javax.servlet.ServletContextAttributeEvent)
     */
    public void attributeReplaced(ServletContextAttributeEvent event) {
        this.invoke(this.servletContextAttributeListeners, ShieldingListener.CONTEXT_ATTR_REPLACED, event);
    }
}