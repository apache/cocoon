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
package org.apache.cocoon.reading.imageop;

import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import org.apache.avalon.framework.component.Component;

import org.apache.avalon.framework.parameters.Parameters;

import org.apache.cocoon.ProcessingException;

public interface ImageOperation extends Component 
{
    static String ROLE = ImageOperation.class.getName();
    
    void setPrefix( String prefix );
    
    void setup( Parameters params ) 
        throws ProcessingException;
    
    WritableRaster apply( WritableRaster raster );
    
    String getKey();
} 
