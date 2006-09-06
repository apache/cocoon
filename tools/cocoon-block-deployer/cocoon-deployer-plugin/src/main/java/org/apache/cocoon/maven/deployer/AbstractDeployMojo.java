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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.cocoon.maven.deployer.monolithic.DevelopmentBlock;
import org.apache.cocoon.maven.deployer.monolithic.DevelopmentProperty;
import org.apache.cocoon.maven.deployer.monolithic.MonolithicCocoonDeployer;
import org.apache.cocoon.maven.deployer.utils.CopyUtils;
import org.apache.cocoon.maven.deployer.utils.WebApplicationRewriter;
import org.apache.cocoon.maven.deployer.utils.XMLUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.SystemUtils;
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
    private boolean useShieldingClassloader = true;

    /**
     * Move jars for shielded classloading
     * 
     * @parameter expression="${maven.war.shieldingrepository}"
     */
    private boolean useShieldingRepository = true;

    /**
     * Deploy a monolithic Cocoon web application. This means it doesn't use the
     * features that the blocks-fw offers.
     */
    protected void deployMonolithicCocoonAppAsWebapp(final String blocksdir) throws MojoExecutionException {
        this.buildExplodedWebapp(getWebappDirectory());
        MonolithicCocoonDeployer deployer = new MonolithicCocoonDeployer(this.getLog());
        deployer.deploy(getBlockArtifactsAsMap(null), getWebappDirectory(), blocksdir);

        // make sure that all configuration files available in the webapp
        // override block configuration files
        try {
            copyResources(getWarSourceDirectory(), getWebappDirectory(), getWebXml());
        } catch (IOException e) {
            throw new MojoExecutionException("A problem occurred while copying webapp resources.", e);
        }

        // TODO XPatch web.xml here!

        // take care of paranoid classloading
        if (this.useShieldingClassloader) {
            shieldCocoonWebapp();
        }
    }

    /**
     * Deploy a particular block at development time.
     */
    protected void blockDeploymentMonolithicCocoon(final String blocksdir, final DevelopmentBlock[] blocks,
            final DevelopmentProperty[] properties) throws MojoExecutionException {
        this.buildExplodedWebapp(getWebappDirectory());
        // remove WEB-INF/classes as they are loaded by ReloadingClassloader
        // from a dirrerent location
        try {
            FileUtils.deleteDirectory(new File(webappDirectory, "WEB-INF/classes"));
        } catch (IOException e) {
            throw new MojoExecutionException("unable to delete WEB-INF/classes directory", e);
        }

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

        // deploy all blocks
        MonolithicCocoonDeployer deployer = new MonolithicCocoonDeployer(this.getLog());
        deployer.deploy(getBlockArtifactsAsMap(blocks), getWebappDirectory(), blocksdir, extBlocks, properties);

        if (useShieldingClassloader)
            shieldCocoonWebapp();
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

    // ~~~~~~~~~~ utility methods ~~~~~~~~~~~
    /**
     * Make a Cocoon webapp using the ShieldingClassloader. This method rewrites
     * the web.xml and moves all libs from WEB-INF/lib to WEB-INF/cocoon/lib,
     * except cocoon-bootstrap which remains in WEB-INF/lib.
     */
    private void shieldCocoonWebapp() throws MojoExecutionException {
        String webInfSlashWebXml = "WEB-INF" + File.separatorChar + "web.xml";

        String webXmlLocation = this.getWebXml();
        if (webXmlLocation == null) {
            webXmlLocation = getWarSourceDirectory().getAbsolutePath() + File.separatorChar + webInfSlashWebXml;
        }

        String targetWebXmlLocation = getWebappDirectory().getAbsolutePath() + File.separatorChar + webInfSlashWebXml;
        if (!new File(webXmlLocation).exists()) {
            getLog().debug("no web.xml in source location. checking for generated web.xml in target location.");
            if (!new File(targetWebXmlLocation).exists()) {
                this.getLog().info("No web.xml supplied. Will install default web.xml");
                File outFile = org.apache.cocoon.maven.deployer.utils.FileUtils.createPath(new File(
                        getWebappDirectory(), webInfSlashWebXml));

                try {
                    CopyUtils.copy(readResourceFromClassloader("WEB-INF/web.xml"), new FileOutputStream(outFile));
                } catch (IOException ioex) {
                    throw new MojoExecutionException("cannot copy resource " + webXml, ioex);
                }
            }
            webXmlLocation = targetWebXmlLocation;
        } else {
            this.getLog().info("web.xml present");
        }
        this.getLog().info("Adding shielded classloader configuration to webapp configuration.");
        this.getLog().debug("Reading web.xml: " + webXmlLocation);

        InputStream is = null;
        final Document webAppDoc;
        try {
            is = new BufferedInputStream(new FileInputStream(new File(webXmlLocation)));
            webAppDoc = XMLUtils.parseXml(is);
        } catch (Exception e) {
            throw new MojoExecutionException("Unable to read web.xml from " + webXmlLocation, e);
        } finally {
            IOUtils.closeQuietly(is);
        }

        WebApplicationRewriter.rewrite(webAppDoc);
        this.getLog().debug("Writing web.xml: " + targetWebXmlLocation);

        try {
            XMLUtils.write(webAppDoc, new FileOutputStream(targetWebXmlLocation));
        } catch (Exception e) {
            throw new MojoExecutionException("Unable to write web.xml to " + targetWebXmlLocation, e);
        }

        if (this.useShieldingRepository) {
            this.getLog().info("Moving classes and libs to shielded location.");
            final String webInfDir = getWebappDirectory().getAbsolutePath() + File.separatorChar + "WEB-INF";
            try {
                this.move(webInfDir, "lib", COCOON_LIB);
                this.move(webInfDir, "classes", COCOON_CLASSES);
            } catch (IOException e) {
                throw new MojoExecutionException("unable to shield classes/libs", e);
            }
        }
    }

    private InputStream readResourceFromClassloader(String fileName) {
        return MonolithicCocoonDeployer.class.getClassLoader().getResourceAsStream(
                "org/apache/cocoon/maven/deployer/monolithic/" + fileName);
    }

    private void move(String parentDir, String srcDir, String destDir) throws IOException {
        final File srcDirectory = new File(parentDir, srcDir);
        if (srcDirectory.exists() && srcDirectory.isDirectory()) {
            File destDirectory = new File(parentDir, destDir);
            if (this.getLog().isDebugEnabled()) {
                this.getLog().debug("Deleting directory " + destDirectory);
            }
            FileUtils.deleteDirectory(destDirectory);
            destDirectory = new File(parentDir, destDir);
            if (this.getLog().isDebugEnabled()) {
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
                        if (this.getLog().isDebugEnabled()) {
                            this.getLog().debug("Excluding " + files[i] + " from moving.");
                        }
                    }
                    if (!exclude) {
                        if (this.getLog().isDebugEnabled()) {
                            this.getLog().debug("Moving " + files[i] + " to " + destDirectory);
                        }
                        files[i].renameTo(new File(destDirectory, files[i].getName()));
                    }
                }
            }
        }
    }
}
