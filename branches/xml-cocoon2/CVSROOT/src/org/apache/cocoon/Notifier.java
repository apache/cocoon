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

import java.text.DateFormat;

import java.io.PrintStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletOutputStream;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;

/**
 * Generates an XML representation of the current notification.
 *
 * @author <a href="mailto:nicolaken@supereva.it">Nicola Ken Barozzi</a> Aisa
 * @created 24 August 2000
 */
 
public class Notifier {

    private static int printStreamNotifications = 0;

    /**
     * The URI of the namespace of this generator.
     */
    protected final static String URI = "http://apache.org/cocoon/2.0/error";

    /**
     * The namespace prefix for this namespace URI.
     */
    protected final static String PREFIX = "ntf";

    /**
     * Gets the CocoonAsciArt attribute of the Notification object
     *
     * @return The CocoonAsciArt value
     */
    public static String getCocoonAsciArt() {

        return " ษอออออ ษออออป ษอออออ ษออออป ษออออป ษออออป  \n"
               + " บ      บ   .บ บ      บ   .บ บ.   บ บ    บ  \n"
               + " ศอออออ ศออออผ ศอออออ ศออออผ ศออออผ บ    บ";
    }

    /**
     * Generate notification information on PrintStream Out in text.
     *
     * @param  Out               The PrintStream to use.
     */
    public static void notify(Notificable n, PrintStream Out) {

        printStreamNotifications++;

        Out.println("");
        Out.println(
            "**********************************************************");
        Out.println("");
        Out.println(getCocoonAsciArt() + " "
                    + org.apache.cocoon.Cocoon.VERSION);
        Out.println("");
        Out.println("--- " + n.getTitle() + " ---");
        Out.println("");
        Out.println(" " + n.getType() + " - " + n.getMessage());
        Out.println("");
        Out.println(" description - " + n.getDescription());
        Out.println("");
        Out.println(" from - " + n.getSender());
        Out.println("");
        Out.println(" source - " + n.getSource());
        Out.println("");

        HashMap  extraDescriptions = n.getExtraDescriptions();
        Iterator keyIter           = extraDescriptions.keySet().iterator();

        while (keyIter.hasNext()) {
            String key = (String) keyIter.next();

            Out.println(" " + key + " - " + extraDescriptions.get(key));
            Out.println("");
        }

        Out.println("**************** printStream notifications: "
                    + String.valueOf(printStreamNotifications)
                    + " ***************");
        Out.println("");
    }

    public static void notify(Notificable n, HttpServletRequest req,
                              HttpServletResponse res) {

        StringBuffer sb = new StringBuffer();

        try {

            // get the request user agent
            String agent = req.getParameter("user-Agent");

            if (agent == null) {
                agent = req.getHeader("user-Agent");
            }

            if (agent == null) {
                agent = "";
            }

            String ContentType = req.getContentType();

            if (ContentType == null) {
                ContentType = "";
            }

            if (agent.startsWith("Mozilla")
                    || ContentType.equals("txt/html")) {
                res.setContentType("text/html");
                sb.append("<html><head><title>" + n.getTitle() + "</title>");
                sb.append(
                    "<STYLE><!--H1{font-family : sans-serif,Arial,Tahoma;color : white;background-color : #0086b2;} ");
                sb.append(
                    "BODY{font-family : sans-serif,Arial,Tahoma;color : black;background-color : white;} ");
                sb.append("B{color : white;background-color : #0086b2;} ");
                sb.append("HR{color : #0086b2;} ");
                sb.append("--></STYLE> ");
                sb.append("</head><body>");
                sb.append("<h1>Cocoon 2 - " + n.getTitle() + "</h1>");
                sb.append("<HR size=\"1\" noshade>");
                sb.append("<p><b>type</b> " + n.getType() + "</p>");
                sb.append("<p><b>message</b> <u>" + n.getMessage()
                          + "</u></p>");
                sb.append("<p><b>description</b> <u>" + n.getDescription()
                          + "</u></p>");
                sb.append("<p><b>sender</b> " + n.getSender() + "</p>");
                sb.append("<p><b>source</b> " + n.getSource() + "</p>");

                HashMap  extraDescriptions = n.getExtraDescriptions();
                Iterator keyIter           =
                    extraDescriptions.keySet().iterator();

                while (keyIter.hasNext()) {
                    String key = (String) keyIter.next();

                    sb.append("<p><b>" + key + "</b> <pre> "
                              + extraDescriptions.get(key) + "</pre></p>");
                }

                sb.append("<HR size=\"1\" noshade>");
                sb.append("</body></html>");
            } else {
                res.setContentType("text/xml");
                sb.append("<notify type=\"" + n.getType() + "\" sender=\""
                          + n.getSender() + "\">");
                sb.append("<title>" + n.getTitle() + "</title>");
                sb.append("<source>" + n.getSource() + "</source>");
                sb.append("<message>" + n.getMessage() + "</message>");
                sb.append("<description>" + n.getDescription()
                          + "</description>");

                HashMap  extraDescriptions = n.getExtraDescriptions();
                Iterator keyIter           =
                    extraDescriptions.keySet().iterator();

                while (keyIter.hasNext()) {
                    String key = (String) keyIter.next();

                    sb.append("<extra description=\"" + key + "\">"
                              + extraDescriptions.get(key) + "</extra>");
                }

                sb.append("</notify>");
            }

            ServletOutputStream Out = res.getOutputStream();

            Out.print(new String(sb));

            //Out.flush();
        } catch (IOException ioe) {}
        finally {
            notify(n, System.out);
        }
    }

