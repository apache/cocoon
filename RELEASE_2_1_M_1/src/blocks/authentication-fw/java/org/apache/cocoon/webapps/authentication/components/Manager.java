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
package org.apache.cocoon.webapps.authentication.components;

import java.io.IOException;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.Redirector;
import org.apache.excalibur.source.SourceParameters;



/**
 * This is the authentication manager.
 * It is used to authenticate (login, logout) a user. Usually, this
 * component should not be used from custom code. The provided
 * actions perform all required tasks.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: Manager.java,v 1.4 2003/04/27 12:52:53 cziegeler Exp $
*/
public interface Manager {

    /** The Avalon Role */
    public static final String ROLE = Manager.class.getName();

    /**
     * Is the current user authenticated for the given handler?
     * @return Returns true of the user is authenticated.
     */
    boolean isAuthenticated(String handlerName)
    throws ProcessingException;

    /**
     * Is the current user authenticated for the given handler?
     * If the user is already authenticated, the {@link RequestState}
     * is updated to the provided information (handler and application).
     */
    boolean checkAuthentication(Redirector redirector,
                                 String     handlerName,
                                 String     applicationName)
    throws ProcessingException, IOException;

    /**
     * Try to login the user.
     * If the authentication is successful, <code>true</code> is returned.
     * If not, <code>false</code> is returned.
     */
    boolean login(String              handlerName,
                   String              applicationName,
                   SourceParameters    parameters)
    throws ProcessingException;

    /**
     * Perform a logout of the user.
     */
    void logout(String handlerName,
                 int mode)
    throws ProcessingException;
}
