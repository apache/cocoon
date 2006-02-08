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

import java.awt.geom.AffineTransform;

import java.awt.image.AffineTransformOp;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import org.apache.avalon.framework.parameters.Parameters;

public class ScaleOperation
    implements ImageOperation
{
    private String  m_Prefix;
    private boolean m_Enabled;
    private float   m_Scale;
    
    public void setPrefix( String prefix )
    {
        m_Prefix = prefix;
    }
    
    public void setup( Parameters params )
    {
        m_Enabled = params.getParameterAsBoolean( m_Prefix + "enabled", true);
        m_Scale = params.getParameterAsFloat( m_Prefix + "scale", 1.0f );
    }
    
    public WritableRaster apply( WritableRaster image )
    {
        if( ! m_Enabled )
            return image;
        AffineTransform scale = AffineTransform.getScaleInstance( m_Scale, m_Scale );
        AffineTransformOp op = new AffineTransformOp( scale, AffineTransformOp.TYPE_BILINEAR );
        WritableRaster scaledRaster = op.filter( image, null );
        return scaledRaster;
    }

    public String getKey()
    {
        return "scale:" 
               + ( m_Enabled ? "enable" : "disable" )
               + ":" + m_Scale
               + ":" + m_Prefix;
    }
} 
