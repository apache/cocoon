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
package org.apache.cocoon.webservices.system;

import java.util.Properties;
import org.apache.excalibur.util.SystemUtil;

/**
 * Class which provides JVM system related SOAP services.
 *
 * @author <a href="mailto:crafterm@apache.org">Marcus Crafter</a>
 * @version CVS $Id: System.java,v 1.2 2004/03/05 13:01:45 bdelacretaz Exp $
 */
public class System {
	
    /**
     * <code>getProperties</code> returns the current System Properties object.
     *
     * @return a <code>Properties</code> instance
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
        return SystemUtil.architecture();
    }

    /**
     * <code>getCPUInfo</code> returns host CPU information.
     *
     * @return host CPU information
     */
    public String getCPUInfo() {
        return SystemUtil.cpuInfo();
    }

    /**
     * <code>getNumProcessors</code> returns the number of processors in
     * this machine.
     *
     * @return number of processors
     */
    public int getNumProcessors() {
        return SystemUtil.numProcessors();
    }

    /**
     * <code>getOperatingSystem</code> returns the host operating system
     *
     * @return host operating system
     */
    public String getOperatingSystem() {
        return SystemUtil.operatingSystem();
    }

    /**
     * <code>getOperatingSystemVersion</code> returns the host operating system
     * version
     *
     * @return host operating system version
     */
    public String getOperatingSystemVersion() {
        return SystemUtil.osVersion();
    }
}
