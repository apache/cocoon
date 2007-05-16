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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.cocoon.maven.deployer.monolithic.DeploymentException;
import org.apache.cocoon.maven.deployer.monolithic.RuleBasedZipExtractor;
import org.apache.cocoon.maven.deployer.monolithic.XPatchDeployer;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.war.AbstractWarMojo;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.artifact.MavenMetadataSource;

/**
 * Create a Cocoon web application based on a block deployment descriptor.
 * 
 * @version $Id$
 */
public abstract class AbstractDeployMojo extends AbstractWarMojo {

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
    private boolean useShieldingRepository = false;


    /**
     * Deploy a monolithic Cocoon web application. This means it doesn't use the
     * features that the blocks-fw offers.
     */
    protected void deployWebapp() throws MojoExecutionException, MojoFailureException {
        
        this.buildExplodedWebapp(getWebappDirectory());

        try {
            super.copyResources(getWarSourceDirectory(), getWebappDirectory());
        } catch (IOException e) {
            throw new MojoExecutionException("A problem occurred while copying webapp resources.", e);
        }

        xpatch(getBlockArtifactsAsMap(this.getProject(), this.getLog()), new File[0], getWebappDirectory(), this.getLog());        
        
        // take care of shielded classloading
        if (this.useShieldingClassLoader) {
            WebApplicationRewriter.shieldWebapp(new File(getWebappDirectory(), "WEB-INF"), getLog(), this.useShieldingRepository);
        }
    }

    public static void xpatch(final Map libraries, File[] xpatchFiles, final File basedir, Log log) throws DeploymentException {
        XPatchDeployer xwebPatcher = new XPatchDeployer("WEB-INF");
        xwebPatcher.setBasedir(basedir);
        xwebPatcher.setLogger(log);
        // iterate over all blocks that need to be installed into a J2EE web
        // application
        for (Iterator it = libraries.entrySet().iterator(); it.hasNext();) {
            final Map.Entry entry = (Map.Entry) it.next();
            final Object id = entry.getKey();
            File lib = (File) entry.getValue();
            try {
                log.debug("Scanning " + id);
                RuleBasedZipExtractor zipExtractor = new RuleBasedZipExtractor(basedir, log);
                // add the matching/execution rules
                zipExtractor.addRule("META-INF/cocoon/xpatch/*.xweb", xwebPatcher);
                // extract all configurations files
                zipExtractor.extract(lib);
            } catch (IOException e) {
                throw new DeploymentException("Can't deploy '" + lib.getAbsolutePath() + "'.", e);
            }
        }
        
        for (int i = 0; i < xpatchFiles.length; i++ ) {
            File patch = xpatchFiles[i];
            try {
                xwebPatcher.addPatch(patch);
                log.info("Adding xpatch: " + patch);
            } catch (IOException e) {
                throw new DeploymentException("Can't use patches '" + patch + "'.", e);                
            }
        }

        InputStream sourceWebXmlFile = null;
        File webXml = new File(basedir, "WEB-INF/web.xml");
        System.out.println("webXml.getAbsolutePath=" + webXml.getAbsolutePath());
        try {
            sourceWebXmlFile = new FileInputStream(webXml);
            xwebPatcher.applyPatches(sourceWebXmlFile, "WEB-INF/web.xml");
        } catch (FileNotFoundException e) {
            throw new DeploymentException("Can't apply patches on " + webXml + ".", e);
        } finally {
            IOUtils.closeQuietly(sourceWebXmlFile);
        }

    }    
    
    /**
     * Create a <code>Map</code> of <code>java.io.File</code> objects
     * pointing to artifacts.
     */
    public static Map getBlockArtifactsAsMap(MavenProject project, Log log) throws MojoExecutionException {
        Map files = new HashMap();
        for (Iterator it = project.getArtifacts().iterator(); it.hasNext();) {
            Artifact artifact = (Artifact) it.next();
            String id = artifact.getArtifactId();
            if (files.containsKey(id)) {
                // Now search for all artifacts and print their dependency trail
                StringBuffer msg = new StringBuffer("There are at least two artifacts with the ID '");
                msg.append(id);
                msg.append("':");
                msg.append(SystemUtils.LINE_SEPARATOR);
                for (Iterator ai = project.getArtifacts().iterator(); ai.hasNext();) {
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


            files.put(id, artifact.getFile());
            if (log.isDebugEnabled()) {
                StringBuffer msg = new StringBuffer("Deploying " + artifact);
                final List l = artifact.getDependencyTrail();
                final Iterator i = l.iterator();
                while (i.hasNext()) {
                    msg.append("    ");
                    msg.append(i.next().toString());
                    msg.append(SystemUtils.LINE_SEPARATOR);
                }
                log.debug(msg.toString());
            }

        }
        return files;
    }
}
