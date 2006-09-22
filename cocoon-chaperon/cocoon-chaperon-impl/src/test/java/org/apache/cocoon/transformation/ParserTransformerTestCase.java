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

/**
 *
 *
 * @version $Id$
 */
public class ParserTransformerTestCase extends SitemapComponentTestCase {

    public void testParserTransformer1() throws Exception {

        String src = "resource://org/apache/cocoon/transformation/parsertest-grammar1.xml";
        Parameters parameters = new Parameters();
        String input = "resource://org/apache/cocoon/transformation/parsertest-input1.xml";
        String result = "resource://org/apache/cocoon/transformation/parsertest-result1.xml";

        assertEqual(load(result), transform("parser", src, parameters, load(input)));
    }

    public void testParserTransformer2() throws Exception {

        String src = "resource://org/apache/cocoon/transformation/parsertest-grammar1.xml";
        Parameters parameters = new Parameters();
        String input = "resource://org/apache/cocoon/transformation/parsertest-input2.xml";
        String result = "resource://org/apache/cocoon/transformation/parsertest-result2.xml";

        assertEqual(load(result), transform("recovery-parser", src, parameters, load(input)));
    }

    /*public void testParserTransformer3() throws Exception {
        String src = "resource://org/apache/cocoon/transformation/parsertest-grammar1.xml";
        Parameters parameters = new Parameters();
        String input = "resource://org/apache/cocoon/transformation/parsertest-input3.xml";
        String result = "resource://org/apache/cocoon/transformation/parsertest-result3.xml";

        assertEqual(load(result), transform("recovery-parser", src, parameters, load(input)));
    }*/
}
