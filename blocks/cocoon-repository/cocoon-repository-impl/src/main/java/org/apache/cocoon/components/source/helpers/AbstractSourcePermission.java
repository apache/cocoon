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
 * This class is an abstract implementation of a source permission
 *
 * @version $Id$
 */
public abstract class AbstractSourcePermission implements SourcePermission{

    private String  privilege;
    private boolean inheritable;
    private boolean negative;

    /**
     * Sets the privilege of the permission
     *
     * @param privilege Privilege of the permission
     */
    public void setPrivilege(String privilege) {
        this.privilege   = privilege;
    }

    /**
     * Returns the privilege of the permission
     * 
     * @return Privilege of the permission
     */
    public String getPrivilege() {
        return this.privilege;
    }

    /**
     * Sets the inheritable flag
     *
     * @param inheritable If the permission is inheritable
     */
    public void setInheritable(boolean inheritable) {
        this.inheritable = inheritable;
    }

    /**
     * Returns the inheritable flag
     *
     * @return If the permission is inheritable
     */
    public boolean isInheritable() {
        return this.inheritable;
    }

    /**
     * Sets the negative flag
     *
     * @param negative If the permission is a negative permission
     */
    public void setNegative(boolean negative) {
        this.negative = negative;
    }

    /**
     * Returns the negative flag
     * 
     * @return If the permission is a negative permission
     */
    public boolean isNegative() {
        return this.negative;
    }
}
