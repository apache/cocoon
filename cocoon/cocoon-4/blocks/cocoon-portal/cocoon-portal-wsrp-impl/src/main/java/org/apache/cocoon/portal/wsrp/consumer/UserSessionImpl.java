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

import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.wsrp4j.consumer.GroupSessionMgr;
import org.apache.wsrp4j.consumer.driver.GenericUserSessionImpl;
import org.apache.wsrp4j.exception.WSRPException;

/**
 * Implements a simple consumer-based user session<br/>
 * 
 * Note: Since most of this methods all only for the session handler,
 * consider to make most of the methods package scoped.<br/>
 * 
 * @version $Id$
 */
public class UserSessionImpl extends GenericUserSessionImpl {

    /** The logger */
    protected final Log logger;

    /**
     * Constructor <br/
     * 
     * @param producerID
     * @param userID
     * @param markupURL
     * @param logger
     * @throws WSRPException
     */
    public UserSessionImpl(String producerID, 
                           String userID, 
                           String markupURL,
                           Log    logger)
    throws WSRPException {
        super(producerID, userID, markupURL);
        this.logger = logger;
        this.setGroupSessionTable(new Hashtable());
    }

    /**
     * Get the group session for this group ID<br/>
     *     
     * @param groupID ID of the portlet application
     * @return The a group session for the provided group ID or a new groupSession
     **/
    public GroupSessionMgr getGroupSession(String groupID) throws WSRPException {
        GroupSessionMgr groupSession = null;
        if (groupID != null) {
            groupSession = (GroupSessionMgr)this.groupSessions.get(groupID);
            if (groupSession == null) {
                groupSession = new GroupSessionImpl(groupID, this.getMarkupInterfaceURL(), this.logger);
                addGroupSession(groupSession);
            }
        }
        return groupSession;
    }
}
