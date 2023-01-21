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

import java.awt.image.RescaleOp;
import java.awt.image.WritableRaster;
import org.apache.avalon.framework.parameters.Parameters;

public class ColorOperation
    implements ImageOperation {

    private RescaleOp colorFilter;
    private String   prefix;
    private boolean  enabled;

    public void setPrefix( String prefix ) {
        this.prefix = prefix;
    }

    public void setup( Parameters params ) {
        enabled = params.getParameterAsBoolean( prefix + "enabled", true);
        float[] scaleColor = new float[3];
        float[] offsetColor = new float[3];
        scaleColor[0] = params.getParameterAsFloat( prefix + "scale-red", -1.0f);
        scaleColor[1] = params.getParameterAsFloat( prefix + "scale-green", -1.0f);
        scaleColor[2] = params.getParameterAsFloat( prefix + "scale-blue", -1.0f);
        offsetColor[0] = params.getParameterAsFloat( prefix + "offset-red", 0.0f);
        offsetColor[1] = params.getParameterAsFloat( prefix + "offset-green", 0.0f);
        offsetColor[2] = params.getParameterAsFloat( prefix + "offset-blue", 0.0f);
        colorFilter = new RescaleOp( scaleColor, offsetColor, null );
    }

    public WritableRaster apply( WritableRaster image ) {
        if( ! enabled ) {
            return image;
        }
        WritableRaster r = colorFilter.filter( image, null );
        return r;
    }    

    public String getKey() {
        return "colorop:"
               + ( enabled ? "enable" : "disable" )
               + ":" + colorFilter
               + ":" + prefix;
    }
}
