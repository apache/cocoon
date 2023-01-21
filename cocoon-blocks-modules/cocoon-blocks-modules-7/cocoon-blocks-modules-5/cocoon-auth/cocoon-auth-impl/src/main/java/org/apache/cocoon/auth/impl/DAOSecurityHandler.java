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

import java.util.Date;
import java.util.Map;

import org.apache.cocoon.auth.ApplicationManager;
import org.apache.cocoon.auth.AuthenticationException;
import org.apache.cocoon.auth.User;
import org.apache.commons.lang.StringUtils;

/**
 * Implementation of a DAO security handler.
 * This implementation supports:
 * - a retry count (counting up and down)
 * - an expires date for the user account.
 *
 * The database access is forwarded to a configured UserDAO instance.
 *
 * @version $Id$
 */
public class DAOSecurityHandler extends AbstractSecurityHandler {

    protected UserDAO userDAO;

    protected boolean checkExpires = true;
    protected boolean useRetryCount = true;
    protected boolean negateRetryCount = false;
    protected int defaultRetryCount = 3;

    public void setCheckExpires(boolean checkExpires) {
        this.checkExpires = checkExpires;
    }

    public void setDefaultRetryCount(int defaultRetryCount) {
        this.defaultRetryCount = defaultRetryCount;
    }

    public void setNegateRetryCount(boolean negateRetryCount) {
        this.negateRetryCount = negateRetryCount;
    }

    public void setUserDAO(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public void setUseRetryCount(boolean useRetryCount) {
        this.useRetryCount = useRetryCount;
    }

    /**
     * @see org.apache.cocoon.auth.SecurityHandler#login(java.util.Map)
     */
    public User login(Map loginContext)
    throws AuthenticationException {
        // get user name and password
        final String name = (String)loginContext.get(ApplicationManager.LOGIN_CONTEXT_USERNAME_KEY);
        if ( name == null ) {
            throw new AuthenticationException("Required user name property is missing for login.");            
        }
        final String password = (String)loginContext.get(ApplicationManager.LOGIN_CONTEXT_PASSWORD_KEY);

        final UserInfo userinfo = this.userDAO.getUserInfo(name);
        if ( userinfo == null ) {
            return null;
        }
        boolean pwCorrect = StringUtils.equals(userinfo.getPassword(), password);
        // check retry count
        if ( this.useRetryCount ) {
            if ( !negateRetryCount ) {
                if ( userinfo.getRetryCount() >= this.defaultRetryCount ) {
                    throw new AuthenticationException(AuthenticationException.AUTHENTICATION_FAILED_ACCOUNT_IS_CLOSED);
                }
                if ( !pwCorrect ) {
                    userinfo.setRetryCount(userinfo.getRetryCount() + 1);
                    this.userDAO.storeUserInfo(userinfo);
                    if ( userinfo.getRetryCount() == this.defaultRetryCount ) {
                        throw new AuthenticationException(AuthenticationException.AUTHENTICATION_FAILED_ACCOUNT_CLOSED);
                    }
                } else {
                    // reset retry count
                    if ( userinfo.getRetryCount() != 0 ) {
                        userinfo.setRetryCount(0);
                        this.userDAO.storeUserInfo(userinfo);
                    }
                }
            } else {
                // the account is disabled when the counter is zero!
                if ( userinfo.getRetryCount() == 0 ) {
                    throw new AuthenticationException(AuthenticationException.AUTHENTICATION_FAILED_ACCOUNT_IS_CLOSED);
                }
                if ( !pwCorrect ) {
                    userinfo.setRetryCount(userinfo.getRetryCount() - 1);
                    this.userDAO.storeUserInfo(userinfo);
                    if ( userinfo.getRetryCount() == 0 ) {
                        throw new AuthenticationException(AuthenticationException.AUTHENTICATION_FAILED_ACCOUNT_CLOSED);
                    }
                } else {
                    // reset retry count
                    if ( userinfo.getRetryCount() != this.defaultRetryCount ) {
                        userinfo.setRetryCount(this.defaultRetryCount);
                        this.userDAO.storeUserInfo(userinfo);
                    }
                }
            }
        }
        // check expires
        if ( pwCorrect && this.checkExpires ) {
            final Date now = new Date();
            if ( userinfo.getExpires() != null ) {
                if ( userinfo.getExpires().before(now) ) {
                    throw new AuthenticationException(AuthenticationException.AUTHENTICATION_FAILED_PASSWORD_EXPIRED);
                }
            }
        }
        // everything still correct?
        if ( !pwCorrect ) {
            return null;
        }
        return this.userDAO.getUser(userinfo);
    }

    /**
     * @see org.apache.cocoon.auth.SecurityHandler#logout(java.util.Map, org.apache.cocoon.auth.User)
     */
    public void logout(Map context, User user) {
        // nothing to do here
    }

}
