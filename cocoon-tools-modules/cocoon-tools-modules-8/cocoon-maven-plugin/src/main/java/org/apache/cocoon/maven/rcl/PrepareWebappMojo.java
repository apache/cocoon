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
package org.apache.cocoon.maven.rcl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.antlr.stringtemplate.StringTemplate;
import org.apache.cocoon.maven.deployer.AbstractDeployMojo;
import org.apache.cocoon.maven.deployer.WebXmlRewriter;
import org.apache.cocoon.maven.deployer.utils.XMLUtils;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.Validate;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.artifact.InvalidDependencyVersionException;
import org.apache.maven.project.artifact.MavenMetadataSource;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Create a web application environment for a Cocoon block, including support
 * for the reloading classloader. 
 *
 * @goal prepare
 * @requiresProject true
 * @requiresDependencyResolution runtime
 * @execute phase="process-classes"
 * @version $Id$
 */
public class PrepareWebappMojo extends AbstractMojo {

    private static final String LIB_VERSION_WRAPPER = "1.0.0-RC1-SNAPSHOT";

    private static final String LIB_VERSION_SPRING_RELOADER = "1.0.0-RC1-SNAPSHOT";

    private static final String WEB_INF_WEB_XML = "WEB-INF/web.xml";

    private static final String WEB_INF_APP_CONTEXT = "WEB-INF/applicationContext.xml";

    private static final String WEB_INF_LOG4J = "WEB-INF/log4j.xml";

    private static final String WEB_INF_LIB = "WEB-INF/lib";

    private static final String WEB_INF_COCOON_SPRING_PROPS = "WEB-INF/cocoon/spring/rcl.properties";

    private static final String WEB_INF_COCOON_PROPS = "WEB-INF/cocoon/properties/rcl.properties";

    private static final String WEB_INF_RCL_URLCL_CONF = "WEB-INF/cocoon/rclwrapper.urlcl.conf";

    private static final String WEB_INF_RCLWRAPPER_RCL_CONF = "WEB-INF/cocoon/rclwrapper.rcl.conf";

    private static final String WEB_INF_RCLWRAPPER_PROPERTIES = "/WEB-INF/cocoon/rclwrapper.properties";


    /**
     * The directory that contains the Cocoon web application.
     *
     * @parameter expression="./target/rcl"
     */
    private File target;

    /**
     * The central property file that contains all information about where to find blocks.
     *
     * @parameter expression="./rcl.properties"
     */
    private File rclPropertiesFile;

    /**
     * Logging: Use socket appender?
     *
     * @parameter
     */
    private boolean useSocketAppender = false;

    /**
     * Logging: Use console appender?
     *
     * @parameter
     */
    private boolean useConsoleAppender = false;

    /**
     * Enable reloading of the Spring application context. Note: The reload of the
     * application context doesn't work properly if it contains beans which are based
     * on proxies with interfaces which are loaded by the reloading class loader. As a
     * workaround you can put all those interfaces into a separate module which is NOT
     * loaded by the reloading class loader.
     *
     * @parameter
     */
    private boolean reloadingSpringEnabled = true;

    /**
     * Enable the reloading class loader. Default value is <code>true</code>.
     *
     * @parameter
     */
    private boolean reloadingClassLoaderEnabled = true;

    /**
     * Logging: Use a custom log4j xml configuration file.
     *
     * @parameter
     */
    private String customLog4jXconf;

    /**
     * Use a custom web application directory.
     *
     * @parameter
     */
    private File customWebappDirectory;

    /**
     * This goal prepares a minimal Cocoon web application using the default
     * profile 'cocoon-22' that can be used to run a block. Alternatively a
     * 'ssf' (servlet-service framework) profile is supported, which creates a
     * web application that want to use the servlet-service framework only.
     *
     * @parameter
     */
    private String webappProfile = "cocoon-22";

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
     * Remote repositories which will be searched for blocks.
     *
     * @parameter expression="${project.remoteArtifactRepositories}"
     * @required
     * @readonly
     */
    private List remoteArtifactRepositories;

    /**
     * Local maven repository.
     *
     * @parameter expression="${localRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository localRepository;

    /**
     * Artifact resolver, needed to download source jars for inclusion in classpath.
     *
     * @component role="org.apache.maven.artifact.metadata.ArtifactMetadataSource"
     * @required
     * @readonly
     */
    private MavenMetadataSource metadataSource;

