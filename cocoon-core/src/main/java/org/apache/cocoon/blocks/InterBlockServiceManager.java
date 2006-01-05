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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;

/**
 * Look for a component in blocks that are connected to the current block.
 * 
 * @version $Id$
 */
public class InterBlockServiceManager extends AbstractLogEnabled implements ServiceManager {
    
    private BlockWiring blockWiring;
    private Blocks blocks;
    private Map managers = new HashMap();
    private boolean called;

    /**
     * @param blockWiring
     * @param blocks
     */
    public InterBlockServiceManager(BlockWiring blockWiring, Blocks blocks) {
        this.blockWiring = blockWiring;
        this.blocks = blocks;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.ServiceManager#lookup(java.lang.String)
     */
    public Object lookup(String role) throws ServiceException {
        ServiceManager manager = this.findServiceManager(role);
        if (manager == null) {
            throw new ServiceException(role, "Could not find any manager in connected blocks that contains the role");
        }
        Object component = manager.lookup(role);
        // Keep track on what manager that was used so that we can return the component
        this.managers.put(component, manager);
        return component;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.ServiceManager#hasService(java.lang.String)
     */
    public boolean hasService(String role) {
        ServiceManager manager = this.findServiceManager(role);
        return manager != null;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.ServiceManager#release(java.lang.Object)
     */
    public void release(Object component) {
        if (component == null)
            return;
        ServiceManager manager = (ServiceManager)this.managers.get(component);
        if (manager != null) {
            manager.release(component);
        }
    }

    
    /**
     * Find a service manager that contains a given role from one of the connected blocks. The component
     * managers of the connected blocks are searched in the order they are declared in the block configuration.
     * The super block, if there is one, is tried after the connected blocks. This is to make certain that
     * a blocks connections is searched before its super blocks connections. 
     * 
     * @param role the role to find a service manager for
     * @return the found service manager or null if not found
     */
    private ServiceManager findServiceManager(String role) {
        this.getLogger().debug("findServiceManager: blockId=" + this.blockWiring.getId() + " role=" + role);
        // FIXME: Called is used for protection about infinite loops for blocks with circular dependencies.
        // It must be made thread safe.
        if (called) {
            return null;
        } else {
            this.called = true;
        }
        ServiceManager manager = null;
        try {
            Enumeration connectionNames = this.blockWiring.getConnectionNames();
            while (connectionNames.hasMoreElements()) {
                String blockName = (String)connectionNames.nextElement();
                String blockId = this.blockWiring.getBlockId(blockName);
                Block block = this.blocks.getBlock(blockId);
                // Don't access blocks that isn't setup yet
                if (block != null) {
                    manager = block.getServiceManager();
                    if (manager != null && manager.hasService(role)) {
                        return manager;
                    }
                } else {
                    this.getLogger().debug("Serching for role=" + role + " in blockId=" + blockId + " that isn't setup.");
                }
            }
            String superId = this.blockWiring.getBlockId(Block.SUPER);
            if (superId != null) {
                Block superBlock = this.blocks.getBlock(superId);
                // Don't access blocks that isn't setup yet
                if (superBlock != null) {
                    manager = superBlock.getServiceManager();
                    if (manager.hasService(role)) {
                        return manager;
                    }
                } else {
                    this.getLogger().debug("Serching for role=" + role + " in blockId=" + superId + " that isn't setup.");
                }
            }
        } finally {
            this.called = false;
        }
        return null;
    }
}
