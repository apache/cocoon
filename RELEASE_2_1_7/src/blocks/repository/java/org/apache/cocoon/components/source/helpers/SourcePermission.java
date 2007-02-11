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
package org.apache.cocoon.components.source.helpers;

/**
 * This interface represents a permission for a source
 *
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @version CVS $Id: SourcePermission.java,v 1.2 2004/03/05 13:02:21 bdelacretaz Exp $
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
