/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.cocoon.maven.test.jetty;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Stop the Jetty instance.
 *
 * @goal jetty-stop
 */
public class JettyStopperMojo extends AbstractMojo {

    /**
     * @parameter
     */
    private boolean skip;

    public void execute() throws MojoExecutionException {
        if (this.skip) {
            this.getLog().info("Skip starting server environment.");
            return;
        }
        try {
            new JettyContainer().stop();
        } catch (Exception e) {
            this.getLog().error("Can't stop JettyContainer. ", e);
        }
        // flush information collected by Cobertura
        try {
            String className = "net.sourceforge.cobertura.coveragedata.ProjectData";
            String methodName = "saveGlobalProjectData";
            Class saveClass = Class.forName(className);
            java.lang.reflect.Method saveMethod = saveClass.getDeclaredMethod(methodName, new Class[0]);
            saveMethod.invoke(null, new Object[0]);
        } catch (Throwable t) {
            this.getLog().debug("Error while flushing information collected by Cobertura.");
        }
    }
}
