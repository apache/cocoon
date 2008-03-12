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
package org.apache.cocoon.objectmodel.impl;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.cocoon.configuration.Settings;
import org.apache.cocoon.el.objectmodel.ObjectModelProvider;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.processing.ProcessInfoProvider;

/**
 * {@link ObjectModelProvider} for <code>cocoon</code> entry.
 *
 * @version $Id$
 * @since 2.2
 */
public class CocoonEntryObjectModelProvider implements ObjectModelProvider {
    
    private Settings settings;
    private ProcessInfoProvider processInfoProvider;

    public Settings getSettings() {
        return settings;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    public ProcessInfoProvider getProcessInfoProvider() {
        return processInfoProvider;
    }

    public void setProcessInfoProvider(ProcessInfoProvider processInfoProvider) {
        this.processInfoProvider = processInfoProvider;
    }

    public Object getObject() {
        Map objectModel = processInfoProvider.getObjectModel();
        
        Map cocoonMap = new HashMap();
        
        //cocoon.request
        Request request = ObjectModelHelper.getRequest(objectModel);
        cocoonMap.put("request", request);
        
        //cocoon.session
        HttpSession session = request.getSession(false);
        if (session != null) {
            cocoonMap.put("session", session);
        }
        
        // cocoon.context
        org.apache.cocoon.environment.Context context = ObjectModelHelper.getContext(objectModel);
        cocoonMap.put("context", context);
        
        cocoonMap.put("settings", this.settings);
        
        return cocoonMap;
    }
    
}
