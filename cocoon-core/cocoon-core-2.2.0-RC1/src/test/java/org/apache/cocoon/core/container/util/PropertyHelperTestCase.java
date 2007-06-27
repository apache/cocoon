/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.core.container.util;

import org.apache.cocoon.configuration.PropertyHelper;

import junit.framework.TestCase;

/**
 * Test cases for the {@link PropertyHelper} class.
 *
 * @version $Id$
 */
public class PropertyHelperTestCase extends TestCase {

    public void testReplace() {
        final String testA = "a simple string";
        final String testB = "a simple string with a start token ${ somewhere";
        final String testC = "and this is the } end token";
        final String testD = "${this.does.not.exists}";
        // some tests for not! replacing
        assertEquals(PropertyHelper.replace(testA, null), testA);
        assertEquals(PropertyHelper.replace(testB, null), testB);
        assertEquals(PropertyHelper.replace(testC, null), testC);
        assertEquals(PropertyHelper.replace(testD, null), testD);
        // and finally we have something to replace
        assertEquals(PropertyHelper.replace("${java.home}", null), System.getProperty("java.home"));
    }
}
