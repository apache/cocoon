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

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.woody.binding;

import org.apache.cocoon.woody.binding.JXPathBindingManager.Assistant;
import org.apache.cocoon.woody.util.DomHelper;
import org.apache.cocoon.woody.util.JavaScriptHelper;
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
 * @version CVS $Id: JavaScriptJXPathBindingBuilder.java,v 1.3 2003/12/18 07:57:21 mpo Exp $
 */
public class JavaScriptJXPathBindingBuilder extends JXpathBindingBuilderBase {

    public JXPathBindingBase buildBinding(Element element, Assistant assistant) throws BindingException {
        try {
            CommonAttributes commonAtts = JXpathBindingBuilderBase.getCommonAttributes(element); 
            
            String id = DomHelper.getAttribute(element, "id");
            String path = DomHelper.getAttribute(element, "path");
            
            Script loadScript = null;
            if (commonAtts.loadEnabled) {
                Element loadElem = DomHelper.getChildElement(element, BindingManager.NAMESPACE, "load-form");
                loadScript = JavaScriptHelper.buildScript(loadElem);
            }
            
            Script saveScript = null;
            if (commonAtts.saveEnabled) {
                Element saveElem = DomHelper.getChildElement(element, BindingManager.NAMESPACE, "save-form");
                saveScript = JavaScriptHelper.buildScript(saveElem);
            }

            return new JavaScriptJXPathBinding(commonAtts, id, path, loadScript, saveScript);

        } catch(Exception e) {
            throw new BindingException("Cannot build binding at " + DomHelper.getLocation(element), e);
        }
    }

}
