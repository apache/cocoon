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
package org.apache.cocoon.portal.pluto.services;

import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.pluto.om.window.PortletWindow;
import org.apache.pluto.services.property.PropertyManagerService;

/**
 * Dummy property manager service. Since vendor specific information from 
 * the portlets is not needed they are discarded.
 *
 * @see javax.portlet.PortletResponse#addProperty(String, String)
 * @see javax.portlet.PortletResponse#setProperty(String, String) 
 *
 * @version $Id$
 */
public class PropertyManagerServiceImpl implements PropertyManagerService {

	/**
	 * @see org.apache.pluto.services.property.PropertyManagerService#setResponseProperties(org.apache.pluto.om.window.PortletWindow, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.util.Map)
	 */
	public void setResponseProperties(PortletWindow window, 
                                      HttpServletRequest request, 
                                      HttpServletResponse response,
                                      Map properties) {
		// do nothing
	}

	/**
	 * @see org.apache.pluto.services.property.PropertyManagerService#getRequestProperties(org.apache.pluto.om.window.PortletWindow, javax.servlet.http.HttpServletRequest)
	 */
	public Map getRequestProperties(PortletWindow window, HttpServletRequest request) {
		return Collections.EMPTY_MAP;
	}
}
