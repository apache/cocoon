/*
 * Copyright 2006 The Apache Software Foundation.
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
package org.apache.cocoon.osgitests;

import junit.textui.TestRunner;

import org.apache.avalon.framework.logger.Logger;
import org.osgi.service.component.ComponentContext;

/**
 * @version $Id$
 */
public class OSGiLoggerTest extends AbstractOSGiTestCase {

	private static Logger logger;

	protected Logger getLogger() {
		return logger;
	}

	protected void setLogger(Logger logger) {
		OSGiLoggerTest.logger = logger;
	}
	
	public void testIsServiceAvailable() {
		logger.debug("testIsServiceAvailable");
	}
	
    protected void activate(ComponentContext componentContext) {
    	TestRunner.run(OSGiLoggerTest.class);
    }	
}
