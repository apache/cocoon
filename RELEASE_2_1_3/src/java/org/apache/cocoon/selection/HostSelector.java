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

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.environment.ObjectModelHelper;

import java.util.Map;

/**
 * A <code>Selector</code> that matches a string from within the host parameter
 * of the HTTP request.
 *
 * <p>Configuration:
 * <pre>
 * &lt;map:selector name="host" src="org.apache.cocoon.selection.HostSelector"&gt;
 *   &lt;host name="uk-site" value="www.foo.co.uk"/&gt;
 * &lt;/map:selector&gt;
 * </pre>
 * <p>Usage:
 * <pre>
 * &lt;map:select type="host"&gt;
 *   &lt;map:when test="uk-site"&gt;
 *     &lt;map:transform src="stylesheets/page/uk.xsl"/&gt;
 *   &lt;/map:when&gt;
 *   &lt;map:otherwise&gt;
 *     &lt;map:transform src="stylesheets/page/us.xsl"/&gt;
 *   &lt;/map:otherwise&gt;
 * &lt;/map:select&gt;
 * </pre>
 *
 * @author <a href="mailto:cbritton@centervilletech.com">Colin Britton</a>
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: HostSelector.java,v 1.1 2003/03/09 00:09:35 pier Exp $
 */

public class HostSelector extends NamedPatternsSelector {

    public void configure(Configuration conf) throws ConfigurationException {
        configure(conf, "host", "name", "value");
    }

    public boolean select(String expression, Map objectModel, Parameters parameters) {
        // Inform proxies that response varies with the Host header
        ObjectModelHelper.getResponse(objectModel).addHeader("Vary", "Host");

        // Get the host request header
        String host = ObjectModelHelper.getRequest(objectModel).getHeader("Host");
        if (host == null) {
            getLogger().debug("No Host header -- failing.");
            return false;
        }

        return checkPatterns(expression, host);
    }
}
