/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
package org.apache.cocoon.components.blocks;

import java.io.IOException;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.cocoon.ProcessingException;
import org.xml.sax.SAXException;

/**
 * @version SVN $Id$
 */
public class BlocksManager
    extends AbstractLogEnabled
    implements Configurable, Disposable, Serviceable, ThreadSafe { 

    public static String ROLE = BlocksManager.class.getName();

    private ServiceManager manager;
    private SourceResolver resolver;

    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
        this.resolver = (SourceResolver) this.manager.lookup(SourceResolver.ROLE);
    }

    public void configure(Configuration config)
    throws ConfigurationException {
        String file = config.getAttribute("file");
        Source source = null;
        Configuration wiring = null;

        // Read the wiring file
        try {
            source = this.resolver.resolveURI(file);
            DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
            wiring = builder.build( source.getInputStream() );
        } catch (SAXException se) {
            String msg = "SAXException while reading " + file + ": " + se.getMessage();
            throw new ConfigurationException(msg, se);
        } catch (IOException ie) {
              String msg = "IOException while reading " + file + ": " + ie.getMessage();
              throw new ConfigurationException(msg, ie);
        } finally {
            this.resolver.release(source);
        }
        Configuration[] blocks = wiring.getChildren("block");
        for (int i = 0; i < blocks.length; i++) {
            Configuration block = blocks[i];
            getLogger().debug("BlocksManager configure: " + block.getName() +
                              " id=" + block.getAttribute("id") +
                              " location=" + block.getAttribute("location"));
        }
    }

    public void dispose() {
        if (this.manager != null) {
            this.manager.release(this.resolver);
            this.resolver = null;
            this.manager = null;
        }
    }
}
