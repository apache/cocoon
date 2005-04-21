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
package org.apache.cocoon.components.blocks;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.SitemapComponentTestCase;

public class BlocksManagerTestCase extends SitemapComponentTestCase {

    /**
     * This method should return true if the source factories should
     * be added automatically. Can be overwritten by subclasses. The
     * default is true.
     */
    protected boolean addSourceFactories() {
        return false;
    }
    
    public void testConfigure() throws ServiceException {
        BlocksManager blocks = (BlocksManager)this.lookup(BlocksManager.ROLE);
        this.release(blocks);
    }
}
