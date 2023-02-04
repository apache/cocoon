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

import org.apache.commons.logging.Log;
import org.apache.wsrp4j.consumer.PortletWindowSession;
import org.apache.wsrp4j.consumer.driver.GenericPortletSessionImpl;
import org.apache.wsrp4j.util.Modes;
import org.apache.wsrp4j.util.WindowStates;

/**
 * Defines a session object at the consumer-side
 * to store remote portlet related information that
 * are needed to interact with the portlet<br/> 
 * 
 * @version $Id$
 */
public class PortletSessionImpl extends GenericPortletSessionImpl {

    /** The logger. */
    protected final Log logger;

    /**
     * constructor
     * 
     * @param handle
     * @param logger
     */
    public PortletSessionImpl(String handle, Log logger) {
    	super(handle);
        this.logger = logger;
    }

    /**
     * Get the <code>PortletWindowSession</code> of the portlet window with the given ID<br/>
     * 
     * @param windowID The ID of the portlet window
     * @return The <code>PorletWindowSession</code> with the given ID.    
     **/
    public PortletWindowSession getPortletWindowSession(String windowID) {
        SimplePortletWindowSession session = (SimplePortletWindowSession)this.windowSessions.get(windowID);
        if (session == null) {
            session = new SimplePortletWindowSessionImpl(windowID, this);
            session.setMode(Modes._view);
            session.setWindowState(WindowStates._normal);
            this.windowSessions.put(windowID, session);

            if ( this.logger.isDebugEnabled() ) {
            	logger.debug("Created windowSession with ID: " + windowID);
            }
        }
        return session;
    }
}
