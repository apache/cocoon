/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import org.apache.avalon.framework.CascadingException;
import org.xml.sax.SAXException;


/**
 *  Generates an XML representation of the current notification.
 *
 * @author <a href="mailto:nicolaken@supereva.it">Nicola Ken Barozzi</a> Aisa
 * @created 24 August 2000
 */

public class Notification implements Notificable {

    /**
     *  The type of the notification. Examples can be "warning" or "error"
     */
    private String type = "";

    /**
     *  The title of the notification.
     */
    private String title = "";

    /**
     *  The source that generates the notification.
     */
    private String source = "";

    /**
     *  The sender of the notification.
     */
    private String sender = "";

    /**
     *  The notification itself.
     */
    private String message = "";

    /**
     *  A more detailed description of the notification.
     */
    private String description = "";

    /**
     *  A Hashtable containing extra notifications that do
     */
    private HashMap extraDescriptions = new HashMap();

    /**
     *  Constructor for the Notification object
     */
    public Notification(Object sender) {
        setSender(sender);
    }

    /**
     * Constructor for the Notification object
     *
     * @param  o  Description of Parameter
     */
    public Notification(Object sender, Object o) {
        this(sender);
        setType("object");
        setTitle("Object notification");
        setSource(o.getClass().getName());
        setMessage(o.toString());
    }

    /**
     * Constructor for the Notification object
     *
     * @param  t  Description of Parameter
     */
    public Notification(Object sender, Throwable t) {
        this(sender);
        setType("error");
        setTitle("Cocoon error");
        if(t != null)
        {
            setSource(t.getClass().getName());
            setMessage(t.getMessage());
            setDescription(t.toString());

            extraDescriptions.put("exception", t.toString());
            StringWriter stackTrace = new StringWriter();
            t.printStackTrace(new PrintWriter(stackTrace));
            extraDescriptions.put("stacktrace", stackTrace.toString());

            if(t instanceof CascadingException) {
                Throwable cause = ((CascadingException)t).getCause();
                if(cause != null) {
                    extraDescriptions.put("embedded exception", cause.toString());
                    stackTrace = new StringWriter();
                    cause.printStackTrace(new PrintWriter(stackTrace));
                    extraDescriptions.put("embedded exception stacktrace", stackTrace.toString());
                }
            }
        }
    }

    /**
     *  Constructor for the Notification object
     *
     *@param  t  Description of Parameter
     */
    public Notification(Object sender, SAXException t) {
        this(sender, (Throwable) t);
        addExtraDescription("SAX-processing-exception", ((SAXException) t).getException().toString());
    }

    /**
     *  Sets the Type attribute of the Notification object
     *
     *@param  type  The new Type value
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     *  Sets the Title attribute of the Notification object
     *
     *@param  title  The new Title value
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     *  Sets the Source attribute of the Notification object
     *
     *@param  source  The new Source value
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     *  Sets the Sender attribute of the Notification object
     *
     *@param  sender  The new sender value
     */
    private void setSender(Object sender) {
        this.sender = sender.getClass().getName();
    }

    /**
     *  Sets the Sender attribute of the Notification object
     *
     *@param  sender  The new sender value
     */
    private void setSender(String sender) {
        this.sender = sender;
    }

    /**
     *  Sets the Message attribute of the Notification object
     *
     *@param  message  The new Message value
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     *  Sets the Description attribute of the Notification object
     *
     *@param  description  The new Description value
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     *  Sets the ExtraDescriptions attribute of the Notification object
     *
     *@param  extraDescriptions  The new ExtraDescriptions value
     */
    public void addExtraDescription(String extraDescriptionDescription,
                                    String extraDescription) {
        this.extraDescriptions.put(extraDescriptionDescription,
                                   extraDescription);
    }

    /**
     *  Gets the Type attribute of the Notification object
     */
    public String getType() {
        return type;
    }

    /**
     *  Gets the Title attribute of the Notification object
     */
    public String getTitle() {
        return title;
    }

    /**
     *  Gets the Source attribute of the Notification object
     */
    public String getSource() {
        return source;
    }

    /**
     *  Gets the Sender attribute of the Notification object
     */
    public String getSender() {
        return sender;
    }

    /**
     *  Gets the Message attribute of the Notification object
     */
    public String getMessage() {
        return message;
    }

    /**
     *  Gets the Description attribute of the Notification object
     */
    public String getDescription() {
        return description;
    }

    /**
     *  Gets the ExtraDescriptions attribute of the Notification object
     */
    public HashMap getExtraDescriptions() {
        return extraDescriptions;
    }
}

