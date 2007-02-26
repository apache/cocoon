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
package org.apache.cocoon.portal.services.impl.links;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.cocoon.portal.event.Event;
import org.apache.cocoon.portal.event.layout.ChangeTabEvent;


/**
 * The PageLabelLinkService generates links for named items defined in the layout portal.xml.
 * Links for other entities are passed to the DefaultLinkService to be resolved.
 *
 * @version $Id$
 */
public class PageLabelLinkService extends DefaultLinkService {

    /**
     * Test implementation for generating page labels.
     * @see org.apache.cocoon.portal.services.impl.links.DefaultLinkService#createUrl(java.util.List, java.util.List, java.lang.Boolean)
     */
    protected String createUrl(List events, List parameterDescriptions, Boolean secure) {
        if ( events != null ) {
            final Iterator i = events.iterator();
            while ( i.hasNext() ) {
                final Event current = (Event)i.next();
                if ( current instanceof ChangeTabEvent ) {
                    final ChangeTabEvent tabEvent = (ChangeTabEvent)current;
                    if ( tabEvent.getItem().getTemporaryAttribute("pageLabel") != null ) {
                        if ( parameterDescriptions == null ) {
                            parameterDescriptions = new ArrayList();
                        }
                        parameterDescriptions.add(new ParameterDescription("pageLabel=" + tabEvent.getItem().getTemporaryAttribute("pageLabel")));
                    }
                }
            }
        }
        return super.createUrl(events, parameterDescriptions, secure);
    }


}
