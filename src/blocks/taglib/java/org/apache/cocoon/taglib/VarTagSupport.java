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
package org.apache.cocoon.taglib;


import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.jxpath.JXPathCocoonContexts;

import org.apache.commons.jxpath.JXPathContext;

/**
 * Add support for setting and getting variables
 * 
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * @version CVS $Id: VarTagSupport.java,v 1.6 2004/03/05 13:02:24 bdelacretaz Exp $
 */
public abstract class VarTagSupport extends TagSupport implements Serviceable {
    protected String var;
    protected ServiceManager manager;
    private JXPathCocoonContexts jxpathContexts;
    private Request request;

    /**
     * Get the attribute to store the result in.
     */
    public final String getVar() {
        return this.var;
    }

    /**
     * Set the attribute to store the result in.
     */
    public final void setVar(String var) {
        this.var = var;
    }

    protected final Request getRequest() {
        if (request == null)
            request = ObjectModelHelper.getRequest(objectModel);
        return request;
    }

    protected final Object getVariable(String name) {
        JXPathContext context = getVariableContext();
        if (name.charAt(0) == '$')
            return context.getValue(name);
        else
            return context.getVariables().getVariable(name);
        //getRequest().getAttribute(name);
    }

    /**
     * Register the name and object specified.
     *
     * @param name the name of the attribute to set
     * @param value  the object to associate with the name
     */
    protected final void setVariable(String name, Object value) {
        JXPathContext context = getVariableContext();
        if (name.charAt(0) == '$')
            context.setValue(name, value);
        else
            context.getVariables().declareVariable(name, value);
        //getRequest().setAttribute(name, value);
    }

    protected final void removeVariable(String name) {
        JXPathContext context = getVariableContext();
        if (name.charAt(0) == '$')
            context.setValue(name, null);
        else
            context.getVariables().declareVariable(name, null);
        //getRequest().removeAttribute(name);
    }

    private final JXPathContext getVariableContext() {
        if (jxpathContexts == null) {
            try {
                jxpathContexts = (JXPathCocoonContexts) manager.lookup(JXPathCocoonContexts.ROLE);
            } catch (ServiceException e) {
                //XXX
            }
        }
        return jxpathContexts.getVariableContext();
        //return JXPathCocoonContexts.getVariableContext(objectModel);
    }

    /*
     * @see Serviceable#service(ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

    public void recycle() {
        this.var = null;
        if ( this.manager != null ) {
            this.manager.release(this.jxpathContexts);
        }
        this.jxpathContexts = null;
        this.request = null;
        super.recycle();
    }
}
