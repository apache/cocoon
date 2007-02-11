/*
 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

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
