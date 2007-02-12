/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

// TODO rcl
//import org.apache.avalon.framework.activity.Disposable;
//import org.apache.avalon.framework.activity.Initializable;
//import org.apache.avalon.framework.logger.AbstractLogEnabled;
//import org.apache.avalon.framework.thread.ThreadSafe;
//import org.apache.cocoon.classloader.reloading.Monitor;
//import org.apache.cocoon.classloader.reloading.NotifyingResourceStore;
//import org.apache.commons.jci.listeners.NotificationListener;
//import org.apache.commons.jci.listeners.ReloadingListener;
//import org.apache.commons.jci.monitor.FilesystemAlterationListener;
//import org.apache.commons.jci.monitor.FilesystemAlterationMonitor;

/**
 * @version $Id$
 */
public final class MonitorImpl  {
// TODO rcl    
//    extends AbstractLogEnabled 
//    implements Monitor, ThreadSafe, Initializable, Disposable {
//
//    private FilesystemAlterationMonitor monitor;
//    private NotificationListener sitemapNotifier;
//
//    /**
//     * @see org.apache.avalon.framework.activity.Initializable#initialize()
//     */
//    public void initialize() throws Exception {
//        this.monitor = new FilesystemAlterationMonitor();
//        this.monitor.start();
//    }
//
//    /**
//     * @see org.apache.avalon.framework.activity.Disposable#dispose()
//     */
//    public void dispose() {
//        if ( this.monitor != null ) {
//            this.monitor.stop();
//            this.monitor = null;
//        }
//    }
//
//    /**
//     * @see org.apache.cocoon.classloader.reloading.Monitor#subscribe(org.apache.commons.jci.monitor.FilesystemAlterationListener)
//     */
//    public void subscribe(final FilesystemAlterationListener listener) {
//        this.monitor.addListener(listener);
//        this.monitor.addListener(new ReloadingListener(listener.getRepository(),new NotifyingResourceStore(this.sitemapNotifier)));
//    }
//
//    /**
//     * @see org.apache.cocoon.classloader.reloading.Monitor#unsubscribe(org.apache.commons.jci.monitor.FilesystemAlterationListener)
//     */
//    public void unsubscribe(final FilesystemAlterationListener listener) {
//        this.monitor.removeListener(listener);
//        this.monitor.removeListener(new ReloadingListener(listener.getRepository(),new NotifyingResourceStore(this.sitemapNotifier)));
//    }
//    
//    /**
//     * @see org.apache.cocoon.classloader.reloading.Monitor#setSitemapNotifier(org.apache.commons.jci.listeners.NotificationListener)
//     */
//    public void setSitemapNotifier(NotificationListener sitemapNotifier) {
//        this.sitemapNotifier = sitemapNotifier;
//    }    
}
