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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.artifact.InvalidDependencyVersionException;
import org.apache.maven.project.artifact.MavenMetadataSource;

/**
 * @goal deploy
 * @requiresProject false
 */
public class DeployMojo extends AbstractMojo {

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
     * The source directory containing *.xsd files
     * 
     * @parameter expression="${basedir}/deploy.xml"
     */
    private String deployDescriptor;    
	
    public void execute() throws MojoExecutionException 
    {
        getLog().info("Cocoon block deployer");
        getLog().info("factory: " + artifactFactory.toString());
        
        Artifact sourceArtifact = artifactFactory.createArtifactWithClassifier( "junit", "junit", "3.8.1", "java-source", "sources" );        
        Set artifacts = null;
        ArtifactResolutionResult result = null;  
        try {
			artifactResolver.resolve( sourceArtifact, remoteArtifactRepositories, localRepository );
			
			// see org.apache.maven.artifact.ant.DependenciesTask
			Dependency dependency = new Dependency();
			dependency.setArtifactId("easymock");
			dependency.setGroupId("easymock");
			dependency.setVersion("1.1");
			Dependency dependency1 = new Dependency();
			dependency1.setArtifactId("commons-dbcp");
			dependency1.setGroupId("commons-dbcp");
			dependency1.setVersion("1.2");
			Dependency dependency2 = new Dependency();
			dependency2.setArtifactId("log4j");
			dependency2.setGroupId("log4j");
			dependency2.setVersion("1.2.8");			
			List dependencies = new ArrayList();
			dependencies.add(dependency);
			dependencies.add(dependency1);
			dependencies.add(dependency2);			

            Artifact pomArtifact = artifactFactory.createBuildArtifact( "log4j", "log4j", "1.2.9", "jar");			
            Map managedDependencies = Collections.EMPTY_MAP;			
            artifacts = MavenMetadataSource.createArtifacts( artifactFactory, dependencies, null, null, null );			
            result = artifactResolver.resolveTransitively( artifacts, pomArtifact, managedDependencies, localRepository,
                    remoteArtifactRepositories, metadataSource);

            
		} catch (ArtifactResolutionException e) {
			e.printStackTrace();
		} catch (ArtifactNotFoundException e) {
			e.printStackTrace();
		} catch (InvalidDependencyVersionException e) {
			e.printStackTrace();
		}

        for ( Iterator i = artifacts.iterator(); i.hasNext(); )
        {
            Artifact artifact = (Artifact) i.next();
            System.out.println("art: " + artifact.getId());
        }		
		
	    for ( Iterator i = result.getArtifacts().iterator(); i.hasNext(); )
        {
            Artifact artifact = (Artifact) i.next();
            System.out.println("res " + artifact.getId());
        }		
		getLog().info("deployDescriptor: " + new File(deployDescriptor).getAbsolutePath());
        getLog().info("sourceArtifact: " + sourceArtifact.toString());
        getLog().info("sourceArtifact path: " + sourceArtifact.getFile().getAbsolutePath());                
        getLog().info("sourceArtifact exists: " + sourceArtifact.getFile().exists());        

    }	
	
}