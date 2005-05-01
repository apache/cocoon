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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.cocoon.components.container.CocoonServiceManager;
import org.apache.cocoon.core.Core;
import org.apache.cocoon.core.CoreUtil;
import org.apache.cocoon.ProcessingException;
import org.xml.sax.SAXException;

/**
 * @version SVN $Id$
 */
public class BlocksManager
    extends AbstractLogEnabled
    implements Configurable, Contextualizable, Disposable, Initializable, Serviceable, ThreadSafe { 

    public static String ROLE = BlocksManager.class.getName();
    public static String CORE_COMPONENTS_XCONF =
        "resource://org/apache/cocoon/components/blocks/core-components.xconf";
    private ServiceManager serviceManager;
    private SourceResolver resolver;
    private Context context;

    private HashMap blockConfs = new HashMap();
    private HashMap blocks = new HashMap();

    public void service(ServiceManager manager) throws ServiceException {
        this.serviceManager = manager;
        this.resolver = (SourceResolver) this.serviceManager.lookup(SourceResolver.ROLE);
    }

    public void contextualize(Context context) throws ContextException {
        this.context = context;
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
            this.blockConfs.put(block.getAttribute("id"), block);
        }
    }

    public void initialize() throws Exception {
        getLogger().debug("Initializing the Blocks Manager");

        // Create a root service manager for blocks. This should be
        // the minimal number of components that are needed for any
        // block. Only components that not are context dependent
        // should be defined here. Block that depends on e.g. the root
        // context path should be defined in the BlockManager instead.

        Core core = (Core)this.serviceManager.lookup(Core.ROLE);
        ServiceManager blockParentServiceManager =
            new CoreUtil.RootServiceManager(null, core);
        CocoonServiceManager blockServiceManager =
            new CocoonServiceManager(blockParentServiceManager);
        ContainerUtil.enableLogging(blockServiceManager, this.getLogger());
        ContainerUtil.contextualize(blockServiceManager, this.context);

        Source coreComponentsSource =
            this.resolver.resolveURI(CORE_COMPONENTS_XCONF);
        DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
        Configuration coreComponentsConf =
            builder.build( coreComponentsSource.getInputStream() );

        ContainerUtil.configure(blockServiceManager, coreComponentsConf);
        ContainerUtil.initialize(blockServiceManager);

        // Create and store all blocks

        Iterator confIter = this.blockConfs.entrySet().iterator();
        while (confIter.hasNext()) {
            Map.Entry entry = (Map.Entry)confIter.next();
            Configuration blockConf = (Configuration)entry.getValue();
            getLogger().debug("Creating " + blockConf.getName() +
                              " id=" + blockConf.getAttribute("id"));
            BlockManager blockManager = new BlockManager();
            ContainerUtil.enableLogging(blockManager, this.getLogger());
            ContainerUtil.contextualize(blockManager, this.context);
            ContainerUtil.configure(blockManager, blockConf);
            ContainerUtil.initialize(blockManager);
            this.blocks.put(entry.getKey(), blockManager);
        }
    }

    public void dispose() {
        if (this.serviceManager != null) {
            this.serviceManager.release(this.resolver);
            this.resolver = null;
            this.serviceManager = null;
        }
    }
}
