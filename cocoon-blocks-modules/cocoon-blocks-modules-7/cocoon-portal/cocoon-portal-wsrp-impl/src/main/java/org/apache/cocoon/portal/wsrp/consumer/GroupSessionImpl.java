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
import org.apache.wsrp4j.consumer.PortletSession;
import org.apache.wsrp4j.consumer.driver.GenericGroupSessionImpl;
import org.apache.wsrp4j.exception.WSRPException;

/**
 * Implements a consumer based group session<br/>
 *
 * @version $Id$
 */
public class GroupSessionImpl extends GenericGroupSessionImpl {

    /** The logger. */
    protected Log logger;

	/**
	 * Constructs a new <code>GroupSessionImpl</code> object with the given groupID.
	 * The portletServicesURL is needed for implementing the <code>PortletServices</code>
	 * interface.
	 * 
	 * @see org.apache.wsrp4j.consumer.PortletServices
     * 	  	
     * @param groupID
     * @param markupInterfaceURL
     * @param logger
     * @throws WSRPException
     */
    public GroupSessionImpl(String groupID, String markupInterfaceURL, Log logger)
    throws WSRPException {
        super(groupID, markupInterfaceURL);
        this.logger = logger;
    }

	/**
	* Get the portlet session object which is identified with
	* the given instanceKey from the group session. If no portlet session
	* with that instanceKey exists it depends of the implementation wether
	* null or a newly created portlet session object is returned.
	* 
	* @param handle The key which identifies the portlet session object
	* 
	* @return PortletSession The portlet session with the given key
	**/
    public PortletSession getPortletSession(String handle) {
        if (handle == null) {
            return null;
        }

        PortletSession portletSession = (PortletSession)portletSessions.get(handle);
        if (portletSession == null) {
            portletSession = new PortletSessionImpl(handle, this.logger);
            addPortletSession(portletSession);
        }

        return portletSession;
    }
}