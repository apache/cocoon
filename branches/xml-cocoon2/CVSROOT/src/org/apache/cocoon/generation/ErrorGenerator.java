/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
 
package org.apache.cocoon.generation;

import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Enumeration;
import java.text.DateFormat;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.PrintWriter;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;

import org.apache.cocoon.generation.ComposerGenerator;

/**
 * Generates an XML representation of the current notification.
 *
 * @author     <a href="mailto:nicolaken@supereva.it">Nicola Ken Barozzi</a> Aisa
 * @author     <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @created    31 July 2000
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-08-21 17:37:49 $
 */
public class ErrorGenerator extends ComposerGenerator {

    /**
     *  The URI of the namespace of this generator.
     */
    protected final static String URI = "http://apache.org/cocoon/2.0/error";

    /**
     *  The namespace prefix for this namespace URI.
     */
    protected final static String PREFIX = "";

    /**
     *  The Exception to report.
     */
    private Throwable throwable = null;

    /**
     * Set the Exception to report.
     *
     *@param exception The Exception to report
     */
    public void setException (Throwable throwable) {
        this.throwable = throwable;
    }

    /**
     *  Generate the notification information in XML format.
     *
     *@exception  SAXException  Description of problem there is creating the output SAX events.
     *@throws       SAXException when there is a problem creating the
     *      output SAX events.
     */
    public void generate() throws SAXException {
        generate(throwable);
    }


    /**
     *  Start generation of the notification information of a <code>Throwable</code> in XML format.
      *
      *@param  t    The Exception to report
      *@exception  SAXException   Description of problem there is creating the output SAX events.
      *@throws                   SAXException when there is a problem creating the
      *      output SAX events.
      */
    public void generate(Throwable t) throws SAXException {
        ContentHandler ch = this.contentHandler;

        // Start the document
        this.contentHandler.startDocument();
        this.contentHandler.startPrefixMapping(PREFIX, URI);

        genError(this.contentHandler, t);

        // End the document.
        ch.endPrefixMapping(PREFIX);
        ch.endDocument();

    }


    /**
      *  Generate the notification information of a <code>Throwable</code> in XML format.
      *
      *@param  ch               The ContentHandler to use.
      *@param  t                  The Exception to report
      *@exception  SAXException   Description of problem there is creating the output SAX events.
      */
    private void genError(ContentHandler ch,
            Throwable t) throws SAXException {

        StringBuffer stacktrace = new StringBuffer();
        String CurrentString;
        try {
            PipedWriter CurrentPipedWriter = new PipedWriter();
            PrintWriter CurrentWriter = new PrintWriter(CurrentPipedWriter);
            PipedReader CurrentPipedReader = new PipedReader(CurrentPipedWriter);
            BufferedReader CurrentReader = new BufferedReader(CurrentPipedReader);

            Thread CurrentReadThread = new ReadThread(CurrentWriter, t);
            CurrentReadThread.start();

            do {
                try {

                    CurrentString = CurrentReader.readLine();
                    stacktrace.append(CurrentString + "\n");
                } catch (Throwable x) {
                    CurrentString = null;
                }
            } while (CurrentString != null)
                ;
        } catch (IOException ioe) {
            stacktrace.append("Error in StackTrace");
            System.err.println("\n\nError in printing error.");
        }

        Hashtable extras = new Hashtable();
        extras.put("stacktrace", stacktrace);

        genNotify(ch, "error", "Error in the Cocoon 2 pipeline",
                t.getClass().getName(), t.getMessage(), t.toString(),
                extras);

    }


    /**
      *  Generate notification information in XML format.
      *
      *@param  ch               The ContentHandler to use.
      *@param  type            The type of notification.
      *@param  title             The title of the notification.
      *@param  source         The source where the notification comes from.
      *@param  message     The notification.
      *@param  description  A more detailed description of the notification.  
      *@param  extraDescriptions  A Hashtable containing extra notifications that do not fit
      *   in other parameters. The keys are the parameter names, the values are the notifications.
      *@exception  SAXException   Description of problem there is creating the output SAX events.
      */
    private void genNotify(ContentHandler ch, String type,
            String title, String source, String message, String description,
            Hashtable extraDescriptions) throws SAXException {
        String buf;

        // Root element.
        AttributesImpl atts = new AttributesImpl();
        atts.addAttribute(URI, "type", "type", "CDATA", type);
        atts.addAttribute(URI, "sender", "sender", "CDATA", "cocoon-pipeline");

        ch.startElement(URI, "notify", "notify", atts);

        ch.startElement(URI, "title", "title", new AttributesImpl());
        ch.characters(title.toCharArray(), 0, title.length());
        ch.endElement(URI, "title", "title");

        ch.startElement(URI, "source", "source", new AttributesImpl());
        ch.characters(source.toCharArray(), 0, source.length());
        ch.endElement(URI, "source", "source");

        ch.startElement(URI, "message", "message", new AttributesImpl());
        if (message != null)
            ch.characters(message.toCharArray(), 0, message.length());
        ch.endElement(URI, "message", "message");

        ch.startElement(URI, "description", "description",
         new AttributesImpl());
        ch.characters(description.toCharArray(), 0, description.length());
        ch.endElement(URI, "description", "description");

        Enumeration extras = extraDescriptions.keys();

        while (extras.hasMoreElements()) {
            String extraDescription = extras.nextElement().toString();
            String extra =
                    extraDescriptions.get(extraDescription).toString();

            atts = new AttributesImpl();
            atts.addAttribute(URI, "description", "description", "CDATA",
                    extraDescription);
            ch.startElement(URI, "extra", "extra", atts);
            ch.characters(extra.toCharArray(), 0, extra.length());
            ch.endElement(URI, "extra", "extra");
        }

        // End root element.
        ch.endElement(URI, "notify", "notify");
    }



    /**
      *  A thread that prints the stackTrace of a <code>Throwable</code> object
      *  to a PrintWriter.
      *
      * @author     <a href="mailto:nicolaken@supereva.it">Nicola Ken Barozzi</a> Aisa
      * @created    31 July 2000
      */
    class ReadThread extends Thread {
        PrintWriter CurrentWriter;
        Throwable t;

        /**
         *  Constructor for the ReadThread object
         *
         *@param  CurrentWriter  The <code>PrintWriter</code> to print the stacktrace to.
         *@param  t  The <code>Throwable</code> object containing the stackTrace.
         */
        ReadThread(PrintWriter CurrentWriter, Throwable t) {
            this.CurrentWriter = CurrentWriter;
            this.t = t;
        }


        /**
          *  Main processing method for the ReadThread object. 
          *  A thread that prints the stackTrace of a <code>t</code>
          *     to <code>CurrentWriter</code>.
          */
        public void run() {
            t.printStackTrace(CurrentWriter);
        }

    }

}
