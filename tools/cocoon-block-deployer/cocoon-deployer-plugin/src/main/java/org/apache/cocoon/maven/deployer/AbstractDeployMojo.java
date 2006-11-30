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
import org.apache.commons.lang.SystemUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.war.AbstractWarMojo;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.artifact.MavenMetadataSource;
import org.codehaus.plexus.util.FileUtils;

/**
 * Create a Cocoon web application based on a block deployment descriptor.
 * 
 * @version $Id$
 */
abstract class AbstractDeployMojo extends AbstractWarMojo {

    /**
     * Artifact factory, needed to download source jars for inclusion in
     * classpath.
     * 
     * @component role="org.apache.maven.artifact.factory.ArtifactFactory"
     * @required
     * @readonly
     */
    private ArtifactFactory artifactFactory;

    /**
     * Artifact resolver, needed to download source jars for inclusion in
     * classpath.
     * 
     * @component role="org.apache.maven.artifact.resolver.ArtifactResolver"
     * @required
     * @readonly
     */
    private ArtifactResolver artifactResolver;

    /**
     * Artifact resolver, needed to download source jars for inclusion in
     * classpath.
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
    // set properties: necessary because DeployMojo is not in the same package
    // as AbstractWarMojo

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
    private boolean useShieldingClassLoader = false;

    /**
     * Move jars for shielded classloading
     * 
     * @parameter expression="${maven.war.shieldingrepository}"
     */
    private boolean useShieldingRepository = true;

    /**
     * Use console appender
     * 
     * @parameter expression="${maven.war.log4j.useConsoleAppender}"
     */
    private boolean useConsoleAppender = false;

    /**
     * Use custom log4j.xconf
     * 
     * @parameter expression="${maven.war.log4j.useCustomXconf}"
     */
    private String customLog4jXconf;

    /**
     * Deploy a monolithic Cocoon web application. This means it doesn't use the
     * features that the blocks-fw offers.
     */
    protected void deployMonolithicCocoonAppAsWebapp(final String blocksdir) 
    throws MojoExecutionException, MojoFailureException {
        this.buildExplodedWebapp(getWebappDirectory());
        MonolithicCocoonDeployer deployer = new MonolithicCocoonDeployer(this.getLog());
        deployer.deploy(getBlockArtifactsAsMap(null), getWebappDirectory(), blocksdir, 
                        useConsoleAppender, customLog4jXconf);

        // make sure that all configuration files available in the webapp
        // override block configuration files
        try {
            //super.copyResources(getWarSourceDirectory(), getWebappDirectory(), getWebXml());
            super.copyResources(getWarSourceDirectory(), getWebappDirectory());
        } catch (IOException e) {
            throw new MojoExecutionException("A problem occurred while copying webapp resources.", e);
        }

        // take care of shielded classloading
        if (this.useShieldingClassLoader) {
            WebApplicationRewriter.shieldWebapp(new File(getWebappDirectory(), "WEB-INF"), getLog(), this.useShieldingRepository);
        }
    }

