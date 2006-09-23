/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.cocoon.components.repository;

import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;

/**
 * TODO describe class
 *
 * Instances must be thread safe.
 * @version $Id$
 */
public interface RepositoryInterceptor {

    String ROLE = RepositoryInterceptor.class.getName();

    /** called before a source is removed */
    void preRemoveSource(Source source) throws SourceException;

    /** called before a source was successfully removed */
    void postRemoveSource(Source source) throws SourceException;

    /** called before a source is stored */
    void preStoreSource(Source source) throws SourceException;

    /** called after a source was successfully stored */
    void postStoreSource(Source source) throws SourceException;
}
