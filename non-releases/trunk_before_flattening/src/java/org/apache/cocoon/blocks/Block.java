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
package org.apache.cocoon.blocks;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.Processor;

/**
 * @version $Id$
 */
public interface Block extends Processor { 

    public static String NAME = Block.class.getName() + "-name";
    public static String SUPER = "super";

    /**
     * Get the mount path of the block
     */
    public String getMountPath();

    /**
     * Get a block property
     */
    public String getProperty(String name);

    // TODO: We should have a reflection friendly Map getProperties() also

    /**
     * The exported components of the block. Return null if the block doesn't export components.
     * 
     * @return a ServiceManager containing the blocks exported components
     */
    public ServiceManager getServiceManager();
    
    /**
     * Takes the scheme specific part of a block URI (the scheme is
     * the responsibilty of the BlockSource) and resolve it with
     * respect to the blocks mount point.
     */
    public URI absolutizeURI(URI uriToResolve, URI base) throws URISyntaxException;

    /**
     * Parses and resolves the scheme specific part of a block URI
     * with respect to the base URI of the current sitemap. The scheme
     * specific part of the block URI has the form
     * <code>foo:/bar</code> when refering to another block, in this
     * case only an absolute path is allowed. For reference to the own
     * block, both absolute <code>/bar</code> and relative
     * <code>./foo</code> paths are allowed.
     */
    public URI resolveURI(URI uriToResolve, URI base) throws URISyntaxException;
}
