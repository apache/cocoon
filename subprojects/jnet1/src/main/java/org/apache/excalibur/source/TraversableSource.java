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
package org.apache.excalibur.source;


import java.util.Collection;

/**
 * A traversable source is a source that can have children and
 * a parent, like a file system.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id$
 */
public interface TraversableSource extends Source {

    /**
     * Is this source a collection, i.e. it possibly has children ?
     * For a filesystem-based implementation, this would typically mean that
     * this source represents a directory and not a file.
     *
     * @return true if the source exists and is traversable.
     */
    boolean isCollection();

    /**
     * Get the children of this source if this source is traversable.
     * <p>
     * <em>Note:</em> only those sources actually fetched from the
     * collection need to be released using the {@link SourceResolver}.
     *
     * @see #isCollection()
     * @return a collection of {@link Source}s (actually most probably <code>TraversableSource</code>s).
     * @throws SourceException this source is not traversable, or if some problem occurs.
     */
    Collection getChildren() throws SourceException;

    /**
     * Get a child of this source, given its name. Note that the returned source
     * may not actually physically exist, and that this must be checked using
     * {@link Source#exists()}.
     *
     * @param name the child name.
     * @return the child source.
     * @throws SourceException if this source is not traversable or if some other
     *         error occurs.
     */
    Source getChild(String name) throws SourceException;

    /**
     * Return the name of this source relative to its parent.
     *
     * @return the name
     */
    String getName();

    /**
     * Get the parent of this source as a {@link Source} object.
     *
     * @return the parent source, or <code>null</code> if this source has no parent.
     * @throws SourceException if some problem occurs.
     */
    Source getParent() throws SourceException;
}
