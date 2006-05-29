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
import javax.servlet.ServletException;

/**
 * This listener can be used as a wrapper around "real" listeners to
 * support the paranoid class loader.
 *
 * @version $Id$
 */
public abstract class AbstractParanoidListener {

    protected ClassLoader classloader;

    protected List listeners = new ArrayList();

    protected void init(ServletContext context, String key) {
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
            final String listenersConfig = context.getInitParameter(key);
            if ( listenersConfig != null ) {
                final StringTokenizer st = new StringTokenizer(listenersConfig, " \t\r\n\f;,", false);
                while ( st.hasMoreTokens() ) {
                    final String className = st.nextToken();
                    try {    
                        Class listenerClass = this.classloader.loadClass(className);
                        final Object listener = listenerClass.newInstance();
                        this.listeners.add(listener);
                    } catch (Exception e) {
                        throw new RuntimeException("Cannot load listener " + className, e);
                    }
                }
            }
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    protected void invoke(String identifier, Object event) {
        if ( this.classloader != null ) {
            final ClassLoader old = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(this.classloader);
                final Iterator i = this.listeners.iterator();
                while ( i.hasNext() ) {
                    final Object listener = i.next();
                    try {
                    } catch (Exception e) {
                        throw new RuntimeException("Cannot invoke listener " + listener, e);
                    }
                }
            } finally {
                Thread.currentThread().setContextClassLoader(old);
            }
        }
    }

    protected abstract void invokeListener(String identifier, Object listener, Object event)
    throws Exception;
}