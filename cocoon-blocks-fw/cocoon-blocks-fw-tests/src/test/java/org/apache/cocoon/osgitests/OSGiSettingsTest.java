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

import junit.framework.TestCase;

import org.apache.cocoon.core.Settings;

public class OSGiSettingsTest extends TestCase {

	private static Settings settings;

	protected Settings getSettings() {
		return settings;
	}

	protected void setSettings(final Settings settings) {
		OSGiSettingsTest.settings = settings;
	}

	public void testIsServiceAvailable() {
//		assertNotNull(settings);
	}
	
}
