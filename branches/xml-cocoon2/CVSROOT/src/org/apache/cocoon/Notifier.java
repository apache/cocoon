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
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Revision: 1.1.2.8 $ $Date: 2001-02-15 20:28:29 $
 */

public class Notifier {

    /**
     * Generate notification information as servlet response
     */
    public static void notify(Notificable n, HttpServletRequest req, HttpServletResponse res) throws IOException {

        StringBuffer sb = new StringBuffer();

        // FIXME (SM) how can we send the error with the proper content type?

        res.setContentType("text/html");
        sb.append("<html><head><title>").append(n.getTitle()).append("</title>");
        sb.append("<STYLE><!--H1{font-family : sans-serif,Arial,Tahoma;color : white;background-color : #0086b2;} ");
        sb.append("BODY{font-family : sans-serif,Arial,Tahoma;color : black;background-color : white;} ");
        sb.append("B{color : white;background-color : #0086b2;} ");
        sb.append("HR{color : #0086b2;} ");
        sb.append("--></STYLE> ");
        sb.append("</head><body>");
        sb.append("<h1>Cocoon 2 - ").append(n.getTitle()).append("</h1>");
        sb.append("<HR size=\"1\" noshade>");
        sb.append("<p><b>type</b> ").append(n.getType()).append("</p>");
        sb.append("<p><b>message</b> <u>").append(n.getMessage()).append("</u></p>");
        sb.append("<p><b>description</b> <u>").append(n.getDescription()).append("</u></p>");
        sb.append("<p><b>sender</b> ").append(n.getSender()).append("</p>");
        sb.append("<p><b>source</b> ").append(n.getSource()).append("</p>");

        HashMap extraDescriptions = n.getExtraDescriptions();
        Iterator keyIter = extraDescriptions.keySet().iterator();

        while (keyIter.hasNext()) {
            String key = (String) keyIter.next();

            sb.append("<p><b>").append(key).append("</b><pre>").append(extraDescriptions.get(key)).append("</pre></p>");
        }

        sb.append("<HR size=\"1\" noshade>");
        sb.append("</body></html>");

        res.getOutputStream().print(sb.toString());
    }

    /**
     * Generate notification information in XML format.
     */
    public static void notify(Notificable n, ContentHandler ch) throws SAXException {

        final String PREFIX = Constants.ERROR_NAMESPACE_PREFIX;
        final String URI = Constants.ERROR_NAMESPACE_URI;

        String buf;

        // Start the document
        ch.startDocument();
        ch.startPrefixMapping(PREFIX, URI);

        // Root element.
        AttributesImpl atts = new AttributesImpl();

        atts.addAttribute(URI, "type", "error:type", "CDATA", n.getType());
        atts.addAttribute(URI, "sender", "error:sender", "CDATA", n.getSender());
        ch.startElement(URI, "notify", "error:notify", atts);
        ch.startElement(URI, "title", "error:title", new AttributesImpl());
        ch.characters(n.getTitle().toCharArray(), 0, n.getTitle().length());
        ch.endElement(URI, "title", "error:title");
        ch.startElement(URI, "source", "error:source", new AttributesImpl());
        ch.characters(n.getSource().toCharArray(), 0, n.getSource().length());
        ch.endElement(URI, "source", "error:source");
        ch.startElement(URI, "message", "error:message", new AttributesImpl());

        if (n.getMessage() != null) {
            ch.characters(n.getMessage().toCharArray(), 0,
                          n.getMessage().length());
        }

        ch.endElement(URI, "message", "error:message");
        ch.startElement(URI, "description", "error:description",
                        new AttributesImpl());
        ch.characters(n.getDescription().toCharArray(), 0,
                      n.getDescription().length());
        ch.endElement(URI, "description", "error:description");

        HashMap  extraDescriptions = n.getExtraDescriptions();
        Iterator keyIter           = extraDescriptions.keySet().iterator();

        while (keyIter.hasNext()) {
            String key = (String) keyIter.next();

            atts = new AttributesImpl();

            atts.addAttribute(URI, "description", "error:description", "CDATA",
                              key);
            ch.startElement(URI, "extra", "error:extra", atts);
            ch.characters(extraDescriptions.get(key).toString().toCharArray(),
                          0, (extraDescriptions.get(key).toString())
                              .length());
            ch.endElement(URI, "extra", "error:extra");
        }

        // End root element.
        ch.endElement(URI, "notify", "error:notify");

        // End the document.
        ch.endPrefixMapping(PREFIX);
        ch.endDocument();
    }
}
