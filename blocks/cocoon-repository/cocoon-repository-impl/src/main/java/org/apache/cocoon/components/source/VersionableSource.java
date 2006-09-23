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

import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;

/**
 * A source, which could exist in different versions
 *
 * @version $Id$
 */
public interface VersionableSource extends Source {

    /** 
     * If this source versioned
     */
    boolean isVersioned() throws SourceException;

    /** 
     * Get the current revision of the source
     */
    String getSourceRevision() throws SourceException;

    /** 
     * Sets the wanted revision of the source
     */
    void setSourceRevision(String sourcerevision) throws SourceException;

    /** 
     * Get the current branch of the revision from the source
     */
    String getSourceRevisionBranch() throws SourceException;

    /** 
     * Sets the wanted branch of the revision from the source
     */
    void setSourceRevisionBranch(String sourcerevisionbranch) throws SourceException;

    /** 
     * Get the latest revision
     */
    String getLatestSourceRevision() throws SourceException;
}

