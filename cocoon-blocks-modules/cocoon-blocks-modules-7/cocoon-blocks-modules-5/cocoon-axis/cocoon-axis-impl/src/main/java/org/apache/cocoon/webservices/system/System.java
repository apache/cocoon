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
package org.apache.cocoon.webservices.system;

import java.util.Properties;
import org.apache.commons.lang.SystemUtils;

/**
 * Class which provides JVM system related SOAP services.
 *
 * @version $Id$
 */
public class System {

    /**
     * <code>getProperties</code> returns the current System Properties object.
     *
     * @return a <code>Properties</code> instance
     * @throws SecurityException if access is denied
     */
    public Properties getProperties() {
        return java.lang.System.getProperties();
    }

    /**
     * <code>getArchitecture</code> returns the host architecture.
     *
     * @return host architecture
     */
    public String getArchitecture() {
        return SystemUtils.OS_ARCH;
    }

    /**
     * <code>getOperatingSystem</code> returns the host operating system
     *
     * @return host operating system
     */
    public String getOperatingSystem() {
        return SystemUtils.OS_NAME;
    }

    /**
     * <code>getOperatingSystemVersion</code> returns the host operating system
     * version
     *
     * @return host operating system version
     */
    public String getOperatingSystemVersion() {
        return SystemUtils.OS_VERSION;
    }
}