    /**
     * Generate notification information in XML format.
     *
     * @param  ch                The ContentHandler to use.
     * @exception  SAXException  Description of problem there is creating the output
     *      SAX events.
     */
    public static void notify(Notificable n, ContentHandler ch)
            throws SAXException {

        //only for test.
        notify(n, System.out);

        String buf;

        // Start the document
        ch.startDocument();
        ch.startPrefixMapping(PREFIX, URI);

        // Root element.
        AttributesImpl atts = new AttributesImpl();

        atts.addAttribute(URI, "type", "type", "CDATA", n.getType());
        atts.addAttribute(URI, "sender", "sender", "CDATA", n.getSender());
        ch.startElement(URI, "notify", "notify", atts);
        ch.startElement(URI, "title", "title", new AttributesImpl());
        ch.characters(n.getTitle().toCharArray(), 0, n.getTitle().length());
        ch.endElement(URI, "title", "title");
        ch.startElement(URI, "source", "source", new AttributesImpl());
        ch.characters(n.getSource().toCharArray(), 0, n.getSource().length());
        ch.endElement(URI, "source", "source");
        ch.startElement(URI, "message", "message", new AttributesImpl());

        if (n.getMessage() != null) {
            ch.characters(n.getMessage().toCharArray(), 0,
                          n.getMessage().length());
        }

        ch.endElement(URI, "message", "message");
        ch.startElement(URI, "description", "description",
                        new AttributesImpl());
        ch.characters(n.getDescription().toCharArray(), 0,
                      n.getDescription().length());
        ch.endElement(URI, "description", "description");

        HashMap  extraDescriptions = n.getExtraDescriptions();
        Iterator keyIter           = extraDescriptions.keySet().iterator();

        while (keyIter.hasNext()) {
            String key = (String) keyIter.next();

            atts = new AttributesImpl();

            atts.addAttribute(URI, "description", "description", "CDATA",
                              key);
            ch.startElement(URI, "extra", "extra", atts);
            ch.characters(extraDescriptions.get(key).toString().toCharArray(),
                          0, (extraDescriptions.get(key).toString())
                              .length());
            ch.endElement(URI, "extra", "extra");
        }

        // End root element.
        ch.endElement(URI, "notify", "notify");

        // End the document.
        ch.endPrefixMapping(PREFIX);
        ch.endDocument();
    }

    /**
     *  A thread that prints the stackTrace of a <code>Throwable</code> object to a
     *  PrintWriter.
     *
     *@author     <a href="mailto:nicolaken@supereva.it">Nicola Ken Barozzi</a>
     *      Aisa
     *@created    31 July 2000
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
