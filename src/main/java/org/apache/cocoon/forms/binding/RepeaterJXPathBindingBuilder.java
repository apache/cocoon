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

import org.apache.cocoon.forms.util.DomHelper;
import org.apache.cocoon.util.location.LocationAttributes;
import org.w3c.dom.Element;

/**
 * RepeaterJXPathBindingBuilder provides a helper class for the Factory
 * implemented in {@link JXPathBindingManager} that helps construct the
 * actual {@link RepeaterJXPathBinding} out of the configuration in the
 * provided configElement which looks like:
 * <pre><code>
 * &lt;fb:repeater
 *   id="contacts"
 *   parent-path="contacts"
 *   row-path="contact"
 *   row-path-insert="new-contact"  &gt;
 *
 *   &lt;fb:identity&gt;
 *      &lt;!-- nested bindings that map the 'identity' of the items --&gt;
 *   &lt;/fb:identity&gt;
 *
 *   &lt;fb:on-bind&gt;
 *      &lt;!-- nested bindings executed on updates AND right after the insert --&gt;
 *   &lt;/fb:on-bind&gt;
 *
 *   &lt;fb:on-delete-row&gt;
 *      &lt;!-- nested bindings executed on deletion of row --&gt;
 *   &lt;/fb:on-delete-row&gt;
 *
 *   &lt;fb:on-insert-row&gt;
 *      &lt;!-- nested bindings executed to prepare the insertion of a row --&gt;
 *   &lt;/fb:on-insert-row&gt;
 *
 * &lt;/fb:repeater&gt;
 * </code></pre>
 *
 * @version $Id$
 */
public class RepeaterJXPathBindingBuilder extends JXPathBindingBuilderBase {
    
