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
package org.apache.cocoon.portal.pluto.deployment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.CascadingIOException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.portal.deployment.DeploymentException;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.commons.logging.Log;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Makes a portlet (web) application ready for deployment into Cocoon.
 *
 * @version $Id$
 */
public class Deployer {

    public static boolean deploy(InputStream inputStream,
                                 String  outputName,
                                 boolean stripLoggers,
                                 Log     logger,
                                 ServiceManager manager)
    throws DeploymentException, IOException, SAXException, ProcessingException {
        // first test, if the portlet is already deployed
        final File outputFile = new File(outputName);
        if ( outputFile.exists() ) {
            // TODO - check if the file to deploy is newer
            return false;
        }
        File tempFile = null;
        JarInputStream jin = null;
        JarOutputStream jout = null;
        FileChannel srcChannel = null;
        FileChannel dstChannel = null;

        try {
            String portletApplicationName = getPortletApplicationName(outputName);
            tempFile = File.createTempFile(portletApplicationName, "");
            tempFile.deleteOnExit();

            jin = new JarInputStream(inputStream);
            jout = new JarOutputStream(new FileOutputStream(tempFile));

            // copy over all of the files in the input war to the output
            // war except for web.xml, portlet.xml, and context.xml which
            // we parse for use later
            Document webXml = null;
            Document portletXml = null;
            Document contextXml = null;
            ZipEntry src;
            while ( (src = jin.getNextJarEntry()) != null) {
                String target = src.getName();
                if ("WEB-INF/web.xml".equals(target)) {
                    logger.debug("Found web.xml");
                    webXml = parseXml(jin);
                } else if ("WEB-INF/portlet.xml".equals(target)) {
                    logger.debug("Found WEB-INF/portlet.xml");
                    portletXml = parseXml(jin);
                } else if ("META-INF/context.xml".equals(target)) {
                    logger.debug("Found META-INF/context.xml");
                    contextXml = parseXml(jin);
                } else {
                    if ( stripLoggers && target.endsWith(".jar") &&
                         (target.startsWith("WEB-INF/lib/commons-logging") || target.startsWith("WEB-INF/lib/log4j"))) {
                        logger.info("Stripping logger: "+target);
                        continue;
                    }
                    addFile(target, jin, jout);
                }
            }

            if (webXml == null) {
                throw new DeploymentException("WEB-INF/web.xml is missing.");
            }
            if (portletXml == null) {
                throw new DeploymentException("WEB-INF/portlet.xml is missing.");
            }

            WebApplicationRewriter webRewriter = new WebApplicationRewriter(webXml);
            webRewriter.processWebXML();
            ContextRewriter contextRewriter = new ContextRewriter(contextXml, portletApplicationName);
            contextRewriter.processContextXML();
            PortletRewriter.process(portletXml);

            // write the web.xml, portlet.xml, and context.xml files
            addFile("WEB-INF/web.xml", webXml, jout);
            addFile("WEB-INF/portlet.xml", portletXml, jout);
            addFile("META-INF/context.xml", contextXml, jout);

            if (webRewriter.isPortletTaglibAdded()) {
                logger.info("Attempting to add portlet.tld to war...");
                InputStream is = Deployer.class.getResourceAsStream("portlet.tld");
                if (is == null) {
                    logger.warn("Failed to find portlet.tld in classpath");
                } else {
                    logger.info("Adding portlet.tld to war...");
                    try {
                        addFile("WEB-INF/tld/portlet.tld", is, jout);
                    } finally {
                        is.close();
                    }
                }
            }

            jout.close();
            jin.close();
            jin = null;
            jout = null;

            logger.info("Creating war " + outputName + " ...");
            // Now copy the new war to its destination
            srcChannel = new FileInputStream(tempFile).getChannel();
            dstChannel = new FileOutputStream(outputName).getChannel();
            dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
            srcChannel.close();
            srcChannel = null;
            dstChannel.close();
            dstChannel = null;
            tempFile.delete();
            tempFile = null;
            logger.info("War " + outputName + " created");
        } finally {
            if (srcChannel != null && srcChannel.isOpen()) {
                try {
                    srcChannel.close();
                } catch (IOException e1) {
                    // ignore
                }
            }
            if (dstChannel != null && dstChannel.isOpen()) {
                try {
                    dstChannel.close();
                } catch (IOException e1) {
                    // ignore
                }
            }
            if (jin != null) {
                try {
                    jin.close();
                    jin = null;
                } catch (IOException e1) {
                    // ignore
                }
            }
            if (jout != null) {
                try {
                    jout.close();
                    jout = null;
                } catch (IOException e1) {
                    // ignore
                }
            }
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
        return true;
    }

    protected static Document parseXml(InputStream source) throws IOException, SAXException {
        try {
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            documentFactory.setNamespaceAware(true);
            documentFactory.setValidating(false);
            DocumentBuilder docBuilder = documentFactory.newDocumentBuilder();
            // Parse using the local dtds instead of remote dtds. This
            // allows to deploy the application offline
            docBuilder.setEntityResolver(new EntityResolver() {
                public InputSource resolveEntity(String publicId, String systemId) throws SAXException,
                                java.io.IOException {
                    if (systemId.equals("http://java.sun.com/dtd/web-app_2_3.dtd")) {
                        return new InputSource(getClass().getResourceAsStream("web-app_2_3.dtd"));
                    }
                    return null;
                }
            });
            return docBuilder.parse(new WrapperInputStream(source));
        } catch (ParserConfigurationException pce) {
            throw new CascadingIOException("Creating document failed.", pce);
        }
    }

    protected static void addFile(String path, InputStream source, JarOutputStream jos)
    throws IOException  {
        jos.putNextEntry(new ZipEntry(path));
        try {
            byte[] buffer = new byte[4096];
            int count;
            while ((count = source.read(buffer)) > 0) {
                jos.write(buffer, 0, count);
            }
        } finally {
            jos.closeEntry();
        }
    }

    protected static void addFile(String path, Document source, JarOutputStream jos)
    throws IOException, ProcessingException {
        if (source != null) {
            jos.putNextEntry(new ZipEntry(path));
            final String content = XMLUtils.serializeNode(source);
            try {
                jos.write(content.getBytes("utf-8"));
            } finally {
                jos.closeEntry();
            }
        }
    }

    protected static String getPortletApplicationName(String path) {
        File file = new File(path);
        String name = file.getName();
        String portletApplicationName = name;

        int index = name.lastIndexOf(".");
        if (index > -1) {
            portletApplicationName = name.substring(0, index);
        }
        return portletApplicationName;
    }

    protected static final class WrapperInputStream extends InputStream {

        /** The wrapped input stream. */
        protected final InputStream wrapped;

        public WrapperInputStream(InputStream is) {
            this.wrapped = is;
        }

        /**
         * @see java.io.InputStream#available()
         */
        public int available() throws IOException {
            return this.wrapped.available();
        }

        /**
         * @see java.io.InputStream#close()
         */
        public void close() throws IOException {
            // we don't forward close
        }

        /**
         * @see java.io.InputStream#mark(int)
         */
        public synchronized void mark(int readlimit) {
            this.wrapped.mark(readlimit);
        }

        /**
         * @see java.io.InputStream#markSupported()
         */
        public boolean markSupported() {
            return this.wrapped.markSupported();
        }

        /**
         * @see java.io.InputStream#read()
         */
        public int read() throws IOException {
            return this.wrapped.read();
        }

        /**
         * @see java.io.InputStream#read(byte[], int, int)
         */
        public int read(byte[] b, int off, int len) throws IOException {
            return this.wrapped.read(b, off, len);
        }

        /**
         * @see java.io.InputStream#read(byte[])
         */
        public int read(byte[] b) throws IOException {
            return this.wrapped.read(b);
        }

        /**
         * @see java.io.InputStream#reset()
         */
        public synchronized void reset() throws IOException {
            this.wrapped.reset();
        }

        /**
         * @see java.io.InputStream#skip(long)
         */
        public long skip(long n) throws IOException {
            return this.wrapped.skip(n);
        }
    }
}
