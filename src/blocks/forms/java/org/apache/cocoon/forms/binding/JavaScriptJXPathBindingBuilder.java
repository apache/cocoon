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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.cocoon.forms.binding.JXPathBindingManager.Assistant;
import org.apache.cocoon.forms.util.DomHelper;
import org.apache.cocoon.forms.util.JavaScriptHelper;
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
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version CVS $Id: JavaScriptJXPathBindingBuilder.java,v 1.4 2004/06/24 11:32:47 cziegeler Exp $
 */
public class JavaScriptJXPathBindingBuilder extends JXPathBindingBuilderBase implements Contextualizable {

	private Context avalonContext;
	
	public void contextualize(Context context) throws ContextException {
		this.avalonContext = context;
	}

	public JXPathBindingBase buildBinding(Element element, Assistant assistant) throws BindingException {
        try {
            CommonAttributes commonAtts = JXPathBindingBuilderBase.getCommonAttributes(element);

            String id = DomHelper.getAttribute(element, "id");
            String path = DomHelper.getAttribute(element, "path");

            // Build load script
            Function loadScript = null;
            if (commonAtts.loadEnabled) {
                Element loadElem = DomHelper.getChildElement(element, BindingManager.NAMESPACE, "load-form");
                if (loadElem == null) {
                    throw new BindingException("Element \"load-form\" is missing (" +
                        DomHelper.getLocation(element) + ")");
                }
                loadScript = JavaScriptHelper.buildFunction(loadElem, JavaScriptJXPathBinding.LOAD_PARAMS);
            }

            	// Build save script
            Function saveScript = null;
            if (commonAtts.saveEnabled) {
                Element saveElem = DomHelper.getChildElement(element, BindingManager.NAMESPACE, "save-form");
                if (saveElem == null) {
                    throw new BindingException("Element \"save-form\" is missing (" +
                        DomHelper.getLocation(element) + ")");
                }
                saveScript = JavaScriptHelper.buildFunction(saveElem, JavaScriptJXPathBinding.SAVE_PARAMS);
            }
            
            // Build child bindings
            Map childBindings;
            Element[] children = DomHelper.getChildElements(element, BindingManager.NAMESPACE, "child-binding");
            if (children.length == 0) {
            		childBindings = Collections.EMPTY_MAP;
            } else {
            		childBindings = new HashMap();
            		for (int i = 0; i < children.length; i++) {
            			Element child = children[i];
            			
            			// Get the binding name and check its uniqueness
            			String name = DomHelper.getAttribute(child, "name");
            			if (childBindings.containsKey(name)) {
            				throw new BindingException("Duplicate name '" + name + "' at " + DomHelper.getLocation(child));
            			}
            			
            			// Build the child binding
            			JXPathBindingBase[] bindings = assistant.makeChildBindings(child);
            			if (bindings == null) {
            				bindings = new JXPathBindingBase[0];
            			}
            			
            			ComposedJXPathBindingBase composedBinding = new ComposedJXPathBindingBase(commonAtts, bindings);
            			composedBinding.enableLogging(getLogger());
            			childBindings.put(name, composedBinding);
            		}
            }

            JXPathBindingBase result = new JavaScriptJXPathBinding(this.avalonContext, commonAtts, id, path, loadScript, saveScript,
            		Collections.unmodifiableMap(childBindings));
            result.enableLogging(getLogger());
            return result;

        } catch(BindingException be) {
        	    throw be;
        } catch(Exception e) {
            throw new BindingException("Cannot build binding at " + DomHelper.getLocation(element), e);
        }
    }
}
