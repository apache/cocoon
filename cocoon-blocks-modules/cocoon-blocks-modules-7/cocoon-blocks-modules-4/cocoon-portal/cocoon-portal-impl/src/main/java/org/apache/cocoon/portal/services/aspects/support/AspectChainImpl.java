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
package org.apache.cocoon.portal.services.aspects.support;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.cocoon.portal.PortalException;
import org.apache.cocoon.portal.services.aspects.AspectChain;

/**
 * Reusable implementation of an aspect chain.
 *
 * @since 2.2
 * @version $Id$
 */
public class AspectChainImpl implements AspectChain {

    /** The aspect class. */
    protected final Class aspectClass;

    /** The aspects. */
    protected final List aspects;

    /** The configuration for the aspects. */
    protected final List configs;

    /** Do we have any aspects? */
    protected boolean process = false;

    public AspectChainImpl(Class aClass, List aspects, List properties)
    throws PortalException {
        this.aspectClass = aClass;
        if ( aspects.size() != properties.size() ) {
            throw new PortalException("Size of aspects list differes from size of properties list for configuring aspect chain (" + aspects.size() + " vs. " + properties.size()+").");
        }
        this.aspects = new ArrayList(aspects.size());
        this.configs = new ArrayList(aspects.size());
        for(int i=0; i<aspects.size(); i++) {
            this.addAspect(aspects.get(i), (Properties)properties.get(i));
        }
    }

    public AspectChainImpl(Class aClass) {
        this.aspectClass = aClass;
        this.aspects = new ArrayList(3);
        this.configs = new ArrayList(3);
    }

    /**
     * @see org.apache.cocoon.portal.services.aspects.AspectChain#getAspectClass()
     */
    public Class getAspectClass() {
        return this.aspectClass;
    }

    /**
     * @see org.apache.cocoon.portal.services.aspects.AspectChain#addAspect(java.lang.Object, java.util.Properties)
     */
    public void addAspect(Object aspect, Properties config)
    throws PortalException {
        this.addAspect(aspect, config, -1);
    }

    /**
     * @see org.apache.cocoon.portal.services.aspects.AspectChain#addAspect(java.lang.Object, java.util.Properties, int)
     */
    public void addAspect(Object aspect, Properties config, int index)
    throws PortalException {
        if ( !this.aspectClass.isInstance(aspect) ) {
            throw new PortalException("Configured aspect is not an instance of class " + this.aspectClass.getName() + " : " + aspect);
        }
        final Properties aspectConfig = (config == null ? EMPTY_PROPERTIES : config);
        this.process = true;
        if ( index == -1 ) {
            this.aspects.add(aspect);
            this.configs.add(aspectConfig);
        } else {
            this.aspects.add(index, aspect);
            this.configs.add(index, aspectConfig);
        }
    }

    /**
     * @see org.apache.cocoon.portal.services.aspects.AspectChain#hasAspects()
     */
    public boolean hasAspects() {
        return this.process;
    }

    /**
     * @see org.apache.cocoon.portal.services.aspects.AspectChain#getAspectsIterator()
     */
    public Iterator getAspectsIterator() {
        return this.aspects.iterator();
    }

    /**
     * @see org.apache.cocoon.portal.services.aspects.AspectChain#getPropertiesIterator()
     */
    public Iterator getPropertiesIterator() {
        return this.configs.iterator();
    }
}
