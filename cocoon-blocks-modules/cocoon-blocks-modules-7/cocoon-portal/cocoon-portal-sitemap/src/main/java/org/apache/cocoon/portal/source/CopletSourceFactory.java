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
package org.apache.cocoon.portal.source;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.portal.avalon.AbstractComponent;
import org.apache.cocoon.portal.om.CopletInstance;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceFactory;

/**
 * The source factory for the coplet sources.
 *
 * @version $Id$
 */
public class CopletSourceFactory
    extends AbstractComponent
    implements SourceFactory {

	/**
	 * @see org.apache.excalibur.source.SourceFactory#getSource(String, Map)
	 */
	public Source getSource(String location, Map parameters)
    throws MalformedURLException, IOException {

        String uri = location;
        String protocol = null;

        // remove the protocol
        int position = location.indexOf(':') + 1;
        if (position != 0) {
            protocol = location.substring(0, position);
            location = location.substring(position+2);
        }
        try {
            CopletInstance coplet = this.portalService.getProfileManager().getCopletInstance(location);
            if ( coplet == null ) {
                throw new IOException("Unable to get coplet for " + location);
            }
            CopletSource copletSource =
                new CopletSource(uri,
                                 protocol,
                                 coplet);
            copletSource.service(this.manager);
            return copletSource;
        } catch (ServiceException ce) {
            throw new SourceException("Unable to setup coplet source.", ce);
        }
	}

    /**
     * @see org.apache.excalibur.source.SourceFactory#release(Source)
     */
    public void release(Source source) {
        // nothing to do
    }
}
