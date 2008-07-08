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
    private boolean allowEnlarge;

    public void setPrefix( String prefix ) {
        this.prefix = prefix;
    }

    public void setup( Parameters params )
    throws ProcessingException {
        enabled = params.getParameterAsBoolean( prefix + "enabled", true);
        height = params.getParameterAsInteger( prefix + "height", 0 );
        if( height < 0 ) {
            throw new ProcessingException( "Negative Height is not allowed: " + height );
        }
        width = params.getParameterAsInteger( prefix + "width", 0 );
        if( width < 0 ) {
            throw new ProcessingException( "Negative Width is not allowed: " + width );
        }
        preserveRatio = params.getParameterAsBoolean( prefix + "preserve-ratio", false );
        adjustX = params.getParameterAsBoolean( prefix + "adjust-x", false );
        allowEnlarge = params.getParameterAsBoolean( prefix + "allow-enlarge", true );
    }
 
    public WritableRaster apply( WritableRaster image ) {
        if( ! enabled ) {
            return image;
        }

        // If parameter width or height is zero, use the original image size.
        // Therefore, if both are zero, the image is returned unchanged.

        if ( width == 0 && height == 0 ) {
            return image;
        }

        double xScale = width == 0 ? 1 : width / (double) image.getWidth();
        double yScale = height == 0 ? 1 : height / (double) image.getHeight();

        if (allowEnlarge || (xScale <= 1 && yScale <= 1))
        {
	        if( preserveRatio )
	        {
	        	if (allowEnlarge) {
	        		if (xScale >= 1) {
	        			yScale = xScale;
	        		} else if (yScale >= 1) {
	        			xScale = yScale;
	        		}
	        	} else {
	        		if (xScale <= 1) {
	        			yScale = xScale;
	        		} else if (yScale <= 1) {
	        			xScale = yScale;
	        		}
	        	}
	            if( adjustX )
	                xScale = yScale;
	            else
	                yScale = xScale;
	        }
	        
	        AffineTransform scale = AffineTransform.getScaleInstance( xScale, yScale );
	        AffineTransformOp op = new AffineTransformOp( scale, AffineTransformOp.TYPE_BILINEAR );
	        WritableRaster scaledRaster = op.filter( image, null );
	        return scaledRaster;
        } else {
        		return image;
        }
    }

    public String getKey() {
        return "resize:"
               + ( enabled ? "enable" : "disable" )
               + ":" + width
               + ":" + height
               + ":" + prefix
               + ":" + ( allowEnlarge ? "allowEnlarge" : "disallowEnlarge" );
    }
} 
