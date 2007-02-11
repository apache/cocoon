/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
package org.apache.cocoon.components.flow.apples;

import java.util.List;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.WrapperComponentManager;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.components.LifecycleHelper;
import org.apache.cocoon.components.flow.AbstractInterpreter;
import org.apache.cocoon.components.flow.ContinuationsDisposer;
import org.apache.cocoon.components.flow.InvalidContinuationException;
import org.apache.cocoon.components.flow.WebContinuation;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;

/**
 * ApplesProcessor is the core Cocoon component that provides the 'Apples' 
 * flow implementation. 
 */
public class ApplesProcessor extends AbstractInterpreter implements Serviceable, ContinuationsDisposer {


    private ServiceManager serviceManager;


    public void callFunction(
        String className,
        List params,
        Redirector redirector)
        throws Exception {

        AppleController app = instantiateController(className);

        WebContinuation wk = this.continuationsMgr.createWebContinuation(app, null, 0, this);

        DefaultContext appleContext = new DefaultContext();
        appleContext.put("continuation-id", wk.getId());

        getLogger().debug("Pulling fresh apple through the lifecycle... | continuationid=" + wk.getId());
        
        LifecycleHelper.setupComponent( app, getLogger(), appleContext, 
                                        this.serviceManager, new WrapperComponentManager(this.serviceManager),  
                                        null, null, true);
        
        processApple(params, redirector, app, wk);
    }



    public void handleContinuation(
        String continuationId,
        List params,
        Redirector redirector)
        throws Exception {

        WebContinuation wk =
            this.continuationsMgr.lookupWebContinuation(continuationId);
        if (wk == null) {
            // Throw an InvalidContinuationException to be handled inside the
            // <map:handle-errors> sitemap element.
            throw new InvalidContinuationException(
                "The continuation ID " + continuationId + " is invalid.");
        }

        AppleController app =
            (AppleController) wk.getContinuation();

        getLogger().debug("found apple from continuation: " + app);

        // TODO access control checks? exception to be thrown for illegal access?
        processApple(params, redirector, app, wk);

    }


    private AppleController instantiateController(String className)
        throws Exception {

        // TODO think about dynamic reloading of these beasts in future
        // classloading stuf et al.

        Class clazz = Class.forName(className);
        Object o = clazz.newInstance();
        return (AppleController) o;
    }



    private void processApple(
        List params,
        Redirector redirector,
        AppleController app,
        WebContinuation wk)
        throws Exception {

        Request cocoonRequest = ContextHelper.getRequest(this.avalonContext);
        AppleRequest req = new DefaultAppleRequest(params, cocoonRequest);
        DefaultAppleResponse res = new DefaultAppleResponse();
        app.process(req, res);

        if (res.isRedirect()) {
            redirector.redirect(false, res.getURI());
        } else {
            String uri = res.getURI();
            getLogger().debug("Apple forwards to " + uri + " with bizdata= " + res.getData() + " and continuationid= " + wk.getId());
            this.forwardTo(uri, res.getData(), wk, redirector);
        }

        //TODO allow for AppleResponse to set some boolean saying the use case
        // is completed and the continuation can be invalidated ?
    }


    public void disposeContinuation(WebContinuation webContinuation) {
        AppleController app =
            (AppleController) webContinuation.getContinuation();
        if (app instanceof Disposable) {
            ((Disposable)app).dispose();            
        }

    }


    public void service(ServiceManager serviceManager) throws ServiceException {
        super.service(serviceManager);
        this.serviceManager = serviceManager;
    }


}
