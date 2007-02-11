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

public class RotateOperation
    implements ImageOperation
{
    private String  m_Prefix;
    private boolean m_Enabled;
    private double  m_Angle;
    
    public void setPrefix( String prefix )
    {
        m_Prefix = prefix;
    }
    
    public void setup( Parameters params )
        throws ProcessingException
    {
        m_Enabled = params.getParameterAsBoolean( m_Prefix + "enabled", true);
        double angle = params.getParameterAsFloat( m_Prefix + "angle", 0.0f );
        boolean useRadians = params.getParameterAsBoolean( m_Prefix + "use-radians", false);
        if( ! useRadians )
            m_Angle = ( angle / 180.0 ) * Math.PI;
        else
            m_Angle = angle;
    }
    
    public WritableRaster apply( WritableRaster image )
    {
        if( ! m_Enabled )
            return image;
        int width = image.getWidth();
        int height = image.getHeight();
        int x = width / 2;
        int y = height / 2;
        
        WritableRaster newRaster1 = image.createCompatibleWritableRaster(-x, -y, width, height );
        
        AffineTransform translate = AffineTransform.getTranslateInstance( -x, -y );
        AffineTransformOp op = new AffineTransformOp( translate, AffineTransformOp.TYPE_BILINEAR );
        op.filter( image, newRaster1 );
        
        AffineTransform rotate = AffineTransform.getRotateInstance( m_Angle );
        op = new AffineTransformOp( rotate, AffineTransformOp.TYPE_BILINEAR );
        
        WritableRaster newRaster2 = image.createCompatibleWritableRaster(-x, -y, width, height );
        op.filter( newRaster1, newRaster2 );
        
        return newRaster2;
    }

    public String getKey()
    {
        return "rotate:"
               + ( m_Enabled ? "enable" : "disable" )
               + ":" + m_Angle
               + ":" + m_Prefix;
    }
} 
