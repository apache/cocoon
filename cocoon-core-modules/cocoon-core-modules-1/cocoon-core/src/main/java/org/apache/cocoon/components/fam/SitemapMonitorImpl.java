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
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.commons.jci.monitor.FilesystemAlterationListener;
import org.apache.commons.jci.monitor.FilesystemAlterationMonitor;

/**
 * @version $Id$
 */
public final class SitemapMonitorImpl 
    extends AbstractLogEnabled 
    implements SitemapMonitor, ThreadSafe, Initializable, Disposable {

    private FilesystemAlterationMonitor monitor;

    /**
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize() throws Exception {
        this.monitor = new FilesystemAlterationMonitor();
        this.monitor.start();
    }

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.monitor != null ) {
            this.monitor.stop();
            this.monitor = null;
        }
    }

    /**
     * @see org.apache.cocoon.components.fam.SitemapMonitor#subscribe(org.apache.commons.jci.monitor.FilesystemAlterationListener)
     */
    public void subscribe(final FilesystemAlterationListener listener) {
        this.monitor.addListener(listener);
    }

    /**
     * @see org.apache.cocoon.components.fam.SitemapMonitor#unsubscribe(org.apache.commons.jci.monitor.FilesystemAlterationListener)
     */
    public void unsubscribe(final FilesystemAlterationListener listener) {
        this.monitor.removeListener(listener);
    }
}
