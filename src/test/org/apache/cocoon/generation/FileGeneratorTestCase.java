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

import org.apache.avalon.framework.parameters.Parameters;

import org.apache.cocoon.AbstractCompositeTestCase;

/**
 *
 * @version CVS $Id: FileGeneratorTestCase.java,v 1.6 2004/03/08 14:04:19 cziegeler Exp $
 */
public class FileGeneratorTestCase extends AbstractCompositeTestCase {

    public FileGeneratorTestCase(String name) {
        super(name);
    }

    public void testFileGenerator() {

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
