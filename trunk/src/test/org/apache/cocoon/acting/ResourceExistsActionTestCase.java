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
 * @version CVS $Id: ResourceExistsActionTestCase.java,v 1.2 2004/03/08 14:04:19 cziegeler Exp $
 */
public class ResourceExistsActionTestCase extends AbstractActionTestCase {

    public ResourceExistsActionTestCase(String name) {
        super(name);
    }

    public void testExistAction() {

        String src = "resource://org/apache/cocoon/acting/ResourceExistsActionTestCase.xtest";
        Parameters parameters = new Parameters();

        Map result = act("exist", src, parameters);
        assertNotNull("Test if resource exists", result);

        src = "resource://org/apache/cocoon/acting/ResourceExistsActionTestCase.abc";

        result = act("exist", src, parameters);
        assertNull("Test if resource not exists", result);
    }
}
