/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon;

import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.PrintWriter;

import org.xml.sax.SAXException;
import org.xml.sax.ContentHandler;

import org.apache.avalon.ComponentNotAccessibleException;

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
     *  Constructor for the Notification object
     *
     *@param  t  Description of Parameter
     */
    public Notification(Object sender, Throwable t) {
        this.setThrowable(sender, t);
    }

    /**
     *  Constructor for the Notification object
     *
     *@param  t  Description of Parameter
     */
    public Notification(Object sender, SAXException t) {

        this.setThrowable(sender, t);
        addExtraDescription("SAX-processing-exception",
                            ((SAXException) t).getException().toString());
    }

    /**
     *  Constructor for the Notification object
     *
     *@param  t  Description of Parameter
     */
    public Notification(Object sender, ComponentNotAccessibleException t) {

        this.setThrowable(sender, t);
        addExtraDescription("SAX-processing-exception",
                            ((ComponentNotAccessibleException) t)
                                .getException().toString());
    }

    private void setThrowable(Object sender, Throwable t) {

        setType("error");
        setTitle("Cocoon error");
        setSource(t.getClass().getName());
        setSender(sender);
        setMessage(t.getMessage());
        setDescription(t.toString());

        StringBuffer stacktrace = new StringBuffer();
        String       CurrentString;

        try {
            PipedWriter    CurrentPipedWriter = new PipedWriter();
            PrintWriter    CurrentWriter      =
                new PrintWriter(CurrentPipedWriter);
            PipedReader    CurrentPipedReader =
                new PipedReader(CurrentPipedWriter);
            BufferedReader CurrentReader      =
                new BufferedReader(CurrentPipedReader);
            Thread         CurrentReadThread  = new ReadThread(CurrentWriter,
                                                    t);

            CurrentReadThread.start();

            do {
                try {
                    CurrentString = CurrentReader.readLine();

                    stacktrace.append(CurrentString + "\n");
                } catch (Throwable x) {
                    CurrentString = null;
                }
            } while (CurrentString != null);
        } catch (IOException ioe) {
            stacktrace.append("C2: Error in getting StackTrace");
            System.err.println(
                "\n\nC2:Error in printing error: cannot get stacktrace correctly..");
        }

        extraDescriptions.put("stacktrace", stacktrace);
    }

    /**
     *  Constructor for the Notification object
     *
     *@param  o  Description of Parameter
     */
    public Notification(Object sender, Object o) {

        setType("object");
        setTitle("Object notification");
        setSource(o.getClass().getName());
        setSender(sender);
        setMessage(o.toString());
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

    /**
     *  A thread that prints the stackTrace of a <code>Throwable</code> object to a
     *  PrintWriter.
     *
     * @author <a href="mailto:nicolaken@supereva.it">Nicola Ken Barozzi</a> Aisa
     * @created 31 July 2000
     */
    class ReadThread extends Thread {

        PrintWriter CurrentWriter;
        Throwable   t;

        /**
         *  Constructor for the ReadThread object
         *
         *@param  CurrentWriter  The <code>PrintWriter</code> to print the stacktrace
         *      to.
         *@param  t              The <code>Throwable</code> object containing the
         *      stackTrace.
         */
        ReadThread(PrintWriter CurrentWriter, Throwable t) {
            this.CurrentWriter = CurrentWriter;
            this.t             = t;
        }

        /**
         *  Main processing method for the ReadThread object. A thread that prints the
         *  stackTrace of a <code>t</code> to <code>CurrentWriter</code> .
         */
        public void run() {
            t.printStackTrace(CurrentWriter);
        }
    }
}

