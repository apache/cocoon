/*
 * Copyright 2006 The Apache Software Foundation.
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
package org.apache.cocoon.maven.deployer;

import org.apache.cocoon.maven.deployer.monolithic.DevelopmentBlock;
import org.apache.cocoon.maven.deployer.monolithic.DevelopmentProperty;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Create a Cocoon web application based on a block deployment descriptor.
 * 
 * @goal deploy
 * @requiresProject true
 * @phase package
 * @requiresDependencyResolution runtime
 * @description Create a Cocoon web application.
 */
public class DeployExplodedMojo extends AbstractDeployMojo {
	
    /** 
     * @parameter expression="${targetVersion}" default-value="2.2"	
     */
	private String serverVersion;

    /** 
     * @parameter expression="${blocksDirectory}" default-value="apps"	
     */
	private String blocksdir;	
    
    /**
     * All blocks that should't be deployed; a path reference is used instead.
     * 
     * @parameter
     */
    private DevelopmentBlock[] blocks;
    
    /**
     * Custom Cocoon properties
     * 
     * @parameter
     */
    private DevelopmentProperty[] properties;        
	
	public void execute() throws MojoExecutionException {
		if(this.serverVersion.equals("2.2")) {
            if(this.blocks == null) {
                this.deployMonolithicCocoonAppAsWebapp(this.blocksdir);
            } else {
                this.blockDeploymentMonolithicCocoon(this.blocksdir, this.blocks, this.properties);
            }
        } else {
            throw new MojoExecutionException("Only version 2.2 is supported.");
        }
	}

}