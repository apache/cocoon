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
package org.apache.cocoon.auth;

/**
 * This exception is thrown by a {@link SecurityHandler} if the authentication
 * fails.
 *
 * @version $Id$
 */
public class AuthenticationException extends Exception {

    public static final int AUTHENTICATION_FAILED = -1;
    public static final int AUTHENTICATION_FAILED_ACCOUNT_CLOSED = -2;
    public static final int AUTHENTICATION_FAILED_ACCOUNT_IS_CLOSED = -3;
    public static final int AUTHENTICATION_FAILED_PASSWORD_EXPIRED = -4;

    protected int errorCode = AUTHENTICATION_FAILED;

    public AuthenticationException() {
        super();
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(Throwable cause) {
        super(cause);
    }

    public AuthenticationException(int s) {
        super();
        this.errorCode = s;
    }

    public AuthenticationException(String message, int s, Throwable cause) {
        super(message, cause);
        this.errorCode = s;
    }

    public AuthenticationException(String message, int s) {
        super(message);
        this.errorCode = s;
    }

    public AuthenticationException(int s, Throwable cause) {
        super(cause);
        this.errorCode = s;
    }

    public int getErrorCode() {
        return this.errorCode;
    }
}
