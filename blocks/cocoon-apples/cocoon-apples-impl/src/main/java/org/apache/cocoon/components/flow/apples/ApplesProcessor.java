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
package org.apache.cocoon.components.flow.apples;

import java.util.List;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.springframework.web.context.WebApplicationContext;

import org.apache.cocoon.components.LifecycleHelper;
import org.apache.cocoon.components.flow.AbstractInterpreter;
import org.apache.cocoon.components.flow.ContinuationsDisposer;
import org.apache.cocoon.components.flow.InvalidContinuationException;
import org.apache.cocoon.components.flow.WebContinuation;
import org.apache.cocoon.core.container.spring.avalon.AvalonUtils;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Response;
import org.apache.cocoon.spring.configurator.WebAppContextUtils;

/**
 * ApplesProcessor is the core Cocoon component that provides the 'Apples' flow
 * implementation.
 *
 * @version $Id$
 */
public class ApplesProcessor extends AbstractInterpreter
                             implements ContinuationsDisposer {

    /**
     * @see org.apache.cocoon.components.flow.Interpreter#callFunction(String, List, Redirector)
     */
    public void callFunction(String className, List params, Redirector redirector) throws Exception {
        // Get the current web application context
        final WebApplicationContext webAppContext = WebAppContextUtils.getCurrentWebApplicationContext();

        // Use the current sitemap's service manager for components
        final ServiceManager sitemapManager = (ServiceManager) webAppContext.getBean(AvalonUtils.SERVICE_MANAGER_ROLE);

        AppleController app = instantiateController(className, sitemapManager);

        WebContinuation wk = null;
        if (!(app instanceof StatelessAppleController)) {
            wk = this.continuationsMgr.createWebContinuation(app, null, 0, getInterpreterID(), this);
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Instantiated a stateful apple, continuationid = " + wk.getId());
            }
        }

        DefaultContext appleContext = new DefaultContext(avalonContext);
        if (wk != null) {
            appleContext.put("continuation-id", wk.getId());
        }

        LifecycleHelper.setupComponent(app, getLogger(), appleContext, sitemapManager, null);

        processApple(params, redirector, app, wk);
    }

    public void handleContinuation(String continuationId, List params, Redirector redirector) throws Exception {
        WebContinuation wk = this.continuationsMgr.lookupWebContinuation(continuationId, getInterpreterID());
        if (wk == null) {
            // Throw an InvalidContinuationException to be handled inside the
            // <map:handle-errors> sitemap element.
            throw new InvalidContinuationException("The continuation ID " + continuationId + " is invalid.");
        }

        AppleController app = (AppleController) wk.getContinuation();

        getLogger().debug("found apple from continuation: " + app);

        // TODO access control checks? exception to be thrown for illegal
        // access?
        processApple(params, redirector, app, wk);
    }

    protected AppleController instantiateController(String appleName, ServiceManager sitemapManager)
    throws AppleNotFoundException {
    	if(appleName.startsWith("#")) {
    		String beanName = appleName.substring(1);
    		try {
    			return (AppleController) sitemapManager.lookup(beanName);
    		} catch(ClassCastException e) {
    			throw new AppleNotFoundException("The bean '" + beanName + "' doesn't implement the AppleController interface.", e);
    		} catch (ServiceException e) {
    			throw new AppleNotFoundException("Can't find any bean of name '" + beanName + "'.", e);
			}
    	}
        AppleController appleController;
		try {
			Class clazz = Thread.currentThread().getContextClassLoader().loadClass(appleName);
	        appleController = (AppleController) clazz.newInstance();
		} catch (ClassNotFoundException e) {
			throw new AppleNotFoundException("Can't find a class of name '" + appleName + "'.", e);
		} catch (InstantiationException e) {
			throw new AppleNotFoundException("Can't instatiate the class '" + appleName + "'.", e);
		} catch (IllegalAccessException e) {
			throw new AppleNotFoundException("The class '" + appleName + "' can't be accessed. Check the class modifier.", e);
		}
		return appleController;
    }

    private void processApple(List params, Redirector redirector, AppleController app, WebContinuation wk)
    throws Exception {
        Request cocoonRequest = ObjectModelHelper.getRequest(this.processInfoProvider.getObjectModel());
        AppleRequest req = new DefaultAppleRequest(params, cocoonRequest);
        Response cocoonResponse = ObjectModelHelper.getResponse(this.processInfoProvider.getObjectModel());
        DefaultAppleResponse res = new DefaultAppleResponse(cocoonResponse);

        try {
            app.process(req, res);
        } finally {
            if (wk == null) {
                // dispose stateless apple immediatelly
                if (app instanceof Disposable) {
                    try {
                        ((Disposable) app).dispose();
                    } catch (Exception e) {
                        getLogger().error("Error disposing Apple instance.", e);
                    }
                }
            }
        }

        if (res.isRedirect()) {
            redirector.redirect(false, res.getURI());
        }
        else if (res.isSendStatus()) {
        	redirector.sendStatus(res.getStatus());
        }
        else {
            String uri = res.getURI();
            if (getLogger().isDebugEnabled()) {
                getLogger().debug(
                        "Apple forwards to " + uri + " with bizdata= " + res.getData()
                                + (wk != null ? " and continuationid= " + wk.getId() : " without continuationid"));
            }

            // Note: it is ok for wk to be null
            this.forwardTo(uri, res.getData(), wk, redirector);
        }

        // TODO allow for AppleResponse to set some boolean saying the use case
        // is completed and the continuation can be invalidated ?
    }

    public void disposeContinuation(WebContinuation webContinuation) {
        AppleController app = (AppleController) webContinuation.getContinuation();
        if (app instanceof Disposable) {
            ((Disposable) app).dispose();
        }

    }
}
