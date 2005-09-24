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

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.commons.jci.monitor.FilesystemAlterationListener;
import org.apache.commons.jci.monitor.FilesystemAlterationMonitor;
import org.apache.excalibur.source.SourceResolver;

public final class SitemapMonitorImpl extends AbstractLogEnabled implements SitemapMonitor, Serviceable, ThreadSafe, Initializable, Disposable {

    private ServiceManager manager;
    private SourceResolver resolver;
    private FilesystemAlterationMonitor monitor;

    public void service( ServiceManager manager ) throws ServiceException {
        this.manager = manager;
        this.resolver = (SourceResolver) manager.lookup(SourceResolver.ROLE);
    }

    public void initialize() throws Exception {
        monitor = new FilesystemAlterationMonitor();
        monitor.start();
    }

    public void dispose() {
        monitor.stop();
    }
    
    public void subscribe(final FilesystemAlterationListener listener) {
        monitor.addListener(listener);
    }
    
    public void unsubscribe(final FilesystemAlterationListener listener) {
        monitor.removeListener(listener);
    }
}
