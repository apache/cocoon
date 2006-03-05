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
import java.io.FileReader;
import java.util.List;

import org.apache.cocoon.deployer.BlockDeployer;
import org.apache.cocoon.deployer.generated.deploy.x10.Deploy;
import org.apache.cocoon.deployer.resolver.NullVariableResolver;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.war.AbstractWarMojo;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.artifact.MavenMetadataSource;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

/**
 * Create a Cocoon web application based on a block deployment descriptor.
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
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~    
	
    protected void deployBlocks() throws MojoExecutionException 
    {
    	File webappDirectory_ = getWebappDirectory();
    	
    	// build the web application without blocks
        this.buildExplodedWebapp(webappDirectory_);
        
        // read the deployment descriptor
    	Deploy deploy;
    	try 
    	{
        	this.getLog().info("using deploymentDescriptor at " + deploymentDescriptor.getAbsolutePath());    		
			deploy = (Deploy) Deploy.unmarshal(new FileReader(deploymentDescriptor));
		} 
    	catch (MarshalException e) 
		{
			throw new MojoExecutionException("The deployment descriptor at '" + deploymentDescriptor.getAbsolutePath() + "' can't be parsed.");
		} 
    	catch (ValidationException e) 
    	{
			throw new MojoExecutionException("The deployment descriptor at '" + deploymentDescriptor.getAbsolutePath() + "' is not valid XML.");
		} 
    	catch (FileNotFoundException e) 
    	{
			throw new MojoExecutionException("The deployment descriptor can't be found at '" + deploymentDescriptor.getAbsolutePath() + "'.");
		}
        
    	// set the target directory 
    	if(webappDirectory_ != null) {
    		String targetUri = "file:///" + webappDirectory_.getAbsolutePath().replaceAll("\\\\", "/");
    		this.getLog().debug("targetUrl: " + targetUri);
    		
    		deploy.getCocoon().setTargetUrl(targetUri);
    		
        	if(deploy.getCocoon().getTargetUrl() != null) {
        		this.getLog().warn("The targetUrl set in the <cocoon> element of '" + deploymentDescriptor.getAbsolutePath() +
        	        "' was overriden by '" + targetUri + "'");
        	}    		
    	}
    	
    	// finally use the block deployer to add blocks to the web app
        BlockDeployer blockDeployer =
            new BlockDeployer(new MavenArtifactProvider(this.artifactResolver, 
            											this.artifactFactory,
                                                        this.localRepository,
                                                        this.remoteArtifactRepositories,
                                                        this.metadataSource, 
                                                        this.getLog()),
                              new NullVariableResolver(),
                              new MavenLoggingWrapper(this.getLog()));
        
        blockDeployer.deploy(deploy, false, true);
        
    }	
    
}