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
package org.apache.cocoon.forms.binding.library;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.forms.binding.Binding;
import org.apache.cocoon.forms.binding.BindingManager;
import org.apache.cocoon.forms.binding.JXPathBindingManager;
import org.apache.cocoon.forms.util.DomHelper;
import org.apache.cocoon.util.location.LocationAttributes;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Element;

/**
 * @version $Id$
 *
 */
public class Library {

	public static final String SEPARATOR = ":";
	
	// own references
	protected LibraryManager manager = null;
	
	// own instances
	protected Map definitions = new HashMap();
	protected Map inclusions = new HashMap();
	
	// shared object with dependencies
	protected Object shared = new Object();
	
	protected String sourceURI = null;
	protected JXPathBindingManager.Assistant assistant = null;
	
	public Library(ServiceManager sm) throws ServiceException {
        manager = (LibraryManager)sm.lookup(LibraryManager.ROLE);
	}
	
	public Library(LibraryManager lm) {
		manager = lm;
	}
	
	public void setAssistant(JXPathBindingManager.Assistant assistant) {
		this.assistant = assistant;
	}
	
	public void setSourceURI(String uri) {
		sourceURI = uri;
	}
	public String getSourceURI() {
		return sourceURI;
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
	
	public Binding getBinding(String key) throws LibraryException {
		
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
					return manager.getLibrary(((Dependency)inclusions.get(librarykey)).dependencySourceURI, sourceURI).getBinding(definitionkey);
				} catch(Exception e) {
					throw new LibraryException("Couldn't get Library key='"+librarykey+"' source='"+inclusions.get(librarykey)+"",e);
				}
			} else {
				throw new LibraryException("Library '"+librarykey+"' does not exist! (lookup: '"+key+"')");
			}
		} else {
			return (Binding)definitions.get(definitionkey);
		}
	}
	
	public void buildLibrary(Element libraryElement) throws Exception {
		sourceURI = LocationAttributes.getURI(libraryElement);
		this.assistant.getContext().setLocalLibrary(this);
        Element[] bindingElements = DomHelper.getChildElements(libraryElement, BindingManager.NAMESPACE);
        for (int i = 0; i < bindingElements.length; i++) {
            Element bindingElement = bindingElements[i];
            Binding  binding = this.assistant.getBindingForConfigurationElement(bindingElement);
            addBinding(binding);
        }
	}
	
	public void addBinding(Binding binding) throws LibraryException {
		if(definitions.containsKey(binding.getId()))
			throw new LibraryException("Library already contains a binding with this ID!");
		
		binding.setEnclosingLibrary(this);
		
		definitions.put(binding.getId(),binding);
		manager.debug(this+": Put binding with id: "+binding.getId());
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
