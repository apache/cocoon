/*
 * Copyright 2006 The Apache Software Foundation.
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
package org.apache.cocoon.core.osgi;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.environment.impl.AbstractContext;
import org.osgi.service.component.ComponentContext;

/**
 * @version $Id$
 */
public class OSGiServletContext extends AbstractContext {
	
	private Logger logger;
	private ComponentContext componentContext;
	
	public OSGiServletContext(Logger logger, ComponentContext componentContext) {
		super();
		this.logger = logger;
		this.componentContext = componentContext;
	}	

	/**
	 * @see org.apache.cocoon.environment.impl.AbstractContext#getResource(java.lang.String)
	 */
	public URL getResource(String path) throws MalformedURLException {
		if(path.length() == 0 || path.charAt(0) != '/') {
			throw new MalformedURLException("The path (" + path + ") must start with '/'.");
		}
		return this.componentContext.getBundleContext().getBundle().getEntry(path);
	}

	/**
	 * Logging to debug
	 */
	public void log(String msg) {
		this.logger.info(msg);
	}

	/**
	 * Logging to debug
	 */	
	public void log(Exception e, String msg) {
		this.logger.info(msg);
	}

	/**
	 * Logging to debug
	 */	
	public void log(String msg, Throwable t) {
		this.logger.info(msg, t);
	}

}
