/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

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
 * @version CVS $Id: SoapServer.java,v 1.1 2003/03/09 00:02:26 pier Exp $
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