    /**
     * The project whose project files to create.
     *
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject project;


    public void execute() throws MojoExecutionException {
        // check if this plugin is useful at all
        if (!project.getPackaging().equals("jar") || !rclPropertiesFile.exists()) {
            getLog().info("Don't execute the Cocoon RCL plugin becaues either its packaging "
                            + "type is not 'jar' or "
                            + "there is no rcl.properties file in the block's base directory.");
            return;
        }

        // check profile
        if ("cocoon-22".equals(this.webappProfile)) {
            getLog().info("Preparing a Cocoon web application.");
        } else if ("ssf".equals(this.webappProfile)) {
            getLog().info("Preparing a Servlet-Service web application.");
        } else {
            throw new MojoExecutionException("Only the profiles 'cocoon-22' and 'ssf' are supported.");
        }

        // create web application containing all necessary files (web.xml, applicationContext.xml, log4j.xconf)
        File webAppBaseDir = new File(target, "webapp");
        writeInputStreamToFile(readResourceFromClassloader(WEB_INF_WEB_XML),
                createPath(new File(webAppBaseDir, WEB_INF_WEB_XML)));
        writeInputStreamToFile(readResourceFromClassloader(WEB_INF_APP_CONTEXT),
                createPath(new File(webAppBaseDir, WEB_INF_APP_CONTEXT)));
        writeLog4jXml(webAppBaseDir);

        // copy the content of a custom webapp context directory to the prepared web application.
        copyCustomWebappDirectory(webAppBaseDir);

        // copy rcl webapp wrapper and all its dependencies to WEB-INF/lib
        copyRclWrapperLibs(webAppBaseDir);

        // read the properties
        RwmProperties props = readProperties();

        // create a file that contains the URLs of all libraries (config for the UrlClassLoader)
        createUrlClassLoaderConf(webAppBaseDir, props);

        // create a file that contains the URLs of all classes directories (config for the ReloadingClassLoader)
        createReloadingClassLoaderConf(webAppBaseDir, props);

        // based on the RCL configuration file, create a Spring properties file
        createSpringProperties(webAppBaseDir, props);

        // based on the RCL configuration file, create a Cocoon properties file
        createCocoonProperties(webAppBaseDir, props);

        // create RCL properties
        createProperties(webAppBaseDir);

        // apply xpatch files
        applyXpatchFiles(webAppBaseDir, props);

        // rewrite WebXml
        rewriteWebXml(webAppBaseDir);
    }

    protected RwmProperties readProperties() throws MojoExecutionException {
        RwmProperties props = null;
        try {
            props  = new RwmProperties(this.rclPropertiesFile, this.project.getBasedir());
        } catch (ConfigurationException e) {
            throw new MojoExecutionException("Can't read " + this.rclPropertiesFile.getAbsolutePath(), e);
        }
        return props;
    }

    @SuppressWarnings("unchecked")
    protected void createReloadingClassLoaderConf(File webAppBaseDir, RwmProperties props) throws MojoExecutionException {
        File urlClConfFile = createPath(new File(webAppBaseDir, WEB_INF_RCLWRAPPER_RCL_CONF));
        try {
            FileWriter fw = new FileWriter(urlClConfFile);
            for(Iterator<?> aIt = props.getClassesDirs().iterator(); aIt.hasNext();) {
                String dir = (String) aIt.next();
                fw.write(dir + "\n");
                this.getLog().debug("Adding classes-dir to RCLClassLoader configuration: " + dir);
            }

            Set<Artifact> artifacts = this.project.getArtifacts();
            Set excludeLibProps = props.getExcludedLibProps();
            ScopeArtifactFilter filter = new ScopeArtifactFilter(Artifact.SCOPE_RUNTIME);
            for (Artifact artifact : artifacts) {
                // exclude optional and snapshot dependencies
                if (artifact.isOptional()) {
                    continue;
                }
                // exclude all artifacts that are not in the runtime scope
                if (!filter.include(artifact)) {
                    continue;
                }
                // keep only snapshot artifacts
                if (!artifact.isSnapshot()) {
                    continue;
                }
                // skip explicit excludes
                if (excludeLibProps.contains(artifact.getGroupId() + ":" + artifact.getArtifactId())) {
                    continue;
                }
                
                fw.write(artifact.getFile().toURI().toURL().toExternalForm() + "\n");
                this.getLog().debug("Adding library (URLClassLoader configuration): " + artifact.getArtifactId());
            }
            
            fw.close();
        } catch(IOException e) {
            throw new MojoExecutionException("Error while writing to " + urlClConfFile, e);
        }
    }

    protected void createUrlClassLoaderConf(File webAppBaseDir, RwmProperties props) throws MojoExecutionException {
        File urlClConfFile = createPath(new File(webAppBaseDir, WEB_INF_RCL_URLCL_CONF));
        try {
            FileWriter fw = new FileWriter(urlClConfFile);
            Set<?> excludeLibProps = props.getExcludedLibProps();

            for(Iterator<?> aIt = props.getClassesDirs().iterator(); aIt.hasNext();) {
                String dir = (String) aIt.next();
                fw.write(dir + "\n");
                this.getLog().debug("Adding classes-dir (URLClassLoader configuration): " + dir);
            }

            // add all project artifacts
            Set<Artifact> artifacts = project.getArtifacts();
            ScopeArtifactFilter filter = new ScopeArtifactFilter(Artifact.SCOPE_RUNTIME);

            Set<Artifact> filteredArtifacts = new HashSet<Artifact>();
            for (Artifact eachArtifact : artifacts) {
                // remove optional artifacts
                if(eachArtifact.isOptional()) {
                    continue;
                }
                // remove artifacts that are not in runtime scope
                if(!filter.include(eachArtifact)) { 
                    continue;
                }
                // skip explicit excludes
                if(excludeLibProps.contains(eachArtifact.getGroupId() + ":" + eachArtifact.getArtifactId())) {
                    continue;
                }
                
                filteredArtifacts.add(eachArtifact);
            }
            
            // add the Spring reloader libraries
            Set<Artifact> springReloaderArtifacts = getDependencies("org.apache.cocoon", "cocoon-rcl-spring-reloader",
                            LIB_VERSION_SPRING_RELOADER, "jar");
            filteredArtifacts.addAll(springReloaderArtifacts);

            for (Artifact eachArtifact : filteredArtifacts) {
                fw.write(eachArtifact.getFile().toURI().toURL().toExternalForm() + "\n");
                this.getLog().debug("Adding library (URLClassLoader configuration): " + eachArtifact.getArtifactId());
            }
            
            fw.close();
        } catch(IOException e) {
            throw new MojoExecutionException("Error while writing to " + urlClConfFile, e);
        }
    }

    protected void createSpringProperties(File webAppBaseDir, RwmProperties props) throws MojoExecutionException {
        File springPropFile = createPath(new File(webAppBaseDir, WEB_INF_COCOON_SPRING_PROPS));
        try {
            FileOutputStream springPropsOs = new FileOutputStream(springPropFile);
            props.getSpringProperties().store(springPropsOs, "Spring properties as read from " + this.rclPropertiesFile.toURI().toURL());
            springPropsOs.close();
        } catch (IOException e) {
            throw new MojoExecutionException("Can't write to  " + springPropFile.getAbsolutePath(), e);
        }
    }

    protected void createCocoonProperties(File webAppBaseDir, RwmProperties props) throws MojoExecutionException {
        File springPropFile = createPath(new File(webAppBaseDir, WEB_INF_COCOON_PROPS));
        try {
            FileOutputStream springPropsOs = new FileOutputStream(springPropFile);
            props.getCocoonProperties().store(springPropsOs, "Cocoon properties as read from " + this.rclPropertiesFile.toURI().toURL());
            springPropsOs.close();
        } catch (IOException e) {
            throw new MojoExecutionException("Can't write to  " + springPropFile.getAbsolutePath(), e);
        }
    }

    protected void copyRclWrapperLibs(File webAppBaseDir) throws MojoExecutionException {
        Set rclWebappDependencies = getDependencies("org.apache.cocoon", "cocoon-rcl-webapp-wrapper", LIB_VERSION_WRAPPER, "jar");
        for (Iterator rclIt = rclWebappDependencies.iterator(); rclIt.hasNext();) {
            Artifact artifact = (Artifact) rclIt.next();
            try {
                FileUtils.copyFileToDirectory(artifact.getFile(), createPath(new File(webAppBaseDir, WEB_INF_LIB)));
            } catch (IOException e) {
                throw new MojoExecutionException("Can't copy artifact " + artifact);
            }
            getLog().info("Adding lib to " + WEB_INF_LIB + ": " + artifact.getGroupId() + ":" + artifact.getArtifactId() + ":"
                    + artifact.getVersion() + ":" + artifact.getType());
        }
    }

    protected void createProperties(File webAppBaseDir) throws MojoExecutionException {
        File rclProps = createPath(new File(webAppBaseDir, WEB_INF_RCLWRAPPER_PROPERTIES));
        try {
            Properties props = new Properties();
            props.setProperty("reloading.spring.enabled", Boolean.toString(this.reloadingSpringEnabled));
            props.setProperty("reloading.classloader.enabled", Boolean.toString(this.reloadingClassLoaderEnabled));
            props.store(new FileOutputStream(rclProps), "Reloading Classloader Properties");
        } catch (IOException e) {
            throw new MojoExecutionException("Can't write to  " + rclProps.getAbsolutePath(), e);
        }
    }

    protected void writeLog4jXml(File webAppBaseDir) throws MojoExecutionException {
        Map log4jTemplateMap = new HashMap();
        log4jTemplateMap.put("useConsoleAppender", new Boolean(this.useConsoleAppender));
        log4jTemplateMap.put("useSocketAppender", new Boolean(this.useSocketAppender));
        writeStringTemplateToFile(webAppBaseDir, WEB_INF_LOG4J, customLog4jXconf, log4jTemplateMap);
    }

    protected void copyCustomWebappDirectory(File webAppBaseDir) throws MojoExecutionException {
        if (this.customWebappDirectory == null) {
            return;
        }
        if (!this.customWebappDirectory.exists()) {
            throw new MojoExecutionException("The custom web application directory does not exist.");
        }
        if (!this.customWebappDirectory.isDirectory()) {
            throw new MojoExecutionException(
                            "The value of the parameter 'customWebappDirectory' doesn't point to a directory.");
        }

        try {
            FileUtils.copyDirectory(this.customWebappDirectory, webAppBaseDir);
        } catch (IOException e) {
            throw new MojoExecutionException("Can't copy custom webapp files (directory: '" + this.customWebappDirectory
                            + ") to the web application in preparation.", e);
        }
    }

    protected void applyXpatchFiles(File webAppBaseDir, RwmProperties props) throws MojoExecutionException {
        // find all xpatch files in all configured blocks
        Set classesDirs = props.getClassesDirs();
        File[] allXPatchFiles = new File[0];
        for(Iterator it = classesDirs.iterator(); it.hasNext();) {
            String f = RwmProperties.calcRootDir((String) it.next());
            try {
                File f1 = new File(new File(new URI(f)), "src/main/resources/META-INF/cocoon/xpatch");
                File[] xmlFiles = f1.listFiles(new FilenameFilter() {
                    public boolean accept(File d, String name) {
                        return name.toLowerCase().endsWith(".xweb");
                    }
                });
                if(xmlFiles != null) {
                    File[] mergedArray = new File[allXPatchFiles.length + xmlFiles.length];
                    System.arraycopy(allXPatchFiles, 0, mergedArray, 0, allXPatchFiles.length);
                    System.arraycopy(xmlFiles, 0, mergedArray, allXPatchFiles.length, xmlFiles.length);
                    allXPatchFiles = mergedArray;
                }
            } catch (URISyntaxException e) {
            }
        }

        Map libs = AbstractDeployMojo.getBlockArtifactsAsMap(this.project, this.getLog());
        AbstractDeployMojo.xpatch(libs, allXPatchFiles, webAppBaseDir, this.getLog());
    }

    protected void rewriteWebXml(File webAppBaseDir) throws MojoExecutionException {
        File webXml = new File(webAppBaseDir, WEB_INF_WEB_XML);
        Document webXmlDocument;
        try {
            webXmlDocument = XMLUtils.parseXml(webXml);
        } catch (IOException e) {
            throw new MojoExecutionException("Problem while reading from " + webXml);
        } catch (SAXException e) {
            throw new MojoExecutionException("Problem while parsing " + webXml);
        }
        WebXmlRewriter webXmlRewriter = new WebXmlRewriter(
                        "org.apache.cocoon.tools.rcl.wrapper.servlet.ReloadingServlet",
                        "org.apache.cocoon.tools.rcl.wrapper.servlet.ReloadingListener",
                        "org.apache.cocoon.tools.rcl.wrapper.servlet.ReloadingServletFilter", false);
        if (webXmlRewriter.rewrite(webXmlDocument)) {
            // save web.xml
            try {
                if (this.getLog().isDebugEnabled()) {
                    this.getLog().debug("Rewriting web.xml: " + webXml);
                }
                XMLUtils.write(webXmlDocument, new FileOutputStream(webXml));
            } catch (Exception e) {
                throw new MojoExecutionException("Unable to write web.xml to " + webXml, e);
            }
        }
    }


    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ utility methods ~~~~~~~~~~

    protected Set<Artifact> getDependencies(final String groupId, final String artifactId, final String version,
            final String packaging) throws MojoExecutionException {
        Set returnSet = new HashSet();
        try {
            Set artifacts = null;
            ArtifactResolutionResult result = null;

            Dependency dependency = new Dependency();
            dependency.setGroupId(groupId);
            dependency.setArtifactId(artifactId);
            dependency.setVersion(version);

            List dependencies = new ArrayList();
            dependencies.add(dependency);
            Artifact pomArtifact = artifactFactory.createBuildArtifact("unspecified", "unspecified", "0.0", "jar");
            Map managedDependencies = Collections.EMPTY_MAP;
            artifacts = MavenMetadataSource.createArtifacts(artifactFactory, dependencies, "compile", null, null);
            result = artifactResolver.resolveTransitively(artifacts, pomArtifact, managedDependencies, localRepository,
                    remoteArtifactRepositories, metadataSource);

            for (Iterator i = artifacts.iterator(); i.hasNext();) {
                Artifact artifact = (Artifact) i.next();
                returnSet.add(artifact);
            }
            for (Iterator i = result.getArtifacts().iterator(); i.hasNext();) {
                Artifact artifact = (Artifact) i.next();
                returnSet.add(artifact);
            }
        } catch (ArtifactResolutionException e) {
            throw new MojoExecutionException("Can't resolve artifact " + groupId + ":" + artifactId + ":" + version, e);
        } catch (ArtifactNotFoundException e) {
            throw new MojoExecutionException("Can't find artifact " + groupId + ":" + artifactId + ":" + version, e);
        } catch (InvalidDependencyVersionException e) {
            throw new MojoExecutionException("Invalid version of artifact " + groupId + ":" + artifactId + ":"
                    + version, e);
        }
        return returnSet;
    }

    protected void writeStringTemplateToFile(final File basedir, final String fileName, final String customFile,
            final Map templateObjects) throws MojoExecutionException {
        OutputStream fos = null;
        try {
            File outFile = createPath(new File(basedir, fileName));
            fos = new BufferedOutputStream(new FileOutputStream(outFile));
            InputStream fileIs = null;
            if (customFile != null) {
                if (new File(customFile).exists()) {
                    fileIs = new BufferedInputStream(new FileInputStream(customFile));
                } else {
                    this.getLog().info(
                            "supplied custom file " + customFile + " doesn't exist. Fallback to default: " + fileName);
                    fileIs = readResourceFromClassloader(fileName);
                }
            } else {
                fileIs = readResourceFromClassloader(fileName);
            }
            StringTemplate stringTemplate = new StringTemplate(IOUtils.toString(fileIs));
            for (Iterator templateObjectsIt = templateObjects.keySet().iterator(); templateObjectsIt.hasNext();) {
                Object key = templateObjectsIt.next();
                stringTemplate.setAttribute((String) key, templateObjects.get(key));
            }
            this.getLog().info("Deploying string-template to " + fileName);
            IOUtils.write(stringTemplate.toString(), fos);
        } catch (FileNotFoundException e) {
            throw new MojoExecutionException((customFile == null ? fileName : customFile) + " not found.", e);
        } catch (IOException e) {
            throw new MojoExecutionException("Error while reading or writing.", e);
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                throw new MojoExecutionException("Error while closing the output stream.", e);
            }
        }
    }

    protected InputStream readResourceFromClassloader(String fileName) {
        String resource = PrepareWebappMojo.class.getPackage().getName().replace('.', '/') + "/profiles/"
                        + this.webappProfile + "/" + fileName;
        return PrepareWebappMojo.class.getClassLoader().getResourceAsStream(resource);
    }

    protected static File createPath(File file) {
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        return file;
    }

    protected void writeInputStreamToFile(final InputStream is, final File f) throws MojoExecutionException {
        Validate.notNull(is);
        Validate.notNull(f);
        
        try {
            FileWriter fw = new FileWriter(f);
            IOUtils.copy(is, fw);
            fw.close();
        } catch (IOException e) {
            throw new MojoExecutionException("Can't write to file " + f);
        }
    }
}
