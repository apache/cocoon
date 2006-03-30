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

import java.util.List;

import org.apache.cocoon.ProcessingException;

/**
 * A versioning helper interface intended to be used by flowscripts or corresponding wrapper components.
 */
public interface RepositoryVersioningHelper {
    
    /**
     * checkout a resource
     * 
     * @param uri  the uri of the resource.
     * @return  a boolean indicating success.
     * @throws ProcessingException
     */
    boolean checkout(String uri) throws ProcessingException;

    /**
     * checkin a resource
     * 
     * @param uri  the uri of the resource.
     * @return  a boolean indicating success.
     * @throws ProcessingException
     */
    boolean checkin(String uri) throws ProcessingException;

    /**
     * undo a previously done checkout
     * 
     * @param uri  the uri of the resource.
     * @return  a boolean indicating success.
     * @throws ProcessingException
     */
    boolean uncheckout(String uri) throws ProcessingException;

    /**
     * check if a resource is under version control
     * 
     * @param uri  the uri of the resource.
     * @return  a boolean indicating if the resource is under version control.
     * @throws ProcessingException
     */
    boolean isVersioned(String uri) throws ProcessingException;

    /**
     * set a resource under version control
     * 
     * @param uri  the uri of the resource.
     * @param versioned  if true the resource is set under version control.
     * @return  a boolean indicating success.
     * @throws ProcessingException
     */
    boolean setVersioned(String uri, boolean versioned) throws ProcessingException;

    /**
     * get the version history of a resource
     * 
     * @param uri  the uri of the resource.
     * @return  a list containing the versions.
     * @throws ProcessingException
     */
    List getVersions(String uri) throws ProcessingException;

}