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
package org.apache.cocoon.portal.coplet;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * @author <a href="mailto:bluetkemeier@s-und-n.de">Bj&ouml;rn L&uuml;tkemeier</a>
 * 
 * @version CVS $Id$
 */
public final class CopletBaseData { 

	private Map copletConfig = new HashMap(3);

	private String id;

	private String copletAdapterName;

	public CopletBaseData() {
        // Nothing to do
	}

	public String getId() {
		return id;
	}

	public void setId(String name) {
		this.id = name;
	}

	public String getCopletAdapterName() {
		return this.copletAdapterName;
	}

	public Object getCopletConfig(String key) {
		return this.copletConfig.get(key);
	}

	public void setCopletConfig(String key, Object value) {
		this.copletConfig.put(key, value);
	}

	public Map getCopletConfig() {
		return this.copletConfig;
	}

	public void setCopletConfig(Map config) {
		this.copletConfig = config;
	}

	public void setCopletAdapterName(String name) {
		this.copletAdapterName = name;
	}
}
