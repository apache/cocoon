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

import org.apache.cocoon.components.source.helpers.SourceProperty;

import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;

/**
 * A source, which owns meta informations in form of properties
 *
 * @author <a href="mailto:stephan@vern.chem.tu-berlin.de">Stephan Michels</a>
 * @version CVS $Id: InspectableSource.java,v 1.2 2004/03/05 13:02:21 bdelacretaz Exp $
 */
public interface InspectableSource extends Source {

    /** 
     * To get a meta information from a source 
     */
    public SourceProperty getSourceProperty(String namespace, String name) throws SourceException;

    /** 
     * To set a meta information 
     */
    public void setSourceProperty(SourceProperty property) throws SourceException;

    /** 
     * Get alll informations 
     */
    public SourceProperty[] getSourceProperties() throws SourceException;

    /**
     * Remove property
     */
    public void removeSourceProperty(String namespace, String name) throws SourceException;
}

