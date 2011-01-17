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

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Start a Jetty container to run the Cocoon integration tests.
 * 
 * @goal jetty-start
 */
public class JettyStarterMojo extends AbstractMojo {

    /**
     * The absolute path to the web application under test.
     * 
     * @parameter
     * @required
     */
    private File webAppDirectory;

    /**
     * The HTTP port of the container.
     * 
     * @parameter
     */
    private int port = 8888;

    /**
     * Keep the container running. This keeps the Maven process in a while loop after the container has been started.
     * This is useful for debugging the 'integration-test' phase.
     * 
     * @parameter expression="${keepRunning}"
     */
    private boolean keepRunning = false;

    /**
     * @parameter expression="${project.build.directory}"
     */
    private File builddir;

    /**
     * Don't start the container.
     * 
     * @parameter
     */
    private boolean skip;

    public void execute() throws MojoExecutionException {
        if (this.skip) {
            this.getLog().info("Skip starting server environment.");
            return;
        }
        try {
            System.setProperty("org.apache.cocoon.mode", "dev");
            System.setProperty("net.sourceforge.cobertura.datafile", new File(this.builddir, "cobertura.ser")
                    .getAbsolutePath());
            new JettyContainer().start("/", this.webAppDirectory.getAbsolutePath(), this.port);
            if (this.keepRunning) {
                while (true) {
                    // keep it running
                }
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Can't start Jetty.", e);
        }
    }
}
