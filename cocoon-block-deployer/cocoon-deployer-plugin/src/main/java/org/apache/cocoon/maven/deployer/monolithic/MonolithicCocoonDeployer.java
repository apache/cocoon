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
package org.apache.cocoon.maven.deployer.monolithic;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.antlr.stringtemplate.StringTemplate;
import org.apache.cocoon.maven.deployer.utils.CopyUtils;
import org.apache.cocoon.maven.deployer.utils.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.logging.Log;

/**
 * Deploy blocks to a monolithic Cocoon web application. The files contained by
 * a block are copied to the right places. based on rules.
 * 
 * @version $Id: MonolithicCocoonDeployer.java 438198 2006-08-29 20:38:09Z
 *          lgawron $
 */
public class MonolithicCocoonDeployer {
    XPatchDeployer xwebPatcher = new XPatchDeployer("WEB-INF");

    private Log logger;

    public MonolithicCocoonDeployer(Log logger) {
        this.logger = logger;
    }

    public void deploy(final Map libraries, final File basedir, final String blocksdir) throws DeploymentException {
        deploy(libraries, basedir, blocksdir, new DevelopmentBlock[0], new DevelopmentProperty[0]);
    }

    public void deploy(final Map libraries, final File basedir, final String blocksdir,
            final DevelopmentBlock[] developmentBlocks, DevelopmentProperty[] developmentProperties)
            throws DeploymentException {

        // iterate over all blocks that need to be installed into a J2EE web
        // application
        for (Iterator it = libraries.entrySet().iterator(); it.hasNext();) {
            final Map.Entry entry = (Map.Entry) it.next();
            final Object id = entry.getKey();
            File lib = (File) entry.getValue();
            try {
                this.logger.info("Deploying " + id);
                RuleBasedZipExtractor zipExtractor = new RuleBasedZipExtractor(basedir, logger);
                zipExtractor.addRule("META-INF/legacy/cocoon.xconf", new SingleFileDeployer("WEB-INF/cocoon"));
                zipExtractor.addRule("META-INF/legacy/xconf/**", new SingleFileDeployer("WEB-INF/cocoon/xconf"));
                zipExtractor.addRule("META-INF/legacy/sitemap-additions/**", new SingleFileDeployer(
                        "WEB-INF/cocoon/sitemap-additions"));
                zipExtractor.addRule("META-INF/spring/**", new SingleFileDeployer("WEB-INF/cocoon/spring"));

                // TODO clearly a hack, there should be a parameter what part of
                // source path should be removed, the rest should stay
                // preserving directory structure (currently only filename
                // stays)
                zipExtractor.addRule("META-INF/properties/**", new SingleFileDeployer("WEB-INF/cocoon/", true));
                zipExtractor.addRule("WEB-INF/classes/**", new SingleFileDeployer("WEB-INF/classes"));
                zipExtractor.addRule("WEB-INF/db/**", new SingleFileDeployer("WEB-INF/db"));
                zipExtractor.addRule("COB-INF**", new SingleFileDeployer(blocksdir + "/" + (String) id, true));
                zipExtractor.addRule("META-INF/xpatch/*.xweb", xwebPatcher);
                // extract all configurations files
                zipExtractor.extract(lib);
            } catch (IOException e) {
                throw new DeploymentException("Can't deploy '" + lib.getAbsolutePath() + "'.", e);
            }
        }

        // deploy all blocks that are under development by adding their
        // src/main/java (--> <map:classpath>),
        // src/main/resources/COB-INF (--> <map:mount>), and
        // src/main/resources/META-INF/*
        // (--> <map:include>) to the root sitemap.
        if (developmentBlocks != null && developmentBlocks.length > 0) {
            Map templateObjects = new HashMap();
            templateObjects.put("devblocks", developmentBlocks);
            templateObjects.put("curblock", developmentBlocks[developmentBlocks.length - 1]);
            writeStringTemplateToFile(basedir, "sitemap.xmap", templateObjects);
            writeStringTemplateToFile(basedir, "WEB-INF/cocoon/cocoon.xconf", templateObjects);

            copyFile(basedir, "blocks/sitemap.xmap");
            copyFile(basedir, "WEB-INF/cocoon/log4j.xconf");
            // copyFile(basedir, "WEB-INF/web.xml");

            for (int i = 0; i < developmentBlocks.length; ++i) {
                DevelopmentBlock currentBlock = developmentBlocks[i];
                if (currentBlock.xPatchPath != null) {
                    URI uri = null;
                    try {
                        uri = new URI(currentBlock.xPatchPath);
                    } catch (URISyntaxException e) {
                        throw new RuntimeException("should not happen", e);
                    }

                    File xPatchDir = new File(uri);
                    File[] xPatchFiles = xPatchDir.listFiles(new FileFilter() {
                        public boolean accept(File pathname) {
                            if (pathname.isDirectory())
                                return false;
                            if (pathname.getName().endsWith(".xweb"))
                                return true;
                            return false;
                        }
                    });

                    for (int j = 0; j < xPatchFiles.length; ++j) {
                        File currentFile = xPatchFiles[j];
                        try {
                            xwebPatcher.addPatch(currentFile);
                        } catch (IOException e) {
                            throw new DeploymentException("Can't process patch file '" + currentFile.getAbsolutePath()
                                    + "'.", e);
                        }
                    }
                }
            }

            InputStream sourceWebXmlFile = readResourceFromClassloader("WEB-INF/web.xml");
            try {
                xwebPatcher.applyPatches(sourceWebXmlFile, "WEB-INF/web.xml");
            } finally {
                IOUtils.closeQuietly(sourceWebXmlFile);
            }
            copyFile(basedir, "WEB-INF/applicationContext.xml");
            copyFile(basedir, "WEB-INF/cocoon/properties/core.properties");
        }

        // write properties
        if (developmentProperties != null && developmentProperties.length > 0) {
            Properties properties = new Properties();
            for (int i = 0; i < developmentProperties.length; i++) {
                properties.setProperty(developmentProperties[0].name, developmentProperties[0].value);
            }
            writeProperties(basedir, "WEB-INF/cocoon/properties/dev/core.properties", properties);
        }
    }

