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
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceFactory;
import org.apache.excalibur.source.SourceResolver;


/** Implementation of a {@link Source} that gets its content from
  * and ZIP archive.
  * 
  * A ZIP source can be reached using the zip:// pseudo-protocol. The syntax is
  * zip://myFile.xml@myZip.zip (zip://[file]@[archive])
  * 
  * @author <a href="http://apache.org/~reinhard">Reinhard Poetz</a>
  * @version CVS $Id: ZipSourceFactory.java,v 1.2 2004/03/05 10:07:25 bdelacretaz Exp $
  * @since 2.1.4
  */ 
public class ZipSourceFactory extends AbstractLogEnabled
    implements SourceFactory, ThreadSafe, Serviceable {

    protected ServiceManager manager;
    public static String ZIP_SOURCE_SCHEME = "zip:";

    public Source getSource(String location, Map parameters)
        throws IOException, MalformedURLException {
        
        if ( this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("Processing " + location);
        }
        
        // syntax checks
        int separatorPos = location.indexOf('@');
        if (separatorPos == -1) {
            throw new MalformedURLException("@ required in URI: " + location);
        }
        int protocolEnd = location.indexOf("://");
        if (protocolEnd == -1) {
            throw new MalformedURLException("URI does not contain '://' : " + location);
        }

        // get the source of the archive and return the ZipSource passing
        // a source retrieved from the SourceResolver
        String documentName = location.substring(protocolEnd + 3, separatorPos);
        Source archive;
        SourceResolver resolver = null;
        try {
            resolver = (SourceResolver)this.manager.lookup( SourceResolver.ROLE );
            archive = resolver.resolveURI(location.substring(separatorPos + 1));            
        } catch (ServiceException se) {
            throw new SourceException("SourceResolver is not available.", se);
        } finally {
            this.manager.release(resolver);
        }
        return new ZipSource(archive, documentName);
    }


    public void release(Source source) {
        // not necessary here
    }

    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

}
