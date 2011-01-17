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
import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.commons.io.IOUtils;
import org.apache.commons.jci.ReloadingClassLoader;
import org.apache.commons.jci.monitor.FilesystemAlterationMonitor;

public abstract class ReloadingClassloaderManager {

    private static final String FILE_PROTOCOL = "file:";

    private static ClassLoader reloadingClassloader = null;

    public static synchronized ClassLoader getClassLoader(ServletContext context) {
        // if there is no classloader, create one
        if (ReloadingClassloaderManager.reloadingClassloader == null) {
            // create the URL classloader
            final ClassLoader urlClassloader = createURLClassLoader(context);

            // check, if the reloading classloader should be used
            if (isReloadingClassloaderEnabled(context)) {
                final ReloadingClassLoader classloader = new ReloadingClassLoader(urlClassloader);
                final FilesystemAlterationMonitor fam = new FilesystemAlterationMonitor();

                org.apache.commons.jci.listeners.ReloadingListener rl = new CocoonReloadingListener();
                rl.addReloadNotificationListener(classloader);

                addResourcesToFam(fam, rl, getRclConfResourceUrls(context));
                fam.start();

                ReloadingClassloaderManager.reloadingClassloader = classloader;
            } else {
                // otherwise use the URL classloader only
                ReloadingClassloaderManager.reloadingClassloader = urlClassloader;
            }
        }

        return ReloadingClassloaderManager.reloadingClassloader;
    }

    protected static ClassLoader createURLClassLoader(ServletContext context) {
        try {
            List<URL> urlsList = new ArrayList<URL>();
            List<?> lines = IOUtils.readLines(context.getResourceAsStream(Constants.WEB_INF_RCL_URLCL_CONF));

            for (Iterator<?> linesIt = lines.iterator(); linesIt.hasNext();) {
                String line = (String) linesIt.next();
                urlsList.add(new URL(line));
            }

            URL[] urls = urlsList.toArray(new URL[urlsList.size()]);

            return new URLClassLoader(urls, ReloadingClassloaderManager.class.getClassLoader());
        } catch (Exception e) {
            throw new ReloadingClassloaderCreationException("Error while creating the URLClassLoader from context:/"
                    + Constants.WEB_INF_RCL_URLCL_CONF, e);
        }
    }

    protected static boolean isReloadingClassloaderEnabled(ServletContext servletContext) {
        Properties rclProps = new Properties();

        try {
            rclProps.load(servletContext.getResourceAsStream(Constants.WEB_INF_RCLWRAPPER_PROPERTIES));
        } catch (IOException e) {
            throw new ReloadingClassloaderCreationException("Error while reading "
                    + Constants.WEB_INF_RCLWRAPPER_PROPERTIES + " from servlet context.", e);
        }

        String reloadingEnabled = rclProps.getProperty(Constants.RELOADING_CLASSLOADER_ENABLED, "true");
        return reloadingEnabled.trim().toLowerCase().equals("true");
    }

    private static void addResourcesToFam(final FilesystemAlterationMonitor fam,
            org.apache.commons.jci.listeners.ReloadingListener rl, List<?> resourceUrl) {
        for (Iterator<?> linesIt = resourceUrl.iterator(); linesIt.hasNext();) {
            String line = (String) linesIt.next();
            if (!line.startsWith(FILE_PROTOCOL)) {
                throw new ReloadingClassloaderCreationException("Only support URLs with file: protocol.");
            }
            String url = line.substring(FILE_PROTOCOL.length());
            // windows paths
            if (url.indexOf(2) == ':') {
                url = url.substring(1);
            }

            // add libraries to the RCL
            if(url.endsWith(".jar")) {
                // deactivate reloading of JARs since it isn't supported the JCI:
                // see https://issues.apache.org/jira/browse/JCI-60
                continue;
            }
            fam.addListener(new File(url), rl);
        }
    }

    private static List<?> getRclConfResourceUrls(ServletContext context) {
        List<?> lines = null;

        try {
            lines = IOUtils.readLines(context.getResourceAsStream(Constants.WEB_INF_RCLWRAPPER_RCL_CONF));
        } catch (IOException ioe) {
            throw new ReloadingClassloaderCreationException("Error while creating the URLClassLoader from context:/"
                    + Constants.WEB_INF_RCLWRAPPER_RCL_CONF, ioe);
        }

        return lines;
    }
}
