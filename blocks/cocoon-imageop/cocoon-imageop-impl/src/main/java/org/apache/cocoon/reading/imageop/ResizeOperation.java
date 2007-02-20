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
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;

public class ResizeOperation
    implements ImageOperation {

    private String  prefix;
    private boolean enabled;
    private int     height;
    private int     width;
    private boolean preserveRatio;
    private boolean adjustX;

    public void setPrefix( String prefix ) {
        this.prefix = prefix;
    }

    public void setup( Parameters params )
    throws ProcessingException {
        enabled = params.getParameterAsBoolean( prefix + "enabled", true);
        height = params.getParameterAsInteger( prefix + "height", 200 );
        if( height < 0 ) {
            throw new ProcessingException( "Negative Height is not allowed: " + height );
        }
        width = params.getParameterAsInteger( prefix + "width", 300 );
        if( width < 0 ) {
            throw new ProcessingException( "Negative Width is not allowed: " + width );
        }
        preserveRatio = params.getParameterAsBoolean( prefix + "preserve-ratio", false );
        adjustX = params.getParameterAsBoolean( prefix + "adjust-x", false );
    }
 
    public WritableRaster apply( WritableRaster image ) {
        if( ! enabled ) {
            return image;
        }
        double height = image.getHeight();
        double width = image.getWidth();
        double xScale = this.width / width;
        double yScale = this.height / height;
        if( preserveRatio )
        {
            if( adjustX )
                xScale = yScale;
            else
                yScale = xScale;
        }
        AffineTransform scale = AffineTransform.getScaleInstance( xScale, yScale );
        AffineTransformOp op = new AffineTransformOp( scale, AffineTransformOp.TYPE_BILINEAR );
        WritableRaster scaledRaster = op.filter( image, null );
        return scaledRaster;
    }

    public String getKey() {
        return "resize:"
               + ( enabled ? "enable" : "disable" )
               + ":" + width
               + ":" + height
               + ":" + prefix;
    }
} 
