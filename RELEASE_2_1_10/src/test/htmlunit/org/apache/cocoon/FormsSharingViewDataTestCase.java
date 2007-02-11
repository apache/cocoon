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
 * Test Cocoon Forms, when subsequent showForm are sharing the same viewdata and
 * an action is invoked from within the form inbetween.
 *
 * @version $Id$
 */
public class FormsSharingViewDataTestCase extends HtmlUnitTestCase {
    final String testPipeline = "/test-suite/forms/";
    final String continuationPath = "/html/body/form/div/input[@name='continuation-id']/@value";
    public void testCachingProcessingPipeline() throws Exception {
        // Loading form "a"
        loadXmlPage(testPipeline + "a");
        // Get continuation id
        String firstScreenContinuation = evalXPath(continuationPath);
        //System.out.println("firstScreenContinuation="+firstScreenContinuation);

        // Invoke the "change" action
        loadXmlPage(testPipeline + firstScreenContinuation + ".continue?forms_submit_id=change&a=0");
        // Get continuation id
        String firstScreenActionContinuation = evalXPath(continuationPath);
        //System.out.println("firstScreenActionContinuation="+firstScreenActionContinuation);

        // Submit the form with "ok" button, which will load form "b"
        loadXmlPage(testPipeline + firstScreenActionContinuation + ".continue?forms_submit_id=ok&a=1");

        // Reactivating the continuation of first screen
        loadXmlPage(testPipeline + firstScreenContinuation + ".continue?forms_submit_id=change&a=0");
    }
}
