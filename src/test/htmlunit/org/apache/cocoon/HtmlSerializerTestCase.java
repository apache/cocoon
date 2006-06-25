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
package org.apache.cocoon;

/**
 * Testcase to simulate the behavior of a user that opens a browser and run
 * HTML Serializer test.
 *
 * @version $Id: $
 */
public class HtmlSerializerTestCase extends HtmlUnitTestCase {

    final String pageurl = "/samples/test/serializer-html/";

    
    public void testHtmltSerializer() throws Exception {
        // Using standard reader
        loadHtmlPage(pageurl + "/index.html");

        // Using generator a serializer
        loadHtmlPage(pageurl + "/index1.html");
    }
}
