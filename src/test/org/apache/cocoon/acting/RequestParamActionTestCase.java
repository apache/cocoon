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
package org.apache.cocoon.acting;

import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;

/**
 *
 *
 * @author <a href="mailto:stephan@apache.org">Stephan Michels </a>
 * @version CVS $Id: RequestParamActionTestCase.java,v 1.3 2004/03/08 14:04:19 cziegeler Exp $
 */
public class RequestParamActionTestCase extends AbstractActionTestCase {

    public RequestParamActionTestCase(String name) {
        super(name);
    }

    public void testRequestAction() {

        getRequest().setRequestURI("test.xml?abc=def&ghi=jkl");
        getRequest().setQueryString("abc=def&ghi=jkl");
        getRequest().setContextPath("servlet");
        getRequest().addParameter("abc", "def");

        Parameters parameters = new Parameters();
        parameters.setParameter("parameters", "true");

        Map result = act("request", null, parameters);

        assertNotNull("Test if resource exists", result);
        assertEquals("Test for parameter", "test.xml?abc=def&ghi=jkl", result.get("requestURI"));
        assertEquals("Test for parameter", "?abc=def&ghi=jkl", result.get("requestQuery"));
        assertEquals("Test for parameter", "servlet", result.get("context"));
        assertEquals("Test for parameter", "def", result.get("abc"));
        assertNull("Test for parameter", result.get("ghi"));
    }
}
