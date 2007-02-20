/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
import java.awt.image.WritableRaster;
import java.util.StringTokenizer;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;

public class ConvolveOperation
    implements ImageOperation {

    private String   prefix;
    private boolean  enabled;
    private int      convolveHeight;
    private int      convolveWidth;
    private float[]  data;

    public void setPrefix( String prefix ) {
        this.prefix = prefix;
    }
    
    public void setup( Parameters params )
    throws ProcessingException {
        enabled = params.getParameterAsBoolean( prefix + "enabled", true);
        convolveWidth = params.getParameterAsInteger( prefix + "width", 3 );
        convolveHeight = params.getParameterAsInteger( prefix + "height", 3 );
        String values = params.getParameter( prefix + "data", "" );
        data = getFloatArray( values );
        if( data.length != convolveWidth * convolveHeight ) {
            throw new ProcessingException( "The width*height must be equal to the number of data elements given: " + (convolveWidth * convolveHeight) + " is not compatible with '"  + values + "'" );
        }
    }

    public WritableRaster apply( WritableRaster image ) {
        if( ! enabled ) {
            return image;
        }
        Kernel kernel = new Kernel( convolveWidth, convolveHeight, data );
        ConvolveOp op = new ConvolveOp( kernel, ConvolveOp.EDGE_NO_OP, null );
        WritableRaster r = op.filter( image, null );
        return r;
    }

    private float[] getFloatArray( String values ) {
        float[] fvalues = new float[ 30 ];
        int counter = 0;
        StringTokenizer st = new StringTokenizer( values, ",", false );
        for( int i = 0 ; st.hasMoreTokens() ; i++ ) {
            String value = st.nextToken().trim();
            fvalues[ i ] = Float.parseFloat( value );
            counter = counter + 1;
        }
        float[] result = new float[ counter ];
        for( int i = 0 ; i < counter ; i++ ) {
            result[i] = fvalues[i];
        }
        return result;
    }

    private String getDataAsString() {
        StringBuffer b = new StringBuffer();
        for( int i = 0 ; i < data.length ; i++ ) {
            if( i != 0 ) {
                b.append( "," );
            }
            b.append( data[ i ] );
        }
        String result = b.toString();
        b.setLength( 0 );
        return result;
    }

    public String getKey() {
        return "convolve:" 
               + ( enabled ? "enable" : "disable" )
               + ":" + convolveWidth
               + ":" + convolveHeight
               + ":" + getDataAsString()
               + ":" + prefix;
    }
} 
 
