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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.logging.Log;

/**
 * Deploy blocks to a monolithic Cocoon web application. The files contained by
 * a block are copied based on rules to the right places.
 * 
 * @version $Id: MonolithicCocoonDeployer.java 438198 2006-08-29 20:38:09Z
 *          lgawron $
 */
public class MonolithicCocoonDeployer {

    private Log logger;

    public MonolithicCocoonDeployer(Log logger) {
        this.logger = logger;
    }

    public void deploy(final Map libraries, final File basedir) throws DeploymentException {
        XPatchDeployer xwebPatcher = new XPatchDeployer("WEB-INF");
        xwebPatcher.setLogger(logger);
        // iterate over all blocks that need to be installed into a J2EE web
        // application
        for (Iterator it = libraries.entrySet().iterator(); it.hasNext();) {
            final Map.Entry entry = (Map.Entry) it.next();
            final Object id = entry.getKey();
            File lib = (File) entry.getValue();
            try {
                this.logger.info("Deploying " + id);
                RuleBasedZipExtractor zipExtractor = new RuleBasedZipExtractor(basedir, logger);

                // TODO clearly a hack, there should be a parameter what part of
                // source path should be removed, the rest should stay
                // preserving directory structure (currently only filename
                // stays)
                zipExtractor.addRule("WEB-INF/db/**", new SingleFileDeployer("WEB-INF/db"));
                zipExtractor.addRule("META-INF/xpatch/*.xweb", xwebPatcher);

                // extract all configurations files
                zipExtractor.extract(lib);
            } catch (IOException e) {
                throw new DeploymentException("Can't deploy '" + lib.getAbsolutePath() + "'.", e);
            }
        }

        InputStream sourceWebXmlFile = null;
        File webXml = new File(basedir, "WEB-INF/web.xml");
        try {
            sourceWebXmlFile = new FileInputStream(webXml);
            xwebPatcher.applyPatches(sourceWebXmlFile, "WEB-INF/web.xml");
        } catch (FileNotFoundException e) {
            throw new DeploymentException("Can't apply patches on " + webXml + ".", e);
        } finally {
            IOUtils.closeQuietly(sourceWebXmlFile);
        }

    }

}
