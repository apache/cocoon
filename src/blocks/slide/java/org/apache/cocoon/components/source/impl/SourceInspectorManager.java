/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache Cocoon" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.cocoon.components.source.impl;

import java.util.ArrayList;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.cocoon.components.source.SourceInspector;
import org.apache.cocoon.components.source.helpers.SourceProperty;

import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;

/**
 * This source inspector manage several source inspectors
 *
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @version CVS $Id: SourceInspectorManager.java,v 1.2 2003/03/16 17:49:07 vgritsenko Exp $
 */
public class SourceInspectorManager extends AbstractLogEnabled implements 
    SourceInspector, ThreadSafe, Contextualizable, Composable, Configurable, 
    Initializable, Disposable {

    private ArrayList inspectors = new ArrayList();

    private Context context = null;
    private ComponentManager manager = null;

    /**
     * Get the context
     */
    public void contextualize(Context context) throws ContextException {
        this.context = context;
    }

    public void compose(ComponentManager manager) {
        this.manager = manager;
    }

    /**
     * Pass the Configuration to the Configurable class. This method must 
     * always be called after the constructor and before any other method.
     * 
     * @param configuration the class configurations.
     */
    public void configure(Configuration configuration)
        throws ConfigurationException {

        ClassLoader classloader = Thread.currentThread().getContextClassLoader();

        final Configuration[] configurations = configuration.getChildren("sourceinspector");
        for(int i=0; i<configurations.length; i++) {
            String className = configurations[i].getAttribute( "class", "" );

            SourceInspector inspector = null;

            try {
                final Class inspectorClass = classloader.loadClass(className);
                inspector = (SourceInspector)inspectorClass.newInstance();
            } catch (InstantiationException ie) {
                throw new ConfigurationException("Could not instantiate class "+className, ie);
            } catch (ClassNotFoundException cnfe) {
                throw new ConfigurationException("Could not load class "+className, cnfe);
            } catch (IllegalAccessException iae) {
                 throw new ConfigurationException("Could not load class "+className, iae);
            }

            if (inspector instanceof Configurable) 
                ((Configurable)inspector).configure(configurations[i]);

            try {
                if (inspector instanceof Parameterizable)
                    ((Parameterizable)inspector).parameterize(Parameters.fromConfiguration(configurations[i]));
            } catch (ParameterException pe) {
                throw new ConfigurationException("Could not parameterize inspector", pe);
            }

            inspectors.add(inspector);
        }
    }

    /**
     * Initialialize the component. Initialization includes
     * allocating any resources required throughout the
     * components lifecycle.
     *
     * @throws Exception if an error occurs
     */
    public void initialize() throws Exception {

        SourceInspector inspector;
        for(int i=0; i<this.inspectors.size(); i++) {
            inspector = (SourceInspector)this.inspectors.get(i);

            if (inspector instanceof LogEnabled)
                ((LogEnabled)inspector).enableLogging(getLogger());

            if (inspector instanceof Contextualizable) 
                ((Contextualizable)inspector).contextualize(this.context);

            if (inspector instanceof Composable) 
                ((Composable)inspector).compose(this.manager);

            if (inspector instanceof Initializable) 
                ((Initializable)inspector).initialize();
        }
    }

    /**
     * This method should be implemented to remove all costly resources
     * in object. These resources can be object references, database connections,
     * threads, etc. What is categorised as "costly" resources is determined on
     * a case by case analysis.
     */
    /*public void recycle() {
        SourceInspector inspector;
        for(int i=0; i<this.inspectors.size(); i++) {
            inspector = (SourceInspector)this.inspectors.get(i);
         
            if (inspector instanceof Recyclable)
                ((Recyclable)inspector).recycle();
        }
    }*/

    /**
     * The dispose operation is called at the end of a components lifecycle.
     * This method will be called after Startable.stop() method (if implemented
     * by component). Components use this method to release and destroy any
     * resources that the Component owns.
     */
    public void dispose() {
        SourceInspector inspector;
        for(int i=0; i<this.inspectors.size(); i++) {
            inspector = (SourceInspector)this.inspectors.get(i);
            
            if (inspector instanceof Disposable)
                ((Disposable)inspector).dispose();
        }
    }

    public SourceProperty getSourceProperty(Source source, String namespace, String name) 
        throws SourceException {

        SourceInspector inspector;
        SourceProperty property;
        for(int i=0; i<this.inspectors.size(); i++) {
            inspector = (SourceInspector)this.inspectors.get(i);
      
            property = inspector.getSourceProperty(source, namespace, name);
            if (property!=null)
                return property;
        }
        return null;
    }

    public SourceProperty[] getSourceProperties(Source source) throws SourceException {
        ArrayList list = new ArrayList();

        SourceInspector inspector;
        SourceProperty[] properties;
        SourceProperty property;
        boolean propertyExists;
        for(int i=0; i<this.inspectors.size(); i++) {
            inspector = (SourceInspector)this.inspectors.get(i);

            try {
                properties = inspector.getSourceProperties(source);

                if (properties!=null)
                    for(int j=0; j<properties.length; j++) {
                        propertyExists = false;
                        for(int k=0; k<list.size() && !propertyExists; k++) {
                            property = (SourceProperty)list.get(k);
                            if ((property.getNamespace().equals(properties[j].getNamespace())) &&
                                (property.getName().equals(properties[j].getName())))
                                propertyExists = true;
                    }
                    if (!propertyExists)
                        list.add(properties[j]);
                }
            } catch (SourceException se) {
                getLogger().warn("Couldn't get properties from '"+source.getURI()+"'", se);
            }
        }

        properties = new SourceProperty[list.size()];
        for(int i=0; i<list.size(); i++)
          properties[i] = (SourceProperty)list.get(i);

        return properties;
    }
}

