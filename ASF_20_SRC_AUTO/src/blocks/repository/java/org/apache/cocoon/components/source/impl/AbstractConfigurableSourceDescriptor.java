/*
 * Copyright 1999-2004 The Apache Software Foundation.
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

import org.apache.cocoon.components.source.SourceDescriptor;
import org.apache.cocoon.components.source.helpers.SourceProperty;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;

/**
 * Abstract base class SourceDescriptors that want to 
 * configure the set of properties they handle beforehand.
 * 
 * <p>
 * Knowing which properties an inspector handles beforehand
 * greatly improves property management performance.
 * </p> 
 * 
 * @author <a href="mailto:unico@apache.org">Unico Hommes</a>
 */
public abstract class AbstractConfigurableSourceDescriptor 
extends AbstractConfigurableSourceInspector implements SourceDescriptor {


    // ---------------------------------------------------- SourceDescriptor methods

    /**
     * Checks if this SourceDescriptor is configured to handle the 
     * given property and if so forwards the call to 
     * <code>doRemoveSourceProperty()</code>.
     */
    public final void removeSourceProperty(Source source, String namespace, String name)
        throws SourceException {
        
        if (handlesProperty(namespace,name)) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Removing property " + namespace + "#" 
                    + name + " from source " + source.getURI());
            }
            doRemoveSourceProperty(source,namespace,name);
        }
    }

    /**
     * Checks if this SourceDescriptor is configured to handle the 
     * given property and if so forwards the call to 
     * <code>doSetSourceProperty()</code>.
     */
    public final void setSourceProperty(Source source, SourceProperty property) 
        throws SourceException {
        
        if (handlesProperty(property.getNamespace(),property.getName())) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Setting property " + property.getNamespace() + "#" 
                    + property.getName() + " on source " + source.getURI());
            }
            doSetSourceProperty(source,property);
        }
    }

    // ---------------------------------------------------- abstract methods

    /**
     * Do the actual work of removing the given property from the provided Source.
     */
    protected abstract void doRemoveSourceProperty(Source source, String namespace,String name)
        throws SourceException;

    /**
     * Do the actual work of setting the provided SourceProperty on the given Source.
     */
    protected abstract void doSetSourceProperty(Source source, SourceProperty property)
        throws SourceException;

}
