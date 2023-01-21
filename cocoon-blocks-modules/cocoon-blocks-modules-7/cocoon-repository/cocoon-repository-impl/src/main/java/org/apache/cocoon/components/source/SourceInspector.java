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

import org.apache.cocoon.components.source.helpers.SourceProperty;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceValidity;

/**
 * A source inspector exposes source properties.
 *
 * @version $Id$
 */
public interface SourceInspector {

    String ROLE = SourceInspector.class.getName();

    /**
     * Gets the SourceProperty associated with the given Source identified 
     * by the requested namespace and name.
     * 
     * @param source  the source for which to compute the property
     * @param namespace  the namespace of the property
     * @param name  the name of the property
     * @return  the SourceProperty associated with the Source, <code>null</code>
     * if the inspector does not provide this property.
     * @throws SourceException
     */
    SourceProperty getSourceProperty(Source source, String namespace, String name) 
        throws SourceException;

    /**
     * Gets all the SourceProperties associated with the given Source.
     * 
     * @param source  the Source for which to compute the property.
     * @return  the collection of all SourceProperties that could be computed
     * by this SourceInspector.
     * @throws SourceException  
     */
    SourceProperty[] getSourceProperties(Source source) throws SourceException;
    
    /**
     * Check if this inspector handles the property of the given type.
     * 
     * @param namespace  the namespace of the property
     * @param name  the name of the property
     * @return  <code>true</code> if this inspector handles properties of the given type
     * else <code>false</code>.
     */
    boolean handlesProperty(String namespace, String name);
    
    /**
     * Get the validity object that describes the validity state
     * of the properties belonging to the given source.
     * 
     * @param source  the Source for which to calculate the validity
     * its properties, <code>null</code> if the source properties
     * are not cacheable.
     */
    SourceValidity getValidity(Source source);
}

