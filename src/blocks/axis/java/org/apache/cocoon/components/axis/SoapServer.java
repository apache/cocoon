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
package org.apache.cocoon.components.axis;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContext;

import org.apache.avalon.framework.activity.Startable;
import org.apache.avalon.framework.component.Component;

import org.apache.axis.MessageContext;

/**
 * <code>SoapServer</code> interface.
 *
 * <p>
 *  This interface describes the operations provided by any Axis
 *  Soap Server implementations.
 * </p>
 *
 * <p>
 *  Example use:
 *
 *  <pre>
 *    SoapServer server = (SoapServer) manager.lookup(SoapServer.ROLE);
 *    MessageContext message = server.createMessageContext(req, res, con);
 *    server.invoke(message);
 *    manager.release(server);
 *    // message sent back to sender
 *  </pre>
 * </p>
 *
 * @author <a href="mailto:crafterm@apache.org">Marcus Crafter</a>
 * @version CVS $Id: SoapServer.java,v 1.2 2004/03/05 13:01:41 bdelacretaz Exp $
 */
public interface SoapServer extends Component, Startable
{
    /**
     * Component's ROLE definition
     */
    String ROLE = SoapServer.class.getName();

    /**
     * Constant used to key message context entries for an avalon logger
     */
    String LOGGER = "axis-message-context-logger";

    /**
     * Invoke a particular message context on this server. This method
     * takes the given message, invokes it on the server and sets
     * the response inside it for the caller to retrieve.
     *
     * @param message a <code>MessageContext</code> instance
     * @exception Exception if an error occurs
     */
    void invoke(MessageContext message)
        throws Exception;

    /**
     * Method to create a new message context, based on this Axis
     * server instance, and the caller's request, response, and 
     * context objects.
     *
     * @param req a <code>HttpServletRequest</code> instance
     * @param res a <code>HttpServletResponse</code> instance
     * @param con a <code>ServletContext</code> instance
     * @return a <code>MessageContext</code> instance
     */
    MessageContext createMessageContext(
        HttpServletRequest req,
        HttpServletResponse res,
        ServletContext con
    );
}
