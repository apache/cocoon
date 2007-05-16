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

import org.apache.cocoon.util.AbstractLogEnabled;
import org.apache.wsrp4j.consumer.ConsumerEnvironment;
import org.apache.wsrp4j.consumer.SessionHandler;
import org.apache.wsrp4j.consumer.UserSession;
import org.apache.wsrp4j.consumer.UserSessionMgr;
import org.apache.wsrp4j.exception.WSRPException;

/**
 * Simple Session Handler<br/>
 *
 * @version $Id$
 */
public class SessionHandlerImpl
    extends AbstractLogEnabled
    implements SessionHandler, RequiresConsumerEnvironment {

    /** Holds all user-sessions */
    protected Hashtable userSessions = new Hashtable();
    
    /** The consumerEnvironment */
    protected ConsumerEnvironment consumerEnv;
	
	/**
	 * @see org.apache.cocoon.portal.wsrp.consumer.RequiresConsumerEnvironment#setConsumerEnvironment(org.apache.wsrp4j.consumer.ConsumerEnvironment)
	 */
	public void setConsumerEnvironment(ConsumerEnvironment env) {
        this.consumerEnv = env;
    }

    /**
	 * Get the session manager of the user session with the given user ID and producer ID<br/>
	 * 
	 * @return The user session object representing a session between an end-user and
	 *         a producer.
	 **/
    public UserSessionMgr getUserSession(String producerID, String userID)
    throws WSRPException {
        UserSessionMgr userSession = null;

        if (producerID != null && userID != null) {
            String key = createKey(userID, producerID);
            userSession = (UserSessionMgr)this.userSessions.get(key);

            if (userSession == null) {
                String url = consumerEnv.getProducerRegistry().getProducer(producerID).getMarkupInterfaceEndpoint();
                userSession = new UserSessionImpl(producerID, userID, url, this.getLogger());
                addUserSession(userSession);
            }
        }

        return userSession;
    }

    /**
     * Set the Session into the sessionHandler
     * 
     * @param userSession
     */
    private void addUserSession(UserSession userSession) {
        if (userSession != null) {
            this.userSessions.put(createKey(userSession.getUserID(), userSession.getProducerID()), userSession);
        }
    }

    /**
     * Represents the values of the user and the producer in a nice form<br/>
     * 
     * @param userID
     * @param producerID
     * @return the string containing information of the user and the producer
     */
    private String createKey(String userID, String producerID) {
        return "user :" + userID + " producer:" + producerID;
    }
}
