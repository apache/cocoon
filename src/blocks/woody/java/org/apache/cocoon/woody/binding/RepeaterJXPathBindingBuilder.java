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

import java.util.Locale;

import org.apache.cocoon.i18n.I18nUtils;
import org.apache.cocoon.woody.Constants;
import org.apache.cocoon.woody.datatype.convertor.Convertor;
import org.apache.cocoon.woody.util.DomHelper;
import org.w3c.dom.Element;

/**
 * RepeaterJXPathBindingBuilder provides a helper class for the Factory
 * implemented in {@link JXPathBindingManager} that helps construct the
 * actual {@link RepeaterJXPathBinding} out of the configuration in the
 * provided configElement which looks like:
 * <pre><code>
 * &lt;wb:repeater
 *   id="contacts"
 *   parent-path="contacts"
 *   row-path="contact"
 *   unique-row-id="id"
 *   unique-path="@id"   &gt;
 *
 *   &lt;wb:on-bind&gt;
 *      &lt;!-- nested bindings executed on updates AND right after the insert --&gt;
 *   &lt;/wb:on-bind&gt;
 *
 *   &lt;wb:on-delete-row&gt;
 *      &lt;!-- nested bindings executed on deletion of row --&gt;
 *   &lt;/wb:on-delete-row&gt;
 *
 *   &lt;wb:on-insert-row&gt;
 *      &lt;!-- nested bindings executed to prepare the insertion of a row --&gt;
 *   &lt;/wb:on-insert-row&gt;
 *
 * &lt;/wb:repeater&gt;
 * </code></pre>
 *
 * @version CVS $Id: RepeaterJXPathBindingBuilder.java,v 1.9 2004/01/11 20:51:16 vgritsenko Exp $
 */
public class RepeaterJXPathBindingBuilder
    extends JXpathBindingBuilderBase {

    /**
     * Creates an instance of {@link RepeaterJXPathBinding} according to the
     * attributes and nested comfiguration elements of the bindingElm.
     *
     * @param bindingElm
     * @param assistant
     * @return JXPathBindingBase
     */
    public JXPathBindingBase buildBinding(
        Element bindingElm,
        JXPathBindingManager.Assistant assistant) throws BindingException {

        try {
            CommonAttributes commonAtts = JXpathBindingBuilderBase.getCommonAttributes(bindingElm);

            String repeaterId = DomHelper.getAttribute(bindingElm, "id");
            String parentPath =
                DomHelper.getAttribute(bindingElm, "parent-path");
            String rowPath = DomHelper.getAttribute(bindingElm, "row-path");
            String rowPathForInsert = DomHelper.getAttribute(bindingElm, "row-path-insert", rowPath);
            String uniqueRowId =
                DomHelper.getAttribute(bindingElm, "unique-row-id");
            String uniqueRowIdPath =
                DomHelper.getAttribute(bindingElm, "unique-path");


            Convertor convertor = null;
            Locale convertorLocale = Locale.US;
            Element convertorEl = DomHelper.getChildElement(bindingElm, Constants.WD_NS, "convertor");
            if (convertorEl != null) {
                String datatype = DomHelper.getAttribute(convertorEl, "datatype");
                String localeStr = convertorEl.getAttribute("datatype");
                if (!localeStr.equals(""))
                    convertorLocale = I18nUtils.parseLocale(localeStr);
                convertor = assistant.getDatatypeManager().createConvertor(datatype, convertorEl);
            }

            Element childWrapElement =
                DomHelper.getChildElement(
                    bindingElm,
                    BindingManager.NAMESPACE,
                    "on-bind");

            if (childWrapElement == null) throw new BindingException("RepeaterBinding misses '<on-bind>' child definition. " + DomHelper.getLocation(bindingElm));

            JXPathBindingBase[] childBindings = assistant.makeChildBindings(childWrapElement);

            Element deleteWrapElement =
                DomHelper.getChildElement(
                    bindingElm,
                    BindingManager.NAMESPACE,
                    "on-delete-row");
            JXPathBindingBase[] deleteBindings = null;
            if(deleteWrapElement != null) {
                deleteBindings = assistant.makeChildBindings(deleteWrapElement);
            }

            Element insertWrapElement =
                DomHelper.getChildElement(
                    bindingElm,
                    BindingManager.NAMESPACE,
                    "on-insert-row");
            JXPathBindingBase insertBinding = null;
            if (insertWrapElement != null) {
                insertBinding = assistant.makeChildBindings(insertWrapElement)[0];

            }

            RepeaterJXPathBinding repeaterBinding =
                new RepeaterJXPathBinding(
                    commonAtts,
                    repeaterId, parentPath, rowPath, rowPathForInsert,
                    uniqueRowId, uniqueRowIdPath,
                    convertor, convertorLocale,
                    childBindings, insertBinding, deleteBindings);

            return repeaterBinding;
        } catch (BindingException e) {
            throw e;
        } catch (Exception e) {
            throw new BindingException("Error building repeater binding defined at " + DomHelper.getLocation(bindingElm), e);
        }
    }
}
