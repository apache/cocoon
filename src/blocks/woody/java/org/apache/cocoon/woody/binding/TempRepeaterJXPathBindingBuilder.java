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
import org.w3c.dom.Element;

/**
 * An experimental simple repeater binding that will replace
 * (i.e. delete then re-add all) its content.
 * Based on SimpleRepeater code.
 * <pre>
 * &lt;wb:temp-repeater
 *   id="contacts"
 *   parent-path="contacts"&gt;
 *   &lt;<em>... child bindings ...</em>
 * &lt;/wb:temp-repeater&gt;
 * </pre>
 *
 * @author Timothy Larson
 * @version CVS $Id: TempRepeaterJXPathBindingBuilder.java,v 1.2 2004/01/11 20:51:16 vgritsenko Exp $
 */
public class TempRepeaterJXPathBindingBuilder
    extends JXpathBindingBuilderBase {

    public JXPathBindingBase buildBinding(
        Element bindingElem,
        JXPathBindingManager.Assistant assistant) throws BindingException {

        try {
            CommonAttributes commonAtts = JXpathBindingBuilderBase.getCommonAttributes(bindingElem);

            String repeaterId = DomHelper.getAttribute(bindingElem, "id");
            String parentPath = DomHelper.getAttribute(bindingElem, "parent-path");
            String rowPath = DomHelper.getAttribute(bindingElem, "row-path");
            String rowPathInsert = DomHelper.getAttribute(bindingElem, "row-path-insert", rowPath);
            boolean clearOnLoad = DomHelper.getAttributeAsBoolean(bindingElem, "clear-before-load", true);
            boolean deleteIfEmpty = DomHelper.getAttributeAsBoolean(bindingElem, "delete-parent-if-empty", false);

            Element childWrapElement =
                DomHelper.getChildElement(bindingElem, BindingManager.NAMESPACE, "on-bind");
            JXPathBindingBase[] childBindings = assistant.makeChildBindings(childWrapElement);

            Element insertWrapElement =
                DomHelper.getChildElement(
                    bindingElem,
                    BindingManager.NAMESPACE,
                    "on-insert-row");
            JXPathBindingBase[] insertBindings = null;
            if (insertWrapElement != null)
                insertBindings = assistant.makeChildBindings(insertWrapElement);

            return new TempRepeaterJXPathBinding( commonAtts, repeaterId, parentPath, rowPath, rowPathInsert, clearOnLoad, deleteIfEmpty,
                new ComposedJXPathBindingBase(JXpathBindingBuilderBase.CommonAttributes.DEFAULT, childBindings),
                new ComposedJXPathBindingBase(JXpathBindingBuilderBase.CommonAttributes.DEFAULT, insertBindings));
        } catch (BindingException e) {
            throw e;
        } catch (Exception e) {
            throw new BindingException("Error building temp-repeater binding defined at " + DomHelper.getLocation(bindingElem), e);
        }
    }
}
