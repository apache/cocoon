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

import org.apache.avalon.framework.CascadingException;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.forms.CacheManager;
import org.apache.cocoon.forms.binding.JXPathBindingManager;
import org.apache.cocoon.forms.util.DomHelper;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * @version $Id$
 *
 */
public class LibraryManagerImpl extends AbstractLogEnabled implements LibraryManager, ThreadSafe, Serviceable,
Configurable, Initializable, Disposable {

	protected static final String PREFIX = "CocoonFormBindingLibrary:";
	
	private ServiceManager serviceManager;
    private Configuration configuration;
    private CacheManager cacheManager;
    
    private JXPathBindingManager bindingManager;

    public void configure(Configuration configuration) throws ConfigurationException {
        this.configuration = configuration;
        getLogger().debug("Gotten a config: top level element: "+this.configuration);
    }

    public void service(ServiceManager serviceManager) throws ServiceException {
        this.serviceManager = serviceManager;
        this.cacheManager = (CacheManager)serviceManager.lookup(CacheManager.ROLE);
    }
    
    public void setBindingManager(JXPathBindingManager bindingManager) {
    	this.bindingManager = bindingManager;
    }
    
    public void initialize() throws Exception {
    	
        // read config to "preload" libraries
    }
    
    public boolean libraryInCache(String librarysource) throws Exception {
    	return libraryInCache(librarysource,null);
    }
    
    public boolean libraryInCache(String librarysource, String relative) throws Exception {
    	SourceResolver sourceResolver = null;
        Source source = null;
        
        if(getLogger().isDebugEnabled())
        	getLogger().debug("Checking if library is in cache: '"+librarysource+"' relative to '"+relative+"'");

        Library lib = null;
        boolean result = false;
        
        try {
            sourceResolver = (SourceResolver)serviceManager.lookup(SourceResolver.ROLE);
            source = sourceResolver.resolveURI(librarysource, relative, null);
            
            lib = (Library)this.cacheManager.get(source, PREFIX);
            
            if( lib != null && lib.dependenciesHaveChanged() ) {
            	result = false;
            	this.cacheManager.set(null,source,PREFIX); //evict?
            }
            else if( lib == null )
            	result = false;
            else
            	result = true;
        } catch(Exception e) {
        	if(getLogger().isErrorEnabled())
            	getLogger().error("Problem getting library '"+librarysource+"' relative to '"+relative+"'!",e);
        	throw e;
        } finally {
            if (source != null)
                sourceResolver.release(source);
            if (sourceResolver != null)
            	serviceManager.release(sourceResolver);
        }

        if(getLogger().isDebugEnabled()) {
        	if(result)
        		getLogger().debug("Library IS in cache : '"+librarysource+"' relative to '"+relative+"'");
        	else
        		getLogger().debug("Library IS NOT in cache : '"+librarysource+"' relative to '"+relative+"'");
        }
        
        return result;
    }
    
    public Library getLibrary(String librarysource) throws Exception {
    	return getLibrary(librarysource,null);
    }
    
	public Library getLibrary(String librarysource, String relative) throws Exception {
		SourceResolver sourceResolver = null;
        Source source = null;
        Document libraryDocument = null;

        Library lib = null;
        
        if(getLogger().isDebugEnabled())
        	getLogger().debug("Getting library instance: '"+librarysource+"' relative to '"+relative+"'");
        
        try {
            sourceResolver = (SourceResolver)serviceManager.lookup(SourceResolver.ROLE);
            source = sourceResolver.resolveURI(librarysource, relative, null);
            
            lib = (Library)this.cacheManager.get(source, PREFIX);
            
            if( lib != null && lib.dependenciesHaveChanged() ) {
            	if(getLogger().isDebugEnabled())
                	getLogger().debug("Library dependencies changed, invalidating!");
            	
            	lib = null;
            }
            
            if( lib == null ) {
            	if(getLogger().isDebugEnabled())
                	getLogger().debug("Library not in cache, creating!");
            	
            	try {
                    InputSource inputSource = new InputSource(source.getInputStream());
                    inputSource.setSystemId(source.getURI());
                    libraryDocument = DomHelper.parse(inputSource, this.serviceManager);
                    
                    lib = getNewLibrary();
                    lib.buildLibrary(libraryDocument.getDocumentElement());
                    
                    this.cacheManager.set(lib,source,PREFIX);
                    
                } catch (Exception e) {
                    throw new CascadingException("Could not parse form definition from " +
                                                 source.getURI(), e);
                }
            }
        } finally {
            if (source != null)
                sourceResolver.release(source);
            if (sourceResolver != null)
            	serviceManager.release(sourceResolver);
        }

        return lib;
	}
	
	public Library getNewLibrary() {
		Library lib = new Library(this);
		lib.setAssistant(this.bindingManager.getBuilderAssistant());
        
        if(getLogger().isDebugEnabled())
        	getLogger().debug("Created new library! "+lib);
        
        return lib;
	}

	public void dispose() {
		this.serviceManager.release(this.cacheManager);
	    this.cacheManager = null;
	    this.serviceManager = null;
	}
	
	public void debug(String msg) {
		if(getLogger().isDebugEnabled()) {
			getLogger().debug(msg);
		}
	}
	
}
