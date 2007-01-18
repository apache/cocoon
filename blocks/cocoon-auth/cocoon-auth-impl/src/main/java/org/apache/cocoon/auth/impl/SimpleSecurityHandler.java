/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.auth.impl;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.cocoon.auth.ApplicationManager;
import org.apache.cocoon.auth.AuthenticationException;
import org.apache.cocoon.auth.SecurityHandler;
import org.apache.cocoon.auth.User;
import org.apache.commons.lang.StringUtils;

/**
 * The simple security handler implements the {@link SecurityHandler} interface.
 * The user configuration is done through a properties object which can be configured
 * in the Spring application context.
 * The property file should have the following format:
 * {username}={userpassword}
 * 
 * For example:
 * cziegeler=secret_password
 * cocoon=apache
 * 
 * If you want to specify additional user attributes, use this format:
 * {username}.{attributename}={attributevalue}
 *
 * @version $Id$
 */
public class SimpleSecurityHandler
    extends AbstractSecurityHandler {

    /** The properties. */
    protected Properties userProperties;

    public void setUserProperties(Properties p) {
        this.userProperties = p;
    }

    /**
     * @see org.apache.cocoon.auth.SecurityHandler#login(java.util.Map)
     */
    public User login(final Map loginContext)
    throws AuthenticationException {
        // get user name and password
        final String name = (String)loginContext.get(ApplicationManager.LOGIN_CONTEXT_USERNAME_KEY);
        if ( name == null ) {
            throw new AuthenticationException("Required user name property is missing for login.");            
        }
        final String password = (String)loginContext.get(ApplicationManager.LOGIN_CONTEXT_PASSWORD_KEY);
        // compare password
        if ( !StringUtils.equals(password, this.userProperties.getProperty(name)) ) {
            return null;
        }
        final User user = new StandardUser(name);
        // check for additional attributes
        final String prefix = name + '.';
        final Iterator i = this.userProperties.entrySet().iterator();
        while ( i.hasNext() ) {
            final Map.Entry current = (Map.Entry)i.next();
            if ( current.getKey().toString().startsWith(prefix) ) {
                final String key = current.getKey().toString().substring(prefix.length());
                user.setAttribute(key, current.getValue());
            }
        }
        return user;
    }

    /**
     * @see org.apache.cocoon.auth.SecurityHandler#logout(java.util.Map, org.apache.cocoon.auth.User)
     */
    public void logout(final Map logoutContext, final User user) {
        // nothing to do here
    }
}
