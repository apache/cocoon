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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.cocoon.maven.deployer.monolithic.DevelopmentBlock;
import org.apache.cocoon.maven.deployer.monolithic.DevelopmentProperty;
import org.apache.cocoon.maven.deployer.monolithic.MonolithicCocoonDeployer;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.war.AbstractWarMojo;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.artifact.MavenMetadataSource;

/**
 * Create a Cocoon web application based on a block deployment descriptor.
 * 
 * @version $Id$
 */
abstract class AbstractDeployMojo extends AbstractWarMojo 
{
    /**
     * Artifact factory, needed to download source jars for inclusion in classpath.
     *
     * @component role="org.apache.maven.artifact.factory.ArtifactFactory"
     * @required
     * @readonly
     */
    private ArtifactFactory artifactFactory;	
    
    /**
     * Artifact resolver, needed to download source jars for inclusion in classpath.
     *
     * @component role="org.apache.maven.artifact.resolver.ArtifactResolver"
     * @required
     * @readonly
     */
    private ArtifactResolver artifactResolver;   
    
    /**
     * Artifact resolver, needed to download source jars for inclusion in classpath.
     *
     * @component role="org.apache.maven.artifact.metadata.ArtifactMetadataSource"
     * @required
     * @readonly
     */
    private MavenMetadataSource metadataSource;    
    
    /**
     * Local maven repository.
     *
     * @parameter expression="${localRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository localRepository;    
    
    /**
     * Remote repositories which will be searched for blocks.
     *
     * @parameter expression="${project.remoteArtifactRepositories}"
     * @required
     * @readonly
     */
    private List remoteArtifactRepositories;  
    
    /**
     * The deploy descriptor
     * 
     * @parameter expression="${basedir}/cocoon-deploy.xml"
     */
    private File deploymentDescriptor;     
    
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // set properties: necessary because DeployMojo is not in the same package as AbstractWarMojo
    
    /**
     * The project whose project files to create.
     *
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject project;	    
    
    /**
     * The directory containing generated classes.
     *
     * @parameter expression="${project.build.outputDirectory}"
     * @required
     * @readonly
     */
    private File classesDirectory;

    /**
     * The directory where the webapp is built.
     *
     * @parameter expression="${project.build.directory}/${project.build.finalName}"
     * @required
     */
    private File webappDirectory;

    /**
     * Single directory for extra files to include in the WAR.
     *
     * @parameter expression="${basedir}/src/main/webapp"
     * @required
     */
    private File warSourceDirectory;

    /**
     * The path to the web.xml file to use.
     *
     * @parameter expression="${maven.war.webxml}"
     */
    private String webXml;
    
	/**
	 * Deploy a monolithic Cocoon web application. This means it doesn't use
	 * the features that the blocks-fw offers.
	 */
	protected void deployMonolithicCocoonAppAsWebapp(final String blocksdir)  throws MojoExecutionException {
    	File webappDirectory_ = getWebappDirectory();
    	
    	// build the web application
        this.buildExplodedWebapp(webappDirectory_);
        
        MonolithicCocoonDeployer deployer = new MonolithicCocoonDeployer(this.getLog());        
        deployer.deploy(getBlockArtifactsAsMap(null), webappDirectory_, 
                blocksdir, new DevelopmentBlock[0], new DevelopmentProperty[0]);
        
        // make sure that all configuration files available in the webapp override block configuration files
        try {
            copyResources( getWarSourceDirectory(), webappDirectory_, getWebXml() );
        } catch (IOException e) {
            throw new MojoExecutionException("A problem occurred while copying webapp resources.", e);
        }
        
        // take care of paranoid classloading
        // TBD
	}  
    
    /**
     * Deploy a particular block at development time.
     * 
     * @param blocksdir
     * @param blocks 
     * @param properties 
     * @throws MojoExecutionException
     */
    protected void blockDeploymentMonolithicCocoon(final String blocksdir, final DevelopmentBlock[] blocks, 
            final DevelopmentProperty[] properties) throws MojoExecutionException {
        File webappDirectory_ = getWebappDirectory();        
        
        File webinfDir = new File(webappDirectory_, WEB_INF);
        webinfDir.mkdirs();
        
        // add current block to the development properties
        DevelopmentBlock curBlock = new DevelopmentBlock();
        curBlock.artifactId = this.getProject().getArtifactId();
        curBlock.groupId = this.getProject().getGroupId();
        try {
            curBlock.setLocalPath(this.getProject().getBasedir().getAbsolutePath());
        } catch (FileNotFoundException e) {
            throw new MojoExecutionException("Problems with setting the basedir of this block.", e);
        }
        DevelopmentBlock[] extBlocks = new DevelopmentBlock[blocks.length + 1];
        System.arraycopy(blocks, 0, extBlocks, 0, blocks.length);
        extBlocks[blocks.length] = curBlock;
        
        // deploy all blocks
        MonolithicCocoonDeployer deployer = new MonolithicCocoonDeployer(this.getLog());
        deployer.deploy(getBlockArtifactsAsMap(blocks), webappDirectory_, 
                blocksdir, extBlocks, properties);
    }      
    

    /**
     * Create a <code>Map</code> of <code>java.io.File</code> objects pointing to artifacts.
     */
    private Map getBlockArtifactsAsMap(DevelopmentBlock[] excludedBlocks) throws MojoExecutionException {
        // loop over all artifacts and deploy them correctly
        Map files = new HashMap();
        for(Iterator it = this.getProject().getArtifacts().iterator(); it.hasNext(); ) {
            Artifact artifact = (Artifact) it.next();
            String id = artifact.getArtifactId();
            if(files.containsKey(id)) {
                throw new MojoExecutionException("There are at least two artifacts with the ID '" + id + "'.");
            }
            if(containsArtifact(excludedBlocks, artifact.getArtifactId(), artifact.getGroupId())) {
                this.getLog().debug("Skipping " + artifact.getArtifactId() + ":" + artifact.getGroupId());
            } else {
                files.put(id, artifact.getFile());
            }
        }
        return files;
    }     
    
    /**
     * @return true if the DevelopmentBlock array contains a block with the passed artifactId and groupId
     */
    private boolean containsArtifact(DevelopmentBlock[] blocks, String artifactId, String groupId) {
        if(blocks != null) {
            for(int i = 0; i < blocks.length; i++) {
                if(blocks[i].artifactId.equals(artifactId) && blocks[i].groupId.equals(groupId)) {
                    return true;
                }
            }
        }
        return false;
    }

}