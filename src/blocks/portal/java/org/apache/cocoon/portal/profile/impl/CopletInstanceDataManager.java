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
package org.apache.cocoon.portal.profile.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.cocoon.portal.coplet.CopletInstanceData;

/**
 * Holds instances of CopletInstanceData.
 *
 * @author <a href="mailto:bluetkemeier@s-und-n.de">Bj&ouml;rn L&uuml;tkemeier</a>
 * 
 * @version CVS $Id$
 */
public class CopletInstanceDataManager {
	
	/**
	 * The coplet instance data instances.
	 */
	private final Map copletInstanceData;

    public CopletInstanceDataManager() {
        this.copletInstanceData = new HashMap();
    }

    public CopletInstanceDataManager(Map instances) {
        this.copletInstanceData = instances;
    }

	/**
	 * Gets all coplet instance data.
	 */
	public Map getCopletInstanceData() {
		return this.copletInstanceData;
	}

	/**
	 * Gets the specified coplet instance data. 
	 */
	public CopletInstanceData getCopletInstanceData(String copletId) {
		return (CopletInstanceData)this.copletInstanceData.get(copletId);
	}
	
	/**
	 * Puts the specified coplet instance data to the manager.
	 */
	public void putCopletInstanceData(CopletInstanceData data) {
		this.copletInstanceData.put(data.getId(), data);
	}
}
