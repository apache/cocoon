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

public class ResizeOperation
    implements ImageOperation
{
    private String  m_Prefix;
    private boolean m_Enabled;
    private int     m_Height;
    private int     m_Width;
    private boolean m_PreserveRatio;
    private boolean m_AdjustX;
    
    public void setPrefix( String prefix )
    {
        m_Prefix = prefix;
    }
    
    public void setup( Parameters params )
        throws ProcessingException
    {
        m_Enabled = params.getParameterAsBoolean( m_Prefix + "enabled", true);
        m_Height = params.getParameterAsInteger( m_Prefix + "height", 200 );
        if( m_Height < 0 )
            throw new ProcessingException( "Negative Height is not allowed: " + m_Height );
        m_Width = params.getParameterAsInteger( m_Prefix + "width", 300 );
        if( m_Width < 0 )
            throw new ProcessingException( "Negative Width is not allowed: " + m_Width );
        m_PreserveRatio = params.getParameterAsBoolean( m_Prefix + "preserve-ratio", false );
        m_AdjustX = params.getParameterAsBoolean( m_Prefix + "adjust-x", false );
    }
    
    public WritableRaster apply( WritableRaster image )
    {
        if( ! m_Enabled )
            return image;
        double height = image.getHeight();
        double width = image.getWidth();
        double xScale = m_Width / width;
        double yScale = m_Height / height;
        if( m_PreserveRatio )
        {
            if( m_AdjustX )
                xScale = yScale;
            else
                yScale = xScale;
        }
        AffineTransform scale = AffineTransform.getScaleInstance( xScale, yScale );
        AffineTransformOp op = new AffineTransformOp( scale, AffineTransformOp.TYPE_BILINEAR );
        WritableRaster scaledRaster = op.filter( image, null );
        return scaledRaster;
    }

    public String getKey()
    {
        return "resize:"
               + ( m_Enabled ? "enable" : "disable" )
               + ":" + m_Width
               + ":" + m_Height
               + ":" + m_Prefix;
    }
} 
