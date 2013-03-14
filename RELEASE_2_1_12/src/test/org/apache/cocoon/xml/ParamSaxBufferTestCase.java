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
package org.apache.cocoon.xml;

import java.util.HashMap;
import java.util.Map;

/**
 * Testcase for ParamSaxBuffer
 */
public class ParamSaxBufferTestCase extends AbstractXMLTestCase {

    public ParamSaxBufferTestCase(final String s) {
        super(s);
    }

    public void testSimpleReplacement() throws Exception {
        char[] chars = "test {param} test".toCharArray();
        Map params = new HashMap();
        params.put("param", "test");

        ParamSaxBuffer sb = new ParamSaxBuffer();
        sb.characters(chars, 0, chars.length);

        assertEquals("test test test", sb.toString(params));
    }

    public void testToString() throws Exception {
        char[] chars = "test {param} test".toCharArray();

        ParamSaxBuffer sb = new ParamSaxBuffer();
        sb.characters(chars, 0, chars.length);

        assertEquals("test  test", sb.toString());
    }

    public void testOpenBrace() throws Exception {
        char[] chars = "test {param test".toCharArray();
        Map params = new HashMap();
        params.put("param", "test");

        ParamSaxBuffer sb = new ParamSaxBuffer();
        // startDocument()/endDocument() is just for getting the chars flushed
        sb.startDocument();
        sb.characters(chars, 0, chars.length);
        sb.endDocument();

        assertEquals("test {param test", sb.toString(params));
    }

}
