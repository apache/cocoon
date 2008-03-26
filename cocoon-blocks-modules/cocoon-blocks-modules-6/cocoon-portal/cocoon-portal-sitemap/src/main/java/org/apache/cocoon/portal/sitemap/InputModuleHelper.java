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
package org.apache.cocoon.portal.sitemap;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolver;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolverFactory;
import org.apache.cocoon.sitemap.PatternException;

/**
 * @version $Id$ 
 */
public class InputModuleHelper {

    /** Service manager. */
    protected final ServiceManager manager;

    protected final Map processedPatterns = new HashMap();

    public InputModuleHelper(ServiceManager serviceManager) {
        this.manager = serviceManager;
    }

    public void dispose() {
        final Iterator i = this.processedPatterns.values().iterator();
        while ( i.hasNext() ) {
            final VariableResolver resolver = (VariableResolver)i.next();
            ContainerUtil.dispose(resolver);
        }
        this.processedPatterns.clear();
    }

    public String resolve(String value) throws ProcessingException {
        VariableResolver resolver = null;
        try {
            resolver = VariableResolverFactory.getResolver( value, this.manager );
            // FIXME - object model is not passed
            return resolver.resolve(null);
        } catch (PatternException e) {
            throw new ProcessingException("Error parsing pattern: " + value, e);
        } finally {
            ContainerUtil.dispose(resolver);
        }
    }

    public VariableResolver getVariableResolver(String value) throws ProcessingException {
        VariableResolver resolver = (VariableResolver)this.processedPatterns.get(value);
        if ( resolver == null ) {
            try {
                resolver = VariableResolverFactory.getResolver( value, this.manager );
                this.processedPatterns.put(value, resolver);
            } catch (PatternException e) {
                throw new ProcessingException("Error parsing pattern: " + value, e);
            }
        }
        return resolver;
    }
}
