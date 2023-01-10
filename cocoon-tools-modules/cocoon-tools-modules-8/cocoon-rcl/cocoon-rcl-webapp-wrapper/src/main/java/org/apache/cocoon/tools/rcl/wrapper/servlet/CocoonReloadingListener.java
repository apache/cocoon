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
package org.apache.cocoon.tools.rcl.wrapper.servlet;

import java.io.File;

import org.apache.commons.jci.listeners.ReloadingListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CocoonReloadingListener extends ReloadingListener {

    private final Log log = LogFactory.getLog(CocoonReloadingListener.class);

    private static boolean reload = false;

    private static boolean consoleOutput = false;

    public CocoonReloadingListener() {
        super();
    }

    public static void enableConsoleOutput() {
        CocoonReloadingListener.consoleOutput = true;
    }

    public static synchronized boolean isReload() {
        if (reload == true) {
            reload = false;
            return true;
        }

        return reload;
    }

    @Override
    public void onFileChange(File file) {
        super.onFileChange(file);

        // after the first change/deletion of a file is detected, log to the console
        this.changeDetected(file, "update");
    }

    @Override
    public void onFileCreate(File file) {
        super.onFileCreate(file);

        this.changeDetected(file, "create");
    }

    @Override
    public void onFileDelete(File file) {
        super.onFileDelete(file);

        // after the first change/deletion of a file is detected, log to the console
        this.changeDetected(file, "delete");
    }

    protected void changeDetected(File changedFile, String operation) {
        String changedFileParentPath = changedFile.getParent().replace('\\', '/');
        String changedFilePath = changedFile.getAbsolutePath().replace('\\', '/');

        if(changedFileParentPath.endsWith("META-INF/cocoon/spring") ||              // global Spring beans configurations
                changedFileParentPath.endsWith("config/avalon") ||                  // global Avalon components
                changedFilePath.endsWith(".xmap") ||                                // any file that ends with xmap (sitemaps)
                changedFilePath.endsWith(".xmap.xml") ||                            // any sitemap that ends with xmap.xml (sitemaps)
                changedFilePath.endsWith(".class") ||                               // Java class file change
                changedFilePath.endsWith(".jar") ||                                 // JAR file change
                changedFileParentPath.endsWith("config/spring")                     // local Spring bean configurations
        ) {
            this.log.debug("Configuration or .class file change detected [" + operation + "]: " + changedFile);

            if (CocoonReloadingListener.consoleOutput) {
                System.out.println("RCL [" + operation + "]: " + changedFile);
            }

            reload = true;
        } else {
            this.log.debug("Other file change detected, no reload [" + operation + "]: " + changedFile);  // any other file change
        }
    }
}