/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.cocoon.maven.docscleaner;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * This plugin removes all document which are not read from Daisy, the changes page, the index
 * page or the .htaccess file.
 *
 * This is recommended to run this goal before you run site:deploy in order to avoid publishing
 * any docs that contain snapshot information.
 *
 * @goal clean
 * @requiresProject true
 * @version $Id$
 */
public class DocsCleanerMojo extends AbstractMojo {

    /**
     * Directory containing the generated project sites and report distributions.
     *
     * @parameter expression="${project.reporting.outputDirectory}"
     * @required
     */
    protected File siteOutputDirectory;

    public void execute() throws MojoExecutionException, MojoFailureException {
        File[] files = this.siteOutputDirectory.listFiles();
        if (null != files) {
            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                if (deleteFile(f)) {
                    this.getLog().info("[delete] " + f.getAbsolutePath());
                    if (f.isDirectory()) {
                        try {
                            FileUtils.deleteDirectory(f);
                        } catch (IOException e) {
                            throw new MojoExecutionException("Can't delete directory " + f.getAbsolutePath());
                        }
                    } else {
                        f.delete();
                    }
                }
            }
        }
    }

    private boolean deleteFile(File file) {
        if("project-summary.html".equals(file.getName())) {
            return true;
        }
        if("dependencies.html".equals(file.getName())) {
            return true;
        }
        return false;
    }

}
