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
package org.apache.cocoon.components.modules.input;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;

/**
 * Input module for accessing properties in a properties file.
 * 
 * <p>
 *  The properties file can only be configured statically and
 *  is resolved via the SourceResolver system.
 * </p>
 * 
 * @author <a href="mailto:unico@apache.org">Unico Hommes</a>
 */
public class PropertiesFileModule extends AbstractJXPathModule 
implements InputModule, Serviceable, Configurable, ThreadSafe {
    
    private ServiceManager m_manager;
    
    private SourceResolver m_resolver;
    
    private Properties m_properties;
    
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        m_manager = manager;
        m_resolver = (SourceResolver) m_manager.lookup(SourceResolver.ROLE);
    }
    
	/* (non-Javadoc)
	 * @see org.apache.avalon.framework.activity.Disposable#dispose()
	 */
	public void dispose() {		
		super.dispose();
        if ( this.m_manager != null ) {
            this.m_manager.release( this.m_resolver );
            this.m_manager = null;
            this.m_resolver = null;
        }
	}
    /**
     * Configure the location of the properties file:
     * <p>
     *  <code>&lt;file src="resource://my.properties" /&gt;</code>
     * </p>
     */
    public void configure(Configuration configuration) throws ConfigurationException {
        super.configure(configuration);
        String file = configuration.getChild("file").getAttribute("src");
        load(file);
    }
    
    private void load(String file) throws ConfigurationException {
        Source source = null;
        InputStream stream = null;
        try {
            source = m_resolver.resolveURI(file);
            stream = source.getInputStream();
            m_properties = new Properties();
            m_properties.load(stream);
        }
        catch (IOException e) {
            throw new ConfigurationException("Cannot load properties file " + file);
        }
        finally {
            if (source != null) {
                m_resolver.release(source);
            }
            if (stream != null) {
                try {
                    stream.close();
                }
                catch (IOException e) {
                }
            }
        }
    }
    
    protected Object getContextObject(Configuration modeConf, Map objectModel)
        throws ConfigurationException {
        
        return m_properties;
    }

}
