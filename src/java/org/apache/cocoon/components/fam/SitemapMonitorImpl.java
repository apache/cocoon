/* 
 * Copyright 2002-2005 The Apache Software Foundation
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.components.fam;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.commons.jci.monitor.FilesystemAlterationListener;
import org.apache.commons.jci.monitor.FilesystemAlterationMonitor;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;

public final class SitemapMonitorImpl extends AbstractLogEnabled implements SitemapMonitor, Serviceable, ThreadSafe, Initializable, Disposable {

    private ServiceManager manager;
    private SourceResolver resolver;
    private FilesystemAlterationMonitor monitor = new FilesystemAlterationMonitor();


    public void service( ServiceManager manager ) throws ServiceException {
        this.manager = manager;
        this.resolver = (SourceResolver) manager.lookup(SourceResolver.ROLE);
    }

    private File[] parseConfiguration(Configuration config) throws ConfigurationException {
        List urlList = new ArrayList();
        Configuration[] children = config.getChild("components").getChild("classpath").getChildren();
        for (int i = 0; i < children.length; i++) {
            Configuration child = children[i];
            String name = child.getName();
            Source src = null;
            try {
                src = resolver.resolveURI(child.getAttribute("src"));

                if ("class-dir".equals(name)) {
                    String dir = src.getURI();
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("class-dir:" + dir);
                    }
                    urlList.add(new File(dir.substring(5)));
                } else if ("lib-dir".equals(name)) {
                    String dir = src.getURI();
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("lib-dir:" + dir);
                    }
                    urlList.add(new File(dir.substring(5)));
                } else {
                    throw new ConfigurationException("Unexpected element " + name + " at " + child.getLocation());
                }
            } catch(ConfigurationException ce) {
                resolver.release(src);
                throw ce;
            } catch(Exception e) {
                resolver.release(src);
                throw new ConfigurationException("Error loading " + name + " at " + child.getLocation(), e);
            }
        }
        
        return (File[])urlList.toArray(new File[urlList.size()]);
    }

    public void subscribeSitemap( FilesystemAlterationListener listener, Configuration sitemap ) throws ConfigurationException {
        File[] dirs = parseConfiguration(sitemap);
        for (int i = 0; i < dirs.length; i++) {
            monitor.addListener(listener, dirs[i]);            
        }
    }

    public void unsubscribeSitemap( FilesystemAlterationListener listener) {
        monitor.removeListener(listener);
    }
    
    public void initialize() throws Exception {
        Thread myThread = new Thread(monitor);
        myThread.start();
    }


    public void dispose() {
    }
}
