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
package org.apache.cocoon.portal.deployment.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.cocoon.portal.deployment.DeploymentObject;

/**
 * Default implementation of a {@link org.apache.cocoon.portal.deployment.DeploymentObject}.
 *
 * @version $Id$
 */
public class DefaultDeploymentObject implements DeploymentObject {

    /** The deployment artifact. */
    protected String deploymentObject;

    protected ZipFile zipFile;

    /**
     * @throws SourceNotDeployableException
     */
    public DefaultDeploymentObject(String deploymentObject)
    throws SourceNotDeployableException {
        if ( !verifyExtension(deploymentObject)) {
            throw new SourceNotDeployableException("Artifact '" + deploymentObject
                    + " is not supported by the default deployment object.");
        }
        this.deploymentObject = deploymentObject;
    }

    /**
     * @see org.apache.cocoon.portal.deployment.DeploymentObject#close()
     */
    public void close() throws IOException {
        if (zipFile != null) {
            zipFile.close();
            zipFile = null;
        }
        this.deploymentObject = null;
    }

    /**
     * @see org.apache.cocoon.portal.deployment.DeploymentObject#getConfiguration(java.lang.String)
     */
    public InputStream getConfiguration(String configPath)
    throws IOException {
        final ZipFile file = this.getZipFile();
        final ZipEntry entry = file.getEntry(configPath);
        if (entry != null) {
            return file.getInputStream(entry);
        }
        return null;
    }

    /**
     * @see org.apache.cocoon.portal.deployment.DeploymentObject#getName()
     */
    public String getName() {
        int pos = this.deploymentObject.lastIndexOf('/');
        return this.deploymentObject.substring(pos+1);
    }

    /**
     * Get a zip file for the artifact.
     * @throws IOException
     */
    protected ZipFile getZipFile() throws IOException {
        if (this.zipFile == null) {
            if( !this.deploymentObject.startsWith( "file:" ) ) {
                throw new IOException("Handling of sources of type '" + this.deploymentObject + "' is currently not supported.");
            }
            File file = new File(this.deploymentObject.substring( 5 ));
            this.zipFile = new ZipFile(file);
        }
        return this.zipFile;
    }


    /**
     * @see org.apache.cocoon.portal.deployment.DeploymentObject#getUri()
     */
    public String getUri() {
        return this.deploymentObject;
    }

    /**
     * Test if the extension of the source is either "war", "jar" or "zip".
     */
    protected boolean verifyExtension(String uri) {
        int dot = uri.lastIndexOf('.');
        if (dot != -1) {
            final String ext = uri.substring(dot);
            return ext.equals(".war") || ext.equals(".jar") || ext.equals(".zip");
        }
        return false;
    }

}