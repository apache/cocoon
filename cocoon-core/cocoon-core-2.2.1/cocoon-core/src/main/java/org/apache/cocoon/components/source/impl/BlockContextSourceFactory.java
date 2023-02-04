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
package org.apache.cocoon.components.source.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.blockdeployment.BlockDeploymentServletContextListener;
import org.apache.cocoon.util.AbstractLogEnabled;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceFactory;
import org.apache.excalibur.source.SourceResolver;

/**
 * Create a <code>BlockContextSource</code> that makes the resources of a block available.
 * The form of the URL is blockcontext:/blockname/path.
 *
 * @version $Id$
 */
public class BlockContextSourceFactory extends AbstractLogEnabled
                                       implements SourceFactory, Serviceable, ThreadSafe {

    private ServiceManager serviceManager;

    private Map blockContexts;

    /**
     * @see Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager aServiceManager) throws ServiceException {
        this.serviceManager = aServiceManager;
        ServletContext servletContext = (ServletContext) serviceManager.lookup(ServletContext.class.getName());
        this.blockContexts = (Map) servletContext.getAttribute(BlockDeploymentServletContextListener.BLOCK_CONTEXT_MAP);
    }

    /**
     * @see SourceFactory#getSource(java.lang.String, java.util.Map)
     */
    public Source getSource(String location, Map parameters) throws IOException {
        // the root "directory" of the blocks
        if (location.endsWith(":/")) {
            return new BlockContextSource(location, this.blockContexts, this.serviceManager);
        }

        // Remove the protocol and the first '/'
        int pos = location.indexOf(":/");
        String path = location.substring(pos+2);

        pos = path.indexOf('/');
        if (pos != -1) {
            // extract the block name and get the block context path
            String blockName = path.substring(0, pos);
            path = path.substring(pos);
            String blockContext = (String) this.blockContexts.get(blockName);
            if (blockContext == null) {
                throw new MalformedURLException("Unknown block name " + blockName +
                                                " in block context uri " + location);
            }

            // construct the path relative the block context
            String resolvedPath = blockContext + path;
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("block context source " + location + " is resolved to " + resolvedPath);
            }

            SourceResolver resolver = null;
            try {
                resolver = (SourceResolver) this.serviceManager.lookup(SourceResolver.ROLE);
                return resolver.resolveURI(resolvedPath);
            } catch (ServiceException se) {
                throw new SourceException("SourceResolver is not available.", se);
            } finally {
                this.serviceManager.release(resolver);
            }
        } else {
            throw new MalformedURLException("The block name part of a block context uri must end with a '/' in " + location);
        }
    }

    /**
     * @see SourceFactory#release(org.apache.excalibur.source.Source)
     */
    public void release(Source source) {
        // nothing to do
    }
}
