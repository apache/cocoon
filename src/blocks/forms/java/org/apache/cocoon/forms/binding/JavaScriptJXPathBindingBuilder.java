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

import org.apache.cocoon.forms.binding.JXPathBindingManager.Assistant;
import org.apache.cocoon.forms.util.DomHelper;
import org.apache.cocoon.forms.util.JavaScriptHelper;
import org.mozilla.javascript.Script;
import org.w3c.dom.Element;

/**
 * Builds a {@link Binding} based on two JavaScript snippets, respectively for loading and saving the form.
 * <p>
 * The syntax for this binding is as follows :
 * <pre>
 *   &lt;wb:javascript id="foo" path="@foo"&gt;
 *     &lt;wb:load-form&gt;
 *       var appValue = jxpathPointer.getValue();
 *       var formValue = doLoadConversion(appValue);
 *       widget.setValue(formValue);
 *     &lt;/wb:load-form&gt;
 *     &lt;wb:save-form&gt;
 *       var formValue = widget.getValue();
 *       var appValue = doSaveConversion(formValue);
 *       jxpathPointer.setValue(appValue);
 *     &lt;/wb:save-form&gt;
 *   &lt;/wb:javascript&gt;
 * </pre>
 * This example is rather trivial and could be replaced by a simple &lt;wb:value&gt;, but
 * it shows the available variables in the script:
 * <ul>
 * <li><code>widget</code>: the widget identified by the "id" attribute,
 * <li><code>jxpathPointer</code>: the JXPath pointer corresponding to the "path" attribute,
 * <li><code>jxpathContext</code> (not shown): the JXPath context corresponding to the "path" attribute
 * </ul>
 * <b>Notes:</b><ul>
 * <li>The &lt;wb:save-form&gt; snippet should be ommitted if the "direction" attribute is set to "load".</li>
 * <li>The &lt;wb:load-form&gt; snippet should be ommitted if the "direction" attribute is set to "save".</li>
 * </ul>
 *
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version CVS $Id: JavaScriptJXPathBindingBuilder.java,v 1.1 2004/03/09 10:33:55 reinhard Exp $
 */
public class JavaScriptJXPathBindingBuilder extends JXPathBindingBuilderBase {

    public JXPathBindingBase buildBinding(Element element, Assistant assistant) throws BindingException {
        try {
            CommonAttributes commonAtts = JXPathBindingBuilderBase.getCommonAttributes(element);

            String id = DomHelper.getAttribute(element, "id");
            String path = DomHelper.getAttribute(element, "path");

            Script loadScript = null;
            if (commonAtts.loadEnabled) {
                Element loadElem = DomHelper.getChildElement(element, BindingManager.NAMESPACE, "load-form");
                if (loadElem == null) {
                    throw new BindingException("Element \"load-form\" is missing (" +
                        DomHelper.getLocation(element) + ")");
                }
                loadScript = JavaScriptHelper.buildScript(loadElem);
            }

            Script saveScript = null;
            if (commonAtts.saveEnabled) {
                Element saveElem = DomHelper.getChildElement(element, BindingManager.NAMESPACE, "save-form");
                if (saveElem == null) {
                    throw new BindingException("Element \"save-form\" is missing (" +
                        DomHelper.getLocation(element) + ")");
                }
                saveScript = JavaScriptHelper.buildScript(saveElem);
            }

            return new JavaScriptJXPathBinding(commonAtts, id, path, loadScript, saveScript);

        } catch(Exception e) {
            throw new BindingException("Cannot build binding at " + DomHelper.getLocation(element), e);
        }
    }
}
