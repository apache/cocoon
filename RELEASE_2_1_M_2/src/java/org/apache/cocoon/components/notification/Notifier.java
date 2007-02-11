/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.components.notification;

import org.apache.cocoon.Constants;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Iterator;

/**
 * Generates a representations of the specified Notifying Object.
 *
 * @author <a href="mailto:nicolaken@supereva.it">Nicola Ken Barozzi</a>
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Id: Notifier.java,v 1.3 2003/03/16 16:44:02 vgritsenko Exp $
 */
public class Notifier {

    /*
     * Generate notification information as a response.
     * The notification is directly written to the OutputStream.
     * @param  n The <code>Notifying</code> object
     * @param outputStream The output stream the notification is written to
     *        This could be <code>null</code>.
     * @deprecated There is no way in which this method could understand what mime/type to use. Instead use void notify(Notifying n, OutputStream outputStream, String mimetype), where the mime/type is requested.
     * @see #notify(Notifying n, OutputStream, String)

    public static String notify(Notifying n, OutputStream outputStream) throws IOException {
      notify(n, outputStream, "text/html") ;
      return "text/html";
    }
     */

    /**
     * Generate notification information as a response.
     * The notification is directly written to the OutputStream.
     * @param  n The <code>Notifying</code> object
     * @param outputStream The output stream the notification is written to
     *        This could be <code>null</code>.
     */
    public static void notify(Notifying n, OutputStream outputStream, String mimetype) throws IOException {
        //(NKB) FIXME should use error page templates, one per mime-type
        // currently uses hardcoded html, should be used only as last resort.
        notifyHTML(n, outputStream);
    }

    /**
     * Generate notification information as html.
     * The notification is directly written to the OutputStream.
     * @param  n The <code>Notifying</code> object
     * @param outputStream The output stream the notification is written to
     *        This could be <code>null</code>.
     */
    private static void notifyHTML(Notifying n, OutputStream outputStream) throws IOException {
        StringBuffer sb = new StringBuffer();

        sb.append("<html><head><title>").append(n.getTitle()).append("</title>");
        sb.append("<style><!--");
        sb.append("body { background-color: white; color: black; font-family: verdana, helvetica, sanf serif;}");
        sb.append("h1 {color: #336699; margin: 0px 0px 20px 0px; border-width: 0px 0px 1px 0px; border-style: solid; border-color: #336699;}");
        sb.append("p.footer { color: #336699; border-width: 1px 0px 0px 0px; border-style: solid; border-color: #336699; }");
        sb.append("span {color: #336699;}");
        sb.append("pre {padding-left: 20px;}");
        sb.append("a:link {font-weight: bold; color: #336699;}");
        sb.append("a:visited {color: #336699; }");
        sb.append("a:hover {color: #800000; background-color: #ffff80;}");
        sb.append("a:active {color: #006666;}");
        sb.append("--></style>");
        sb.append("</head><body>");
        sb.append("<h1>").append(n.getTitle()).append("</h1>");
        sb.append("<p><span>Message:</span> ").append(n.getMessage()).append("</p>");
        sb.append("<p><span>Description:</span> ").append(n.getDescription()).append("</p>");
        sb.append("<p><span>Sender:</span> ").append(n.getSender()).append("</p>");
        sb.append("<p><span>Source:</span> ").append(n.getSource()).append("</p>");

        Map extraDescriptions = n.getExtraDescriptions();
        Iterator keyIter = extraDescriptions.keySet().iterator();

        while (keyIter.hasNext()) {
            String key = (String) keyIter.next();

            sb.append("<p><span>").append(key).append("</span><pre>").append(
                    extraDescriptions.get(key)).append("</pre></p>");
        }

        sb.append("<p class='footer'><a href='http://cocoon.apache.org/'>").append(Constants.COMPLETE_NAME).append("</p>");
        sb.append("</body></html>");

        if (outputStream != null)
            outputStream.write(sb.toString().getBytes());
    }

    /*
     * Generate notification information in XML format.
     * @deprecated Using a ContentHandler doesn't mean that a mimetype cannot be specified; it could be svg or
     * @see #notify(Notifying, ContentHandler, String)

    public static void notify(Notifying n, ContentHandler ch) throws SAXException {
      notify(n, ch, "text/xml");
    }
     */

    /**
     * Generate notification information in XML format.
     */
    public static void notify(Notifying n, ContentHandler ch, String mimetype) throws SAXException {
        final String PREFIX = Constants.ERROR_NAMESPACE_PREFIX;
        final String URI = Constants.ERROR_NAMESPACE_URI;

        // Start the document
        ch.startDocument();
        ch.startPrefixMapping(PREFIX, URI);

        // Root element.
        AttributesImpl atts = new AttributesImpl();

        atts.addAttribute(URI, "type", PREFIX + ":type", "CDATA", n.getType());
        atts.addAttribute(URI, "sender", PREFIX + ":sender", "CDATA", n.getSender());
        ch.startElement(URI, "notify", PREFIX + ":notify", atts);
        ch.startElement(URI, "title", PREFIX + ":title", new AttributesImpl());
        ch.characters(n.getTitle().toCharArray(), 0, n.getTitle().length());
        ch.endElement(URI, "title", PREFIX + ":title");
        ch.startElement(URI, "source", PREFIX + ":source", new AttributesImpl());
        ch.characters(n.getSource().toCharArray(), 0, n.getSource().length());
        ch.endElement(URI, "source", PREFIX + ":source");
        ch.startElement(URI, "message", PREFIX + ":message", new AttributesImpl());

        if (n.getMessage() != null) {
            ch.characters(n.getMessage().toCharArray(), 0, n.getMessage().length());
        }

        ch.endElement(URI, "message", PREFIX + ":message");
        ch.startElement(URI, "description", PREFIX + ":description",
                        new AttributesImpl());
        ch.characters(n.getDescription().toCharArray(), 0, n.getDescription().length());
        ch.endElement(URI, "description", PREFIX + ":description");

        Map extraDescriptions = n.getExtraDescriptions();
        Iterator keyIter = extraDescriptions.keySet().iterator();
        while (keyIter.hasNext()) {
            String key = (String) keyIter.next();
            String value = String.valueOf(extraDescriptions.get(key));
            atts = new AttributesImpl();

            atts.addAttribute(URI, "description", PREFIX + ":description", "CDATA", key);
            ch.startElement(URI, "extra", PREFIX + ":extra", atts);
            ch.characters(value.toCharArray(), 0, value.length());
            ch.endElement(URI, "extra", PREFIX + ":extra");
        }

        // End root element.
        ch.endElement(URI, "notify", PREFIX + ":notify");

        // End the document.
        ch.endPrefixMapping(PREFIX);
        ch.endDocument();
    }
}
