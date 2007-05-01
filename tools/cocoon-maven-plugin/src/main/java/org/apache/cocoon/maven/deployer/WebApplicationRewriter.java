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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
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
import org.w3c.dom.Element;

/**
 * @version $Id$
 */
public class WebApplicationRewriter {

    protected static final String SERVLET_CLASS = ShieldingServlet.class.getName();

    protected static final String LISTENER_CLASS = ShieldingListener.class.getName();

    protected static final String FILTER_CLASS = ShieldingServletFilter.class.getName();

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
        InputStream is = null;
        final Document webAppDoc;
        try {
            is = new BufferedInputStream(new FileInputStream(new File(webInfSlashWebXml)));
            webAppDoc = XMLUtils.parseXml(is);
        } catch (Exception e) {
            throw new MojoExecutionException("Unable to read web.xml from " + webInfSlashWebXml, e);
        } finally {
            if ( is != null ) {
                try {
                    is.close();
                } catch (IOException ignore) {
                    // ignore
                }
            }
        }

        // rewrite
        if ( WebApplicationRewriter.rewrite(webAppDoc, useShieldingRepository) ) {

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

    public static boolean rewrite(Document webAppDoc, boolean useShieldingRepository) {
        boolean rewritten = false;
        final Element rootElement = webAppDoc.getDocumentElement();
        // first rewrite servlets
        final List servlets = XMLUtils.getChildNodes(rootElement, "servlet");
        Iterator i = servlets.iterator();
        while ( i.hasNext() ) {
            final Element servletElement = (Element)i.next();
            final Element servletClassElement = XMLUtils.getChildNode(servletElement, "servlet-class");
            if ( servletClassElement != null ) {
                final String className = XMLUtils.getValue(servletClassElement);
                XMLUtils.setValue(servletClassElement, SERVLET_CLASS);
                // create init-param with real servlet class
                final Element initParamElem = webAppDoc.createElementNS(null, "init-param");
                final Element initParamNameElem = webAppDoc.createElementNS(null, "param-name");
                final Element initParamValueElem = webAppDoc.createElementNS(null, "param-value");
                initParamElem.appendChild(initParamNameElem);
                initParamElem.appendChild(initParamValueElem);
                XMLUtils.setValue(initParamNameElem, "servlet-class");
                XMLUtils.setValue(initParamValueElem, className);
                Element beforeElement = XMLUtils.getChildNode(servletElement, "load-on-startup");
                if ( beforeElement == null ) {
                    beforeElement = XMLUtils.getChildNode(servletElement, "run-as");                    
                    if ( beforeElement == null ) {
                        beforeElement = XMLUtils.getChildNode(servletElement, "security-role-ref");                    
                    }
                }
                if ( beforeElement == null ) {
                    servletElement.appendChild(initParamElem);
                } else {
                    servletElement.insertBefore(initParamElem, beforeElement);
                }
                rewritten = true;
            }
        }

        // now rewrite listeners
        final List listeners = XMLUtils.getChildNodes(rootElement, "listener");
        i = listeners.iterator();
        boolean hasListener = false;
        final StringBuffer rewrittenListeners = new StringBuffer();
        while ( i.hasNext() ) {
            final Element listenerElement = (Element)i.next();
            final Element listenerClassElement = XMLUtils.getChildNode(listenerElement, "listener-class");
            if ( listenerClassElement != null ) {
                final String className = XMLUtils.getValue(listenerClassElement);
                if ( rewrittenListeners.length() > 0 ) {
                    rewrittenListeners.append(',');
                }
                rewrittenListeners.append(className);
                if ( hasListener ) {
                    rootElement.removeChild(listenerElement);                        
                } else {
                    XMLUtils.setValue(listenerClassElement, LISTENER_CLASS);
                    hasListener = true;
                }
                rewritten = true;
            }
        }
        // remove old parameter
        i = XMLUtils.getChildNodes(rootElement, "context-param").iterator();
        while ( i.hasNext() ) {
            final Element child = (Element)i.next();
            if ( LISTENER_CLASS.equals(XMLUtils.getValue(XMLUtils.getChildNode(child, "param-name")))) {
                rootElement.removeChild(child);
            }
        }
        if ( hasListener ) {
            addContextParameter(rootElement, LISTENER_CLASS, rewrittenListeners.toString());
        }

        // and now filters
        i = XMLUtils.getChildNodes(rootElement, "filter").iterator();
        while ( i.hasNext() ) {
            final Element filterElement = (Element)i.next();
            final Element filterClassElement = XMLUtils.getChildNode(filterElement, "filter-class");
            if ( filterClassElement != null ) {
                final String className = XMLUtils.getValue(filterClassElement);
                XMLUtils.setValue(filterClassElement, FILTER_CLASS);
                // create init-param with real servlet class
                final Element initParamElem = webAppDoc.createElementNS(null, "init-param");
                final Element initParamNameElem = webAppDoc.createElementNS(null, "param-name");
                final Element initParamValueElem = webAppDoc.createElementNS(null, "param-value");
                initParamElem.appendChild(initParamNameElem);
                initParamElem.appendChild(initParamValueElem);
                XMLUtils.setValue(initParamNameElem, "filter-class");
                XMLUtils.setValue(initParamValueElem, className);
                filterElement.appendChild(initParamElem);
                rewritten = true;
            }
        }

        if ( !useShieldingRepository ) {
            addContextParameter(rootElement,
                                ShieldedClassLoaderManager.SHIELDED_CLASSLOADER_USE_REPOSITORY,
                                "false");
            rewritten = true;
        } else {
            if ( removeContextParameter(rootElement, ShieldedClassLoaderManager.SHIELDED_CLASSLOADER_USE_REPOSITORY) ) {
                rewritten = true;
            }
        }

        return rewritten;
    }

    protected static boolean removeContextParameter(Element root, String name) {
        boolean removed = false;
        final Iterator i = XMLUtils.getChildNodes(root, "context-param").iterator();
        while ( !removed && i.hasNext() ) {
            final Element parameterElement = (Element)i.next();
            final String paramName = XMLUtils.getValue(XMLUtils.getChildNode(parameterElement, "param-name"));
            if ( name.equals(paramName) ) {
                parameterElement.getParentNode().removeChild(parameterElement);
                removed = true;
            }
        }
        return removed;
    }

    protected static void addContextParameter(Element root, String name, String value) {
        removeContextParameter(root, name);
        // search the element where we have to put the new context parameter before!
        // we know that we have listeners so this is the last element to search for
        Element searchElement = XMLUtils.getChildNode(root, "context-param");
        if ( searchElement == null ) {
            searchElement = XMLUtils.getChildNode(root, "filter");
            if ( searchElement == null ) {
                searchElement = XMLUtils.getChildNode(root, "filter-mapping");
                if ( searchElement == null ) {
                    searchElement = XMLUtils.getChildNode(root, "listener");
                }
            }
        }
        final Element contextParamElement = root.getOwnerDocument().createElementNS(null, "context-param");
        final Element contextParamNameElement = root.getOwnerDocument().createElementNS(null, "param-name");
        final Element contextParamValueElement = root.getOwnerDocument().createElementNS(null, "param-value");
        contextParamElement.appendChild(contextParamNameElement);
        contextParamElement.appendChild(contextParamValueElement);
        XMLUtils.setValue(contextParamNameElement, name);
        XMLUtils.setValue(contextParamValueElement, value);
        root.insertBefore(contextParamElement, searchElement);
    }
}