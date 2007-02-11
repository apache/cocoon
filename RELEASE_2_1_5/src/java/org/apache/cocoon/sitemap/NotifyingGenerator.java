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
package org.apache.cocoon.sitemap;

import java.util.Map;
import java.io.IOException;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.components.notification.Notifying;
import org.apache.cocoon.components.notification.Notifier;
import org.apache.cocoon.generation.AbstractGenerator;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.Constants;

import org.xml.sax.SAXException;

/**
 * Generates an XML representation of the current notification.
 *
 * @author <a href="mailto:barozzi@nicolaken.com">Nicola Ken Barozzi</a>
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @author <a href="mailto:proyal@managingpartners.com">Peter Royal</a>
 * @version CVS $Id: NotifyingGenerator.java,v 1.4 2004/03/05 13:02:58 bdelacretaz Exp $
 */
public class NotifyingGenerator extends AbstractGenerator {
    
    /**
     * The <code>Notification</code> to report.
     */
    private Notifying notification;

    public void setup(SourceResolver resolver, Map objectModel, String src,
                      Parameters par) throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, par);

        this.notification  = (Notifying)objectModel.get(Constants.NOTIFYING_OBJECT);

        if ( this.notification  == null) {
            throw new ProcessingException("Expected Constants.NOTIFYING_OBJECT not found in object model");
        }
    }

    /**
     * Generate the notification information in XML format.
     *
     * @throws SAXException when there is a problem creating the
     *      output SAX events.
     */
    public void generate() throws SAXException {
        Notifier.notify(notification, this.contentHandler, "text/xml");
    }

    /**
     * Recycle
     */
    public void recycle() {
        super.recycle();
        this.notification = null;
    }
}

