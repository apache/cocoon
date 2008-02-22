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
package org.apache.cocoon.maven.rcl;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * The ReloadingWebappMojo creates a web application environment for a Cocoon
 * block.
 *
 * @goal rcl
 * @requiresProject true
 * @requiresDependencyResolution runtime
 * @execute phase="process-classes"
 * @version $Id$
 * @deprecated Use {@link PrepareWebappMojo} instead.
 */
public class RCLMojo extends PrepareWebappMojo {

    public void execute() throws MojoExecutionException {
        this.getLog().warn(
                        "The 'rcl' goal has been deprecated. "
                                        + "Use the 'prepare' goal to create a web application wrapper for a block instead.");
        super.execute();
    }

}
