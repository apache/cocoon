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
 * A source descriptor handles modifiable source properties.
 * 
 * @author <a href="mailto:unico@apache.org">Unico Hommes</a>
 */
public interface SourceDescriptor extends SourceInspector {

    public static final String ROLE = SourceDescriptor.class.getName();
    
    /**
     * Set a property on a Source.
     * 
     * @param source  the Source to set the SourceProperty on
     * @param property  the SourceProperty to set
     */
    public void setSourceProperty(Source source, SourceProperty property) 
        throws SourceException;
    
    /**
     * Remove a property from a Source.
     * 
     * @param source  the Source to remove the property from
     * @param namespace  namespace identifier of the property to remove
     * @param name  name of the property to remove
     */
    public void removeSourceProperty(Source source, String namespace, String name) 
        throws SourceException;

}
