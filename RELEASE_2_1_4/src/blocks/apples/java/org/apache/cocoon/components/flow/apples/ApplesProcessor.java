/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/
package org.apache.cocoon.components.flow.apples;

import java.util.List;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.cocoon.components.LifecycleHelper;
import org.apache.cocoon.components.flow.AbstractInterpreter;
import org.apache.cocoon.components.flow.ContinuationsDisposer;
import org.apache.cocoon.components.flow.InvalidContinuationException;
import org.apache.cocoon.components.flow.WebContinuation;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.ObjectModelHelper;
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
        Environment env)
        throws Exception {

        AppleController app = instantiateController(className);

        WebContinuation wk = this.continuationsMgr.createWebContinuation(app, null, 0, this);

        DefaultContext appleContext = new DefaultContext();
        appleContext.put("continuation-id", wk.getId());

        getLogger().debug("Pulling fresh apple through the lifecycle... | continuationid=" + wk.getId());
        
        LifecycleHelper.setupComponent( app, getLogger(), appleContext, 
                                        this.serviceManager, super.manager,  
                                        null, null, true);
        
        processApple(params, env, app, wk);
    }



    public void handleContinuation(
        String continuationId,
        List params,
        Environment env)
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
        processApple(params, env, app, wk);

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
        Environment env,
        AppleController app,
        WebContinuation wk)
        throws Exception {

        Request cocoonRequest = ObjectModelHelper.getRequest(env.getObjectModel());
        AppleRequest req = new DefaultAppleRequest(params, cocoonRequest);
        DefaultAppleResponse res = new DefaultAppleResponse();
        app.process(req, res);

        if (res.isRedirect()) {
            env.redirect(false, res.getURI());
        } else {
            String uri = res.getURI();
            getLogger().debug("Apple forwards to " + uri + " with bizdata= " + res.getData() + " and continuationid= " + wk.getId());
            this.forwardTo(uri, res.getData(), wk, env);
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
