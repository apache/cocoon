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

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.WritableRaster;
import java.util.StringTokenizer;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;

public class AffineTransformOperation
    implements ImageOperation {

    private String  prefix;
    private boolean enabled;
    private float[] matrix;

    public void setPrefix( String prefix ) {
        this.prefix = prefix;
    }

    public void setup( Parameters params )
    throws ProcessingException {
        enabled = params.getParameterAsBoolean( prefix + "enabled", true);
        int size = params.getParameterAsInteger( prefix + "matrix-size", 6 );
        String values = params.getParameter( prefix + "values", null );

        if( size != 4 && size != 6 ) {
            throw new ProcessingException( "Only matrices of 4 or 6 elements can be used." );
        }

        if( values != null ) {
            matrix = getFloatArray( values );
        } else {
            matrix = new float[ size ];
        }

        if( matrix.length != 4 && matrix.length != 6 ) {
            throw new ProcessingException( "Only matrices of 4 or 6 elements can be used." );
        }

        float m00 = params.getParameterAsFloat( prefix + "m00", Float.NaN );
        float m01 = params.getParameterAsFloat( prefix + "m01", Float.NaN );
        float m02 = params.getParameterAsFloat( prefix + "m02", Float.NaN );
        float m10 = params.getParameterAsFloat( prefix + "m10", Float.NaN );
        float m11 = params.getParameterAsFloat( prefix + "m11", Float.NaN );
        float m12 = params.getParameterAsFloat( prefix + "m12", Float.NaN );

        if( matrix.length == 4 ) {
            matrix[0] = m00;
            matrix[1] = m01;
            matrix[2] = m10;
            matrix[3] = m11;
        } else {
            matrix[0] = m00;
            matrix[1] = m01;
            matrix[2] = m02;
            matrix[3] = m10;
            matrix[4] = m11;
            matrix[5] = m12;
        }
    }

    public WritableRaster apply( WritableRaster image ) {
        if( ! enabled ) {
            return image;
        }
        AffineTransform transform = new AffineTransform( matrix );
        AffineTransformOp op = new AffineTransformOp( transform, AffineTransformOp.TYPE_BILINEAR );
        WritableRaster scaledRaster = op.filter( image, null );
        return scaledRaster;
    }

    public String getKey() {
        return "affine:" 
               + ( enabled ? "enable" : "disable" )
               + ":" + getMatrixAsString()
               + ":" + prefix;
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

    private String getMatrixAsString() {
        StringBuffer b = new StringBuffer();
        for( int i = 0 ; i < matrix.length ; i++ ) {
            if( i != 0 ) {
                b.append( "," );
            }
            b.append( matrix[ i ] );
        }
        String result = b.toString();
        b.setLength( 0 );
        return result;
    }
} 
