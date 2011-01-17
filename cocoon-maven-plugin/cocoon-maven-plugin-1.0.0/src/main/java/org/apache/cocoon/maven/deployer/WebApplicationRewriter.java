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
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

import org.apache.cocoon.maven.deployer.servlet.ShieldedClassLoaderManager;
import org.apache.cocoon.maven.deployer.servlet.ShieldingListener;
import org.apache.cocoon.maven.deployer.servlet.ShieldingServlet;
import org.apache.cocoon.maven.deployer.servlet.ShieldingServletFilter;
import org.apache.cocoon.maven.deployer.utils.XMLUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.w3c.dom.Document;

/**
 * @version $Id$
 */
public class WebApplicationRewriter {

    protected static final String CLASSLOADER_JAR = "cocoon-deployer-plugin-classloading.jar";

    protected static final String JAR_ENTRY_PREFIX = ShieldedClassLoaderManager.class.getPackage().getName().replace('.', '/');

    /**
     * Prepare the web application to use the shielded class loader.
     * The web.xml will be rewritten: all servlets, filters etc. are wrapped
     * by a wrapper which uses the shielded class loader.
     * In addition all libs are moved from WEB-INF/lib to WEB-INF/shielded/lib
     * and all files from WEB-INF/classes are moved to WEB-INF/shielded/classes.
     */
    public static void shieldWebapp(File webInfDir, Log log, boolean useShieldingRepository)
    throws MojoExecutionException {
        if ( log.isDebugEnabled() ) {
            log.debug("Shielding web application in " + webInfDir);
        }
        final String webInfSlashWebXml = webInfDir.getPath() + File.separatorChar + "web.xml";

        if (!new File(webInfSlashWebXml).exists()) {
            throw new MojoExecutionException("No web.xml present - can't add shielded class loading");
        }
        log.info("Adding shielded classloader configuration to web application configuration.");
        if ( log.isDebugEnabled() ) {
            log.debug("Reading web.xml: " + webInfSlashWebXml);
        }

        // load web.xml
        final Document webAppDoc;
        try {
            webAppDoc = XMLUtils.parseXml(new File(webInfSlashWebXml));
        } catch (Exception e) {
            throw new MojoExecutionException("Unable to read web.xml from " + webInfSlashWebXml, e);
        }

        // rewrite
        WebXmlRewriter webXmlRewriter = new WebXmlRewriter(ShieldingServlet.class.getName(), ShieldingListener.class
                        .getName(), ShieldingServletFilter.class.getName(), useShieldingRepository);
        if ( webXmlRewriter.rewrite(webAppDoc) ) {
            // save web.xml
            try {
                if ( log.isDebugEnabled() ) {
                    log.debug("Writing web.xml: " + webInfSlashWebXml);
                }
                XMLUtils.write(webAppDoc, new FileOutputStream(webInfSlashWebXml));
            } catch (Exception e) {
                throw new MojoExecutionException("Unable to write web.xml to " + webInfSlashWebXml, e);
            }
        }

        // move classes and libs
        if (useShieldingRepository) {
            log.info("Moving classes and libs to shielded location.");
            try {
                move(webInfDir, "lib", ShieldedClassLoaderManager.WEB_INF_SHIELDED_LIB, log);
                move(webInfDir, "classes", ShieldedClassLoaderManager.WEB_INF_SHIELDED_CLASSES, log);
            } catch (IOException e) {
                throw new MojoExecutionException("unable to shield classes/libs", e);
            }
        }

        // add classloading stuff to WEB-INF/lib
        // let's search our jar
        final String rsrc = ShieldedClassLoaderManager.class.getName().replace('.', '/') + ".class";
        if ( log.isDebugEnabled() ) {
            log.debug("Searching class file for: " + rsrc);
        }
        try {
            final Enumeration e = WebApplicationRewriter.class.getClassLoader().getResources(rsrc);
            boolean found = false;
            while ( e.hasMoreElements() ) {
                final URL url = (URL)e.nextElement();
                if ( log.isDebugEnabled() ) {
                    log.debug("Found class in " + url);
                }
                if ( url.getProtocol().equals("jar")) {
                    String jarUrlString = url.toExternalForm();
                    int pos = jarUrlString.indexOf('!');
                    // include !/
                    jarUrlString = jarUrlString.substring(0, pos + 2);
                    final URL jarUrl = new URL(jarUrlString);
                    final JarURLConnection connection = (JarURLConnection)jarUrl.openConnection();
                    final JarFile jarFile = connection.getJarFile();
                    final File destFile = new File(webInfDir, "lib" + File.separator + CLASSLOADER_JAR);
                    final JarOutputStream jos = new JarOutputStream(new FileOutputStream(destFile));
                    final Enumeration entries = jarFile.entries();
                    while ( entries.hasMoreElements() ) {
                        final JarEntry current = (JarEntry)entries.nextElement();
                        // only include classes from the shielded class loader package
                        if ( current.getName().startsWith(JAR_ENTRY_PREFIX) ) {
                            jos.putNextEntry(current);
                            IOUtil.copy(jarFile.getInputStream(current), jos);
                            jos.closeEntry();
                        }
                    }
                    jos.close();
                    found = true;
                }
            }
            if (!found) {
                throw new MojoExecutionException("Unable to find jar file for shielded class loading classes.");
            }
        } catch (IOException ioe) {
            throw new MojoExecutionException("unable to find classes for shielded class loading.", ioe);
        }
    }

    private static void move(File parentDir, String srcDir, String destDirOrig, Log log) throws IOException {
        String destDir = destDirOrig;
        // use correct separators on windows
        if ( File.separatorChar != '/' ) {
            destDir = destDir.replace('/', File.separatorChar);
        }
        final File srcDirectory = new File(parentDir, srcDir);
        if (srcDirectory.exists() && srcDirectory.isDirectory()) {
            File destDirectory = new File(parentDir, destDir);
            if (log.isDebugEnabled()) {
                log.debug("Deleting directory " + destDirectory);
            }
            FileUtils.deleteDirectory(destDirectory);
            destDirectory = new File(parentDir, destDir);
            if (log.isDebugEnabled()) {
                log.debug("Recreating directory " + destDirectory);
            }
            destDirectory.mkdirs();
            final File[] files = srcDirectory.listFiles();
            if (files != null && files.length > 0) {
                for (int i = 0; i < files.length; i++) {
                    // move everything but our own jar
                    if ( !files[i].getName().equals(CLASSLOADER_JAR) ) {
                        if (log.isDebugEnabled()) {
                            log.debug("Moving " + files[i] + " to " + destDirectory);
                        }
                        files[i].renameTo(new File(destDirectory, files[i].getName()));
                    }
                }
            }
        }
    }
}