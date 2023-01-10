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

package org.apache.cocoon.acting;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.mock.MockRequest;

/**
 *
 *
 * @version $Id$
 */
public class RequestParamActionTestCase extends TestCase {
    private Map objectModel = new HashMap();

    public RequestParamActionTestCase(String name) {
        super(name);
    }

    public void testRequestAction() throws Exception {

        MockRequest request = new MockRequest();
        request.setRequestURI("test.xml?abc=def&ghi=jkl");
        request.setQueryString("abc=def&ghi=jkl");
        request.setContextPath("servlet");
        request.addParameter("abc", "def");
        objectModel.put(ObjectModelHelper.REQUEST_OBJECT, request);

        Parameters parameters = new Parameters();
        parameters.setParameter("parameters", "true");

        RequestParamAction action = new RequestParamAction();
        Map result = action.act(null, null, objectModel, null, parameters); 

        assertNotNull("Test if resource exists", result);
        assertEquals("Test for parameter", "test.xml?abc=def&ghi=jkl", result.get("requestURI"));
        assertEquals("Test for parameter", "?abc=def&ghi=jkl", result.get("requestQuery"));
        assertEquals("Test for parameter", "servlet", result.get("context"));
        assertEquals("Test for parameter", "def", result.get("abc"));
        assertNull("Test for parameter", result.get("ghi"));
    }
}
