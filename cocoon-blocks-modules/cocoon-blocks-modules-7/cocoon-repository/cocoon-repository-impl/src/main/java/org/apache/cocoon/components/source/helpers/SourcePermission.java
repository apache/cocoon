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
package org.apache.cocoon.components.source.helpers;

/**
 * This interface represents a permission for a source
 *
 * @version $Id$
 */
public interface SourcePermission {

    String PRIVILEGE_ALL               = "all";
    String PRIVILEGE_READ              = "read";
    String PRIVILEGE_WRITE             = "write";

    String PRIVILEGE_READ_ACL          = "read-acl";
    String PRIVILEGE_WRITE_ACL         = "write-acl";

    String PRIVILEGE_READ_SOURCE       = "read-source";
    String PRIVILEGE_CREATE_SOURCE     = "create-source";
    String PRIVILEGE_REMOVE_SOURCE     = "remove-source";

    String PRIVILEGE_LOCK_SOURCE       = "lock-source";
    String PRIVILEGE_READ_LOCKS        = "read-locks";

    String PRIVILEGE_READ_PROPERTY     = "read-property";
    String PRIVILEGE_CREATE_PROPERTY   = "create-property";
    String PRIVILEGE_MODIFY_PROPERTY   = "modify-property";
    String PRIVILEGE_REMOVE_PROPERTY   = "remove-property";

    String PRIVILEGE_READ_CONTENT      = "read-content";
    String PRIVILEGE_CREATE_CONTENT    = "create-content";
    String PRIVILEGE_MODIFY_CONTENT    = "modify-content";
    String PRIVILEGE_REMOVE_CONTENT    = "remove-content";

    String PRIVILEGE_GRANT_PERMISSION  = "grant-permission";
    String PRIVILEGE_REVOKE_PERMISSION = "revoke-permission";

    /**
     * Sets the privilege of the permission
     *
     * @param privilege Privilege of the permission
     */
    void setPrivilege(String privilege);

    /**
     * Returns the privilege of the permission
     * 
     * @return Privilege of the permission
     */
    String getPrivilege();

    /**
     * Sets the inheritable flag
     *
     * @param inheritable If the permission is inheritable
     */
    void setInheritable(boolean inheritable);

    /**
     * Returns the inheritable flag
     *
     * @return If the permission is inheritable
     */
    boolean isInheritable();

    /**
     * Sets the negative flag
     *
     * @param negative If the permission is a negative permission
     */
    void setNegative(boolean negative);

    /**
     * Returns the negative flag
     * 
     * @return If the permission is a negative permission
     */
    boolean isNegative();
}
