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

import org.apache.cocoon.forms.binding.JXPathBindingManager.Assistant;
import org.apache.cocoon.forms.binding.library.Library;
import org.apache.cocoon.forms.binding.library.LibraryException;
import org.apache.cocoon.forms.util.DomHelper;

import org.w3c.dom.Element;

/**
 * Handles binding library imports
 *
 * @version $Id$
 */
public class ImportJXPathBindingBuilder extends JXPathBindingBuilderBase {

	/**
	 * @see JXPathBindingBuilderBase#buildBinding(Element, JXPathBindingManager.Assistant)
	 */
	public JXPathBindingBase buildBinding(Element bindingElm, Assistant assistant)
    throws BindingException {
		Library lib = assistant.getContext().getLocalLibrary();

        String prefix = DomHelper.getAttribute(bindingElm, "prefix", null);
        String uri = DomHelper.getAttribute(bindingElm, "uri", null);
        if (prefix == null || uri == null) {
            throw new BindingException("Import needs to specify both @uri and @prefix!",
                                       DomHelper.getLocationObject(bindingElm));
        }

        try {
            lib.includeAs(prefix, uri);
        } catch (LibraryException e) {
            throw new BindingException("Could not import library", e,
                                       DomHelper.getLocationObject(bindingElm));
        }

        return new ImportJXPathBinding();
	}
}
