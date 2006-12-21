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
package org.apache.cocoon.sitemap;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.SourceResolver;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Map;

/**
 * The SitemapModelComponent identifies the contract between the Sitemap and
 * your pipeline components that create or transform information.  The types
 * of components that fit within this umbrella are your Generators,
 * Transformers, and your Readers.  It is very important to note that all
 * components impementing this interface must be pooled or created on demand.
 * This is due to the separation between the setup and the execution.  If you
 * don't ensure every instance of the component is unique within a pipeline,
 * or accross pipelines, then the setup process will start killing all the
 * other setups and you will end up with serious race conditions.  It's not
 * that they need synchronized keywords applied to the methods, its that the
 * methods have to be called in a certain order.  This is by design.  If you
 * really think about it, due to the SAX infrastructure we would still need to
 * keep them synchronized because the order of SAX events affects the validity
 * of your XML document.
 * 
 * @version $Id$
 */
public interface SitemapModelComponent {
    /**
     * The Sitemap will call the setup() method to prepare the component for
     * use.  This is where you start the process of getting your information
     * ready to generate your results.  See {@link org.apache.cocoon.environment.ObjectModelHelper} for help with the <code>objectModel</code>.
     *
     * @param resolver     The <code>SourceResolver</code> to find resources within your context.
     * @param objectModel  A <code>java.util.Map</code> that contains the request and session information.
     * @param src          The value of the "src" attribute in the sitemap.
     * @param par          The sitemap parameters passed into your component.
     *
     * @throws SAXException if there is a problem reading a SAX stream.
     * @throws IOException  if there is a problem reading files.
     * @throws ProcessingException if there is any other unexpected problem.
     */
    void setup(SourceResolver resolver, Map objectModel, String src, Parameters par) 
    throws ProcessingException, SAXException, IOException;
}
