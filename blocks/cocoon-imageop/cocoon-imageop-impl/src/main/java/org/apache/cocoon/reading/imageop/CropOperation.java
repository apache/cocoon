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

import java.awt.image.WritableRaster;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;

/**
 * The Crop operation crops the image according to the specified aspect ratio.
 * A ratio of 2 means, the height of the image is twice the width, a ratio of 0.5
 * means the height of the image is half of the width.
 * Add summary documentation here.
 *
 * @version $Id$
 */
public class CropOperation
    implements ImageOperation {

    private String  prefix;
    private boolean enabled;

    /**
     * ratio = height : width
     */
    private float     ratio;

    public void setPrefix( String prefix ) {
        this.prefix = prefix;
    }

    public void setup( Parameters params )
    throws ProcessingException {
        enabled = params.getParameterAsBoolean( prefix + "enabled", true);
        ratio = params.getParameterAsFloat( prefix + "ratio", 200 );
        if( ratio < 0 ) {
            throw new ProcessingException( "Negative Height is not allowed: " + ratio );
        }
    }

    public WritableRaster apply( WritableRaster image ) {
        if( ! enabled ) {
            return image;
        }
        //maximum new height or width
        int max = Math.min(image.getHeight(), image.getWidth());
        int height;
        int width;
        if (ratio>1) {
        	height = max;
        	width = (int) (max / ratio);
        } else {
        	width = max;
        	height = (int) (max * ratio);
        }

        int hdelta = (image.getWidth() - width) / 2;
        int vdelta = (image.getHeight() - height) / 2;

        WritableRaster result = image.createWritableChild(hdelta, vdelta, width, height, hdelta, vdelta, null);

        return result;
    }

    public String getKey() {
        return "crop:"
               + ( enabled ? "enable" : "disable" )
               + ":" + ratio
               + ":" + prefix;
    }
} 
