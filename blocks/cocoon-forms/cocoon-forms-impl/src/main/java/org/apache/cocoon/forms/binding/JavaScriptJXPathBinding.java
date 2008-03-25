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
package org.apache.cocoon.forms.binding;

import java.util.Iterator;
import java.util.Map;

import org.apache.cocoon.components.flow.javascript.ScriptableMap;
import org.apache.cocoon.forms.formmodel.Widget;
import org.apache.cocoon.forms.util.JavaScriptHelper;
import org.apache.cocoon.processing.ProcessInfoProvider;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

/**
 *
 * @version $Id$
 */
public class JavaScriptJXPathBinding extends JXPathBindingBase {

    final static String[] LOAD_PARAMS = { "widget", "jxpathPointer", "jxpathContext", "childBindings" };
    final static String[] SAVE_PARAMS = { "widget", "jxpathPointer", "jxpathContext", "childBindings" };

    private final String id;
    private final String path;
    private final Function loadScript;
    private final Function saveScript;
    private final Scriptable childBindings;
    private final Map childBindingsMap;
    private ProcessInfoProvider processInfoProvider;


    public JavaScriptJXPathBinding(ProcessInfoProvider processInfoProvider,
                                   JXPathBindingBuilderBase.CommonAttributes commonAtts,
                                   String id,
                                   String path,
                                   Function loadScript,
                                   Function saveScript,
                                   Map childBindings) {
        super(commonAtts);
        this.id = id;
        this.path = path;
        this.loadScript = loadScript;
        this.saveScript = saveScript;
        this.processInfoProvider = processInfoProvider;

        // Set parent on child bindings
        for (Iterator i = childBindings.values().iterator(); i.hasNext();) {
            ((Binding) i.next()).setParent(this);
        }

        this.childBindingsMap = childBindings;
        this.childBindings = new ScriptableMap(childBindings);
    }

    public String getPath() { return path; }
    public String getId() { return id; }
    public Function getLoadScript() { return loadScript; }
    public Function getSaveScript() { return saveScript; }
    public Map getChildBindingsMap() { return childBindingsMap; }

    public void doLoad(Widget frmModel, JXPathContext jctx) {
        if (this.loadScript != null) {
            Widget widget = selectWidget(frmModel, this.id);

            // Move to widget context
            Pointer pointer = jctx.getPointer(this.path);

            Map objectModel = processInfoProvider.getObjectModel();

            try {
                JXPathContext newCtx = pointer.getNode() == null ? null :
                        jctx.getRelativeContext(pointer);

                JavaScriptHelper.callFunction(this.loadScript, widget,
                        new Object[]{widget, pointer, newCtx, this.childBindings}, objectModel);

            } catch (RuntimeException re) {
                // rethrow
                throw re;
            } catch (Exception e) {
                throw new RuntimeException("Error invoking JavaScript event handler", e);
            }
        } else {
            if (getLogger().isInfoEnabled()) {
                getLogger().info("[Javascript Binding] - loadForm: No javascript code available. Widget id=" + this.getId());
            }
        }
    }

    public void doSave(Widget frmModel, JXPathContext jctx) throws BindingException {
        if (this.saveScript != null) {
            Widget widget = selectWidget(frmModel, this.id);

            // Move to widget context and create the path if needed
            Pointer pointer = jctx.createPath(this.path);
            JXPathContext widgetCtx = jctx.getRelativeContext(pointer);
            try {
                Map objectModel = processInfoProvider.getObjectModel();

                JavaScriptHelper.callFunction(this.saveScript, widget,
                        new Object[]{widget, pointer, widgetCtx, this.childBindings}, objectModel);

            } catch (RuntimeException re) {
                // rethrow
                throw re;
            } catch (Exception e) {
                throw new RuntimeException("Error invoking JavaScript event handler", e);
            }
        } else {
            if (getLogger().isInfoEnabled()) {
                getLogger().info("[Javascript Binding] - saveForm: No code available on the javascript binding with id '" + getId() + "'");
            }
        }
    }
}
