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

import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.context.Context;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.components.flow.javascript.ScriptableMap;
import org.apache.cocoon.forms.formmodel.Widget;
import org.apache.cocoon.forms.util.JavaScriptHelper;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

/**
 *
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version CVS $Id: JavaScriptJXPathBinding.java,v 1.8 2004/06/24 11:32:47 cziegeler Exp $
 */
public class JavaScriptJXPathBinding extends JXPathBindingBase {

	private final Context avalonContext;
    private final String id;
    private final String path;
    private final Function loadScript;
    private final Function saveScript;
    private final Scriptable childBindings;
    
    final static String[] LOAD_PARAMS = { "widget", "jxpathPointer", "jxpathContext", "childBindings" };
    final static String[] SAVE_PARAMS = { "widget", "jxpathPointer", "jxpathContext", "childBindings" };

    public JavaScriptJXPathBinding(
    		    Context context, JXPathBindingBuilderBase.CommonAttributes commonAtts, String id,
            String path, Function loadScript, Function saveScript, Map childBindings) {
        super(commonAtts);
        this.id = id;
        this.path = path;
        this.loadScript = loadScript;
        this.saveScript = saveScript;
        this.avalonContext = context;
        
        // Set parent on child bindings
        for(Iterator iter = childBindings.values().iterator(); iter.hasNext(); ) {
        		((Binding)iter.next()).setParent(this);
        }
        
        this.childBindings = new ScriptableMap(childBindings);
    }

    public void doLoad(Widget frmModel, JXPathContext jctx) {
        if (this.loadScript != null) {
            Widget widget = selectWidget(frmModel,this.id);
    
            // Move to widget context
            Pointer pointer = jctx.getPointer(this.path);
    
            Map objectModel = ContextHelper.getObjectModel(this.avalonContext);

            try {
//                Map values = new HashMap(3);
//                values.put("widget", widget);
//                values.put("jxpathPointer", pointer);
//                if (pointer.getNode() != null) {
//                    values.put("jxpathContext", jctx.getRelativeContext(pointer));
//                }
//                values.put("childBindings", this.childBindings);
                
                JXPathContext newCtx = pointer.getNode() == null ? null :
                	    jctx.getRelativeContext(pointer);

                JavaScriptHelper.callFunction(this.loadScript, frmModel,
                		new Object[] {widget, pointer, newCtx, this.childBindings}, objectModel);
    
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
            Widget widget = selectWidget(frmModel,this.id);

            // Move to widget context and create the path if needed
            Pointer pointer = jctx.createPath(this.path);
            JXPathContext widgetCtx = jctx.getRelativeContext(pointer);
            try {
                Map objectModel = ContextHelper.getObjectModel(this.avalonContext);

//                Map values = new HashMap();
//                values.put("widget", widget);
//                values.put("jxpathContext", widgetCtx);
//                values.put("jxpathPointer", pointer);
//                values.put("childBindings", this.childBindings);

                JavaScriptHelper.callFunction(this.saveScript, frmModel,
                		new Object[] {widget, pointer, widgetCtx, this.childBindings}, objectModel);

            } catch(RuntimeException re) {
                // rethrow
                throw re;
            } catch(Exception e) {
                throw new CascadingRuntimeException("Error invoking JavaScript event handler", e);
            }
        } else {
            if (this.getLogger().isInfoEnabled()) {
                this.getLogger().info("[Javascript Binding] - saveForm: No code avaliable on the javascript binding with id \"" + this.getId() + "\"");
            }
        }
    }
}
