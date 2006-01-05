/*
 * Copyright 2005 The Apache Software Foundation
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.blocks;

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.cocoon.Processor;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.SourceResolver;

/**
 * @version $Id$
 */
public class BlockDispatcherProcessor extends AbstractLogEnabled implements Processor {

    private Blocks blocks;

    /** Processor attributes */
    private Map processorAttributes = new HashMap();
   
    /**
     * @param blocks
     */
    public BlockDispatcherProcessor(Blocks blocks) {
        this.blocks = blocks;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.Processor#process(org.apache.cocoon.environment.Environment)
     */
    public boolean process(Environment environment) throws Exception {
        String uri = environment.getURI();
        String oldPrefix = environment.getURIPrefix();
        String oldURI = uri;
        // The mount points start with '/' make sure that the URI also
        // does, so that they are compareable.
        if (uri.length() == 0 || uri.charAt(0) != '/') {
            uri = "/" + uri;
        }
        Block block = this.blocks.getMountedBlock(uri);
        if (block == null) {
            return false;
        } else {
            // Resolve the URI relative to the mount point
            String mountPoint = block.getMountPath();
            uri = uri.substring(mountPoint.length());
            getLogger().debug("Enter processing in block at " + mountPoint);
            try {
                environment.setURI("", uri);
                // It is important to set the current block each time
                // a new block is entered, this is used for the block
                // protocol
                BlockEnvironmentHelper.enterBlock(block);
                return block.process(environment);
            } finally {
                BlockEnvironmentHelper.leaveBlock();
                environment.setURI(oldPrefix, oldURI);
                getLogger().debug("Leaving processing in block at " + mountPoint);
            }
        }
    }

    /* (non-Javadoc)
     * Not relevant for the block dispatcher as it don't has an own pipeline
     * @see org.apache.cocoon.Processor#buildPipeline(org.apache.cocoon.environment.Environment)
     */
    public InternalPipelineDescription buildPipeline(Environment environment)
            throws Exception {
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.Processor#getComponentConfigurations()
     */
    public Configuration[] getComponentConfigurations() {
        return null;
    }

    /* (non-Javadoc)
     * The block dispatcher is always the root processor in the processing chain
     * @see org.apache.cocoon.Processor#getRootProcessor()
     */
    public Processor getRootProcessor() {
        return this;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.Processor#getSourceResolver()
     */
    public SourceResolver getSourceResolver() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.Processor#getContext()
     */
    public String getContext() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.Processor#setAttribute(java.lang.String, java.lang.Object)
     */
    public void setAttribute(String name, Object value) {
        this.processorAttributes.put(name, value);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.Processor#getAttribute(java.lang.String)
     */
    public Object getAttribute(String name) {
        return this.processorAttributes.get(name);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.Processor#removeAttribute(java.lang.String)
     */
    public Object removeAttribute(String name) {
        return this.processorAttributes.remove(name);
    }

}
