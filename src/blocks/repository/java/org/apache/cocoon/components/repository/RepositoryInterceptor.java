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
 * TODO describe class
 * 
 * Instances must be thread safe.
 * 
 * @author <a href="mailto:unico@apache.org">Unico Hommes</a> 
 */
public interface RepositoryInterceptor {
    
    public static final String ROLE = RepositoryInterceptor.class.getName();
    
    /** called before a source is removed */
    public abstract void preRemoveSource(Source source) throws SourceException;
    
    /** called before a source was successfully removed */
    public abstract void postRemoveSource(Source source) throws SourceException;
    
    /** called before a source is stored */
    public abstract void preStoreSource(Source source) throws SourceException;
    
    /** called after a source was successfully stored */
    public abstract void postStoreSource(Source source) throws SourceException;
    
}
