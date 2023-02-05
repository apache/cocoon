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
package org.apache.cocoon.core.xml.resolver;

import java.io.File;

import org.apache.cocoon.blockdeployment.DeploymentUtil;
import org.apache.cocoon.configuration.Settings;
import org.apache.cocoon.core.xml.impl.DefaultEntityResolver;

/**
 *
 * @version $Id$
 * @since 2.2
 */
public class CocoonSystemResolver extends DefaultEntityResolver {

    /** Cocoon settings. */
    protected Settings settings;

    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    /**
     * @see org.apache.cocoon.core.xml.impl.DefaultEntityResolver#init()
     */
    public void init()
    throws Exception {
        // create temporary directory for our entities
        final File workDirectory = new File(settings.getWorkDirectory());
        final File entitiesDirectory = new File(workDirectory, "cocoon_xml_resolver_entities");
        entitiesDirectory.mkdir();
        // deploy resources
        DeploymentUtil.deployJarResources("META-INF/cocoon/entities", entitiesDirectory.getAbsolutePath());
        // set catalog
        this.setCatalog(entitiesDirectory.toURL().toExternalForm() + "/catalog");
        // now initialize
        super.init();
    }
}
