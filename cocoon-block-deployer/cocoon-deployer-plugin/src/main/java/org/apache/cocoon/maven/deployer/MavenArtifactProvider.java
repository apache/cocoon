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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.cocoon.deployer.ArtifactProvider;
import org.apache.commons.lang.Validate;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.artifact.MavenMetadataSource;


/**
 * Implementation of a {@link ArtifactProvider} to bridge from Maven 2
 */
public final class MavenArtifactProvider
    implements ArtifactProvider {
    //~ Instance fields ---------------------------------------------------------------------------------

    /** A field */
    private final Log log;

    /** Artifact factory, needed to download source jars for inclusion in classpath. */
    private ArtifactFactory artifactFactory;

    /** Local maven repository. */
    private ArtifactRepository localRepository;

    /** Artifact resolver, needed to download source jars for inclusion in classpath. */
    private ArtifactResolver artifactResolver;

    /** Remote repositories which will be searched for blocks. */
    private List remoteArtifactRepositories;

    /** Artifact resolver, needed to download source jars for inclusion in classpath. */
    private MavenMetadataSource metadataSource;

    //~ Constructors ------------------------------------------------------------------------------------

    /**
     * Creates a new MavenArtifactProvider object.
     *
     * @param artifactResolver - used to get Maven artifacts from the repositories
     * @param artifactFactory - used to create Maven artifact skeletons
     * @param localRepository - get access to the user's local repo
     * @param remoteArtifactRepositories - get access to all configured remote repositories
     * @param metadataSource - used to resolve transitive dependencies
     * @param log - get access to the logger
     */
    public MavenArtifactProvider(final ArtifactResolver artifactResolver,
                                 final ArtifactFactory artifactFactory,
                                 final ArtifactRepository localRepository,
                                 final List remoteArtifactRepositories,
                                 final MavenMetadataSource metadataSource,
                                 final Log log) {
        super();
        this.log = log;
        this.artifactFactory = artifactFactory;
        this.artifactResolver = artifactResolver;
        this.remoteArtifactRepositories = remoteArtifactRepositories;
        this.metadataSource = metadataSource;
        this.localRepository = localRepository;
    }

    //~ Methods -----------------------------------------------------------------------------------------

    /**
     * @see org.apache.cocoon.deployer.ArtifactProvider#getArtifact(java.lang.String)
     */
    public final File getArtifact(final String artifactId) {
		try {
			final ArtifactBean artifactBean = getArtifactBeanFor(artifactId);
			final Artifact artifact = this.artifactFactory.createBuildArtifact(
					artifactBean.getGroupId(), 
					artifactBean.getArtifactId(),
					artifactBean.getVersion(),
					artifactBean.getType());

			this.artifactResolver.resolve(artifact, this.remoteArtifactRepositories, this.localRepository);

			this.log.debug("[MavenArtifactProvider.getArtifactFor: found "
					+ artifact.getFile().getAbsolutePath());
			
			return artifact.getFile();
		} catch (Exception ex) {
			this.log.error(ex);
		}
		return null;
	}

    /**
	 * @see org.apache.cocoon.deployer.ArtifactProvider#getArtifact(java.lang.String[])
	 */
    public final File[] getArtifact(String[] artifactIds) {
    	
    	Validate.notNull(artifactIds, "artifactIds mustn't be null");
    	Validate.noNullElements(artifactIds, "Quering a 'null'-artifact is not possible");
    	
    	File[] returnFiles = new File[0];
        List returnFilesList = new ArrayList();
        
    	try {
			List dependencies = new ArrayList();
	        Set artifacts = null;

    		for(int i = 0; i < artifactIds.length; i++) {
	    		ArtifactBean artifactBean = getArtifactBeanFor(artifactIds[i]);
				Dependency dependency = new Dependency();
				dependency.setGroupId(artifactBean.getGroupId());				
				dependency.setArtifactId(artifactBean.getArtifactId());
				dependency.setVersion(artifactBean.getVersion());    	
				dependency.setType(artifactBean.getType());
				dependencies.add(dependency);		
	    	}

    		Artifact mainArtifact = artifactFactory.createBuildArtifact( "unspecified", "unspecified", "0.0", "jar");
            
            artifacts = MavenMetadataSource.createArtifacts( this.artifactFactory, dependencies, null, null, null );		   		
		
            Map managedDependencies = Collections.EMPTY_MAP;	
            
            artifacts = MavenMetadataSource.createArtifacts( artifactFactory, dependencies, null, null, null );		
            
            ArtifactResolutionResult result = artifactResolver.resolveTransitively( 
            		artifacts, mainArtifact, managedDependencies, localRepository,
                    remoteArtifactRepositories, metadataSource);   
            

    	    for(Iterator i = result.getArtifacts().iterator(); i.hasNext();) {
                Artifact artifact = (Artifact) i.next();       
                returnFilesList.add(artifact.getFile());
            }		            

    	    returnFiles = (File[]) returnFilesList.toArray((new File[returnFilesList.size()]));
    	    
    	} catch (Exception ex) {
			this.log.error(ex);
			// TBD: catch specific exceptions and throw new MojoExecutionException(...) with explanations what went wrong
		}
    	
    	return returnFiles;
    }

    /**
     * Splits up an ArtifactId into a Dependency object
     *
     * @param artifactSpec The <code>artifactSpec</code>
     *
     * @return Value
     *
     * @throws ArtifactResolutionException The <code>ArtifactResolutionException</code>
     * @throws ArtifactNotFoundException The <code>ArtifactNotFoundException</code>
     */
    private ArtifactBean getArtifactBeanFor(final String artifactSpec)
        throws ArtifactResolutionException, ArtifactNotFoundException {
    	
        final int p1 = artifactSpec.indexOf(':');
        Validate.isTrue(p1 > 0, "invalid artifact specifier: " + artifactSpec);

        final String groupId = artifactSpec.substring(0, p1);
        final int p2 = artifactSpec.indexOf(':', p1 + 1);
        Validate.isTrue(p2 > 0, "invalid artifact specifier: " + artifactSpec);

        final String artifactId = artifactSpec.substring(p1 + 1, p2);
        final int p3 = artifactSpec.indexOf(':', p2 + 1);
        final String version =
            (p3 > 0) ? artifactSpec.substring(p2 + 1, p3) : artifactSpec.substring(p2 + 1);
        final String type = (p3 > 0) ? artifactSpec.substring(p3 + 1) : "jar";
        
        this.log.debug("[MavenArtifactProvider.getArtifactFor: groupId=" + groupId);
        this.log.debug("[MavenArtifactProvider.getArtifactFor: artifactId=" + artifactId);
        this.log.debug("[MavenArtifactProvider.getArtifactFor: version=" + version);       
        this.log.debug("[MavenArtifactProvider.getArtifactFor: type=" + type);            
        
        return new ArtifactBean(groupId, artifactId, version, type);
    }
    
    /**
     * Used to hold the data of the artifact data parsing process.
     */
    static class ArtifactBean {
    	private String groupId;
    	private String artifactId;
    	private String version;
    	private String type;
    	
    	public ArtifactBean(String groupId, String artifactId, String version, String type) {
    		this.groupId = groupId;
    		this.artifactId = artifactId;
    		this.version = version;
    		this.type = type;
    	}
    	
		public String getArtifactId() {
			return artifactId;
		}
		public String getGroupId() {
			return groupId;
		}
		public String getType() {
			return type;
		}
		public String getVersion() {
			return version;
		}

    }
    
}
