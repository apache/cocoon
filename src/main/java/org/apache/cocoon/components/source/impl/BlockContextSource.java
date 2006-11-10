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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.TraversableSource;
import org.apache.excalibur.source.impl.AbstractSource;

/**
 * This source makes the immediate children to the blockcontext:/ uri
 * traversable
 * 
 * @version $Id$
 */
public class BlockContextSource
    extends AbstractSource
    implements TraversableSource {
    
    private Map blockContexts;
    private ServiceManager manager;

    /**
     * 
     */
    public BlockContextSource(String location, Map blockContexts, ServiceManager manager) {
        this.setScheme(location.substring(0, location.indexOf(":/")));
        this.setSystemId(location);
        this.blockContexts = blockContexts;
        this.manager = manager;
    }

    /* (non-Javadoc)
     * @see org.apache.excalibur.source.Source#exists()
     */
    public boolean exists() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.apache.excalibur.source.TraversableSource#getChild(java.lang.String)
     */
    public Source getChild(String name) throws SourceException {
        String blockContext = (String) this.blockContexts.get(name);
        if (blockContext == null)
            throw new SourceException("The block named " + name + "doesn't exist");

        SourceResolver resolver = null;
        try {
            resolver = (SourceResolver) this.manager.lookup(SourceResolver.ROLE);
            return resolver.resolveURI(blockContext);
        } catch (ServiceException se) {
            throw new SourceException("SourceResolver is not available.", se);
        } catch (IOException e) {
            throw new SourceException("The source for block " + name +
                    " could not be resolved ", e);
        } finally {
            this.manager.release(resolver);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.excalibur.source.TraversableSource#getChildren()
     */
    public Collection getChildren() throws SourceException {
        Collection contextPaths = this.blockContexts.values();
        ArrayList children = new ArrayList(contextPaths.size());
        SourceResolver resolver = null;
        String contextPath = null;
        try {
            resolver = (SourceResolver) this.manager.lookup(SourceResolver.ROLE);
            Iterator i = contextPaths.iterator();
            while (i.hasNext()) {
                contextPath = (String) i.next();
                Source child = resolver.resolveURI(contextPath);
                children.add(child);
            }
            return children;
        } catch (ServiceException se) {
            throw new SourceException("SourceResolver is not available.", se);
        } catch (IOException e) {
            throw new SourceException("The block context path " + contextPath +
                    " could not be resolved ", e);
        } finally {
            this.manager.release(resolver);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.excalibur.source.TraversableSource#getName()
     */
    public String getName() {
        return ".";
    }

    /* (non-Javadoc)
     * @see org.apache.excalibur.source.TraversableSource#getParent()
     */
    public Source getParent() throws SourceException {
        // this is the root node so there is no parent
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.excalibur.source.TraversableSource#isCollection()
     */
    public boolean isCollection() {
        // this source is always a directory
        return true;
    }

}
