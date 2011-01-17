/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.maven.deployer;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Create a web application that makes use of Cocoon blocks. In the case of a web application module, (packaging: war)
 * all referenced blocks are added, in the case of block (packaging: jar) additionally a minimal Cocoon environemt is
 * created and it is also possible to reference <i>other</i> locally available blocks and to set custom properties to
 * make development highly dynamic.
 * 
 * @goal deploy
 * @requiresProject true
 * @phase package
 * @requiresDependencyResolution runtime
 */
public class DeployExplodedMojo extends AbstractDeployMojo {

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (this.getProject().getPackaging().equals("war")) {
            this.deployWebapp();
        } else {
            throw new MojoExecutionException("This goal requires a project of packaging type 'war'.");
        }
    }
}