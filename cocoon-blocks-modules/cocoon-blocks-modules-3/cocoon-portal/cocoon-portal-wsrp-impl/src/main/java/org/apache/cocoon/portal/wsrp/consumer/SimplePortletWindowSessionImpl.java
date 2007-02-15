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
package org.apache.cocoon.portal.wsrp.consumer;

import org.apache.wsrp4j.consumer.PortletSession;
import org.apache.wsrp4j.consumer.driver.GenericPortletWindowSessionImpl;

/**
 * Holds the parameters for displaying the portlet in a portlet-window.<br/>
 * The attributes <tt>windowState</tt>, <tt>portletMode</tt> and the navigationalState
 * are representing all preferences of the portlet-window. <br/>
 * 
 * @version $Id$
 */
public class SimplePortletWindowSessionImpl
    extends GenericPortletWindowSessionImpl
    implements SimplePortletWindowSession {

    /** The windowState the portlet has (minimized, normal, maximized)
     * @see org.apache.wsrp4j.util.Constants */
	private String windowState;

    /** The portletMode the portlet has (minimized, normal, maximized)
     * @see org.apache.wsrp4j.util.Constants */
	private String mode;

    /** The navigationalState of the portlet */
	private String navState;

	/**
     * Constructor
     * 
	 * @param windowID
	 * @param pSession
	 */
	public SimplePortletWindowSessionImpl(
		String windowID,
		PortletSession pSession) {
		super(windowID, pSession);
	}
   
	/**
	 * Get the window state of the portlet window 
	 * this session belongs to<br/>  
	 * 
	 * @return the window state
	 **/
	public String getWindowState() {
		return windowState;
	}

   /**
	* Get the portlet mode<br/>
	* 
	* @return The portlet mode of the portlet window.
	**/
	public String getMode()	{
		return mode;
	}

	/**
	 * Set the window state of the portlet instance
	 * this session belongs to<br/>
	 * 
	 * @param windowState The window state  of the portlet window
	 **/
	public void setWindowState(String windowState) {
		this.windowState = windowState;
	}

	/**
	 * Set the portlet mode<br/>
	 * 
	 * @param mode The portlet mode of the portlet window
	 **/
	public void setMode(String mode) {
		this.mode = mode;
	}
	
	/**
	 * @see org.apache.cocoon.portal.wsrp.consumer.SimplePortletWindowSession#getNavigationalState()
	 */
	public String getNavigationalState() {
		return navState;
	}

	/**
	 * @see org.apache.cocoon.portal.wsrp.consumer.SimplePortletWindowSession#setNavigationalState(java.lang.String)
	 */
	public void setNavigationalState(String navState) {
		this.navState = navState;
	}
}
