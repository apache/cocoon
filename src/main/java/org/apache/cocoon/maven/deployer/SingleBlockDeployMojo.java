/*
 * Copyright 2002-2005 The Apache Software Foundation
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.maven.deployer;

import java.util.List;

import org.apache.cocoon.deployer.BlockDeployer;
import org.apache.cocoon.deployer.generated.deploy.x10.Block;
import org.apache.cocoon.deployer.generated.deploy.x10.Cocoon;
import org.apache.cocoon.deployer.generated.deploy.x10.Deploy;
import org.apache.cocoon.deployer.generated.deploy.x10.Mount;
import org.apache.cocoon.deployer.resolver.NullVariableResolver;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.artifact.MavenMetadataSource;


/**
 * This Mojo is used to deploy a single Cocoon block, usually just at development time. All block dependencies
 * are resolved automatically (auto-wiring is true). If you need more, use the @link org.apache.cocoon.maven.deployer.DeployMojo
 * that requires a deployment configuration file.
 *
 * @goal simple-deploy
 * @requiresProject true
 * @description
 * @execute phase="compile"
 */
public class SingleBlockDeployMojo
    extends AbstractMojo {
    //~ Instance fields ---------------------------------------------------------------------------------

    /**
     * The project whose project files to create.
     *
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject project;	
	
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

    //~ Methods -----------------------------------------------------------------------------------------

    /**
     * Create a minimal web application based on the current block
     *
     * @throws MojoExecutionException if any problem occurs while executing this Mojo
     */
    public void execute()
        throws MojoExecutionException {
        
    	// collecting information from POM
        String projectName = this.project.getName();
        String artifactId = this.project.getArtifactId();
        String groupId = this.project.getGroupId();        
        String version = this.project.getVersion();
        
        String urn = groupId + ":" + artifactId + ":" + version;
        
        // inform user what's happening
        this.getLog().info("Start deployment of '" + projectName + "'");
        this.getLog().info("[" + urn + "]");        

        // create the Cocoon object
        Cocoon cocoon = new Cocoon();
        cocoon.setExclusive(true);
        cocoon.setTargetUrl("target/cocoon-webapp");
        cocoon.setWebappUrn("org.apache.cocoon:cocoon-deployer-minimal-webapp:1.0-SNAPSHOT:war");
        cocoon.setBlockFwUrn("org.apache.cocoon:cocoon-default:1.0-SNAPSHOT");
        
        // create the block
        Block block = new Block();
        block.setId(artifactId);
        block.setAutoWire(true);
        Mount mount = new Mount();
        mount.setPath("/");
        block.setMount(mount);
        
        block.setUrn(urn);
        // needs to be commented out as long as the blocks-fw supports directories outside
        // of the servlet context
        block.setLocation("target/classes");
                
        // create the deployment object
        Deploy deploy = new Deploy();
        deploy.setCocoon(cocoon);
        deploy.addBlock(block);
        
        BlockDeployer blockDeployer =
            new BlockDeployer(new MavenArtifactProvider(this.artifactResolver, 
            											this.artifactFactory,
                                                        this.localRepository,
                                                        this.remoteArtifactRepositories,
                                                        this.metadataSource, 
                                                        this.getLog()),
                              new NullVariableResolver(),
                              new MavenLoggingWrapper(this.getLog()));
        
        blockDeployer.deploy(deploy);
        
    }
}
