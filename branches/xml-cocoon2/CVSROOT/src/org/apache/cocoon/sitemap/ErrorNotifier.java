/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.sitemap;

import java.util.Enumeration;
import java.util.Hashtable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.Notification;
import org.apache.cocoon.Notifier;
import org.apache.cocoon.generation.ComposerGenerator;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Generates an XML representation of the current notification.
 *
 * @author <a href="mailto:nicolaken@supereva.it">Nicola Ken Barozzi</a> Aisa
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @created 31 July 2000
 * @version CVS $Revision: 1.1.2.7 $ $Date: 2001-04-30 14:17:42 $
 */
public class ErrorNotifier extends ComposerGenerator {

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

    /**
     * Recycle
     */
    public void recycle() {
        super.recycle();
        this.notification = null;
    }

}

