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

import org.apache.excalibur.source.ModifiableSource;
import org.apache.excalibur.source.SourceException;

/**
 * A source, which can be a directory or collection of sources, which can
 * can be modfied.
 *
 * @deprecated  use the one from excalibur sourceresolve instead
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @version CVS $Id: ModifiableTraversableSource.java,v 1.2 2004/03/05 13:02:40 bdelacretaz Exp $
 */
public interface ModifiableTraversableSource extends TraversableSource, ModifiableSource {

    /**
     * Create a collection of sources.
     *
     * @param collectionname Name of the collectiom, which 
     *                       should be created.
     */
    void createCollection(String collectionname) throws SourceException;
}

