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
 * @deprecated This block is deprecated and will be removed in future versions.
 * @version $Id$
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