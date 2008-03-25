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
package org.apache.cocoon.util.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.SimpleLog;

import org.apache.cocoon.util.Deprecation;
import org.apache.cocoon.util.DeprecationException;

import junit.framework.TestCase;

/**
 * Test Cases for the Deprecation class.
 * @see org.apache.cocoon.util.Deprecation
 *
 * @version $Id$
 */
public class DeprecationTestCase extends TestCase {

    private Log originalLogger;
    private SimpleLog consoleLogger;


    public DeprecationTestCase(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
        originalLogger = Deprecation.logger;
        // Setup a disabled logger: avoids polluting the test output, and also test
        // that isXXXEnabled also matches the forbidden deprecation level
        consoleLogger = new SimpleLog("test");
        consoleLogger.setLevel(SimpleLog.LOG_LEVEL_OFF);
        Deprecation.setLogger(consoleLogger);
        Deprecation.setForbiddenLevel(Deprecation.ERROR);
    }
    
    public void tearDown() throws Exception {
        Deprecation.setLogger(originalLogger);
        super.tearDown();
    }
    
    public void testPrecond() {
        // Double check that our logger won't let anything go through, and that
        // enabled levels are because of the allowed level we've set.
        assertFalse(consoleLogger.isInfoEnabled());
        assertFalse(consoleLogger.isWarnEnabled());
        assertFalse(consoleLogger.isErrorEnabled());
    }
    
    public void testInfoOk() {
        try {
            Deprecation.logger.info("testing deprecation logs");
        } catch(DeprecationException de) {
            fail("Should not throw an exception");
        }
    }

    public void testWarnOk() {        
        try {
            Deprecation.logger.warn("testing deprecation logs");
        } catch(DeprecationException de) {
            fail("Should not throw an exception");
        }
    }
    
    public void testErrorFails() {
        try {
            Deprecation.logger.error("testing deprecation logs");
        } catch(DeprecationException de) {
            return; // success
        }
        fail("Should throw an exception");
    }
    
    public void testDebugFails() {
        Deprecation.setForbiddenLevel(Deprecation.DEBUG);
        try {
            Deprecation.logger.debug("testing deprecation logs");
        } catch(DeprecationException de) {
            return; // success
        }
        fail("Should throw an exception");
    }

    public void testInfoDisabled() {
        assertFalse(Deprecation.logger.isInfoEnabled());
    }

    public void testWarnDisabled() {
        assertFalse(Deprecation.logger.isWarnEnabled());
    }

    public void testErrorEnabled() {
        assertTrue(Deprecation.logger.isErrorEnabled());
    }
}
