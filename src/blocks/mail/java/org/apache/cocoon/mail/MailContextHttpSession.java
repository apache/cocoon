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
 * @author Bernhard Huber
 * @since 02 January 2003
 * @version CVS $Id: MailContextHttpSession.java,v 1.2 2003/03/11 19:04:58 vgritsenko Exp $
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
        this.removeStore();
    }
}

