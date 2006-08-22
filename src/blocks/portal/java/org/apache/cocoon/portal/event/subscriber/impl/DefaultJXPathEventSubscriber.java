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
package org.apache.cocoon.portal.event.subscriber.impl;

import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.event.Receiver;
import org.apache.cocoon.portal.event.impl.JXPathEvent;
import org.apache.commons.jxpath.JXPathContext;

/**
 * This subscriber processes JXPath events.
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 *
 * @version CVS $Id$
 */
public final class DefaultJXPathEventSubscriber 
    implements Receiver {

    public DefaultJXPathEventSubscriber() {
        // nothing to do         
    }

    /**
     * @see Receiver
     */
    public void inform(JXPathEvent event, PortalService service) {
        final Object target = event.getTarget();
        if ( target != null ) {
            final JXPathContext jxpathContext = JXPathContext.newContext(target);
            jxpathContext.setValue(event.getPath(), event.getValue());
        }
    }

}
