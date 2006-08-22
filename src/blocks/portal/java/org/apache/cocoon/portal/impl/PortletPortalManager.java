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
package org.apache.cocoon.portal.impl;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.portal.coplet.adapter.impl.PortletAdapter;
import org.apache.cocoon.util.Deprecation;

/**
 * Extends the PortalManager by initializing Pluto
 * 
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @deprecated Use the {@link org.apache.cocoon.portal.coplet.adapter.impl.PortletAdapter}.
 * @version CVS $Id$
 */
public class PortletPortalManager
	extends PortalManagerImpl
	implements Initializable {

    /**
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize() throws Exception {
        Deprecation.logger.info("The PortletPortalManager is deprecated. Please use the PortletPortalManagerAspect instead.");
        PortletAdapter aspect = (PortletAdapter)this.adapterSelector.select("portlet");
        this.chain.addAsFirst(aspect, new Parameters());
    }
}