    /**
     * Creates an instance of {@link RepeaterJXPathBinding} according to the
     * attributes and nested comfiguration elements of the bindingElm.
     *
     * @param bindingElm
     * @param assistant
     * @return JXPathBindingBase
     */
    public JXPathBindingBase buildBinding(Element bindingElm,
            JXPathBindingManager.Assistant assistant) throws BindingException {
        
        if (bindingElm.hasAttribute("unique-row-id")) {
            throw new BindingException("Attribute 'unique-row-id' is no more supported, use <fb:identity> instead",
                    LocationAttributes.getLocation(bindingElm));
        }

        if (bindingElm.hasAttribute("unique-path")) {
            throw new BindingException("Attribute 'unique-path' is no more supported, use <fb:identity> instead",
                    LocationAttributes.getLocation(bindingElm));
        }

        try {
            CommonAttributes commonAtts =
                JXPathBindingBuilderBase.getCommonAttributes(bindingElm);

            String repeaterId = DomHelper.getAttribute(bindingElm, "id", null);
            String parentPath =
                DomHelper.getAttribute(bindingElm, "parent-path", null);
            String rowPath = DomHelper.getAttribute(bindingElm, "row-path", null);
            String rowPathForInsert =
                DomHelper.getAttribute(bindingElm, "row-path-insert", rowPath);
            
//          do inheritance
            RepeaterJXPathBinding otherBinding = (RepeaterJXPathBinding)assistant.getContext().getSuperBinding();
            JXPathBindingBase[] existingOnBind = null;
            JXPathBindingBase[] existingOnDelete = null;
            JXPathBindingBase[] existingOnInsert = null;
            JXPathBindingBase[] existingIdentity = null;
            if(otherBinding!=null) {
            	commonAtts = JXPathBindingBuilderBase.mergeCommonAttributes(otherBinding.getCommonAtts(),commonAtts);
            	
            	if(repeaterId==null)
            		repeaterId = otherBinding.getId();
            	if(parentPath==null)
            		parentPath = otherBinding.getRepeaterPath();
            	if(rowPath==null)
            		rowPath = otherBinding.getRowPath();
            	if(rowPathForInsert==null)
            		rowPathForInsert = otherBinding.getInsertRowPath();
            	
            	if(otherBinding.getRowBinding() != null)
            		existingOnBind = otherBinding.getRowBinding().getChildBindings();
            	if(otherBinding.getDeleteRowBinding() != null)
            		existingOnDelete = otherBinding.getDeleteRowBinding().getChildBindings();
            	if(otherBinding.getIdentityBinding() != null)
            		existingIdentity = otherBinding.getIdentityBinding().getChildBindings();
            	if(otherBinding.getInsertRowBinding() != null)
            		existingOnInsert = new JXPathBindingBase[] { otherBinding.getInsertRowBinding() };
            }

            // Simple mode will be used if no fb:identity, fb:on-bind, fb:on-delete-row, fb:on-insert-row exists.
            // in that case, the children of fb:repeater will be used as child bindings
            boolean simpleMode = true;
            Element childWrapElement =
                DomHelper.getChildElement(bindingElm, BindingManager.NAMESPACE, "on-bind");
            
            if (childWrapElement != null) simpleMode = false;
            
            JXPathBindingBase[] childBindings =
                assistant.makeChildBindings(childWrapElement,existingOnBind);
            if(childBindings == null)
            	childBindings = existingOnBind;

            Element deleteWrapElement = DomHelper.getChildElement(bindingElm,
                    BindingManager.NAMESPACE, "on-delete-row");
            
            if (deleteWrapElement != null) simpleMode = false;

            JXPathBindingBase[] deleteBindings = null;
            if (deleteWrapElement != null) {
                deleteBindings =
                    assistant.makeChildBindings(deleteWrapElement,existingOnDelete);
                if(deleteBindings == null)
                	deleteBindings = existingOnDelete;
            }

            Element insertWrapElement = DomHelper.getChildElement(bindingElm,
                    BindingManager.NAMESPACE, "on-insert-row");
            
            if (insertWrapElement != null) simpleMode = false;

            JXPathBindingBase insertBinding = null;
            if (insertWrapElement != null) {
                insertBinding =
                    assistant.makeChildBindings(insertWrapElement,existingOnInsert)[0];
                    // TODO: we now safely take only the first element here,
                    // but we should in fact send out a warning to the log 
                    // if more were available!
                if(insertBinding == null && existingOnInsert != null)
                	insertBinding = existingOnInsert[0];
            }

            Element identityWrapElement = DomHelper.getChildElement(bindingElm,
                    BindingManager.NAMESPACE, "identity");
            
            if (identityWrapElement != null) simpleMode = false;

            JXPathBindingBase[] identityBinding = null;
            if (identityWrapElement != null) {
                // TODO: we can only handle ValueJXPathBinding at the moment:
                // http://marc.theaimsgroup.com/?l=xml-cocoon-dev&m=107906438632484&w=4
                identityBinding =
                    assistant.makeChildBindings(identityWrapElement,existingIdentity);
                if (identityBinding != null) {
                    for (int i = 0; i < identityBinding.length;i++) {
                        if (!(identityBinding[i] instanceof ValueJXPathBinding)) {
                            throw new BindingException("Error building repeater binding defined at " +
                                    DomHelper.getLocation(bindingElm) + ": Only value binding (i.e. fb:value) " +
                                    "can be used inside fb:identity at the moment. You can read " +
                                    "http://marc.theaimsgroup.com/?l=xml-cocoon-dev&m=107906438632484&w=4" +
                                    " if you want to know more on this.");
                        }
                    }
                } else {
                	identityBinding = existingIdentity;
                }
            }
            
            if (simpleMode) {
                // Use the children of the current element
                childBindings = assistant.makeChildBindings(bindingElm,existingOnBind);
            }

            RepeaterJXPathBinding repeaterBinding =
                new RepeaterJXPathBinding(commonAtts, repeaterId, parentPath,
                        rowPath, rowPathForInsert, 
                        childBindings, insertBinding, deleteBindings, identityBinding);
            return repeaterBinding;
        } catch (BindingException e) {
            throw e;
        } catch (Exception e) {
            throw new BindingException(
                    "Error building repeater binding defined at " +
                    DomHelper.getLocation(bindingElm), e);
        }
    }
}
