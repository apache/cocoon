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

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.components.LifecycleHelper;
import org.apache.cocoon.components.flow.AbstractInterpreter;
import org.apache.cocoon.components.flow.ContinuationsDisposer;
import org.apache.cocoon.components.flow.InvalidContinuationException;
import org.apache.cocoon.components.flow.WebContinuation;
import org.apache.cocoon.components.flow.apples.AppleController;
import org.apache.cocoon.components.flow.apples.AppleRequest;
import org.apache.cocoon.components.flow.apples.DefaultAppleRequest;
import org.apache.cocoon.components.flow.apples.DefaultAppleResponse;
import org.apache.cocoon.components.flow.apples.StatelessAppleController;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Response;

/**
 * ApplesProcessor is the core Cocoon component that provides the 'Apples' flow
 * implementation.
 * 
 * @version $Id$
 */
public class ApplesProcessor extends AbstractInterpreter implements ContinuationsDisposer {

    public void callFunction(String className, List params, Redirector redirector) throws Exception {
        AppleController app = instantiateController(className);

        WebContinuation wk = null;
        if (!(app instanceof StatelessAppleController)) {
            wk = this.continuationsMgr.createWebContinuation(app, null, 0, getInterpreterID(), this);
            if (getLogger().isDebugEnabled())
                getLogger().debug("Instantiated a stateful apple, continuationid = " + wk.getId());
        }

        DefaultContext appleContext = new DefaultContext(avalonContext);
        if (wk != null) {
            appleContext.put("continuation-id", wk.getId());
        }

        // Use the current sitemap's service manager for components
        ServiceManager sitemapManager;
        try {
            sitemapManager = (ServiceManager) avalonContext.get(ContextHelper.CONTEXT_SITEMAP_SERVICE_MANAGER);
        } catch (ContextException e) {
            throw new CascadingRuntimeException("Cannot get sitemap service manager", e);
        }

        LifecycleHelper.setupComponent(app, getLogger(), appleContext, sitemapManager, null, true);
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

    protected AppleController instantiateController(String className) throws Exception {
        Class clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
        Object o = clazz.newInstance();
        return (AppleController) o;
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
