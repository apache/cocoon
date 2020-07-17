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
package org.apache.cocoon.components.webdav.impl;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.caching.validity.Event;
import org.apache.cocoon.caching.validity.NameValueEvent;
import org.apache.cocoon.components.webdav.WebDAVEventFactory;
import org.apache.commons.httpclient.HttpURL;

/**
 * Default implementation
 */
public class DefaultWebDAVEventFactory extends AbstractLogEnabled implements
		WebDAVEventFactory, Configurable {

	protected static final String HOST_CONFIG_ELEM = "host";
	protected static final String PORT_CONFIG_ELEM = "port";
	
	private String host = "localhost";
	private int port = 60000;
	
	public void configure(Configuration config) throws ConfigurationException {
		host = config.getChild(HOST_CONFIG_ELEM).getValue(host);
		port = config.getChild(PORT_CONFIG_ELEM).getValueAsInteger(port);
	}

	public Event createEvent(String url) {
		
		// it might only be the path, supplement with host/port
		if(url.startsWith("/")) {
			return createEvent(host, port, url);
		}
		
		try {
			HttpURL newurl = new HttpURL(url);
			
			return createEvent(newurl);
			
		} catch (Exception e) {
			if(getLogger().isErrorEnabled())
    			getLogger().error("Invalid URI, can't create event object!",e);
		}
		return null;
	}

	// optimization for preparsed httpclient url
	public Event createEvent(HttpURL url) {
		Event event = null;
		try {
    		String host = url.getHost();
    		int port = url.getPort();
    		String path = url.getEscapedPathQuery();
    		
    		event = createEvent(host, port, path);
    		
    		if(getLogger().isDebugEnabled())
    			getLogger().debug("Created event for url: "+event.toString());
    	
    	} catch (Exception e) {
    		if(getLogger().isErrorEnabled())
    			getLogger().error("could not create Event!",e);
    	}
    	return event;
	}

	public Event createEvent(Parameters params) throws ParameterException {
		return createEvent( params.getParameter("host"), 
				params.getParameterAsInteger("port"), 
				params.getParameter("path"));
	}
	
	protected Event createEvent(String host, int port, String path) {
		
		if(path.endsWith("/"))
			path = path.substring(0,path.length()-1);
		
		return new NameValueEvent("webdav", host+"|"+port+"|"+path);
	}

}
