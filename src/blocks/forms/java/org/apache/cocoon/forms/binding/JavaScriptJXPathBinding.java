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
package org.apache.cocoon.forms.binding;

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.cocoon.components.CocoonComponentManager;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.forms.formmodel.Widget;
import org.apache.cocoon.forms.util.JavaScriptHelper;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.mozilla.javascript.Script;

/**
 *
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version CVS $Id: JavaScriptJXPathBinding.java,v 1.1 2004/03/09 10:33:55 reinhard Exp $
 */
public class JavaScriptJXPathBinding extends JXPathBindingBase {

    private final String id;
    private final String path;
    private final Script loadScript;
    private final Script saveScript;

    public JavaScriptJXPathBinding(
            JXPathBindingBuilderBase.CommonAttributes commonAtts, String id,
            String path, Script loadScript, Script saveScript) {
        super(commonAtts);
        this.id = id;
        this.path = path;
        this.loadScript = loadScript;
        this.saveScript = saveScript;
    }

    public void doLoad(Widget frmModel, JXPathContext jctx) {
        if (this.loadScript != null) {
            Widget widget = frmModel.getWidget(this.id);
    
            // Move to widget context
            Pointer pointer = jctx.getPointer(this.path);
    
            // FIXME: remove this ugly hack and get the request from the
            // Avalon context once binding builder are real components
            Request request = ObjectModelHelper.getRequest(CocoonComponentManager.getCurrentEnvironment().getObjectModel());

            try {
                Map values = new HashMap(3);
                values.put("widget", widget);
                values.put("jxpathPointer", pointer);
                if (pointer.getNode() != null) {
                    values.put("jxpathContext", jctx.getRelativeContext(pointer));
                }

                JavaScriptHelper.execScript(this.loadScript, values, request);
    
            } catch(RuntimeException re) {
                // rethrow
                throw re;
            } catch(Exception e) {
                throw new CascadingRuntimeException("Error invoking JavaScript event handler", e);
            }
        } else {
            if (this.getLogger().isInfoEnabled()) {
                this.getLogger().info("[Javascript Binding] - loadForm: No javascript code avaliable. Widget id=" + this.getId());
            }
        }
    }

    public void doSave(Widget frmModel, JXPathContext jctx) throws BindingException {
        if (this.saveScript != null) {
            Widget widget = frmModel.getWidget(this.id);

            // Move to widget context and create the path if needed
            Pointer pointer = jctx.createPath(this.path);
            JXPathContext widgetCtx = jctx.getRelativeContext(pointer);
            try {
                // FIXME: remove this ugly hack and get the request from the Avalon context once
                // binding builder are real components
                Request request = ObjectModelHelper.getRequest(CocoonComponentManager.getCurrentEnvironment().getObjectModel());

                Map values = new HashMap();
                values.put("widget", widget);
                values.put("jxpathContext", widgetCtx);
                values.put("jxpathPointer", pointer);

                JavaScriptHelper.execScript(this.saveScript, values, request);

            } catch(RuntimeException re) {
                // rethrow
                throw re;
            } catch(Exception e) {
                throw new CascadingRuntimeException("Error invoking JavaScript event handler", e);
            }
        } else {
            if (this.getLogger().isInfoEnabled()) {
                this.getLogger().info("[Javascript Binding] - saveForm: No javascript code avaliable. <wb:javascript id=" + this.getId() + ">");
            }
        }
    }
}
