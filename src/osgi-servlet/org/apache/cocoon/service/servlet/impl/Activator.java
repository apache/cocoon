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

import java.util.Hashtable;
import java.net.URL;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.core.BootstrapEnvironment;
import org.apache.cocoon.core.CoreUtil;
import org.apache.cocoon.core.osgi.OSGiBootstrapEnvironment;
import org.apache.cocoon.environment.Context;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;

/**
 * Activator which register a Cocoon servlet
 */

public class Activator implements BundleActivator {

    static BundleContext bc;
    static final String  SERVLET_ALIAS = "/";     // the http server root
    static final String  SITEMAP = "sitemap";

    private Hashtable registrations = new Hashtable();
    private Bundle sitemapBundle;
    private ClassLoader classLoader = getClass().getClassLoader();;
    private String contextURL;
    private Context environmentContext;
    private Logger logger;
    private CoreUtil coreUtil;

    public void start(BundleContext bc) throws BundleException {

        this.bc  = bc;
        try {
            BootstrapEnvironment env = new OSGiBootstrapEnvironment(this.classLoader, this.bc);
            env.log("OSGiBootstrapEnvironment created");
            this.coreUtil = new CoreUtil(env);
            env.log("CoreUtil created");
            this.contextURL = env.getContextURL();
            this.environmentContext = env.getEnvironmentContext();
            this.logger = env.getBootstrapLogger(null);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BundleException("Failed to create core util", e);
        }

        // FIXME: Add a BundleListener to detect if a sitemap bundle
        // is installed, updated or uninstalled.
        this.sitemapBundle = getSitemapBundle();
        this.logger.info("set sitemap bundle " + this.sitemapBundle);

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
  
    public void stop(BundleContext bc) throws BundleException {
    }

    private Bundle getSitemapBundle() {
        Bundle[] bundles = this.bc.getBundles();
        for (int i = 0; i < bundles.length; i++) {
            Bundle bundle = bundles[i];
            try {
                this.logger.info("checking bundle " + bundle + " name=" + bundle.getHeaders().get(Constants.BUNDLE_NAME) + " category=" + bundle.getHeaders().get(Constants.BUNDLE_CATEGORY) + " state=" + bundle.getState());
                if ((bundle.getState() == Bundle.INSTALLED ||
                     bundle.getState() == Bundle.RESOLVED ||
                     bundle.getState() == Bundle.ACTIVE)) {
                    this.logger.info("ok state");
                    if (SITEMAP.equals(bundle.getHeaders().get(Constants.BUNDLE_CATEGORY))) {
                        this.logger.info("sitemap");
                        return bundle;
                    }
                }
            } catch (NullPointerException e) {
                // BUNDLE_CATEGORY not present in the headers of the
                // bundle, nothing to do.
            }
        }
        return null;
    }

    private void setRoot(ServiceReference sr) {
        
        if(registrations.containsKey(sr)) {
            return; // already done
        }
        
        this.logger.info("set root for " + sr);

        HttpService http = (HttpService)bc.getService(sr);

        HttpContext context = new HttpContext() {
                public boolean handleSecurity(HttpServletRequest  request,
                                              HttpServletResponse response) 
                    throws IOException {
                    return true;
                }
        
                public URL getResource(String name) {
                    if (sitemapBundle != null) {
                        return sitemapBundle.getResource(name);
                    } else {
                        return null;
                    }
                }
        
                public String getMimeType(String reqEntry) {
                    return null; // server decides type
                }
            };
    
        try {
            Hashtable parameters = new Hashtable();
            parameters.put("init-classloader", "true");
            parameters.put("work-directory", "work");
            http.registerServlet(SERVLET_ALIAS,
                                 new BlocksServlet(),
//                                   new CocoonServlet(this.classLoader,
//                                                     this.contextURL,
//                                                     this.environmentContext,
//                                                     this.logger,
//                                                     this.coreUtil),
                                 parameters,
                                 context);

            registrations.put(sr, context);
        } catch (Exception e) {
            this.logger.info("Failed to register resource", e);
        }
    } 

    private void unsetRoot(ServiceReference sr) {
        if(!registrations.containsKey(sr)) {
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
