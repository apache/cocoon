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
package org.apache.cocoon.reading;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.sitemap.SitemapModelComponent;
import org.apache.cocoon.sitemap.SitemapOutputComponent;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * A reader can be used to generate binary output for a request.
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Id$
 */
public interface Reader extends SitemapModelComponent, SitemapOutputComponent {

    String ROLE = Reader.class.getName();

    /**
     * Generate the response.
     */
    void generate()
    throws IOException, SAXException, ProcessingException;

    /**
     * @return the time the read source was last modified or 0 if it is not
     *         possible to detect
     */
    long getLastModified();
}
