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

/**
 * A source, which can be a directory or collection of sources.
 *
 * @deprecated use the one from excalibur sourceresolve instead 
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @version CVS $Id: TraversableSource.java,v 1.2 2004/03/05 13:02:40 bdelacretaz Exp $
 */
public interface TraversableSource extends Source {

    /** 
     * If the source a directory or a collection
     */
    public boolean isSourceCollection() throws SourceException;

    /**
     * Returns the count of child sources.
     */
    public int getChildSourceCount()  throws SourceException;

    /**
     * Return the system id of a child source.
     *
     * @param index Index of the child
     */
    public String getChildSource(int index)  throws SourceException;

    /**
     * Return the system id of the parent source. The method should return
     * null if the source has no parent.
     */
    public String getParentSource();
}

