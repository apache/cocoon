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
 * Test redirecting
 */
public class RedirectTest extends HtmlUnitTestCase {

    /**
     * A temporary redirect
     */
    public void testTemporaryRedirect() throws Exception {
        this.loadResponse("/cocoon-it/redirect/www.orf.at");
        assertEquals(302, this.response.getStatusCode());
        assertEquals("http://www.orf.at", this.response.getResponseHeaderValue("Location"));
    }

}
