/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
package org.apache.cocoon.util;

import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.ExtendedSitemapComponentTestCase;

public class IncludingConfigurationTestCase extends
        ExtendedSitemapComponentTestCase {
    Logger logger = new ConsoleLogger(ConsoleLogger.LEVEL_WARN);

    public Logger getLogger() {
        return this.logger;
    }

    public void testInclude() throws Exception {
        DefaultConfiguration conf = new DefaultConfiguration("root");
        DefaultConfiguration includeNode = new DefaultConfiguration("include");
        includeNode
                .setValue("resource://org/apache/cocoon/util/included-conf.xml");
        conf.addChild(includeNode);

        IncludingConfiguration resultConf = new IncludingConfiguration(conf,
                getManager());
        assertEquals("root", resultConf.getName());
        assertNotNull(resultConf.getChild("node", false));
        assertEquals("value", resultConf.getChild("node").getValue());
        assertEquals(0, resultConf.getChildren("include").length);
    }
}