/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
package org.apache.cocoon.forms.formmodel.library;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.CascadingException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.cocoon.forms.FormsConstants;
import org.apache.cocoon.forms.formmodel.WidgetDefinition;
import org.apache.cocoon.forms.formmodel.WidgetDefinitionBuilder;
import org.apache.cocoon.forms.formmodel.WidgetDefinitionBuilderContext;
import org.apache.cocoon.forms.util.DomHelper;
import org.apache.cocoon.util.location.LocationAttributes;
import org.apache.commons.lang.StringUtils;

import org.w3c.dom.Element;

/**
 * @version $Id$
 */
public class Library {

	public static final String SEPARATOR = ":";
	
	
	// managed instances
	protected ServiceSelector widgetDefinitionBuilderSelector;
	
	// own references
	protected LibraryManager manager = null;
	
	// own instances
	protected Map definitions = new HashMap();
	protected Map inclusions = new HashMap();
	
	// shared object with dependencies
	protected Object shared = new Object();
	
	protected String sourceURI = null;
	protected WidgetDefinitionBuilderContext context;
	
	public Library(LibraryManager lm) {
		manager = lm;
		context = new WidgetDefinitionBuilderContext();
		context.setLocalLibrary(this);
	}
	
	public void setSourceURI(String uri) {
		sourceURI = uri;
	}
	public String getSourceURI() {
		return sourceURI;
	}
	
	public void setWidgetDefinitionBuilderSelector(ServiceSelector selector) {
		this.widgetDefinitionBuilderSelector = selector;
	}
	
	public boolean dependenciesHaveChanged() throws Exception {
		
		Iterator it = this.inclusions.values().iterator();
		while(it.hasNext()) {
			Dependency dep = (Dependency)it.next();
			if(!dep.isValid())
				return true;
		}
		
		return false;
	}
	
	/**
	 * "Registers" a library to be referenced later under a certain key or prefix.
	 * Definitions will be accessible locally through prefixing: "prefix:definitionid"
	 * 
	 * @param key the key 
	 * @param librarysource the source of the library to be know as "key"
	 * @return true if there was no such key used before, false otherwise
	 */
	public boolean includeAs(String key, String librarysource)
		throws LibraryException 
	{
		try {
			// library keys may not contain ":"!
			if( (!inclusions.containsKey(key) || key.indexOf(SEPARATOR)>-1) 
					&& manager.getLibrary(librarysource, sourceURI)!=null) {
				inclusions.put(key,new Dependency(librarysource));
				return true;
			}
			return false;
		} catch(Exception e) {
			throw new LibraryException("Could not include library '"+librarysource+"'",e);
		}
		
	}
	
	public WidgetDefinition getDefinition(String key) throws LibraryException {
		
		String librarykey = null;
		String definitionkey = key;
		
		if(key.indexOf(SEPARATOR)>-1) {
			String[] parts = StringUtils.split(key,SEPARATOR);
			librarykey = parts[0];
			definitionkey = parts[1];
			for(int i=2; i<parts.length; i++) {
				definitionkey += SEPARATOR+parts[i];
			}
		}
		
		if(librarykey!=null) {
			if(inclusions.containsKey(librarykey)) {
				try {
					return manager.getLibrary(((Dependency)inclusions.get(librarykey)).dependencySourceURI, sourceURI).getDefinition(definitionkey);
				} catch(Exception e) {
					throw new LibraryException("Couldn't get Library key='"+librarykey+"' source='"+inclusions.get(librarykey)+"",e);
				}
			} else {
				throw new LibraryException("Library '"+librarykey+"' does not exist! (lookup: '"+key+"')");
			}
		} else {
			return (WidgetDefinition)definitions.get(definitionkey);
		}
	}
	
	public void buildLibrary(Element libraryElement) throws Exception {
		sourceURI = LocationAttributes.getURI(libraryElement);
		Element widgetsElement = DomHelper.getChildElement(libraryElement, FormsConstants.DEFINITION_NS, "widgets", true);
        // All child elements of the widgets element are widgets
        Element[] widgetElements = DomHelper.getChildElements(widgetsElement, FormsConstants.DEFINITION_NS);
        for (int i = 0; i < widgetElements.length; i++) {
            Element widgetElement = widgetElements[i];
            WidgetDefinition widgetDefinition = buildWidgetDefinition(widgetElement);
            addDefinition(widgetDefinition);
        }
	}
	
	public void addDefinition(WidgetDefinition definition) throws LibraryException {
		if(definition == null)
			return;
		
		if(definitions.containsKey(definition.getId()))
			throw new LibraryException("Library already contains a widget with this ID!");
		
		// let the definition know where it comes from
		definition.setEnclosingLibrary(this);
		
		// add def to our list of defs
		definitions.put(definition.getId(),definition);
		manager.debug(this+": Put definition with id: "+definition.getId());
	}
	
	protected WidgetDefinition buildWidgetDefinition(Element widgetDefinition) throws Exception {
        String widgetName = widgetDefinition.getLocalName();
        WidgetDefinitionBuilder builder = null;
        try {
            builder = (WidgetDefinitionBuilder)widgetDefinitionBuilderSelector.select(widgetName);
        } catch (ServiceException e) {
            throw new CascadingException("Unknown kind of widget '" + widgetName + "' at " +
                                         DomHelper.getLocation(widgetDefinition), e);
        }
        
        context.setSuperDefinition(null);
        String extend = DomHelper.getAttribute(widgetDefinition, "extends", null);
        
        if (extend != null)
            context.setSuperDefinition(getDefinition(extend));
        
        
        return builder.buildWidgetDefinition(widgetDefinition,context);
    }
	
	
	/**
	 * Encapsulates a uri to designate an import plus a timestamp so previously reloaded 
	 */
	public class Dependency {
		
		private String dependencySourceURI;
		private Object shared;
		
		public Dependency(String dependencySourceURI) throws Exception {
			this.dependencySourceURI = dependencySourceURI;
			
			Library lib = manager.getLibrary(this.dependencySourceURI,sourceURI);
			this.shared = lib.shared;
		}
		
		public boolean isValid() throws LibraryException {
			try {
				
				if(manager.libraryInCache(dependencySourceURI,sourceURI)) {
					Library lib = manager.getLibrary(dependencySourceURI,sourceURI);
					
					if(this.shared == lib.shared)
						return true;
				}
				
				return false;
			} catch(Exception forward) {
				throw new LibraryException("Exception occured while checking dependency validity!",forward);
			}
			
		}
	}
	
}
