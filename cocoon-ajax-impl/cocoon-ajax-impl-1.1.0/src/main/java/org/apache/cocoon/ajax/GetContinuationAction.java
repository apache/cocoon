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
package org.apache.cocoon.ajax;

import java.util.Collections;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.acting.ServiceableAction;
import org.apache.cocoon.components.flow.ContinuationsManager;
import org.apache.cocoon.components.flow.FlowHelper;
import org.apache.cocoon.components.flow.WebContinuation;
import org.apache.cocoon.el.objectmodel.ObjectModel;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.sitemap.SitemapParameters;

/**
 * Get a continuation knowing its identifier, and set it as the current continuation
 * in the object model, in the same manner as <code>cocoon.sendPageAndWait()</code> does.
 * This action is useful when an Ajax request is made that needs to access data related
 * to the continuation that originally displayed the current page.
 * 
 * <p>
 * The continuation id is either the <code>src</code> attribute in <code>&lt;map:act&gt;</code>
 * or, if not specified, the <code>continuation-id</code> request parameter.
 *
 * <p>
 * This action is successful if the continuation exists and is still valid.
 * 
 * @cocoon.sitemap.component.documentation
 * Get a continuation knowing its identifier, and set it as the current continuation
 * in the object model, in the same manner as <code>cocoon.sendPageAndWait()</code> does.
 * This action is useful when an Ajax request is made that needs to access data related
 * to the continuation that originally displayed the current page.
 *
 * @since 2.1.8
 * @version $Id$
 */
public class GetContinuationAction extends ServiceableAction implements ThreadSafe, Disposable {
    ContinuationsManager contManager;
    ObjectModel newObjectModel;

    public void service(ServiceManager manager) throws ServiceException {
        super.service(manager);
        this.contManager = (ContinuationsManager)manager.lookup(ContinuationsManager.ROLE);
        this.newObjectModel = (ObjectModel)manager.lookup(ObjectModel.ROLE);
    }

    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception {
        String continuationId;
        if (source == null) {
            continuationId = ObjectModelHelper.getRequest(objectModel).getParameter("continuation-id");
        } else {
            continuationId = source;
        }
        
        // The interpreter id is the sitemap's URI
        String interpreterId = SitemapParameters.getLocation(parameters).getURI();
        WebContinuation wk = this.contManager.lookupWebContinuation(continuationId, interpreterId);
        if (wk == null || wk.disposed()) {
            return null;
        } else {
            // Getting the continuation updates the last access time
            wk.getContinuation();
            FlowHelper.setWebContinuation(objectModel, newObjectModel, wk);
            return Collections.EMPTY_MAP;
        }
    }

    public void dispose() {
        this.manager.release(this.contManager);
    }
}
