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
package org.apache.cocoon.components.source.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceFactory;
import org.apache.excalibur.source.SourceParameters;

/**
 *  A factory for WebDAV sources
 *
 *  @author <a href="mailto:g.casper@s-und-n.de">Guido Casper</a>
 *  @author <a href="mailto:gianugo@apache.org">Gianugo Rabellino</a>
 *  @author <a href="mailto:d.madama@pro-netics.com">Daniele Madama</a>
 *  @version $Id: WebDAVSourceFactory.java,v 1.1 2003/07/11 10:32:35 gianugo Exp $
*/
public class WebDAVSourceFactory
    extends AbstractLogEnabled
    implements SourceFactory, ThreadSafe {

    /** The component manager instance */
    private ComponentManager manager = null;

    /**
     * Set the current <code>ComponentManager</code> instance used by this
     * <code>Composable</code>.
     */
    public void compose(ComponentManager manager) throws ComponentException {
        this.manager = manager;
    }

    /**
     * Get a <code>Source</code> object.
     * @param parameters This is optional.
     */
    public Source getSource(String location, Map parameters)
        throws MalformedURLException, IOException, SourceException {
        if ((this.getLogger() != null)
            && (this.getLogger().isDebugEnabled())) {
            this.getLogger().debug("Creating source object for " + location);
        }

        final String protocol = location.substring(0, location.indexOf(':'));
        int position = location.indexOf("://");
        if (position >= 0)
            position += 3;
        else
            position = 0;

        // create the queryString (if available)
        String queryString = null;
        SourceParameters locationParameters = null;
        int queryStringPos = location.indexOf('?');
        if (queryStringPos != -1) {
            queryString = location.substring(queryStringPos + 1);
            location = location.substring(position, queryStringPos);
            locationParameters = new SourceParameters(queryString);

        } else if (position > 0) {
            location = location.substring(position);
            locationParameters = new SourceParameters();
        }

        String repository = locationParameters.getParameter("repository", null);
        String namespace = locationParameters.getParameter("namespace", null);
        String principal = locationParameters.getParameter("principal", null);
        String password = locationParameters.getParameter("password", null);
        String revision = locationParameters.getParameter("revision", null);

        WebDAVSource source = 
            WebDAVSource.newWebDAVSource(location, principal, password, protocol);

        return source;
    }

    public void release(Source source) {
        // do nothing
    }

}
