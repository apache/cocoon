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

import org.apache.avalon.ThreadSafe;

/**
 * Generates an XML representation of the current notification.
 *
 * @author <a href="mailto:nicolaken@supereva.it">Nicola Ken Barozzi</a> Aisa
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @created 31 July 2000
 * @version CVS $Revision: 1.1.2.3 $ $Date: 2000-10-12 16:44:06 $
 */
 
public class ErrorNotifier extends ComposerGenerator implements ThreadSafe {

    /**
     * The <code>Notification</code> to report.
     */
    private Notification notification = null;

    /**
     * Set the Exception to report.
     *
     * @param exception The Exception to report
     */
    public void setException(Throwable throwable) {
        notification = new Notification(this, throwable);
        notification.setTitle("Error creating the resource");
    }

    /**
     * Set the Notification to report.
     *
     * @param exception The Exception to report
     */
    public void setNotification(Object o) {
        notification = new Notification(this, o);
        notification.setTitle("Error creating the resource");
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

