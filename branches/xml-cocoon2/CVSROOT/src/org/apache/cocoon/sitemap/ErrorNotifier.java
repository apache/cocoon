/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon.sitemap;

import java.util.Hashtable;
import java.util.Enumeration;

import org.xml.sax.SAXException;
import org.xml.sax.ContentHandler;

import org.apache.cocoon.Notifier;
import org.apache.cocoon.Notification;

import org.apache.cocoon.generation.ComposerGenerator;

/**
 * Generates an XML representation of the current notification.
 *
 * @author <a href="mailto:nicolaken@supereva.it">Nicola Ken Barozzi</a> Aisa
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @created 31 July 2000
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-08-31 16:02:20 $
 */
 
public class ErrorNotifier extends ComposerGenerator {

    /**
     * The <code>Notification</code> to report.
     */
    private Notification notification = null;

    //nicola_ken:should it be deprecated?
    
    // (SM) deprecated what?

    /**
     * Set the Exception to report.
     *
     * @param exception The Exception to report
     */
    public void setException(Throwable throwable) {

        notification = new Notification(this, throwable);

        notification.setTitle("Error in the Cocoon 2 pipeline >X==<");
    }

    /**
     * Set the Notification to report.
     *
     * @param exception The Exception to report
     */
    public void setNotification(Object o) {

        notification = new Notification(this, o);

        notification.setTitle("Error in the Cocoon 2 pipeline )X==(");
    }

    /**
     * Generate the notification information in XML format.
     *
     * @exception  SAXException  Description of problem there is creating the output SAX events.
     * @throws SAXException when there is a problem creating the
     *      output SAX events.
     */
    public void generate() throws SAXException {
        Notifier.notify(notification, this.contentHandler);
    }
}

