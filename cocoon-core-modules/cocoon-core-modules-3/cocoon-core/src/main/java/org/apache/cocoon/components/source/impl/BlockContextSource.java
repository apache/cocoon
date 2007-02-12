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
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.CascadingIOException;
import org.apache.commons.collections.map.HashedMap;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.TraversableSource;
import org.apache.excalibur.source.impl.AbstractSource;
import org.springframework.util.ClassUtils;

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
    private Map children;

    /**
     * @throws IOException 
     * @throws MalformedURLException 
     * 
     */
    public BlockContextSource(String location, Map blockContexts, ServiceManager manager)
    throws MalformedURLException, IOException {
        this.setScheme(location.substring(0, location.indexOf(":/")));
        this.setSystemId(location);
        this.blockContexts = blockContexts;
        this.manager = manager;
        this.children = this.createChildren();
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
        Source child = (Source) this.children.get(name);
        if (child == null)
            throw new SourceException("The block named " + name + "doesn't exist");
        return child;
    }

    /* (non-Javadoc)
     * @see org.apache.excalibur.source.TraversableSource#getChildren()
     */
    public Collection getChildren() throws SourceException {
        return this.children.values();
    }

    private Map createChildren() throws MalformedURLException, IOException  {
        Map children = new HashedMap(this.blockContexts.size() * 2 + 1);
        SourceResolver resolver = null;
        try {
            resolver = (SourceResolver) this.manager.lookup(SourceResolver.ROLE);
            Iterator i = this.blockContexts.entrySet().iterator();
            while (i.hasNext()) {
                Entry entry = (Entry) i.next();
                String blockName = (String) entry.getKey();
                String contextPath = (String) entry.getValue();
                Source child = resolver.resolveURI(contextPath);
                if (child instanceof TraversableSource)
                    child = adjustName((TraversableSource) child, blockName);
                children.put(blockName, child);
            }
            return children;
        } catch (ServiceException se) {
            throw new CascadingIOException("SourceResolver is not available.", se);
        } finally {
            this.manager.release(resolver);
        }
    }
    
    /*
     * Adjust the traversable source so that it use the block name as name instead of
     * e.g. the directory name from the source that the block name was resolved to.
     */
    private static Source adjustName(final TraversableSource source, final String blockName) {
        TraversableSource adjustedSource =
            (TraversableSource) Proxy.newProxyInstance(source.getClass().getClassLoader(), ClassUtils.getAllInterfaces(source),
                new InvocationHandler() {

                    /* (non-Javadoc)
                     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
                     */
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if (method.equals(TraversableSource.class.getMethod("getName", new Class[]{})))
                            return blockName;
                        else
                            return method.invoke(source, args);
                    }
        });
        return adjustedSource;
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
