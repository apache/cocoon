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

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.excalibur.source.ModifiableTraversableSource;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceFactory;
import org.apache.excalibur.source.SourceResolver;

/**
 * Creates RepositorySources.
 * 
 * @author <a href="unico@apache.org">Unico Hommes</a>
 */
public class RepositorySourceFactory extends AbstractLogEnabled
implements SourceFactory, Serviceable, Configurable, ThreadSafe {
    
    private ServiceManager m_manager;
    private SourceResolver m_resolver;
    private String m_name;
    
    /**
     * Read the <code>name</code> attribute.
     */
    public void configure(final Configuration configuration) 
        throws ConfigurationException {
        
        m_name = configuration.getAttribute("name");
    }
    
    public void service(final ServiceManager manager) throws ServiceException {
        m_manager = manager;
    }
    
    public Source getSource(String location, Map parameters)
        throws IOException, MalformedURLException {
        
        if (m_resolver == null) {
            try {
                m_resolver = (SourceResolver) m_manager.lookup(SourceResolver.ROLE);
            }
            catch (ServiceException e) {
                throw new IOException("Resolver service is not available: " + e.toString());
            }
        }
        
        // assert location.startsWith(m_name)
        location = location.substring(m_name.length()+1);
        Source source = m_resolver.resolveURI(location);
        if (!(source instanceof ModifiableTraversableSource)) {
            final String message = "Delegate should be a ModifiableTraversableSource";
            throw new SourceException(message);
        }
        return new RepositorySource(
            m_name,    
            (ModifiableTraversableSource) source, 
            m_manager,
            getLogger());
    }
    
    public void release(final Source source) {
        if (source instanceof RepositorySource) {
            m_resolver.release(((RepositorySource) source).m_delegate);
        }
    }

}
