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

import java.util.StringTokenizer;

import org.apache.avalon.framework.parameters.Parameters;

import org.apache.cocoon.ProcessingException;

public class AffineTransformOperation
    implements ImageOperation
{
    private String  m_Prefix;
    private boolean m_Enabled;
    private float[] m_Matrix;
    
    public void setPrefix( String prefix )
    {
        m_Prefix = prefix;
    }
    
    public void setup( Parameters params )
        throws ProcessingException
    {
        m_Enabled = params.getParameterAsBoolean( m_Prefix + "enabled", true);
        int size = params.getParameterAsInteger( m_Prefix + "matrix-size", 6 );
        String values = params.getParameter( m_Prefix + "values", null );
        
        if( size != 4 && size != 6 )
            throw new ProcessingException( "Only matrices of 4 or 6 elements can be used." );
        
        if( values != null )
            m_Matrix = getFloatArray( values );
        else
            m_Matrix = new float[ size ];
        
        if( m_Matrix.length != 4 && m_Matrix.length != 6 )
            throw new ProcessingException( "Only matrices of 4 or 6 elements can be used." );
        
        float m00 = params.getParameterAsFloat( m_Prefix + "m00", Float.NaN );
        float m01 = params.getParameterAsFloat( m_Prefix + "m01", Float.NaN );
        float m02 = params.getParameterAsFloat( m_Prefix + "m02", Float.NaN );
        float m10 = params.getParameterAsFloat( m_Prefix + "m10", Float.NaN );
        float m11 = params.getParameterAsFloat( m_Prefix + "m11", Float.NaN );
        float m12 = params.getParameterAsFloat( m_Prefix + "m12", Float.NaN );
        
        if( m_Matrix.length == 4 )
        {
            m_Matrix[0] = m00;
            m_Matrix[1] = m01;
            m_Matrix[2] = m10;
            m_Matrix[3] = m11;
        }
        else
        {
            m_Matrix[0] = m00;
            m_Matrix[1] = m01;
            m_Matrix[2] = m02;
            m_Matrix[3] = m10;
            m_Matrix[4] = m11;
            m_Matrix[5] = m12;
        }
    }

    public WritableRaster apply( WritableRaster image )
    {
        if( ! m_Enabled )
            return image;
        AffineTransform transform = new AffineTransform( m_Matrix );
        AffineTransformOp op = new AffineTransformOp( transform, AffineTransformOp.TYPE_BILINEAR );
        WritableRaster scaledRaster = op.filter( image, null );
        return scaledRaster;
    }

    public String getKey()
    {
        return "affine:" 
               + ( m_Enabled ? "enable" : "disable" )
               + ":" + getMatrixAsString()
               + ":" + m_Prefix;
    }

    private float[] getFloatArray( String values )
    {
        float[] fvalues = new float[ 30 ];
        int counter = 0;
        StringTokenizer st = new StringTokenizer( values, ",", false );
        for( int i = 0 ; st.hasMoreTokens() ; i++ )
        {
            String value = st.nextToken().trim();
            fvalues[ i ] = Float.parseFloat( value );
            counter = counter + 1;
        }
        float[] result = new float[ counter ];
        for( int i = 0 ; i < counter ; i++ )
            result[i] = fvalues[i];
        return result;
    }
    
    private String getMatrixAsString()
    {
        StringBuffer b = new StringBuffer();
        for( int i = 0 ; i < m_Matrix.length ; i++ )
        {
            if( i != 0 )
                b.append( "," );
            b.append( m_Matrix[ i ] );
        }
        String result = b.toString();
        b.setLength( 0 );
        return result;
    }
    
} 
