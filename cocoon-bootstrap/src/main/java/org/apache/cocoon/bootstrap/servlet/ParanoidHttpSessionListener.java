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
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * This listener can be used as a wrapper around "real" listeners to
 * support the paranoid class loader.
 *
 * @version $Id$
 */
public class ParanoidHttpSessionListener
    extends AbstractParanoidListener
    implements HttpSessionListener {

    protected static final String KEY = ParanoidHttpSessionListener.class.getName();

    protected static final String DESTROYED_EVENT = "d";
    protected static final String CREATED_EVENT = "c";

    protected void invokeListener(String identifier, Object listener, Object event) throws Exception {
        final HttpSessionListener l = (HttpSessionListener)listener;
        final HttpSessionEvent e = (HttpSessionEvent)event;
        if ( identifier.equals(DESTROYED_EVENT) ) {
            l.sessionDestroyed(e);
        } else {
            l.sessionCreated(e);
        }
    }

    /**
     * @see javax.servlet.http.HttpSessionListener#sessionCreated(javax.servlet.http.HttpSessionEvent)
     */
    public void sessionCreated(HttpSessionEvent event) {
        final ServletContext context = event.getSession().getServletContext();
        this.init(context, KEY);

        this.invoke(CREATED_EVENT, event);
    }

    /**
     * @see javax.servlet.http.HttpSessionListener#sessionDestroyed(javax.servlet.http.HttpSessionEvent)
     */
    public void sessionDestroyed(HttpSessionEvent event) {
        this.invoke(DESTROYED_EVENT, event);
    }
}