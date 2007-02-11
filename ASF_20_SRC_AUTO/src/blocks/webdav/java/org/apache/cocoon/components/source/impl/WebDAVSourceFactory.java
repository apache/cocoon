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
package org.apache.cocoon.components.source.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.commons.httpclient.HttpURL;
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
 *  @version $Id: WebDAVSourceFactory.java,v 1.7 2004/03/05 13:02:26 bdelacretaz Exp $
*/
public class WebDAVSourceFactory
    extends AbstractLogEnabled
    implements SourceFactory, ThreadSafe {

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

		HttpURL url = new HttpURL("http://" + location.substring(location.indexOf(':')+3));       
		String principal = url.getUser();
		String password = url.getPassword();
		location = url.getHost() + ":" + url.getPort();
		if(url.getPathQuery() != null) location += url.getPathQuery();

        if(principal == null || password == null) {
			String queryString = url.getQuery();
			SourceParameters locationParameters = new SourceParameters(queryString);
			principal = locationParameters.getParameter("principal", principal);
			password = locationParameters.getParameter("password", password);
        }

        WebDAVSource source =
            WebDAVSource.newWebDAVSource(location, principal, password, protocol,getLogger());
            
        return source;
    }

    public void release(Source source) {
        // do nothing
    }

}
