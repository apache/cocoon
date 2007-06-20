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
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.commons.io.IOUtils;
import org.apache.commons.jci.ReloadingClassLoader;
import org.apache.commons.jci.monitor.FilesystemAlterationMonitor;

public class ReloadingClassloaderManager {

    private static final String FILE_PROTOCOL = "file:";

    private static final String WEB_INF_RCL_URLCL_CONF = "/WEB-INF/cocoon/rclwrapper.urlcl.conf";
    
    private static final String WEB_INF_RCLWRAPPER_RCL_CONF = "/WEB-INF/cocoon/rclwrapper.rcl.conf";        
    
    private static ReloadingClassLoader reloadingClassloader = null;

    private ReloadingClassloaderManager() {
        // only allow static usage
    }
    
    public static synchronized ClassLoader getClassLoader(ServletContext context) {
      if (ReloadingClassloaderManager.reloadingClassloader == null) {
            final ClassLoader urlClassloader = createURLClassLoader(context);
            final ReloadingClassLoader classloader = new ReloadingClassLoader(urlClassloader);
            final FilesystemAlterationMonitor fam = new FilesystemAlterationMonitor();    
                    
            List lines = null;
            try {
                lines = IOUtils.readLines(context.getResourceAsStream(WEB_INF_RCLWRAPPER_RCL_CONF));
            } catch (IOException ioe) {
                throw new ReloadingClassloaderCreationException(
                        "Error while creating the URLClassLoader from context:/" + WEB_INF_RCLWRAPPER_RCL_CONF, ioe);
            }
            for (Iterator linesIt = lines.iterator(); linesIt.hasNext();) {
                String line = (String) linesIt.next();
                if (!line.startsWith(FILE_PROTOCOL)) {
                    throw new ReloadingClassloaderCreationException("Only support URLs with file: protocol.");
                }
                String url = line.substring(FILE_PROTOCOL.length());
                // windows paths
                if (url.indexOf(2) == ':') {
                    url = url.substring(1);
                }
                File directory = new File(url);
                org.apache.commons.jci.listeners.ReloadingListener rl = new CocoonReloadingListener();
                rl.addReloadNotificationListener(classloader);
                fam.addListener(directory, rl);
            }
            fam.start();
            ReloadingClassloaderManager.reloadingClassloader = classloader;
        }
        return ReloadingClassloaderManager.reloadingClassloader;
    }    
    
    protected static ClassLoader createURLClassLoader(ServletContext context) {
        try {
            List urlsList = new ArrayList();
            List lines = IOUtils.readLines(context.getResourceAsStream(WEB_INF_RCL_URLCL_CONF));
            for (Iterator linesIt = lines.iterator(); linesIt.hasNext();) {
                String line = (String) linesIt.next();
                urlsList.add(new URL(line));
            }
            URL[] urls = (URL[]) urlsList.toArray(new URL[urlsList.size()]);
            return new URLClassLoader(urls, ReloadingClassloaderManager.class.getClassLoader());
        } catch (Exception e) {
            throw new ReloadingClassloaderCreationException("Error while creating the URLClassLoader from context:/"
                    + WEB_INF_RCL_URLCL_CONF, e);
        }
    }
    
}
