package org.apache.cocoon.it.sitemap;

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

import org.apache.cocoon.tools.it.HtmlUnitTestCase;

/**
 * Test controller.
 */
public class ControllerTest extends HtmlUnitTestCase {

    /**
     * Passing on to a controller.
     */
    public void testControllerInvocation() throws Exception {
        this.loadResponse("/cocoon-it/controller/invoke");
        assertTrue(this.response.getStatusCode() == 201);
    }

    /**
     * Continue controller execution.
     */
    public void testContinuingController() throws Exception {
        this.loadResponse("/cocoon-it/controller/continue");
        assertTrue(this.response.getStatusCode() == 202);
    }

}
