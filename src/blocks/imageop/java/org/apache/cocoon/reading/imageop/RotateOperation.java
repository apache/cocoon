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

public class RotateOperation
    implements ImageOperation {

    private String  prefix;
    private boolean enabled;
    private double  angle;
 
    public void setPrefix( String prefix ) {
        this.prefix = prefix;
    }

    public void setup( Parameters params )
    throws ProcessingException {
        enabled = params.getParameterAsBoolean( prefix + "enabled", true);
        double angle = params.getParameterAsFloat( prefix + "angle", 0.0f );
        boolean useRadians = params.getParameterAsBoolean( prefix + "use-radians", false);
        if( ! useRadians ) {
            this.angle = ( angle / 180.0 ) * Math.PI;
        } else {
            this.angle = angle;
        }
    }

    public WritableRaster apply( WritableRaster image ) {
        if( ! enabled ) {
            return image;
        }
        int width = image.getWidth();
        int height = image.getHeight();
        int x = width / 2;
        int y = height / 2;

        WritableRaster newRaster1 = image.createCompatibleWritableRaster(-x, -y, width, height );

        AffineTransform translate = AffineTransform.getTranslateInstance( -x, -y );
        AffineTransformOp op = new AffineTransformOp( translate, AffineTransformOp.TYPE_BILINEAR );
        op.filter( image, newRaster1 );

        AffineTransform rotate = AffineTransform.getRotateInstance( angle );
        op = new AffineTransformOp( rotate, AffineTransformOp.TYPE_BILINEAR );

        WritableRaster newRaster2 = image.createCompatibleWritableRaster(-x, -y, width, height );
        op.filter( newRaster1, newRaster2 );

        return newRaster2;
    }

    public String getKey() {
        return "rotate:"
               + ( enabled ? "enable" : "disable" )
               + ":" + angle
               + ":" + prefix;
    }
} 
