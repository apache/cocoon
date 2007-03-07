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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.antlr.stringtemplate.StringTemplate;
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

/**
 * @goal webapp
 * @requiresProject true
 * @requiresDependencyResolution runtime
 * @execute phase="process-classes"
 * @version $Id$ 
 */
public class ReloadingWebappMojo extends AbstractMojo {

    private static final String WEB_INF_WEB_XML = "WEB-INF/web.xml";

    private static final String WEB_INF_APP_CONTEXT = "WEB-INF/applicationContext.xml";

    private static final String WEB_INF_LOG4J = "WEB-INF/log4j.xml";
    
    private static final String WEB_INF_LIB = "WEB-INF/lib";
    
    private static final String WEB_INF_COCOON_SPRING_PROPS = "WEB-INF/cocoon/spring/rcl.properties";

    private static final String WEB_INF_COCOON_PROPS = "WEB-INF/cocoon/properties/rcl.properties";   

    private static final String WEB_INF_RCL_URLCL_CONF = "WEB-INF/cocoon/rclwrapper.urlcl.conf";
    
    private static final String WEB_INF_RCLWRAPPER_RCL_CONF = "WEB-INF/cocoon/rclwrapper.rcl.conf";


    /**
     * The directory that contains the Cocoon web application.
     * 
     * @parameter expression="${cocoon.rcl.target}"
     */
    private File target = new File("./target/rcl");
    
    /**
     * The directory that contains the Cocoon web application.
     * 
     * @parameter expression="${cocoon.rcl.properties}"
     */
    private File rclPropertiesFile = new File("./rcl.properties");    

    /**
     * Use socket appender
     * 
     * @parameter expression="${cocoon.rcl.log4j.useSocketAppender}"
     */
    private boolean useSocketAppender = false;

    /**
     * Use console appender
     * 
     * @parameter expression="${cocoon.rcl.log4j.useConsoleAppender}"
     */
    private boolean useConsoleAppender = false;

    /**
     * Use a custom log4j xml configuration file.
     * 
     * @parameter expression="${cocoon.rcl.log4j.conf}"
     */
    private String customLog4jXconf;
    
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
        getLog().info("Creating a reloading Cocoon web application.");
        
        // create web application containing all necessary files (web.xml, applicationContext.xml, log4j.xconf)
        File webAppBaseDir = new File(target, "webapp");
        writeInputStreamToFile(readResourceFromClassloader(WEB_INF_WEB_XML), createPath(new File(webAppBaseDir, WEB_INF_WEB_XML)));
        writeInputStreamToFile(readResourceFromClassloader(WEB_INF_APP_CONTEXT), createPath(new File(webAppBaseDir, WEB_INF_APP_CONTEXT)));
        writeLog4jXml(webAppBaseDir);        

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

    }

    protected RwmProperties readProperties() throws MojoExecutionException {
        RwmProperties props = null;
        try {
            props  = new RwmProperties(this.rclPropertiesFile);
        } catch (ConfigurationException e) {
            throw new MojoExecutionException("Can't read " + this.rclPropertiesFile.getAbsolutePath(), e);
        }
        return props;
    }

    protected void createReloadingClassLoaderConf(File webAppBaseDir, RwmProperties props) throws MojoExecutionException {
        File urlClConfFile = createPath(new File(webAppBaseDir, WEB_INF_RCLWRAPPER_RCL_CONF));
        try {
            FileWriter fw = new FileWriter(urlClConfFile);
            for(Iterator aIt = props.getClassesDirs().iterator(); aIt.hasNext();) {
                String dir = (String) aIt.next();
                fw.write(dir + "\n");            
                this.getLog().debug("Adding classes-dir to RCLClassLoader configuration: " + dir);
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
            for(Iterator aIt = props.getClassesDirs().iterator(); aIt.hasNext();) {
                String dir = (String) aIt.next();
                fw.write(dir + "\n");
                this.getLog().debug("Adding classes-dir to URLClassLoader configuration: " + dir);
            }
            
            Set artifacts = project.getArtifacts();
            ScopeArtifactFilter filter = new ScopeArtifactFilter(Artifact.SCOPE_RUNTIME);
            for (Iterator iter = artifacts.iterator(); iter.hasNext();) {
                Artifact artifact = (Artifact) iter.next();
                if (!artifact.isOptional() && filter.include(artifact)) {
                    fw.write(artifact.getFile().toURL().toExternalForm() + "\n");
                    this.getLog().debug("Adding dependency to URLClassLoader configuration: " + artifact.getArtifactId());
                }
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
            props.getSpringProperties().store(springPropsOs, "Spring properties as read from " + this.rclPropertiesFile.toURL());
            springPropsOs.close();
        } catch (IOException e) {
            throw new MojoExecutionException("Can't write to  " + springPropFile.getAbsolutePath(), e);
        }
    }
    
    protected void createCocoonProperties(File webAppBaseDir, RwmProperties props) throws MojoExecutionException {
        File springPropFile = createPath(new File(webAppBaseDir, WEB_INF_COCOON_PROPS));
        try {
            FileOutputStream springPropsOs = new FileOutputStream(springPropFile);
            props.getCocoonProperties().store(springPropsOs, "Cocoon properties as read from " + this.rclPropertiesFile.toURL());
            springPropsOs.close();
        } catch (IOException e) {
            throw new MojoExecutionException("Can't write to  " + springPropFile.getAbsolutePath(), e);
        }
    }

    protected void copyRclWrapperLibs(File webAppBaseDir) throws MojoExecutionException {
        Set rclWebappDependencies = getDependencies("org.apache.cocoon",  "cocoon-rcl-webapp-wrapper", "1.0.0-M1-SNAPSHOT", "jar");        
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

    protected void writeLog4jXml(File webAppBaseDir) throws MojoExecutionException {
        Map log4jTemplateMap = new HashMap();
        log4jTemplateMap.put("useConsoleAppender", new Boolean(this.useConsoleAppender));
        log4jTemplateMap.put("useSocketAppender", new Boolean(this.useSocketAppender));
        writeStringTemplateToFile(webAppBaseDir, WEB_INF_LOG4J, customLog4jXconf, log4jTemplateMap);
    }

    protected Set getDependencies(final String groupId, final String artifactId, final String version,
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
        String resource = ReloadingWebappMojo.class.getPackage().getName().replace('.', '/') + "/" + fileName;
        System.out.println("Reading resource from classpath: " + resource);
        return ReloadingWebappMojo.class.getClassLoader().getResourceAsStream(resource);
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