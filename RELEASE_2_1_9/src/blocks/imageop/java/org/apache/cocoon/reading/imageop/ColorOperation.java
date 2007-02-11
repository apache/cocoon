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

import java.awt.image.Raster;
import java.awt.image.RasterOp;
import java.awt.image.RescaleOp;
import java.awt.image.WritableRaster;

import org.apache.avalon.framework.parameters.Parameters;

public class ColorOperation
    implements ImageOperation
{
    private RescaleOp m_ColorFilter = null;
    private String   m_Prefix;
    private boolean  m_Enabled;
     
    public void setPrefix( String prefix )
    {
        m_Prefix = prefix;
    }
    
    public void setup( Parameters params )
    {
        m_Enabled = params.getParameterAsBoolean( m_Prefix + "enabled", true);
        float[] scaleColor = new float[3];
        float[] offsetColor = new float[3];
        scaleColor[0] = params.getParameterAsFloat( m_Prefix + "scale-red", -1.0f);
        scaleColor[1] = params.getParameterAsFloat( m_Prefix + "scale-green", -1.0f);
        scaleColor[2] = params.getParameterAsFloat( m_Prefix + "scale-blue", -1.0f);
        offsetColor[0] = params.getParameterAsFloat( m_Prefix + "offset-red", 0.0f);
        offsetColor[1] = params.getParameterAsFloat( m_Prefix + "offset-green", 0.0f);
        offsetColor[2] = params.getParameterAsFloat( m_Prefix + "offset-blue", 0.0f);
        m_ColorFilter = new RescaleOp( scaleColor, offsetColor, null );
    }
    
    public WritableRaster apply( WritableRaster image )
    {
        if( ! m_Enabled )
            return image;
            
        WritableRaster r = m_ColorFilter.filter( image, null );
        return r;
    }    
    
    public String getKey()
    {
        return "colorop:"
               + ( m_Enabled ? "enable" : "disable" )
               + ":" + m_ColorFilter
               + ":" + m_Prefix;
    }
}
