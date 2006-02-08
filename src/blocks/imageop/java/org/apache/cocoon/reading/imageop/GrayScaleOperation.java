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

import java.awt.RenderingHints;

import java.awt.color.ColorSpace;

import java.awt.image.ColorConvertOp;
import java.awt.image.Raster;
import java.awt.image.RasterOp;
import java.awt.image.RescaleOp;
import java.awt.image.WritableRaster;

import org.apache.avalon.framework.parameters.Parameters;

public class GrayScaleOperation
    implements ImageOperation
{
    private String   m_Prefix;
    private boolean  m_Enabled;
 
    public void setPrefix( String prefix )
    {
        m_Prefix = prefix;
    }
    
    public void setup( Parameters params )
    {
        m_Enabled = params.getParameterAsBoolean( m_Prefix + "enabled", true);
   }
    
    public WritableRaster apply( WritableRaster image )
    {
        if( ! m_Enabled )
            return image;
            
        ColorSpace grayspace = ColorSpace.getInstance( ColorSpace.CS_GRAY );
        ColorConvertOp op = new ColorConvertOp( grayspace, null );
        WritableRaster r = op.filter( image, null );
        return r;
    }    
    
    public String getKey()
    {
        return "grayscale:"
               + ( m_Enabled ? "enable" : "disable" )
               + ":" + m_Prefix;
    }
}