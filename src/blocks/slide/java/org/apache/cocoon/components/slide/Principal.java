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
package org.apache.cocoon.components.slide;

/**
 * This class represents a principal. The implementation is based
 * on the interface java.security.Principal.
 *
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @version CVS $Id: Principal.java,v 1.1 2003/12/02 19:18:46 unico Exp $
 */
public class Principal implements java.security.Principal {

    private String name = null;
    private String role = null;
    private String password = null;

    public Principal(String name) {
        this.name = name;
    }

    public Principal(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public Principal(String name, String role, String password) {
        this.name = name;
        this.role = role;
        this.password = password;
    } 

    /**
     * Returns the name of the principal
     *
     * @return Name of principal
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name
     *
     * @param name Name of principal
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the role of the principal
     *
     * @return Role of the principal
     */
    public String getRole() {
        return this.role;
    }
  
    /**
     * Sets the role of the user
     *
     * @param role Role of the principal
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Returns the password of the principal
     *
     * @return Password of the principal
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Sets the password of the user
     *
     * @param password Password of the principal
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /** 
     * Compares this principal to the specified object. Returns true 
     * if the object passed in matches the principal.
     *
     * @param another Principal to compare with.
     * @return True if the principal passed in is the same as that 
     *         encapsulated by this principal, and false otherwise.
     */
    public boolean equals(Object another) {
        if (another instanceof java.security.Principal)
            return this.name.equals(((java.security.Principal)another).getName());
        return false;
    }

    /**
     * Returns a string representation of this principal.
     *
     * @return A string representation of this principal.
     */
    public String toString() {
        return this.name;
    }

    /**
     * Returns a hashcode for this principal.
     *
     * @return A hashcode for this principal.
     */
    public int hashCode() {
        return this.name.hashCode();
    }
}

