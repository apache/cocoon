/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import java.util.HashMap;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.SitemapComponentTestCase;

/**
 *
 *
 * @author <a href="mailto:stephan@apache.org">Stephan Michels </a>
 * @version CVS $Id: ParserTransformerTestCase.java,v 1.6 2004/03/05 13:01:48 bdelacretaz Exp $
 */
public class ParserTransformerTestCase extends SitemapComponentTestCase {

    public ParserTransformerTestCase(String name) {
        super(name);
    }

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
