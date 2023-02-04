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
package org.apache.cocoon.components.source;

import org.apache.cocoon.components.source.helpers.SourceCredential;
import org.apache.cocoon.components.source.helpers.SourcePermission;

import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;

/**
 * A source, which is restrictable, which means you need a username and password.
 *
 * @version $Id$
 */
public interface RestrictableSource extends Source {

    /** 
     * Get the current credential for the source
     */
    SourceCredential getSourceCredential() throws SourceException;

    /** 
     * Set the credential for the source
     */
    void setSourceCredential(SourceCredential sourcecredential) throws SourceException;

    /**
     * Add a permission to this source
     *
     * @param sourcepermission Permission, which should be set
     *
     * @throws SourceException If an exception occurs during this operation
     **/
    void addSourcePermission(SourcePermission sourcepermission) throws SourceException;

    /**
     * Remove a permission from this source
     *
     * @param sourcepermission Permission, which should be removed
     *
     * @throws SourceException If an exception occurs during this operation
     **/
    void removeSourcePermission(SourcePermission sourcepermission) throws SourceException;

    /**
     * Returns a list of the existing permissions
     *
     * @return Array of SourcePermission
     */
    SourcePermission[] getSourcePermissions() throws SourceException;
}

