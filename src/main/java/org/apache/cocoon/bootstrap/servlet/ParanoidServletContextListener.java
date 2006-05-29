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

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * This listener can be used as a wrapper around "real" listeners to
 * support the paranoid class loader.
 *
 * @version $Id$
 */
public class ParanoidServletContextListener
    extends AbstractParanoidListener
    implements ServletContextListener {

    protected static final String KEY = ParanoidServletContextListener.class.getName();

    protected static final String DESTROYED_EVENT = "d";
    protected static final String CREATED_EVENT = "c";
    
    /**
     * @see org.apache.cocoon.bootstrap.servlet.AbstractParanoidListener#invokeListener(java.lang.String, java.lang.Object, java.lang.Object)
     */
    protected void invokeListener(String identifier, Object listener, Object event) throws Exception {
        final ServletContextListener l = (ServletContextListener)listener;
        final ServletContextEvent e = (ServletContextEvent)event;
        if ( identifier.equals(DESTROYED_EVENT) ) {
            l.contextDestroyed(e);
        } else {
            l.contextInitialized(e);
        }
    }

    /**
     * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent contextEvent) {
        this.invoke(DESTROYED_EVENT, contextEvent);
    }

    /**
     * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent contextEvent) {
        final ServletContext context = contextEvent.getServletContext();
        this.init(context, KEY);

        this.invoke(CREATED_EVENT, contextEvent);
   }
}