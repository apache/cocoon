/*-- $Id: Frontend.java,v 1.7 2000-02-13 18:29:16 stefano Exp $ -- 

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================
 
 Copyright (C) @year@ The Apache Software Foundation. All rights reserved.
 
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
 
 4. The names "Cocoon" and  "Apache Software Foundation"  must not be used to
    endorse  or promote  products derived  from this  software without  prior
    written permission. For written permission, please contact
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
package org.apache.cocoon;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import org.apache.cocoon.framework.*;

/**
 * The Cocoon Frontend.
 *
 * This class implements all those methods used to pretty print the output
 * from the Cocoon servlet itself. Since the engine may not be available,
 * the style is hardcoded into this class, even if this goes against any
 * smart publishing behavior. (ECS may be used instead).
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.7 $ $Date: 2000-02-13 18:29:16 $
 */

public class Frontend implements Defaults {

    public static final String[] colors = { "#f0f0f0", "#e0e0e0" };
    public static final String SINGLE_COLUMN = "***";

	public static void status(ServletResponse response, Hashtable environment, Hashtable engine) throws IOException {
        response.setContentType("text/html");
        PrintWriter out = new PrintWriter(response.getWriter());
        header(out);
        table(out, "Environment", environment);
        table(out, "Engine", engine);
        footer(out);
	}

    public static void error(ServletResponse response, String message) throws IOException {
        print(response, message, null);
    }
	
    public static void error(ServletResponse response, String message, Throwable t) throws IOException {
        StringWriter buffer = new StringWriter();
        if (t != null) t.printStackTrace(new PrintWriter(buffer));
    	print(response, message, buffer.toString());
    }

    public static void print(ServletResponse response, String title, String message) throws IOException {
        response.setContentType("text/html");
        PrintWriter out = new PrintWriter(response.getWriter());
        header(out, 80);
        out.println("<h3 align=\"center\">" + title + "</h3>");
        if (message != null) out.println("<blockquote><pre>" + message + "</pre></blockquote>");
        footer(out);
    }

    public static void header(PrintWriter out) {
        header(out, 60);
    }

    public static void header(PrintWriter out, int width) {
        out.println(
            "<html>" +
            " <head>" +
            "  <meta name=\"GENERATOR\" content=\"" + Cocoon.version() + "\">" +
            " </head>" +
            " <body>" +
            " <p><br></p>" +
            " <center>" +
            "  <table border=\"0\" width=\"" + width + "%\" bgcolor=\"#000000\" cellspacing=\"0\" cellpadding=\"0\">" +
            "  <tr>" +
            "   <td width=\"100%\"><table border=\"0\" width=\"100%\" cellpadding=\"4\">" +
            "    <tr>" +
            "     <td width=\"100%\" bgcolor=\"#c0c0c0\"><p align=\"right\"><font color=\"red\"><big><big>" + Cocoon.version() + "</big></big></font></td>" +
            "    </tr>" +
            "    <tr>" +
            "      <td width=\"100%\" bgcolor=\"#f0f0f0\">" +
            "       <p align=\"center\"><br></p>");
    }

    public static void footer(PrintWriter out) {
        out.println(
            "      </td>" +
            "     </tr>" +
            "     <tr>" +
            "      <td width=\"100%\" bgcolor=\"#FFFFFF\">" +
            "       <strong>Warning</strong>: this page has been dynamically generated." +
            "      </td>" +
            "     </tr>" +
            "    </table>" +
            "   </td>" +
            "  </tr>" +
            " </table>" +
            " </center>" +
            " <p align=\"center\">" +
            "   <font size=\"-1\">" +
            "   Copyright (c) " + YEAR + " <a href=\"http://xml.apache.org\">The Apache XML Project</a>.<br>" +
            "   All rights reserved. " +
            "  </font>" +
            " </p>" +
            " </body>" +
            " </html>");
		out.close();
    }

    public static void table(PrintWriter out, String name, Hashtable table) {

        Enumeration names = table.keys();

        out.println("<p><center><table bgcolor=#000000 border=0 cellpadding=0 cellspacing=0 width=50%>");
        out.println("<tr><td><table border=0 cellpadding=4 cellspacing=2 width=100%>");
        out.println("<tr><td align=right valign=middle colspan=2 bgcolor=#c0c0c0 nowrap>");
        out.println("<h3>" + name + "</td></tr>");

        for (int i = 0, j = 0; names.hasMoreElements(); i++) {
            Object key = names.nextElement();
            Object value = table.get(key);

            try {
                out.println("<tr><td align=right ");

                if (value.equals(SINGLE_COLUMN)) {
                    out.print("colspan=2 ");
                }

                out.println("bgcolor=" + colors[j & 1] + " nowrap>");
                out.println("<font size=-1>" + key.toString() + "</font></td>");

                if (!value.equals(SINGLE_COLUMN)) {
                    out.println("<td align=left bgcolor=" + colors[j & 1] + " nowrap>");

                    if (value instanceof Enumeration) {
                        Enumeration e = (Enumeration) value;
                        while (e.hasMoreElements()) {
                            String s;
                            Object o = e.nextElement();
                            if (o instanceof Status) s = ((Status) o).getStatus();
                            else s = o.toString();
                            out.println("<font size=-1><li>" + s + "</li></font>");
                        }
                    } else {
                        out.println("<font size=-1>" + value.toString() + "</font>");
                    }

                    out.println("</td></tr>");
                }

                j++;
            } catch (Exception valueIgnored) {}
        }

        out.println("</table></td></tr></table></center></p>");
    }

}