    private void writeProperties(final File basedir, final String propertiesFile, final Properties properties) {
        File outFile = new File(basedir, propertiesFile);
        OutputStream os = null;
        try {
            os = new FileOutputStream(FileUtils.createPath(outFile));
            this.logger.info("Deploying dev properties to " + propertiesFile);
            properties.store(os, null);
        } catch (IOException e) {
            throw new DeploymentException("Can't save properties to " + propertiesFile, e);
        } finally {
            IOUtils.closeQuietly(os);
        }
    }

    private void copyFile(final File basedir, final String fileName) {
        try {
            File outFile = FileUtils.createPath(new File(basedir, fileName));
            this.logger.info("Deploying resource file to " + fileName);
            CopyUtils.copy(readResourceFromClassloader(fileName), new FileOutputStream(outFile));
        } catch (FileNotFoundException e) {
            throw new DeploymentException("Can't copy to " + fileName, e);
        } catch (IOException e) {
            throw new DeploymentException("Can't copy to " + fileName, e);
        }
    }

    private void writeStringTemplateToFile(final File basedir, final String fileName, final Map templateObjects) {
        OutputStream fos = null;
        try {
            File outFile = FileUtils.createPath(new File(basedir, fileName));
            fos = new BufferedOutputStream(new FileOutputStream(outFile));
            InputStream fileIs = readResourceFromClassloader(fileName);
            StringTemplate stringTemplate = new StringTemplate(IOUtils.toString(fileIs));
            for (Iterator templateObjectsIt = templateObjects.keySet().iterator(); templateObjectsIt.hasNext();) {
                Object key = templateObjectsIt.next();
                stringTemplate.setAttribute((String) key, templateObjects.get(key));
            }
            this.logger.info("Deploying string-template to " + fileName);
            IOUtils.write(stringTemplate.toString(), fos);
        } catch (FileNotFoundException e) {
            throw new DeploymentException(fileName + " not found.", e);
        } catch (IOException e) {
            throw new DeploymentException("Error while reading or writing.", e);
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                throw new DeploymentException("Error while closing the output stream.", e);
            }
        }
    }

    private InputStream readResourceFromClassloader(String fileName) {
        return MonolithicCocoonDeployer.class.getClassLoader().getResourceAsStream(
                "org/apache/cocoon/maven/deployer/monolithic/" + fileName);
    }

}