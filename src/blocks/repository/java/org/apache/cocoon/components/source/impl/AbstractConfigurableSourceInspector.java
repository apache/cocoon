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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.cocoon.components.source.SourceInspector;
import org.apache.cocoon.components.source.helpers.SourceProperty;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;

/**
 * Abstract base class for configurable SourceInspectors.
 * 
 * @author <a href="mailto:unico@apache.org">Unico Hommes</a>
 */
public abstract class AbstractConfigurableSourceInspector extends AbstractLogEnabled 
    implements SourceInspector, Configurable {

    // the set of properties this inspector is configured to handle
    private Set m_properties;
    
    public AbstractConfigurableSourceInspector() {
    }
    
    /**
     * Configure this source inspector to handle properties of required types.
     */
    public void configure(Configuration configuration) throws ConfigurationException {
        final Configuration[] properties = configuration.getChildren("property");
        m_properties = new HashSet(properties.length);
        for (int i = 0; i < properties.length; i++) {
            String namespace = properties[i].getAttribute("namespace");
            String name = properties[i].getAttribute("name");
            if (namespace.indexOf('#') != -1 || name.indexOf('#') != -1) {
                final String message = "Illegal character '#' in definition at " 
                    + properties[i].getLocation();
                throw new ConfigurationException(message);
            }
            m_properties.add(namespace + "#" + name);
        }
    }
    
    public SourceProperty[] getSourceProperties(Source source) throws SourceException {
        final Set result = new HashSet();
        final Iterator properties = m_properties.iterator();
        while (properties.hasNext()) {
            String property = (String) properties.next();
            int index = property.indexOf('#');
            String namespace = property.substring(0,index);
            String name      = property.substring(index+1);
            SourceProperty sp = getSourceProperty(source,namespace,name);
            if (sp != null) {
                result.add(sp);
            }
        }
        return (SourceProperty[]) result.toArray(new SourceProperty[result.size()]);
    }
    
    public final SourceProperty getSourceProperty(Source source, String namespace, String name) 
        throws SourceException {
        
        if (handlesProperty(namespace,name)) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Getting property " + namespace + "#" 
                    + name + " for source " + source.getURI());
            }
            return doGetSourceProperty(source,namespace,name);
        }
        return null;
    }
    
    protected final boolean handlesProperty(String namespace, String name) {
        return m_properties.contains(namespace + "#" + name);
    }
    
    protected abstract SourceProperty doGetSourceProperty(Source source, String ns, String name)
        throws SourceException;
    
    protected final Set getPropertyTypes() {
        return m_properties;
    }
}
