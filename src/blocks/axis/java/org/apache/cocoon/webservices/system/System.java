/*

============================================================================
                  The Apache Software License, Version 1.1
============================================================================

Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

Redistribution and use in source and binary forms, with or without modifica-
tion, are permitted provided that the following conditions are met:

1. Redistributions of  source code must  retain the above copyright  notice,
   this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

3. The end-user documentation included with the redistribution, if any, must
   include  the following  acknowledgment:  "This product includes  software
   developed  by the  Apache Software Foundation  (http://www.apache.org/)."
   Alternately, this  acknowledgment may  appear in the software itself,  if
   and wherever such third-party acknowledgments normally appear.

4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
   used to  endorse or promote  products derived from  this software without
   prior written permission. For written permission, please contact
   apache@apache.org.

5. Products  derived from this software may not  be called "Apache", nor may
   "Apache" appear  in their name,  without prior written permission  of the
   Apache Software Foundation.

THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
(INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

This software  consists of voluntary contributions made  by many individuals
on  behalf of the Apache Software  Foundation and was  originally created by
Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.webservices.system;

import java.util.Properties;
import org.apache.excalibur.util.SystemUtil;

/**
 * Class which provides JVM system related SOAP services.
 *
 * @author <a href="mailto:crafterm@apache.org">Marcus Crafter</a>
 * @version CVS $Id: System.java,v 1.1 2003/03/09 00:02:31 pier Exp $
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
