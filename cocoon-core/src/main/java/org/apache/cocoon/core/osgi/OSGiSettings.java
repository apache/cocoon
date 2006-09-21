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
package org.apache.cocoon.core.osgi;

import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.configuration.impl.MutableSettings;
import org.osgi.service.component.ComponentContext;

/**
 * @version $Id$
 */
public class OSGiSettings extends MutableSettings {
	
    private Logger logger;

    public OSGiSettings(String mode) {
        super(mode);
    }

	protected void activate(ComponentContext componentContext) {
    	//CoreUtil.initSettingsFiles(this, this.logger);
        // componentContext.getBundleContext().getDataFile("cocoon-files");
//    	this.setConfiguration("/META-INF/xconf/cocoon.xconf"); // TODO (DF/RP) probably not used by the OSGi framework!
    	this.makeReadOnly();
    }

	protected Logger getLogger() {
		return logger;
	}

	protected void setLogger(Logger logger) {
		this.logger = logger;
	}

}
