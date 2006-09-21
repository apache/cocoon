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

package org.apache.cocoon.transformation;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.SitemapComponentTestCase;

import org.w3c.dom.Document;

/**
 *
 *
 * @version $Id$
 */
public class TraxTransformerTestCase extends SitemapComponentTestCase {

    public void testFunctionForXalan() throws Exception {

        String src = "resource://org/apache/cocoon/transformation/traxtest-style.xsl";
        Parameters parameters = new Parameters();
        String input = "resource://org/apache/cocoon/transformation/traxtest-input.xml";
        String result = "resource://org/apache/cocoon/transformation/traxtest-result.xml";

        assertEqual(load(result), transform("xalan", src, parameters, load(input)));
    }

    public void testStressForXalan() throws Exception {

        String src = "resource://org/apache/cocoon/transformation/traxtest-style.xsl";
        Parameters parameters = new Parameters();
        String input = "resource://org/apache/cocoon/transformation/traxtest-input.xml";
        Document document = load(input);

        for(int i=0; i<100; i++)
          transform("xalan", src, parameters, document);
    }

    /*
     FIXME: test doesn't run within a gump build, see
            http://marc.theaimsgroup.com/?l=xml-cocoon-dev&m=105082989401703&w=2

    public void testFunctionForXSLTC() throws Exception {

        String src = "resource://org/apache/cocoon/transformation/traxtest-style.xsl";
        Parameters parameters = new Parameters();
        String input = "resource://org/apache/cocoon/transformation/traxtest-input.xml";
        String result = "resource://org/apache/cocoon/transformation/traxtest-result.xml";

        assertEqual(load(result), transform("xsltc", src, parameters, load(input)));
    }

    public void testStressForXSLTC() throws Exception {

        String src = "resource://org/apache/cocoon/transformation/traxtest-style.xsl";
        Parameters parameters = new Parameters();
        String input = "resource://org/apache/cocoon/transformation/traxtest-input.xml";
        Document document = load(input);

        for(int i=0; i<100; i++)
          transform("xsltc", src, parameters, document);
    }*/
}
