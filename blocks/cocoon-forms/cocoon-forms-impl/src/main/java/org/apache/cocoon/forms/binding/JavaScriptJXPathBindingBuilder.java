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

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.cocoon.forms.binding.JXPathBindingManager.Assistant;
import org.apache.cocoon.forms.util.DomHelper;
import org.apache.cocoon.forms.util.JavaScriptHelper;
import org.apache.cocoon.processing.ProcessInfoProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.mozilla.javascript.Function;
import org.w3c.dom.Element;

/**
 * Builds a {@link Binding} based on two JavaScript snippets, respectively for loading and saving the form.
 * This binding also optionally accepts named child bindings, which are useful when the bound widget is a container.
 * <p>
 * The syntax for this binding is as follows:
 * <pre>
 *   &lt;fb:javascript id="foo" path="@foo"&gt;
 *     &lt;fb:load-form&gt;
 *       var appValue = jxpathPointer.getValue();
 *       var formValue = doLoadConversion(appValue);
 *       widget.setValue(formValue);
 *       childBindings["foo"].loadFormFromModel(widget, jxpathContext);
 *     &lt;/fb:load-form&gt;
 *     &lt;fb:save-form&gt;
 *       var formValue = widget.getValue();
 *       var appValue = doSaveConversion(formValue);
 *       jxpathPointer.setValue(appValue);
 *       childBindings["foo"].saveFormToModel(widget, jxpathContext);
 *     &lt;/fb:save-form&gt;
 *     &lt;fb:child-binding name="foo"&gt;
 *       &lt;fb:value id="bar" path="baz"/&gt;
 *     &lt;/fb:child-binding&gt;
 *   &lt;/fb:javascript&gt;
 * </pre>
 * This example is rather trivial and could be replaced by a simple &lt;fb:value&gt;, but
 * it shows the available variables in the script:
 * <ul>
 * <li><code>widget</code>: the widget identified by the "id" attribute,
 * <li><code>jxpathPointer</code>: the JXPath pointer corresponding to the "path" attribute,
 * <li><code>jxpathContext</code> (not shown): the JXPath context corresponding to the "path" attribute
 * </ul>
 * <b>Notes:</b><ul>
 * <li>The &lt;fb:save-form&gt; snippet should be ommitted if the "direction" attribute is set to "load".</li>
 * <li>The &lt;fb:load-form&gt; snippet should be ommitted if the "direction" attribute is set to "save".</li>
 * </ul>
 *
 * @version $Id$
 */
public class JavaScriptJXPathBindingBuilder extends JXPathBindingBuilderBase {

    private static Log LOG = LogFactory.getLog( JavaScriptJXPathBindingBuilder.class );

    private ProcessInfoProvider processInfoProvider;

    public JXPathBindingBase buildBinding(Element element, Assistant assistant)
    throws BindingException {
        try {
            CommonAttributes commonAtts = JXPathBindingBuilderBase.getCommonAttributes(element);

            String id = DomHelper.getAttribute(element, "id", null);
            String path = DomHelper.getAttribute(element, "path", null);

            JavaScriptJXPathBinding otherBinding = (JavaScriptJXPathBinding)assistant.getContext().getSuperBinding();

            if (otherBinding != null) {
                commonAtts = JXPathBindingBuilderBase.mergeCommonAttributes(otherBinding.getCommonAtts(), commonAtts);

                if (id == null) {
                    id = otherBinding.getId();
                }
                if (path == null) {
                    path = otherBinding.getPath();
                }
            }

            // Build load script
            Function loadScript = null;
            if (commonAtts.loadEnabled) {
                if (otherBinding != null) {
                    loadScript = otherBinding.getLoadScript();
                }

                Element loadElem = DomHelper.getChildElement(element, BindingManager.NAMESPACE, "load-form");
                if (loadElem != null) {
                	loadScript = JavaScriptHelper.buildFunction(loadElem, "loadForm", JavaScriptJXPathBinding.LOAD_PARAMS);
                }
            }

            // Build save script
            Function saveScript = null;
            if (commonAtts.saveEnabled) {
            	if (otherBinding != null) {
                    saveScript = otherBinding.getSaveScript();
                }

                Element saveElem = DomHelper.getChildElement(element, BindingManager.NAMESPACE, "save-form");
                if (saveElem != null) {
                	saveScript = JavaScriptHelper.buildFunction(saveElem, "saveForm", JavaScriptJXPathBinding.SAVE_PARAMS);
                }
            }

            // Build child bindings
            Map childBindings = new HashMap();

            if (otherBinding != null) {
            	Map otherChildren = otherBinding.getChildBindingsMap();
                Iterator it = otherChildren.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry entry = (Map.Entry) it.next();
                    childBindings.put(entry.getKey(), entry.getValue());
                }
            }

            Element[] children = DomHelper.getChildElements(element, BindingManager.NAMESPACE, "child-binding");
            if (children.length != 0) {
                for (int i = 0; i < children.length; i++) {
                    Element child = children[i];

                    // Get the binding name and check its uniqueness
                    String name = DomHelper.getAttribute(child, "name");

                    JXPathBindingBase[] otherBindings = null;
                    if (childBindings.containsKey(name)) {
                        //throw new BindingException("Duplicate name '" + name + "' at " + DomHelper.getLocation(child));
                    	otherBindings = ((ComposedJXPathBindingBase)childBindings.get(name)).getChildBindings();
                    }

                    // Build the child binding
                    JXPathBindingBase[] bindings = assistant.makeChildBindings(child,otherBindings);
                    if (bindings == null) {
                        bindings = new JXPathBindingBase[0];
                    }

                    ComposedJXPathBindingBase composedBinding = new ComposedJXPathBindingBase(commonAtts, bindings);
                    childBindings.put(name, composedBinding);
                }
            }

            JXPathBindingBase result = new JavaScriptJXPathBinding(this.processInfoProvider, commonAtts, id, path, loadScript, saveScript,
                    Collections.unmodifiableMap(childBindings));
            return result;

        } catch (BindingException e) {
            throw e;
        } catch (Exception e) {
            throw new BindingException("Cannot build binding", e,
                                       DomHelper.getLocationObject(element));
        }
    }

    public void setProcessInfoProvider( ProcessInfoProvider processInfoProvider )
    {
        this.processInfoProvider = processInfoProvider;
    }
}
