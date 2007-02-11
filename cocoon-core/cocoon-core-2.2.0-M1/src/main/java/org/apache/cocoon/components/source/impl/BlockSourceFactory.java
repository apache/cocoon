/*
 * Copyright 2006 The Apache Software Foundation.
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
package org.apache.cocoon.components.source.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.Constants;
import org.apache.cocoon.environment.Context;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceFactory;
import org.apache.excalibur.source.SourceResolver;

/**
 * A factory for the block protocol. This is a very simple implementation of the block
 * protocol as it doesn't support polymorphism. There is only a mapping from e.g.
 * block:myblock:abc/x.xml to cocoon://blocks/myblock/abc/x.xml.
 * 
 * @version $Id$
 */
public class BlockSourceFactory extends AbstractLogEnabled implements SourceFactory, Serviceable,
        Contextualizable, ThreadSafe {

    /** The context */
    protected Context envContext;

    /** The ServiceManager */
    protected ServiceManager manager;

    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

    /**
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(org.apache.avalon.framework.context.Context context) throws ContextException {
        this.envContext = (Context) context.get(Constants.CONTEXT_ENVIRONMENT_CONTEXT);
    }

    /**
     * @see org.apache.excalibur.source.SourceFactory#getSource(java.lang.String,
     *      java.util.Map)
     */
    public Source getSource(String location, Map parameters) throws SourceException, MalformedURLException,
            IOException {

        // parse the URL
        String[] locationDetails = getLocationDetails(location);

        // Lookup resolver and resolve URI
        SourceResolver resolver = null;
        Source source = null;
        try {
            resolver = (SourceResolver) this.manager.lookup(SourceResolver.ROLE);
            source = resolver.resolveURI("cocoon://blocks/" + locationDetails[0] + "/" + locationDetails[1]);
        } catch (ServiceException se) {
            throw new SourceException("Unable to lookup source resolver.", se);
        } finally {
            this.manager.release(resolver);
        }
        return source;
    }

    protected static String[] getLocationDetails(final String location) throws MalformedURLException {
        String[] blockDetails = new String[2];
        int pos1 = location.indexOf(":");
        String idAndLocation = location.substring(pos1 + 1);
        int pos2 = idAndLocation.indexOf(":") + pos1 + 1;
        blockDetails[0] = location.substring(pos1 + 1, pos2);
        blockDetails[1] = location.substring(pos2 + 1);
        if (blockDetails[1].indexOf(":") > -1) {
            throw new MalformedURLException(
                    "The block protocol doesn't support other protocols within their location.");
        }
        return blockDetails;
    }

    /**
     * @see org.apache.excalibur.source.SourceFactory#release(org.apache.excalibur.source.Source)
     */
    public void release(Source source) {
        if (null != source) {
            if (this.getLogger().isDebugEnabled()) {
                this.getLogger().debug("Releasing block source " + source.getURI());
            }
            SourceResolver resolver = null;
            try {
                resolver = (SourceResolver) this.manager.lookup(SourceResolver.ROLE);
                resolver.release(source);
            } catch (ServiceException ignore) {
                // we ignore this
            } finally {
                this.manager.release(resolver);
            }
        }
    }

}
