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

import java.util.Iterator;
import java.util.Properties;

import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.services.aspects.BasicAspectContext;

/**
 * Reusable implementation of the {@link BasicAspectContext}.
 *
 * @since 2.2
 * @version $Id$
 */
public abstract class BasicAspectContextImpl implements BasicAspectContext {

    /** The portal service. */
    protected final PortalService portalService;

    /** The iterator used to iterate over the aspects. */
    protected final Iterator iterator;
    /** The iterator used to iterate over the configuration of the aspects. */
    protected final Iterator configIterator;

    /** The current configuration. */
    protected Properties aspectConfig;

    public BasicAspectContextImpl(PortalService service,
                                  AspectChain   chain) {
        this.portalService = service;
        this.iterator = chain.getIterator();
        this.configIterator = chain.getConfigIterator();
    }

    /**
     * @see org.apache.cocoon.portal.services.aspects.BasicAspectContext#getAspectProperties()
     */
    public Properties getAspectProperties() {
        return this.aspectConfig;
    }

    /**
     * @see org.apache.cocoon.portal.services.aspects.BasicAspectContext#getPortalService()
     */
    public PortalService getPortalService() {
        return this.portalService;
    }

    protected Object getNext() {
        if (this.iterator.hasNext()) {
            this.aspectConfig = (Properties)this.configIterator.next();
            return this.iterator.next();
        }
        return null;
    }

}
