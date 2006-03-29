/*
 * Copyright 1999-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.service.servlet.impl;

import java.util.HashSet;

import org.apache.avalon.excalibur.logger.LoggerManager;
import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.Cocoon;
import org.apache.cocoon.Processor;
import org.apache.cocoon.core.Core;
import org.apache.cocoon.core.osgi.OSGiLoggerManager;
import org.apache.cocoon.core.osgi.OSGiServiceManager;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.log.LogService;

/**
 * Activator which register a Cocoon servlet
 * @version $Id$
 */
public class Activator implements BundleActivator {

    static BundleContext bc;
    static final String  SERVLET_ALIAS = "/";     // the http server root

    private HashSet registrations = new HashSet();
    private ClassLoader classLoader = getClass().getClassLoader();
    private Logger logger;
    private Core core;
    private Processor processor;

    public void start(BundleContext bc) throws Exception {

        Activator.bc  = bc;

        LoggerManager logManager = new OSGiLoggerManager(bc, LogService.LOG_DEBUG);
        this.logger = logManager.getDefaultLogger();

        OSGiServiceManager manager = new OSGiServiceManager(Activator.bc);
        this.core = (Core)manager.lookup(Core.ROLE);
        this.processor = (Processor)manager.lookup(Cocoon.class.getName());

        ServiceListener listener = new ServiceListener() {
                public void serviceChanged(ServiceEvent ev) {
                    ServiceReference sr = ev.getServiceReference();

                    switch(ev.getType()) {
                    case ServiceEvent.REGISTERED:
                        setRoot(sr);
                        break;
                    case ServiceEvent.UNREGISTERING:
                        unsetRoot(sr);
                        break;
                    }
                }
            };

        String filter = "(objectclass=" + HttpService.class.getName() + ")";

        try {
            bc.addServiceListener(listener, filter);

            ServiceReference[] srl = bc.getServiceReferences(null, filter);
            for(int i = 0; srl != null && i < srl.length; i++) {
                listener.serviceChanged(new ServiceEvent(ServiceEvent.REGISTERED,
                                                         srl[i]));
            }
        } catch (Exception e) {
            this.logger.info("Failed to set up listener for http service", e);
        }
    }

    public void stop(BundleContext bc) throws Exception {
    }

    private void setRoot(ServiceReference sr) {

        if(registrations.contains(sr)) {
            return; // already done
        }

        this.logger.info("set root for " + sr);

        HttpService http = (HttpService)bc.getService(sr);

        try {
            http.registerServlet(SERVLET_ALIAS,
                                 new BlocksServlet(this.classLoader,
                                                   this.logger,
                                                   this.core,
                                                   this.processor),
                                 null,
                                 null);

            registrations.add(sr);
        } catch (Exception e) {
            this.logger.info("Failed to register resource", e);
        }
    }

    private void unsetRoot(ServiceReference sr) {
        if(!registrations.contains(sr)) {
            return; // nothing to do
        }

        this.logger.info("unset root for " + sr);

        HttpService http = (HttpService)bc.getService(sr);

        if(http != null) {
            http.unregister(SERVLET_ALIAS);
            bc.ungetService(sr);
        }
        registrations.remove(sr);
    }
}
