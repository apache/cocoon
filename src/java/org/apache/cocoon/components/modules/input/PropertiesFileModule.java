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
     * @see Serviceable#service(ServiceManager)
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
        if (this.m_manager != null) {
            this.m_manager.release(this.m_resolver);
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
        } catch (IOException e) {
            throw new ConfigurationException("Cannot load properties file " + file);
        } finally {
            if (source != null) {
                m_resolver.release(source);
            }
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    protected Object getContextObject(Configuration modeConf, Map objectModel)
    throws ConfigurationException {

        return m_properties;
    }
}
