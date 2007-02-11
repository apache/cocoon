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
package org.apache.cocoon.forms.formmodel;

import org.apache.cocoon.forms.formmodel.library.Library;
import org.apache.cocoon.forms.util.DomHelper;
import org.w3c.dom.Element;


/**
 * @version $Id$
 */
public class ImportDefinitionBuilder extends AbstractWidgetDefinitionBuilder {

	public static final String PREFIX_ATTRIBUTE = "prefix";
	public static final String URI_ATTRIBUTE = "uri";
	
	/**
	 * Imports a new library
	 */
	public WidgetDefinition buildWidgetDefinition(Element widgetElement)
			throws Exception {
		
		if(this.context == null || this.context.getLocalLibrary() == null)
			throw new Exception("Import statement seen and context is empty! (at "+DomHelper.getLocation(widgetElement)+")");
			
		Library lib = this.context.getLocalLibrary();
		String prefix = DomHelper.getAttribute(widgetElement, PREFIX_ATTRIBUTE);
		String uri = DomHelper.getAttribute(widgetElement, URI_ATTRIBUTE);
		
		if(!lib.includeAs(prefix,uri))
			throw new Exception("Import statement did not succeed (probably used ':' in the prefix?)! (at "+DomHelper.getLocation(widgetElement)+")");
		
		return null;
	}

}
