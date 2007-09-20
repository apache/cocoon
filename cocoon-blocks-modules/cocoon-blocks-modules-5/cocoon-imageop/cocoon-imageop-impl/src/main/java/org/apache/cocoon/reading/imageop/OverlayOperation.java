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

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;

/**
 * Combines the two images such that the second one is put on top of the
 * other one. This handles transparency, so this operation is best for
 * overlaying an icon with a transparent background on top of another image.
 * 
 * @author <a href="mailto:alexander(dot)klimetschek(at)mindquarry(dot)com">
 *         Alexander Klimetschek</a>
 *
 */
public class OverlayOperation implements CombineImagesOperation {

    private String prefix;
    private boolean enabled;
    private int offset_x;
    private int offset_y;
    private String sourceUri;

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void setup(Parameters params) throws ProcessingException {
        enabled = params.getParameterAsBoolean( prefix + "enabled", true);
        offset_x = params.getParameterAsInteger( prefix + "offset-x", 0);
        offset_y = params.getParameterAsInteger( prefix + "offset-y", 0);
        try {
            sourceUri = params.getParameter( prefix + "source");
        } catch (ParameterException e) {
            throw new ProcessingException("Parameter source is required");
        }
    }
    
    public BufferedImage combine(BufferedImage image, BufferedImage overlay) {
        if (!enabled) {
            return image;
        }
        
        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = result.createGraphics();
        
        // first copy the original image into the result
        g.drawImage(image, 0, 0, null);
        
        // TODO: maybe make the rule for AlphaComposite configurable
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP));
        
        // then overlay the second image with alpha compositing turned on
        g.drawImage(overlay, offset_x, offset_y, null);        
        
        return result;
    }

    public String getKey() {
        return "overlay:"
            + ( enabled ? "enable" : "disable" )
            + ":" + offset_x
            + ":" + offset_y
            + ":" + sourceUri
            + ":" + prefix;
    }

    public String getOverlayURI() {
        return sourceUri;
    }

}
