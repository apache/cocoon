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

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.webapps.authentication.configuration.HandlerConfiguration;
import org.apache.cocoon.webapps.authentication.user.UserHandler;
import org.apache.excalibur.source.SourceParameters;
import org.w3c.dom.Document;

/**
 * Verify if a user can be authenticated.
 * An authenticator can implement all the usual component lifecycle interfaces
 * and gets the information set.
 * An authenticator must be implemented in a thread safe manner!
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: Authenticator.java,v 1.10 2003/10/24 08:41:46 cziegeler Exp $
*/
public interface Authenticator {

    /**
     * This object describes the success or the failure of an attempt
     * to authenticate a user.
     * The boolean flag valid specifies a success (valid) or a failure
     * (not valid).
     * The document result contains in the case of a success the
     * authentication xml that is store in the session.
     * In the case of a failure, the result can contain information
     * about the failure (or the document can be null).
     * If in the case of a failure the result contains information,
     * the xml must follow this format:
     * <root>
     *   <failed/>
     *   if data is available data is included, otherwise:
     *   <data>No information</data>
     *   If exception message contains info, it is included into failed 
     * </root>
     * The root element is removed and the contained elements are stored
     * into the temporary context.
     */
    public static class AuthenticationResult {
        
        public final boolean  valid;
        public final Document result;

        public AuthenticationResult(final boolean  valid,
                                    final Document result) {
            this.valid = valid;
            this.result = result;
        }

    }

    /**
     * Try to authenticate the user.
     * @return A AuthenticationResult that is either valid (authentication
     *         successful) or invalid (authentication failed.
     * @throws ProcessingException Only if an error occurs
     */
    AuthenticationResult authenticate(HandlerConfiguration configuration,
                                      SourceParameters parameters)
    throws ProcessingException;
    
    /**
     * This notifies the authenticator that a user logs out of the given
     * handler.
     * After the authenticator is notified, the AuthenticationManager
     * removes the authentication context, eventually the session etc.
     */
    void logout(UserHandler handler);
}