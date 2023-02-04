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
package org.apache.cocoon.core.xml.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.xml.sax.EntityResolver;

/**
 * An abstract base class for implementing Jaxp based parsers.
 *
 * @see JaxpDOMParser
 * @see JaxpSAXParser
 *
 * @version $Id$
 * @since 2.2
 */
public abstract class AbstractJaxpParser implements BeanFactoryAware {

    /** By default we use the logger for this class. */
    private Log logger = LogFactory.getLog(getClass());

    /** the Entity Resolver */
    protected EntityResolver resolver;

    /** Do we want to validate? */
    protected boolean validate = false;

    /** Do we search for a resolver if it is not configured? */
    protected boolean searchResolver = true;

    protected Log getLogger() {
        return this.logger;
    }

    public void setLogger(Log l) {
        this.logger = l;
    }

    public void setEntityResolver(EntityResolver r) {
        this.resolver = r;
    }

    public EntityResolver getEntityResolver() {
        return this.resolver;
    }

    /**
     * @see #setValidate(boolean)
     */
    public boolean isValidate() {
        return validate;
    }

    /**
     * should the parser validate parsed documents ?
     * Default is false.
     */
    public void setValidate(boolean validate) {
        this.validate = validate;
    }

    public boolean isSearchResolver() {
        return searchResolver;
    }

    public void setSearchResolver(boolean searchResolver) {
        this.searchResolver = searchResolver;
    }

    /**
     * Load a class
     */
    protected Class loadClass( String name ) throws Exception {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if( loader == null ) {
            loader = getClass().getClassLoader();
        }
        return loader.loadClass( name );
    }

    /**
     * @see org.springframework.beans.factory.BeanFactoryAware#setBeanFactory(org.springframework.beans.factory.BeanFactory)
     */
    public void setBeanFactory(BeanFactory factory) throws BeansException {
        // we search for a resolver if we don't have one already 
        if ( this.resolver == null && this.searchResolver ) {
            if ( this.getLogger().isDebugEnabled() ) {
                this.getLogger().debug("Searching for entity resolver in factory: " + factory);
            }
            if ( factory.containsBean(EntityResolver.class.getName()) ) {
                this.resolver = (EntityResolver) factory.getBean(EntityResolver.class.getName());
                if ( this.getLogger().isDebugEnabled() ) {
                    this.getLogger().debug("Set resolver to: " + this.resolver);
                }
            }
        }
    }
}
