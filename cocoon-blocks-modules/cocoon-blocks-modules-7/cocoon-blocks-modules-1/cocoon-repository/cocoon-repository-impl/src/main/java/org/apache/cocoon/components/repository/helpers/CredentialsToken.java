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
package org.apache.cocoon.components.repository.helpers;

/**
 * A CredentialsToken class to be used with a repository implementation.
 */
public class CredentialsToken {
    
    private Principal principal;
    private String credentials;
    
    /**
     * creates a CredentialsToken
     *
     * @param principal  the principal belonging to the credentials token.
     * @param credentials  the credentials of the credentials token.
     */
    public CredentialsToken(Principal principal, String credentials) {
        this.principal = principal;
        this.credentials = credentials;
    }

    /**
     * get the principal belonging to the credentials token
     * 
     * @return  the principal.
     */
    public Principal getPrincipal() {
        return this.principal;
    }

    /**
     * get the credentials belonging to the credentials token
     *
     * @return String
     */
    public String getCredentials() {
        return credentials;
    }

}