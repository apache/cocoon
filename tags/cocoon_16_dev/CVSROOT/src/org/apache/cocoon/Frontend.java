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
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:11 $
 */

public class Frontend {

    public static final String YEAR = "1999";
    public static final String[] colors = { "#f0f0f0", "#e0e0e0" };
    public static final String SINGLE_COLUMN = "***";

	public static void status(ServletResponse response, Hashtable environment, Hashtable engine) throws IOException {
        PrintWriter out = new PrintWriter(response.getWriter());
        response.setContentType("text/html");
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
        PrintWriter out = new PrintWriter(response.getWriter());
        response.setContentType("text/html");
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
            "   Copyright (c) " + YEAR + " <a href=\"http://java.apache.org\">The Java Apache Project</a>.<br>" +
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