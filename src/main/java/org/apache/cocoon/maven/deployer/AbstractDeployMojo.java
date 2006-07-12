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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.cocoon.maven.deployer.monolithic.DevelopmentBlock;
import org.apache.cocoon.maven.deployer.monolithic.DevelopmentProperty;
import org.apache.cocoon.maven.deployer.monolithic.MonolithicCocoonDeployer;
import org.apache.cocoon.maven.deployer.utils.WebApplicationRewriter;
import org.apache.cocoon.maven.deployer.utils.XMLUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.war.AbstractWarMojo;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.artifact.MavenMetadataSource;
import org.codehaus.plexus.util.FileUtils;
import org.w3c.dom.Document;

/**
 * Create a Cocoon web application based on a block deployment descriptor.
 *
 * @version $Id$
 */
abstract class AbstractDeployMojo extends AbstractWarMojo {

    private static final String COCOON_CLASSES = "cocoon" + File.separator + "classes";

    private static final String COCOON_LIB = "cocoon" + File.separator + "lib";

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
     * Use shielded classloading
     *
     * @parameter expression="${maven.war.shieldingclassloader}"
     */
    private boolean useShieldingClassloader = true;

    /**
     * Move jars for shielded classloading
     *
     * @parameter expression="${maven.war.shieldingrepository}"
     */
    private boolean useShieldingRepository = true;

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
        if ( this.useShieldingClassloader ) {
            shieldCocoonWebapp();
        }
	}

    /**
     * Deploy a particular block at development time.
     */
    protected void blockDeploymentMonolithicCocoon(final String blocksdir, final DevelopmentBlock[] blocks, 
            final DevelopmentProperty[] properties) throws MojoExecutionException {
        File webappDirectory_ = getWebappDirectory();        

        File webinfDir = new File(webappDirectory_, WEB_INF);
        webinfDir.mkdirs();

        // add current block to the development blocks
        // it is important that the current block is put at the end of the array - the 
        // MonotlithicCocoonDeployer expects this
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

        // deploy all libraries to WEB-INF/cocoon/lib and cocoon-bootstrap to WEB-INF/lib
        copyLibs();        
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

    // ~~~~~~~~~~ utility methods ~~~~~~~~~~~
    /**
     * Make a Cocoon webapp using the ShieldingClassloader. This method rewrites the web.xml and moves
     * all libs from WEB-INF/lib to WEB-INF/cocoon/lib, except cocoon-bootstrap which remains in WEB-INF/lib.
     */
    private void shieldCocoonWebapp() throws MojoExecutionException {
        File webappDirectory_ = getWebappDirectory();
        String webXmlLocation = this.getWebXml();
        if ( webXmlLocation == null ) {
            webXmlLocation = getWarSourceDirectory().getAbsolutePath() + File.separatorChar + "WEB-INF" + File.separatorChar + "web.xml";
        }
        this.getLog().info("Adding shielded classloader configuration to webapp configuration.");
        this.getLog().debug("Reading web.xml: " + webXmlLocation);
        try {
            final Document webAppDoc = XMLUtils.parseXml(new FileInputStream(new File(webXmlLocation)));
            WebApplicationRewriter.rewrite(webAppDoc);
            final String dest = webappDirectory_.getAbsolutePath() + File.separatorChar + "WEB-INF" + File.separatorChar + "web.xml";
            this.getLog().debug("Writing web.xml: " + dest);
            XMLUtils.write(webAppDoc, new FileOutputStream(dest));
        } catch (Exception e) {
            throw new MojoExecutionException("Unable to read web.xml from " + webXmlLocation, e);
        }
        if ( this.useShieldingRepository ) {
            this.getLog().info("Moving classes and libs to shielded location.");
            final String webInfDir = webappDirectory_.getAbsolutePath() + File.separatorChar + "WEB-INF";
            this.move(webInfDir, "lib", COCOON_LIB);
            this.move(webInfDir, "classes", COCOON_CLASSES);
        }
    }  

    /**
     * Copy all libs that don't have the scope provided or system to WEB-INF/cocoon/lib, except
     * cocoon-bootstrap, which is copied to WEB-INF/lib
     */
    private void copyLibs() throws MojoExecutionException {
        File webappDirectory_ = this.getWebappDirectory();
        for ( Iterator iter = this.getProject().getArtifacts().iterator(); iter.hasNext(); ) {
            Artifact artifact = (Artifact) iter.next();
            // Include runtime and compile time libraries
            if (!Artifact.SCOPE_PROVIDED.equals(artifact.getScope())
                    && !Artifact.SCOPE_TEST.equals(artifact.getScope()) 
                    && "jar".equals(artifact.getType())) {
                try {
                    if (artifact.getArtifactId().equals("cocoon-bootstrap")) {
                        FileUtils.copyFileToDirectory(artifact.getFile(), new File(webappDirectory_, "WEB-INF/lib"));       
                        this.getLog().info("Deploying artifact to WEB-INF/lib/" + artifact.getFile().getName());                       
                    } else {
                        FileUtils.copyFileToDirectory(artifact.getFile(), new File(webappDirectory_, "WEB-INF/"
                            + COCOON_LIB));
                        this.getLog().info("Deploying artifact to WEB-INF/" + COCOON_LIB + "/" + 
                            artifact.getFile().getName());                        
                    }
                } catch (IOException e) {
                    throw new MojoExecutionException("Can't copy artifact '" + artifact.getArtifactId()
                            + "' to WEB-INF/" + COCOON_LIB);
                }
            }
        }
    }       

    /**
     * Move all libs from one directory (WEB-INF/lib) to another (WEB-INF/cocoon/lib).
     */
    private void move(String parentDir, String srcDir, String destDir) {
        final File srcDirectory = new File(parentDir, srcDir);
        if (srcDirectory.exists() && srcDirectory.isDirectory()) {
            File destDirectory = new File(parentDir, destDir);
            if ( this.getLog().isDebugEnabled() ) {
                this.getLog().debug("Deleting directory " + destDirectory);
            }
            org.apache.cocoon.maven.deployer.utils.FileUtils.deleteDirRecursivly(destDirectory);
            destDirectory = new File(parentDir, destDir);
            if ( this.getLog().isDebugEnabled() ) {
                this.getLog().debug("Recreating directory " + destDirectory);
            }
            destDirectory.mkdirs();
            final File[] files = srcDirectory.listFiles();
            if (files != null && files.length > 0) {
                for (int i = 0; i < files.length; i++) {
                    // TODO - replace this hard-coded exlclude with something
                    // configurable
                    boolean exclude = false;
                    if ("lib".equals(srcDir) && files[i].getName().startsWith("cocoon-bootstrap")) {
                        exclude = true;
                        if ( this.getLog().isDebugEnabled() ) {
                            this.getLog().debug("Excluding " + files[i] + " from moving.");
                        }
                    }
                    if (!exclude) {
                        if ( this.getLog().isDebugEnabled() ) {
                            this.getLog().debug("Moving " + files[i] + " to " + destDirectory);
                        }
                        files[i].renameTo(new File(destDirectory, files[i].getName()));
                    }
                }
            }
        }
    }    
}