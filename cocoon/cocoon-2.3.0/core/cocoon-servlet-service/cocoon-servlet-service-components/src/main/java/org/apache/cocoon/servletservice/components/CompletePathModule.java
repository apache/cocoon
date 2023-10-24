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

package org.apache.cocoon.servletservice.components;

import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.components.modules.input.InputModule;
import org.apache.cocoon.environment.ObjectModelHelper;

/**
 * This module provides almost exactly the same functionality as {@link PathModule}. The only difference is that
 * this module adds prefix-path on which block/servlet is mounted.<br>
 * Use this module if you need a base path for URLs pointing resources in your block.
 *
 * @version $Id$
 * @since 1.0.0
 */
public class CompletePathModule implements InputModule {

	PathModule blockPathModule;

	public PathModule getBlockPathModule() {
		return blockPathModule;
	}

	public void setBlockPathModule(PathModule blockPathModule) {
		this.blockPathModule = blockPathModule;
	}

	/* (non-Javadoc)
	 * @see org.apache.cocoon.components.modules.input.InputModule#getAttribute(java.lang.String, org.apache.avalon.framework.configuration.Configuration, java.util.Map)
	 */
	public Object getAttribute(String name, Configuration modeConf, Map objectModel) throws ConfigurationException {
		return ObjectModelHelper.getRequest(objectModel).getContextPath() + blockPathModule.getAttribute(name, modeConf, objectModel);
	}

	/* (non-Javadoc)
	 * @see org.apache.cocoon.components.modules.input.InputModule#getAttributeNames(org.apache.avalon.framework.configuration.Configuration, java.util.Map)
	 */
	public Iterator getAttributeNames(Configuration modeConf, Map objectModel) throws ConfigurationException {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.apache.cocoon.components.modules.input.InputModule#getAttributeValues(java.lang.String, org.apache.avalon.framework.configuration.Configuration, java.util.Map)
	 */
	public Object[] getAttributeValues(String name, Configuration modeConf, Map objectModel) throws ConfigurationException {
		throw new UnsupportedOperationException();
	}

}
