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

import org.apache.cocoon.ProcessingException;

public class OffsetOperation
    implements ImageOperation
{
    private String  m_Prefix;
    private boolean m_Enabled;
    private int     m_Up;
    private int     m_Left;
    
    public void setPrefix( String prefix )
    {
        m_Prefix = prefix;
    }
    
    public void setup( Parameters params )
        throws ProcessingException
    {
        m_Enabled = params.getParameterAsBoolean( m_Prefix + "enabled", true);
        m_Up = params.getParameterAsInteger( m_Prefix + "up", 0 );
        m_Left = params.getParameterAsInteger( m_Prefix + "left", 0 );
    }
    
    public WritableRaster apply( WritableRaster image )
    {
        if( ! m_Enabled )
            return image;
        AffineTransform translate = AffineTransform.getTranslateInstance( m_Left, m_Up );
        AffineTransformOp op = new AffineTransformOp( translate, AffineTransformOp.TYPE_BILINEAR );
        WritableRaster scaledRaster = op.filter( image, null );
        return scaledRaster;
    }

    public String getKey()
    {
        return "offset:"
               + ( m_Enabled ? "enable" : "disable" )
               + ":" + m_Up
               + ":" + m_Left
               + ":" + m_Prefix;
    }
} 
