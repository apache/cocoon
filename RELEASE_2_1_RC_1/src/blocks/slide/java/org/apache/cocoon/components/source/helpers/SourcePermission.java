/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache Cocoon" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.cocoon.components.source.helpers;

/**
 * This interface represents a permission for a source
 *
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @version CVS $Id: SourcePermission.java,v 1.2 2003/03/16 17:49:07 vgritsenko Exp $
 */
public interface SourcePermission {

    public final static String PRIVILEGE_ALL               = "all";
    public final static String PRIVILEGE_READ              = "read";
    public final static String PRIVILEGE_WRITE             = "write";

    public final static String PRIVILEGE_READ_ACL          = "read-acl";
    public final static String PRIVILEGE_WRITE_ACL         = "write-acl";

    public final static String PRIVILEGE_READ_SOURCE       = "read-source";
    public final static String PRIVILEGE_CREATE_SOURCE     = "create-source";
    public final static String PRIVILEGE_REMOVE_SOURCE     = "remove-source";

    public final static String PRIVILEGE_LOCK_SOURCE       = "lock-source";
    public final static String PRIVILEGE_READ_LOCKS        = "read-locks";

    public final static String PRIVILEGE_READ_PROPERTY     = "read-property";
    public final static String PRIVILEGE_CREATE_PROPERTY   = "create-property";
    public final static String PRIVILEGE_MODIFY_PROPERTY   = "modify-property";
    public final static String PRIVILEGE_REMOVE_PROPERTY   = "remove-property";

    public final static String PRIVILEGE_READ_CONTENT      = "read-content";
    public final static String PRIVILEGE_CREATE_CONTENT    = "create-content";
    public final static String PRIVILEGE_MODIFY_CONTENT    = "modify-content";
    public final static String PRIVILEGE_REMOVE_CONTENT    = "remove-content";

    public final static String PRIVILEGE_GRANT_PERMISSION  = "grant-permission";
    public final static String PRIVILEGE_REVOKE_PERMISSION = "revoke-permission";

    /**
     * Sets the privilege of the permission
     *
     * @param privilege Privilege of the permission
     */
    public void setPrivilege(String privilege);

    /**
     * Returns the privilege of the permission
     * 
     * @return Privilege of the permission
     */
    public String getPrivilege();

    /**
     * Sets the inheritable flag
     *
     * @param inheritable If the permission is inheritable
     */
    public void setInheritable(boolean inheritable);

    /**
     * Returns the inheritable flag
     *
     * @return If the permission is inheritable
     */
    public boolean isInheritable();

    /**
     * Sets the negative flag
     *
     * @param negative If the permission is a negative permission
     */
    public void setNegative(boolean negative);

    /**
     * Returns the negative flag
     * 
     * @return If the permission is a negative permission
     */
    public boolean isNegative();
}
