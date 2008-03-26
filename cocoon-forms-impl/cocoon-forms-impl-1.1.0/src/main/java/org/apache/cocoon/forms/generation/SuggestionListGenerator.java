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
package org.apache.cocoon.forms.generation;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.flow.ContinuationsManager;
import org.apache.cocoon.components.flow.InvalidContinuationException;
import org.apache.cocoon.components.flow.WebContinuation;
import org.apache.cocoon.components.flow.javascript.fom.FOM_Cocoon;
import org.apache.cocoon.components.flow.javascript.fom.FOM_JavaScriptFlowHelper;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.forms.FormsConstants;
import org.apache.cocoon.forms.datatype.FilterableSelectionList;
import org.apache.cocoon.forms.datatype.SelectionList;
import org.apache.cocoon.forms.formmodel.Field;
import org.apache.cocoon.forms.formmodel.Form;
import org.apache.cocoon.generation.ServiceableGenerator;
import org.apache.cocoon.sitemap.SitemapParameters;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.continuations.Continuation;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * A generator for suggestion lists.
 *
 * @since 2.1.8
 * @version $Id$
 */
public class SuggestionListGenerator extends ServiceableGenerator implements Contextualizable {

    private ContinuationsManager contManager;
    private WebContinuation wk;
    private SelectionList list;
    private String filter;
    private Locale locale;
    private Context context;

    public void contextualize(Context ctx) throws ContextException {
        this.context = ctx;
    }

    public void service(ServiceManager manager) throws ServiceException {
        super.service(manager);
        this.contManager = (ContinuationsManager)manager.lookup(ContinuationsManager.ROLE);
    }

    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par) throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, par);
        
        Request req = ObjectModelHelper.getRequest(objectModel);
        
        String continuationId = par.getParameter("continuation-id", req.getParameter("continuation-id"));
        String widgetPath = par.getParameter("widget", req.getParameter("widget")).replace('.', '/');
        this.filter = par.getParameter("filter", req.getParameter("filter"));
        
        // The interpreter id is the sitemap's URI
        String interpreterId = SitemapParameters.getLocation(parameters).getURI();
        wk = this.contManager.lookupWebContinuation(continuationId, interpreterId);
        if (wk == null || wk.disposed()) {
            throw new InvalidContinuationException("Cannot get continuation for suggestion list");
        }
        
        Form form = (Form)wk.getAttribute("form");
        if (form == null) {
            throw new ProcessingException("No form is attached to the continuation");
        }
        
        this.locale = form.getLocale();
        
        Field field = (Field)form.lookupWidget(widgetPath);
        list = field.getSuggestionList();
        if (list == null) {
            throw new ProcessingException(field + " has no suggestion list");
        }
    }
    
    public void generate() throws IOException, SAXException, ProcessingException {
        super.contentHandler.startDocument();
        super.contentHandler.startPrefixMapping(FormsConstants.INSTANCE_PREFIX, FormsConstants.INSTANCE_NS);
        ContentHandler handler;
        if (filter == null || filter.length() == 0 || list instanceof FilterableSelectionList) {
            handler = super.contentHandler;
        } else {
            handler = new SelectionListFilter(filter, super.contentHandler);
        }
        
        // Restore the JavaScript execution context, if any
        Scriptable oldScope = null;
        FOM_Cocoon cocoon = null;
        if (wk.getContinuation() instanceof Continuation) {
            oldScope = FOM_JavaScriptFlowHelper.getFOM_FlowScope(objectModel);
            
            Continuation k = (Continuation)wk.getContinuation();
            Scriptable kScope = k.getParentScope();
            // Register the current scope for scripts indirectly called from this function
            FOM_JavaScriptFlowHelper.setFOM_FlowScope(objectModel, kScope);
            cocoon = (FOM_Cocoon)kScope.get("cocoon", kScope);
            cocoon.pushCallContext(null, null, this.context, wk);
        }

        try {
            if (list instanceof FilterableSelectionList) {
                ((FilterableSelectionList)list).generateSaxFragment(handler, this.locale, filter);
            } else {
                list.generateSaxFragment(handler, this.locale);
            }
        } finally {
            if (oldScope != null) {
                // Restore it
                FOM_JavaScriptFlowHelper.setFOM_FlowScope(objectModel, oldScope);
                cocoon.popCallContext();
            }
        }
        
        super.contentHandler.endPrefixMapping(FormsConstants.INSTANCE_PREFIX);
        super.contentHandler.endDocument();
    }

}
