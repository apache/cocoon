/*
* Copyright 1999-2004 The Apache Software Foundation
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
/* Created on Oct 18, 2003 7:00:43 PM by unico */
package org.apache.cocoon.components.repository;

import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;

/**
 * NOP implementation of RepositoryInterceptor.
 * 
 * @author <a href="mailto:unico@apache.org">Unico Hommes</a> 
 */
public abstract class RepositoryInterceptorBase implements RepositoryInterceptor {

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.repository.RepositoryInterceptor#postRemoveSource(org.apache.excalibur.source.Source)
     */
    public void postRemoveSource(Source source) throws SourceException {
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.repository.RepositoryInterceptor#postStoreSource(org.apache.excalibur.source.Source)
     */
    public void postStoreSource(Source source) throws SourceException {
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.repository.RepositoryInterceptor#preRemoveSource(org.apache.excalibur.source.Source)
     */
    public void preRemoveSource(Source source) throws SourceException {
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.repository.RepositoryInterceptor#preStoreSource(org.apache.excalibur.source.Source)
     */
    public void preStoreSource(Source source) throws SourceException {
    }

}