    /**
     * Deploy a particular block at development time.
     */
    protected void blockDeploymentMonolithicCocoon(final String blocksdir, final DevelopmentBlock[] blocks,
            final DevelopmentProperty[] properties) throws MojoExecutionException, MojoFailureException {

        this.buildExplodedWebapp(getWebappDirectory());

        // add current block to the development blocks
        // it is important that the current block is put at the end of the array
        // - the MonotlithicCocoonDeployer expects this
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

        // get all blocks to be deployed
        Map allBlocks = getBlockArtifactsAsMap(blocks);

        // deploying the artifact of the current project as jar too. Making the
        // DeploymentUtil of cocoon-core support deploying blocks from WEB-INF/classes 
        // would be difficult (e.g. What would be the name of the block as it is usually 
        // injected into the META-INF/manifest.mf when the jar is packaged.)
        File currentArtifact = this.getProject().getArtifact().getFile();
        if (currentArtifact == null) {
            throw new MojoExecutionException("The current artifact is null. The problem is that this mojo must"
                    + "be executed after with the packaging mojo.");
        }
        try {
            File classesDir = new File(getWebappDirectory(), "WEB-INF/classes");
            FileUtils.deleteDirectory(classesDir);
            this.getLog().debug("Removed " + classesDir.getAbsolutePath());
        } catch (IOException e) {
            throw new MojoExecutionException("unable to delete WEB-INF/classes directory", e);
        }
        File libDir = new File(getWebappDirectory(), "WEB-INF/lib");
        try {
            FileUtils.copyFileToDirectory(currentArtifact, libDir);
            this.getLog().debug("Copy current artifact (" + currentArtifact + ") to " + libDir.getAbsolutePath());
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to copy " + currentArtifact + " to " + libDir, e);
        }
        allBlocks.put(this.getProject().getArtifactId(), currentArtifact);

        // deploy all blocks
        MonolithicCocoonDeployer deployer = new MonolithicCocoonDeployer(this.getLog());
        deployer.deploy(allBlocks, getWebappDirectory(), blocksdir, extBlocks, properties, useConsoleAppender,
                customLog4jXconf);

        // take care of shielded classloading
        if (this.useShieldingClassLoader) {
            WebApplicationRewriter.shieldWebapp(new File(getWebappDirectory(), "WEB-INF"), getLog(),
                    this.useShieldingRepository);
        }

    }

    /**
     * Create a <code>Map</code> of <code>java.io.File</code> objects
     * pointing to artifacts.
     */
    private Map getBlockArtifactsAsMap(DevelopmentBlock[] excludedBlocks) throws MojoExecutionException {
        // loop over all artifacts and deploy them correctly
        Map files = new HashMap();
        for (Iterator it = this.getProject().getArtifacts().iterator(); it.hasNext();) {
            Artifact artifact = (Artifact) it.next();
            String id = artifact.getArtifactId();
            if (files.containsKey(id)) {
                // Now search for all artifacts and print their dependency trail
                StringBuffer msg = new StringBuffer("There are at least two artifacts with the ID '");
                msg.append(id);
                msg.append("':");
                msg.append(SystemUtils.LINE_SEPARATOR);
                for (Iterator ai = this.getProject().getArtifacts().iterator(); ai.hasNext();) {
                    final Artifact current = (Artifact) ai.next();
                    if (current.getArtifactId().equals(id)) {
                        msg.append(artifact);
                        msg.append(SystemUtils.LINE_SEPARATOR);
                        final List l = current.getDependencyTrail();
                        final Iterator i = l.iterator();
                        while (i.hasNext()) {
                            msg.append("    ");
                            msg.append(i.next().toString());
                            msg.append(SystemUtils.LINE_SEPARATOR);
                        }
                    }
                }
                throw new MojoExecutionException(msg.toString());
            }
            if (containsArtifact(excludedBlocks, artifact.getArtifactId(), artifact.getGroupId())) {
                this.getLog().debug("Skipping " + artifact);
            } else {
                files.put(id, artifact.getFile());
                if (this.getLog().isDebugEnabled()) {
                    StringBuffer msg = new StringBuffer("Deploying " + artifact);
                    final List l = artifact.getDependencyTrail();
                    final Iterator i = l.iterator();
                    while (i.hasNext()) {
                        msg.append("    ");
                        msg.append(i.next().toString());
                        msg.append(SystemUtils.LINE_SEPARATOR);
                    }
                    this.getLog().debug(msg.toString());
                }
            }
        }
        return files;
    }

    /**
     * @return true if the DevelopmentBlock array contains a block with the
     *         passed artifactId and groupId
     */
    private boolean containsArtifact(DevelopmentBlock[] blocks, String artifactId, String groupId) {
        if (blocks != null) {
            for (int i = 0; i < blocks.length; i++) {
                if (blocks[i].artifactId.equals(artifactId) && blocks[i].groupId.equals(groupId)) {
                    return true;
                }
            }
        }
        return false;
    }
}
