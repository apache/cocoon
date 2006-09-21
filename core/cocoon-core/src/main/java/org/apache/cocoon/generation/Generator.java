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
package org.apache.cocoon.generation;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.sitemap.SitemapModelComponent;
import org.apache.cocoon.xml.XMLProducer;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * A generator is the starting point of a pipeline. It "generates" XML
 * and starts streaming them into the pipeline.
 * 
 * @version $Id$
 */
public interface Generator extends XMLProducer, SitemapModelComponent {

    String ROLE = Generator.class.getName();

    /**
     * Generate the XML and stream it into the pipeline
     */
    void generate()
    throws IOException, SAXException, ProcessingException;
}
