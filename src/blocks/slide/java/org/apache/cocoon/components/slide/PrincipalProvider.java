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

import org.apache.avalon.framework.component.Component;

import org.apache.cocoon.ProcessingException;

/**
 * Manager for principals and grop of users. The implementation
 * is similar to the classes java.security.* .
 *
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @version CVS $Id: PrincipalProvider.java,v 1.1 2003/12/02 19:18:46 unico Exp $
 */
public interface PrincipalProvider extends Component {

    /** Role for the component */
    public final static String ROLE = PrincipalProvider.class.getName();

    /**
     * Return all users.
     *
     * @param caller The principal, which should do the operation
     * @return List of all principals
     */
    public Principal[] getPrincipals(Principal caller) 
        throws ProcessingException;

    /**
     * Add or modify a given principal.
     *
     * @param caller The principal, which should do the operation.
     * @param principal The Principal, which should be add/modified.
     */
    public void addPrincipal(Principal caller, Principal principal) 
        throws ProcessingException;

    /**
     * Remove a given principal.
     *
     * @param caller The principal, which should do the operation.
     * @param principal The Principal, which should be removed.
     */
    public void removePrincipal(Principal caller, Principal principal)
        throws ProcessingException;

    /**
     * Return all groups.
     *
     * @param caller The principal, which should do the operation.
     * @return List of all groups.
     */
    public PrincipalGroup[] getPrincipalGroups(Principal caller) 
        throws ProcessingException;

    /**
     * Add or modify a given group.
     *
     * @param caller The principal, which should do the operation.
     * @param group The group, which shoud be add/modified.
     */
    public void addPrincipalGroup(Principal caller, PrincipalGroup group) 
        throws ProcessingException;

    /**
     * Remove a given group.
     *
     * @param caller The principal, which should do the operation.
     * @param group The group, which shoud be removed.
     */
    public void removePrincipalGroup(Principal caller, PrincipalGroup group)
        throws ProcessingException;

    /**
     * Adds the specified member to the group.
     *
     * @param caller The principal, which should do the operation
     * @param group The given group.
     * @param user The principal to add to this group.
     */
    public void addMember(Principal caller, PrincipalGroup group, Principal user) 
        throws ProcessingException;

    /** 
     * Returns true if the passed principal is a member of the group. 
     *
     * @param caller The principal, which should do the operation
     * @param group The given group.
     * @param member The principal whose membership is to be checked.
     * @return True if the principal is a member of this group, false otherwise.
     */
    public boolean isMember(Principal caller, PrincipalGroup group, Principal member) 
        throws ProcessingException;
    
    /** 
     * Returns an array of the members in the group. The 
     * returned objects are instances of Principal 
     *
     * @param caller The principal, which should do the operation
     * @param group The given group.
     * @return An array of the group members.
     */
    public Principal[] members(Principal caller, PrincipalGroup group) 
        throws ProcessingException;

    /**
     * Removes the specified member from the group.
     *
     * @param caller The principal, which should do the operation
     * @param group The given group.
     * @param principal The principal to remove from this group.
     */
    public void removeMember(Principal caller, PrincipalGroup group, Principal principal) 
        throws ProcessingException;
}

