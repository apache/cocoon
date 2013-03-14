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
package org.apache.cocoon;

/**
 * HtmlUnit TestCase for memory leak in internal requests (Bugzilla #26186).
 *
 * @version $Id: $
 */
public class Bug26186InternalRequestMemoryLeakTestCase
    extends HtmlUnitTestCase
{
    static final String pageurl = "/samples/blocks/xsp/java/resolver";

    public void testMemoryLeak()
        throws Exception
    {
        final int iterations = Integer.parseInt(System.getProperty("htmlunit.test.Bug26186InternalRequestMemoryLeak.iterations"));

        final String phrase = "An XSP Page using a source";
        for( int i = 0; i < iterations; i++ ) {
            loadResponse(pageurl);
            assertEquals("Status code", 200, response.getStatusCode());
            assertTrue("Content should contain: "+phrase,
                       response.getContentAsString().indexOf(phrase) != -1);

            if( (i+1)%100 == 0 ) {
                logger.info("Memory leak checking "+(i+1)+" of "+iterations+" iterations");
            }
        }
    }
}
