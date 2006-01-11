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

import javax.servlet.Servlet;

import org.apache.avalon.framework.service.ServiceManager;

/**
 * @version $Id$
 */
public interface Block extends Servlet { 

    public static String NAME = Block.class.getName() + "-name";
    public static String SUPER = "super";

    /**
     * Get the mount path of the block
     */
    public String getMountPath();

    /**
     * The exported components of the block. Return null if the block doesn't export components.
     * 
     * @return a ServiceManager containing the blocks exported components
     */
    public ServiceManager getServiceManager();
    
    public Servlet getBlockServlet();
}
