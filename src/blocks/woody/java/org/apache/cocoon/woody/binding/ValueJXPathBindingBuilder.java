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

import org.apache.cocoon.woody.util.DomHelper;
import org.apache.cocoon.woody.Constants;
import org.apache.cocoon.woody.datatype.convertor.Convertor;
import org.apache.cocoon.i18n.I18nUtils;
import org.w3c.dom.Element;

import java.util.Locale;

/**
 * ValueJXPathBindingBuilder provides a helper class for the Factory
 * implemented in {@link JXPathBindingManager} that helps construct the
 * actual {@link ValueJXPathBinding} out of the configuration in the
 * provided configElement which looks like:
 * <pre><code>
 * &lt;wb:value id="<i>widget-id</i>" path="<i>xpath-expression</i>"&gt;
 *   &lt;!-- optional child binding to be executed upon 'save' of changed value --&gt;
 *   &lt;wb:on-update&gt;
 *     &lt;!-- any childbinding --&gt;
 *   &lt;/wb:on-update&gt;
 * &lt;/wb:value&gt;
 * </code></pre>
 *
 * @version CVS $Id: ValueJXPathBindingBuilder.java,v 1.6 2004/02/03 12:26:21 joerg Exp $
 */
public class ValueJXPathBindingBuilder extends JXPathBindingBuilderBase {

    /**
     * Creates an instance of {@link ValueJXPathBinding} based on the attributes
     * and nested configuration of the provided bindingElm.
     */
    public JXPathBindingBase buildBinding(Element bindingElm, JXPathBindingManager.Assistant assistant) throws BindingException {

        try {
            CommonAttributes commonAtts = JXPathBindingBuilderBase.getCommonAttributes(bindingElm);
            String xpath = DomHelper.getAttribute(bindingElm, "path");
            String widgetId = DomHelper.getAttribute(bindingElm, "id");

            Element updateWrapElement =
                DomHelper.getChildElement(bindingElm, BindingManager.NAMESPACE, "on-update");
            JXPathBindingBase[] updateBindings = assistant.makeChildBindings(updateWrapElement);

            Convertor convertor = null;
            Locale convertorLocale = Locale.US;
            Element convertorEl = DomHelper.getChildElement(bindingElm, Constants.WD_NS, "convertor");
            if (convertorEl != null) {
                String datatype = DomHelper.getAttribute(convertorEl, "datatype");
                String localeStr = convertorEl.getAttribute("datatype");
                if (!localeStr.equals("")) {
                    convertorLocale = I18nUtils.parseLocale(localeStr);
                }

                convertor = assistant.getDatatypeManager().createConvertor(datatype, convertorEl);
            }

            ValueJXPathBinding fieldBinding =
                    new ValueJXPathBinding(commonAtts,
                            widgetId, xpath, updateBindings, convertor, convertorLocale);

            return fieldBinding;
        } catch (BindingException e) {
            throw e;
        } catch (Exception e) {
            throw new BindingException("Error building binding defined at " + DomHelper.getLocation(bindingElm), e);
        }
    }
}
