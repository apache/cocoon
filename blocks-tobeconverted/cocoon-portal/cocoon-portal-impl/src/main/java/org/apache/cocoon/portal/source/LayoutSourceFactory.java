/*
 * Copyright 2005 The Apache Software Foundation.
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
package org.apache.cocoon.portal.source;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

import org.apache.cocoon.portal.impl.AbstractComponent;
import org.apache.cocoon.portal.layout.Layout;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceFactory;

/**
 * The source factory for the layout sources.
 *
 * @version $Id$
 */
public class LayoutSourceFactory     
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
        final int pos = location.indexOf(':');
        final String profileKey;
        final String layoutKey;
        if ( pos == -1 ) {
            profileKey = null;
            layoutKey = location;
        } else {
            profileKey = location.substring(0, pos);
            layoutKey = location.substring(pos+1);
        }
        final Layout layout = this.portalService.getProfileManager().getPortalLayout(profileKey, layoutKey);
        if ( layout == null ) {
            throw new IOException("Unable to get layout for: " + location);
        }
        LayoutSource source = new LayoutSource(uri,
                                               protocol,
                                               layout,
                                               this.portalService,
                                               this.manager,
                                               this.context);
        return source;
	}

    /**
     * @see org.apache.excalibur.source.SourceFactory#release(Source)
     */
    public void release(Source source) {
        // nothing to do 
    }
}
