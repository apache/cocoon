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
package org.apache.cocoon.selection;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.cocoon.environment.ObjectModelHelper;

import java.util.Map;

/**
 * A <code>Selector</code> that matches a string against a configurable
 * request header, e.g. "referer".
 *
 * <p><b>Global and local configuration</b></p>
 * <table border="1">
 * <tr><td><code>header-name</code></td><td>Name of the request header to
 * match against</td></tr>
 * </table>
 *
 * @author <a href="mailto:haul@informatik.tu-darmstadt.de">Christian Haul</a>
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: HeaderSelector.java,v 1.1 2003/03/09 00:09:35 pier Exp $
 */
public class HeaderSelector extends AbstractLogEnabled
  implements Configurable, ThreadSafe, Selector {

    protected String defaultName;

    public void configure(Configuration config) throws ConfigurationException {
        // Check old name
        this.defaultName = config.getChild("parameter-name").getValue(null);
        if (defaultName != null) {
            getLogger().warn("'parameter-name' is deprecated. Please use 'header-name'");
        }
        // Load with new one
        this.defaultName = config.getChild("header-name").getValue(this.defaultName);
    }

    public boolean select(String expression, Map objectModel, Parameters parameters) {
        // Check old name
        String name = parameters.getParameter("parameter-name", null);
        if (name != null) {
            getLogger().warn("'parameter-name' is deprecated. Please use 'header-name'");
        } else {
            name = this.defaultName;
        }

        // Load with new one.
        name = parameters.getParameter("header-name", name);

        if (name == null) {
            getLogger().warn("No header name given -- failing.");
            return false;
        }

        String value = ObjectModelHelper.getRequest(objectModel).getHeader(name);
        if (value == null) {
            getLogger().debug("Header '" + name + "' not set -- failing.");
            return false;
        }

        return value.equals(expression);
    }
}
