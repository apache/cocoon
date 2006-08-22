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

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.SitemapComponentTestCase;

/**
 *
 * @version CVS $Id$
 */
public class FileGeneratorTestCase extends SitemapComponentTestCase {

    public void testFileGenerator() throws Exception {

        System.out.println("testFileGenerator()");

        String type = "file";
        String src = "resource://org/apache/cocoon/generation/FileGeneratorTestCase.source.xml";
        Parameters parameters = new Parameters();
        String result = "resource://org/apache/cocoon/generation/FileGeneratorTestCase.source.xml";

        assertEqual(load(result), generate(type, src, parameters));
    }

    /*
      All comments get lost. The comments within the doctype and _also_ the comments 
      within the document.

    public void testBUG17763() {
        String type = "file";
        String src = "resource://org/apache/cocoon/generation/filetest-input1.xml";
        Parameters parameters = new Parameters();
        String result = "resource://org/apache/cocoon/generation/filetest-result1.xml";

        //print(generate(type, src, parameters));

        //print(load(result));
    
        //assertEqual(load(result), generate(type, src, parameters));
    }*/
}
