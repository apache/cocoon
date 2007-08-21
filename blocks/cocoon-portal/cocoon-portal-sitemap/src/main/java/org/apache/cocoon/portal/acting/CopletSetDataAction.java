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
package org.apache.cocoon.portal.acting;

import java.util.Map;

import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.portal.event.Event;
import org.apache.cocoon.portal.event.EventManager;
import org.apache.cocoon.portal.event.coplet.CopletJXPathEvent;
import org.apache.cocoon.portal.sitemap.Constants;

/**
 * Using this action, you can set values in a coplet.
 *
 * @version $Id$
 */
public class CopletSetDataAction
    extends AbstractPortalAction {

	/**
	 * @see org.apache.cocoon.acting.Action#act(org.apache.cocoon.environment.Redirector, org.apache.cocoon.environment.SourceResolver, java.util.Map, java.lang.String, org.apache.avalon.framework.parameters.Parameters)
	 */
	public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters)
    throws Exception {
        // determine coplet id
        String copletId = null;
        Map context = (Map)objectModel.get(ObjectModelHelper.PARENT_CONTEXT);
        if (context != null) {
            copletId = (String)context.get(Constants.COPLET_ID_KEY);
        } else {
            copletId = (String)objectModel.get(Constants.COPLET_ID_KEY);
        }

        if (copletId == null) {
            throw new ConfigurationException("copletId must be passed in the object model either directly (e.g. by using ObjectModelAction) or within the parent context.");
        }

        // now traverse parameters:
        // parameter name is path
        // parameter value is value
        // if the value is null or empty, the value is not set!
        final String[] names = parameters.getNames();
        if ( names != null ) {
            final EventManager publisher = this.portalService.getEventManager();
            for(int i=0; i<names.length; i++) {
                final String path = names[i];
                final String value = parameters.getParameter(path, null );
                if ( value != null && value.trim().length() > 0 ) {
                    final Event event = new CopletJXPathEvent(this.portalService.getProfileManager().getCopletInstance(copletId),
                            path,
                            value);
                    publisher.send(event);
                }
            }
        }
        return EMPTY_MAP;
	}
}
