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
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceFactory;
import org.apache.excalibur.source.SourceUtil;
import org.apache.excalibur.source.URIAbsolutizer;

/**
 * This class implements the cocoon: protocol.
 * It cannot be used like other source factories
 * as it needs the current <code>Sitemap</code> as input.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: SitemapSourceFactory.java,v 1.8 2004/03/08 14:01:55 cziegeler Exp $
 * 
 * @avalon.component
 * @avalon.service type=SourceFactory
 * @x-avalon.lifestyle type=singleton
 * @x-avalon.info name=cocoon-source
 */
public final class SitemapSourceFactory
    extends AbstractLogEnabled
    implements SourceFactory, Serviceable, URIAbsolutizer
{
    /** The <code>ServiceManager</code> */
    private ServiceManager manager;

    /**
     * Serviceable
     * 
     * @avalon.dependency type="org.apache.excalibur.source.SourceResolver"
     * @avalon.dependency type="org.apache.cocoon.Processor"
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

    /**
     * Get a <code>Source</code> object.
     * @param parameters This is optional.
     */
    public Source getSource( String location, Map parameters )
        throws MalformedURLException, IOException {
        if( getLogger().isDebugEnabled() ) {
            getLogger().debug( "Creating source object for " + location );
        }

        return new SitemapSource( this.manager,
                                  location,
                                  parameters,
                                  getLogger());
    }
    
    /**
     * Release a {@link Source} object.
     */
    public void release( Source source ) {
        if ( null != source ) {
            if ( this.getLogger().isDebugEnabled() ) {
                this.getLogger().debug("Releasing source " + source.getURI());
            }
            ((SitemapSource)source).recycle();
        }
    }

    public String absolutize(String baseURI, String location) {
        return SourceUtil.absolutize(baseURI, location, true);
    }

}
