/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
package org.apache.cocoon.components.jms;

/**
 * JMSConnectionEventListeners can register themselves with a
 * {@link org.apache.cocoon.components.jms.JMSConnectionEventNotifier} 
 * in order to be notified of connect and disconnect events.
 */
public interface JMSConnectionEventListener {
    
    /**
     * Called when a JMS connection has been established.
     * 
     * @param name   the name of the JMS connection.
     */
    void onConnection(String name);

    /**
     * Called when a JMS connection is being disconnected.
     * 
     * @param name  the name of the JMS connection.
     */
    void onDisconnection(String name);

}
