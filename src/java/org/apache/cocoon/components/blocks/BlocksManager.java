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

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.Processor;
import org.apache.cocoon.components.LifecycleHelper;
import org.apache.cocoon.core.Core;
import org.apache.cocoon.environment.Environment;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;

/**
 * @version $Id$
 */
public class BlocksManager
    extends AbstractLogEnabled
    implements Configurable, Contextualizable, Disposable, Initializable, Serviceable, ThreadSafe, Processor { 

    public static String ROLE = BlocksManager.class.getName();
    private ServiceManager serviceManager;
    private Context context;

    private String wiringFile = null;
    private HashMap blocks = new HashMap();
    private TreeMap mountedBlocks = new TreeMap(new InverseLexicographicalOrder());
    
    private Core core;
    private Processor processor;

    public void service(ServiceManager manager) throws ServiceException {
        this.serviceManager = manager;
        this.core = (Core)this.serviceManager.lookup(Core.ROLE);
    }

    public void contextualize(Context context) throws ContextException {
        this.context = context;
    }

    public void configure(Configuration config)
    throws ConfigurationException {
        this.wiringFile = config.getAttribute("file");
    }

    public void initialize() throws Exception {
        getLogger().debug("Initializing the Blocks Manager");

        SourceResolver resolver = null;
        Source source = null;
        Configuration wiring = null;

        // Read the wiring file
        try {
            resolver = (SourceResolver) this.serviceManager.lookup(SourceResolver.ROLE);
            source = resolver.resolveURI(this.wiringFile);
            DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
            wiring = builder.build(source.getInputStream(), source.getURI());
        } finally {
            if (resolver != null) {
                resolver.release(source);
            }
            this.serviceManager.release(resolver);
        }

        Configuration[] blockConfs = wiring.getChildren("block");

        // Create and store all blocks
        for (int i = 0; i < blockConfs.length; i++) {
            Configuration blockConf = blockConfs[i];
            getLogger().debug("Creating " + blockConf.getName() +
                              " id=" + blockConf.getAttribute("id") +
                              " location=" + blockConf.getAttribute("location"));
            BlockManager blockManager = new BlockManager();
            blockManager.setBlocksManager(this);
            LifecycleHelper.setupComponent(blockManager,
                                           this.getLogger(),
                                           this.context,
                                           this.serviceManager,
                                           blockConf);
            this.blocks.put(blockConf.getAttribute("id"), blockManager);
            String mountPath = blockConf.getChild("mount").getAttribute("path", null);
            if (mountPath != null) {
                this.mountedBlocks.put(mountPath, blockManager);
                getLogger().debug("Mounted block " + blockConf.getAttribute("id") +
                                  " at " + mountPath);
            }
        }
        this.createProcessor();
    }

    public void dispose() {
        Iterator blocksIter = this.blocks.entrySet().iterator();
        while (blocksIter.hasNext()) {
            LifecycleHelper.dispose(blocksIter.next());
        }
        if (this.serviceManager != null) {
            this.serviceManager.release(this.core);
            this.core = null;
            this.serviceManager = null;            
        }
        this.blocks = null;
        this.mountedBlocks = null;
    }

    private void createProcessor() {
        this.processor = new BlockDispatcherProcessor(this);
        ((BlockDispatcherProcessor)this.processor).enableLogging(this.getLogger());
    }
    
    public Block getBlock(String blockId) {
        return (Block)this.blocks.get(blockId);
    }
    
    /**
     * The block with the largest mount point that is a prefix of the URI is
     * chosen. The implementation could be made much more efficient.
     * @param uri
     * @return
     */
    public Block getMountedBlock(String uri) {
        Block block = null;
        // All mount points that are before or equal to the URI in
        // lexicographical order. This includes all prefixes.
        Map possiblePrefixes = this.mountedBlocks.tailMap(uri);
        Iterator possiblePrefixesIt = possiblePrefixes.entrySet().iterator();
        // Find the largest prefix to the uri
        while (possiblePrefixesIt.hasNext()) {
            Map.Entry entry = (Map.Entry) possiblePrefixesIt.next();
            String mountPoint = (String)entry.getKey();
            if (uri.startsWith(mountPoint)) {
                block = (BlockManager)entry.getValue();
                break;
            }
        }
        return block;
    }

    private static class InverseLexicographicalOrder implements Comparator {
        public int compare(Object o1, Object o2) {
            return ((String)o2).compareTo((String)o1);
        }
    }

    // Processor methods
    public boolean process(Environment environment) throws Exception {
        return this.processor.process(environment);
    }

    public InternalPipelineDescription buildPipeline(Environment environment) throws Exception {
        return this.processor.buildPipeline(environment);
    }

    public Configuration[] getComponentConfigurations() {
        return this.processor.getComponentConfigurations();
    }

    public Processor getRootProcessor() {
        return this.processor.getRootProcessor();
    }

    public org.apache.cocoon.environment.SourceResolver getSourceResolver() {
        return this.processor.getSourceResolver();
    }

    public String getContext() {
        return this.processor.getContext();
    }

    public void setAttribute(String name, Object value) {
        this.processor.setAttribute(name, value);
    }

    public Object getAttribute(String name) {
        return this.processor.getAttribute(name);
    }

    public Object removeAttribute(String name) {
        return this.processor.removeAttribute(name);
    }
}
