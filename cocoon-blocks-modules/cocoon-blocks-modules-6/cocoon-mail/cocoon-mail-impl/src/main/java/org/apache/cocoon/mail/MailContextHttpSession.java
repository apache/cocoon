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
package org.apache.cocoon.mail;

import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import org.apache.avalon.framework.context.Context;

/**
 *  An extension of MailContext.
 *  <p>
 *    It implments HttpSessionBindingListener for manging MailContext
 *    resources in case of valueUnbound - ie. session removal.
 *  </p>
 *
 * @since 02 January 2003
 * @version $Id$
 */
public class MailContextHttpSession extends MailContext implements HttpSessionBindingListener {

    /**
     *Constructor for the MailContextHttpSession object
     *
     *@param  parent  Description of the Parameter
     */
    public MailContextHttpSession(Context parent) {
        super(parent);
    }


    /**
     * Notifies the object that it is being bound to a session and identifies the session.
     *
     *@param  event  Description of the Parameter
     */
    public void valueBound(HttpSessionBindingEvent event) {
        getLogger().info("value bound " + String.valueOf(event));
    }


    /**
     * Notifies the object that it is being unbound from a session and identifies the session.
     *
     *@param  event  Description of the Parameter
     */
    public void valueUnbound(HttpSessionBindingEvent event) {
        getLogger().info("value unbound " + String.valueOf(event));

        // This should not happen, removeStore of this
        removeStore();
    }
}
