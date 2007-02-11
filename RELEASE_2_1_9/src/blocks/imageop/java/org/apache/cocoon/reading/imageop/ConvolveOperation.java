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

import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.Raster;
import java.awt.image.RasterOp;
import java.awt.image.WritableRaster;

import java.util.StringTokenizer;

import org.apache.avalon.framework.parameters.Parameters;

import org.apache.cocoon.ProcessingException;

public class ConvolveOperation
    implements ImageOperation
{
    private String   m_Prefix;
    private boolean  m_Enabled;
    private int      m_ConvolveHeight;
    private int      m_ConvolveWidth;
    private float[]  m_Data;
        
    public void setPrefix( String prefix )
    {
        m_Prefix = prefix;
    }
    
    public void setup( Parameters params )
        throws ProcessingException
    {
        m_Enabled = params.getParameterAsBoolean( m_Prefix + "enabled", true);
        m_ConvolveWidth = params.getParameterAsInteger( m_Prefix + "width", 3 );
        m_ConvolveHeight = params.getParameterAsInteger( m_Prefix + "height", 3 );
        String values = params.getParameter( m_Prefix + "data", "" );
        m_Data = getFloatArray( values );
        if( m_Data.length != m_ConvolveWidth * m_ConvolveHeight )
            throw new ProcessingException( "The width*height must be equal to the number of data elements given: " + (m_ConvolveWidth * m_ConvolveHeight) + " is not compatible with '"  + values + "'" );
    }
    
    public WritableRaster apply( WritableRaster image )
    {
        if( ! m_Enabled )
            return image;
            
        Kernel kernel = new Kernel( m_ConvolveWidth, m_ConvolveHeight, m_Data );
        ConvolveOp op = new ConvolveOp( kernel, ConvolveOp.EDGE_NO_OP, null );
        WritableRaster r = op.filter( image, null );
        return r;
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
    
    private String getDataAsString()
    {
        StringBuffer b = new StringBuffer();
        for( int i = 0 ; i < m_Data.length ; i++ )
        {
            if( i != 0 )
                b.append( "," );
            b.append( m_Data[ i ] );
        }
        String result = b.toString();
        b.setLength( 0 );
        return result;
    }
    
    public String getKey()
    {
        return "convolve:" 
               + ( m_Enabled ? "enable" : "disable" )
               + ":" + m_ConvolveWidth
               + ":" + m_ConvolveHeight
               + ":" + getDataAsString()
               + ":" + m_Prefix;
    }
} 
 
