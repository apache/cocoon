/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.cocoon.components.modules.input;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.thread.ThreadSafe;

import java.util.Map;

/**
 * SystemPropertyModule is an JXPath based InputModule implementation that
 * provides access to system properties.
 * Available system properties are defined by Java's <a
 * href="http://java.sun.com/j2se/1.4.1/docs/api/java/lang/System.html#getProperties()">System.getProperties()</a>.
 *
 * JXPath allows to apply XPath functions to system properties.
 *
 * <p>If there is a security manager, its <code>checkPropertiesAccess</code>
 * method is called with no arguments. This may result in a security exception
 * which is wrapped into a configuration exception and re-thrown.</p>
 *
 * @author Konstantin Piroumian
 * @version $Id$
 */
public class SystemPropertyModule extends AbstractJXPathModule
                                  implements ThreadSafe {

    /**
     * @throws SecurityException if access is denied
     */
    protected Object getContextObject(Configuration modeConf,
                                      Map objectModel) {
        return System.getProperties();
    }
}
