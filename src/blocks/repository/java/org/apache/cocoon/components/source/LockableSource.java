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
package org.apache.cocoon.components.source;

import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;

import org.apache.cocoon.components.source.helpers.SourceLock;

/**
 * A source, which could be locked
 *
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @version CVS $Id: LockableSource.java,v 1.3 2004/03/05 13:02:21 bdelacretaz Exp $
 */
public interface LockableSource extends Source {

    /**
     * Add a lock to this source
     *
     * @param sourcelock Lock, which should be added
     *
     * @throws SourceException If an exception occurs during this operation
     */
    public void addSourceLocks(SourceLock sourcelock) throws SourceException;

    /**
     * Returns a list of locks on the source.
     *
     * @return Enumeration of SourceLock
     */
    public SourceLock[] getSourceLocks() throws SourceException;
}

