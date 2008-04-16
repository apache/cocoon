/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.cocoon.blockdeployment;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;



public class BlockDeploymentServletContextListener implements ServletContextListener {

    public static final String BLOCK_CONTEXT_MAP = BlockDeploymentServletContextListener.class.getName() + "/"
            + "block-context-map";

    public void contextInitialized(ServletContextEvent sce) {
        try {
            ServletContext servletContext = sce.getServletContext();

            Map blocks = DeploymentUtil.deployBlockArtifacts(this.getWorkdir(servletContext).getAbsolutePath());
            servletContext.setAttribute(BLOCK_CONTEXT_MAP, blocks);
        } catch (IOException e) {
            throw new RuntimeException("The available Cocoon blocks can't be deployed.", e);
        }
    }

    public void contextDestroyed(ServletContextEvent sce) {
        sce.getServletContext().removeAttribute(BLOCK_CONTEXT_MAP);
    }

    private File getWorkdir(ServletContext servletContext) {
        File workdir = (File) servletContext.getAttribute("javax.servlet.context.tempdir");
        if (workdir == null) {
            workdir = new File("cocoon-files");
        }

        return workdir;
    }
}
