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

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

import javax.imageio.ImageIO;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.SitemapComponentTestCase;
import org.apache.commons.io.IOUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.impl.ResourceSource;

public class ImageOpReaderTestCase extends SitemapComponentTestCase {
    String imgBase = "resource://org/apache/cocoon/reading/imageop/";
    
    public void testNoOperations() throws Exception {
        String inputURI = imgBase + "4x2.jpg";
        String outputURI = inputURI;
        
        Source expectedSource = new ResourceSource(outputURI);
        byte[] expectedBinaryArray = IOUtils.toByteArray(expectedSource.getInputStream());
        
        assertIdentical(expectedBinaryArray, read("imageop-no-effects", SitemapComponentTestCase.EMPTY_PARAMS, inputURI));
    }
    
    public void testResizeOperationAllowEnlargeTrue() throws Exception {
        BufferedImage resizedImage = null;
        try {
            String inputURI = imgBase + "4x2.jpg";

            Parameters params = new Parameters();
            
            // Both dimensions being enlarged, image should be resized in both dimensions
            params.setParameter("resize-allow-enlarge", "true");
            params.setParameter("resize-width", "8");
            params.setParameter("resize-height", "4");
            byte[] returnedBinaryArray = read("imageop-resize", params,
                    inputURI);
            resizedImage = ImageIO.read(new ByteArrayInputStream(
                    returnedBinaryArray));
            assertEquals(8, resizedImage.getWidth());
            assertEquals(4, resizedImage.getHeight());
            
            // One dimension being enlarged, image should be resized in one dimension
            params.setParameter("resize-height", "2");
            returnedBinaryArray = read("imageop-resize", params, inputURI);
            resizedImage = ImageIO.read(new ByteArrayInputStream(
                    returnedBinaryArray));
            assertEquals(8, resizedImage.getWidth());
            assertEquals(2, resizedImage.getHeight());
            
            // One dimension being enlarged with preserve-ratio, image should be resized proportionally
            params.removeParameter("resize-height");
            params.setParameter("resize-preserve-ratio", "true");
            returnedBinaryArray = read("imageop-resize", params, inputURI);
            resizedImage = ImageIO.read(new ByteArrayInputStream(
                    returnedBinaryArray));
            assertEquals(8, resizedImage.getWidth());
            assertEquals(4, resizedImage.getHeight());
            
        } catch (Exception e) {
            throw e;
        } finally {
            if (resizedImage != null)
                resizedImage.flush();
        }
    }

    public void testResizeOperationAllowEnlargeFalse() throws Exception {
        BufferedImage resizedImage = null;
        try {
            String inputURI = imgBase + "4x2.jpg";

            Parameters params = new Parameters();
            params.setParameter("resize-allow-enlarge", "false");
            
            // Both dimensions being enlarged, image should not be resized
            params.setParameter("resize-width", "8");
            params.setParameter("resize-height", "4");
            byte[] returnedBinaryArray = read("imageop-resize", params,
                    inputURI);
            resizedImage = ImageIO.read(new ByteArrayInputStream(
                    returnedBinaryArray));
            assertEquals(4, resizedImage.getWidth());
            assertEquals(2, resizedImage.getHeight());
            
            // One dimension being enlarged, image should not be resized
            params.setParameter("resize-height", "2");
            returnedBinaryArray = read("imageop-resize", params, inputURI);
            resizedImage = ImageIO.read(new ByteArrayInputStream(
                    returnedBinaryArray));
            assertEquals(4, resizedImage.getWidth());
            assertEquals(2, resizedImage.getHeight());
            
            // One dimension being enlarged with preserve-ratio, image should not be resized
            params.removeParameter("resize-height");
            params.setParameter("resize-preserve-ratio", "true");
            returnedBinaryArray = read("imageop-resize", params, inputURI);
            resizedImage = ImageIO.read(new ByteArrayInputStream(
                    returnedBinaryArray));
            assertEquals(4, resizedImage.getWidth());
            assertEquals(2, resizedImage.getHeight());
            
        } catch (Exception e) {
            throw e;
        } finally {
            if (resizedImage != null)
                resizedImage.flush();
        }
    }
}
