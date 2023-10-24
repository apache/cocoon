/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.cocoon.servletservice.util;

import org.apache.commons.io.output.ByteArrayOutputStream;

import junit.framework.TestCase;

/**
 * @version $Id$
 */
public class ServletServiceResponseTestCase extends TestCase {
    
    /**
     * Tests if its possible to write to outputStream after
     * calling resetBuffer() method.
     * @throws Exception
     */
    public void testResetBuffer() throws Exception {
        ServletServiceResponse response = new ServletServiceResponse();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        response.setOutputStream(outputStream);

        response.getOutputStream();
        response.resetBuffer();
        response.getOutputStream().write(0);
        
        assertEquals(1, outputStream.size());
    }
    
}
