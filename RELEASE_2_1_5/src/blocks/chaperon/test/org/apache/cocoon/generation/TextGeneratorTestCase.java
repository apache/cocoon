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

package org.apache.cocoon.generation;

import java.io.InputStream;
import java.io.IOException;
import java.util.HashMap;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.SitemapComponentTestCase;

/**
 *
 *
 * @author <a href="mailto:stephan@apache.org">Stephan Michels </a>
 * @version CVS $Id: TextGeneratorTestCase.java,v 1.5 2004/03/05 13:01:48 bdelacretaz Exp $
 */
public class TextGeneratorTestCase extends SitemapComponentTestCase {

    public TextGeneratorTestCase(String name) {
        super(name);
    }

    public void testTextGenerator1() throws Exception {
        String src = "resource://org/apache/cocoon/generation/texttest-input1.txt";
        Parameters parameters = new Parameters();
        String result = "resource://org/apache/cocoon/generation/texttest-result1.xml";

        assertEqual(load(result), generate("text", src, parameters));
    }

    public void testTextGenerator2() throws Exception {

        String src = "resource://org/apache/cocoon/generation/texttest-input2.txt";
        Parameters parameters = new Parameters();
        String result = "resource://org/apache/cocoon/generation/texttest-result2.xml";

        assertEqual(load(result), generate("text", src, parameters));
    }
}
