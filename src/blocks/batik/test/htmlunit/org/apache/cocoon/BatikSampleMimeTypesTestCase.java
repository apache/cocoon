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
package org.apache.cocoon;

/**
 * Tests the basic functionality of the Batik samples by checking
 * their content-type: if a sample gives an error instead of
 * generating an image, its content-type will be text/html.
 *
 * @version $Id: $
 */
public class BatikSampleMimeTypesTestCase
    extends HtmlUnitTestCase
{
    public void testHelloSVG()
        throws Exception
    {
        loadResponse("/samples/hello-world/hello.svg");
        assertEquals("Content type", "image/svg+xml", response.getContentType());
    }

    public void testHelloJPEG()
        throws Exception
    {
        loadResponse("/samples/blocks/batik/hello.jpeg");
        assertEquals("Content type", "image/jpeg", response.getContentType());
    }

    public void testHelloPNG()
        throws Exception
    {
        loadResponse("/samples/blocks/batik/hello.png");
        assertEquals("Content type", "image/png", response.getContentType());
    }

    public void testLogoSVG()
        throws Exception
    {
        loadResponse("/samples/blocks/batik/batikLogo.svg");
        assertEquals("Content type", "image/svg+xml", response.getContentType());
    }

    public void testLogoJPEG()
        throws Exception
    {
        loadResponse("/samples/blocks/batik/batikLogo.jpeg");
        assertEquals("Content type", "image/jpeg", response.getContentType());
    }

    public void testLogoPNG()
        throws Exception
    {
        loadResponse("/samples/blocks/batik/batikLogo.png");
        assertEquals("Content type", "image/png", response.getContentType());
    }

    public void testHenrySVG()
        throws Exception
    {
        loadResponse("/samples/blocks/batik/henryV.svg");
        assertEquals("Content type", "image/svg+xml", response.getContentType());
    }

    public void testHenryJPEG()
        throws Exception
    {
        loadResponse("/samples/blocks/batik/henryV.jpeg");
        assertEquals("Content type", "image/jpeg", response.getContentType());
    }

    public void testHenryPNG()
        throws Exception
    {
        loadResponse("/samples/blocks/batik/henryV.png");
        assertEquals("Content type", "image/png", response.getContentType());
    }

    public void testAnneSVG()
        throws Exception
    {
        loadResponse("/samples/blocks/batik/anne.svg");
        assertEquals("Content type", "image/svg+xml", response.getContentType());
    }

    public void testAnneJPEG()
        throws Exception
    {
        loadResponse("/samples/blocks/batik/anne.jpeg");
        assertEquals("Content type", "image/jpeg", response.getContentType());
    }

    public void testAnnePNG()
        throws Exception
    {
        loadResponse("/samples/blocks/batik/anne.png");
        assertEquals("Content type", "image/png", response.getContentType());
    }
}
