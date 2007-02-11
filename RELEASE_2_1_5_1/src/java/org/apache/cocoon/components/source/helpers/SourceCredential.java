/*
 * Copyright 2001,2004 The Apache Software Foundation.
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

package org.apache.cocoon.components.source.helpers;

/**
 * This class represents a credential for a given user
 *
 * @author <a href="mailto:stephan@vern.chem.tu-berlin.de">Stephan Michels</a>
 * @version CVS $Id: SourceCredential.java,v 1.3 2004/03/05 13:02:50 bdelacretaz Exp $
 */
public class SourceCredential {

    private String principal = "guest";
    private String password = "guest";

    /**
     * Create a new credential
     *
     * @param principal The user name
     */
    public SourceCredential(String principal) {
        this.principal = principal;
    }

    /**
     * Create a new credential
     *
     * @param principal The user name
     * @param password Password
     */
    public SourceCredential(String principal, String password) {
        this.principal = principal;
        this.password  = password;
    }

    /**
     * Sets the principal
     *
     * @param principal The user name
     */
    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    /**
     * Returns the principal
     * 
     * @return Principal
     */
    public String getPrincipal() {
        return this.principal;
    }

    /**
     * Sets the password
     *
     * @param password Password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns the password
     * 
     * @return Password
     */
    public String getPassword() {
        return this.password;
    }
}
