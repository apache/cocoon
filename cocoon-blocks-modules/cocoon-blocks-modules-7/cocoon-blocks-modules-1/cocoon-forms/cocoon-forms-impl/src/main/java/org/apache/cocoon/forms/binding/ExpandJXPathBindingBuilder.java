/*
 * Copyright 2005 The Apache Software Foundation.
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
import org.apache.cocoon.forms.binding.library.Library;
import org.apache.cocoon.forms.binding.library.LibraryException;
import org.apache.cocoon.forms.util.DomHelper;
import org.w3c.dom.Element;

/**
 * @version $Id$
 */
public class ExpandJXPathBindingBuilder extends JXPathBindingBuilderBase {

	/* (non-Javadoc)
	 * @see org.apache.cocoon.forms.binding.JXPathBindingBuilderBase#buildBinding(org.w3c.dom.Element, org.apache.cocoon.forms.binding.JXPathBindingManager.Assistant)
	 */
	public JXPathBindingBase buildBinding(Element bindingElm,
			Assistant assistant) throws BindingException {

		Library lib = assistant.getContext().getLocalLibrary();
		
		String id = DomHelper.getAttribute(bindingElm, "id", null);
		
		if(id == null)
			throw new BindingException("Attribute id is required! (at "+DomHelper.getLocation(bindingElm)+")");
		
		try {
			return (JXPathBindingBase)lib.getBinding(id);
		} catch(LibraryException e) {
			throw new BindingException("Could not expand binding from library! (at "+DomHelper.getLocation(bindingElm)+")",e);
		}	
	}
}
