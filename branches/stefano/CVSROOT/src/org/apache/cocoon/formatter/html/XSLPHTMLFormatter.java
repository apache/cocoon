package org.apache.cocoon.formatter.html;

import java.io.*;
import java.util.*;
import org.w3c.dom.*;
import com.kvisco.xml.*;
import org.apache.cocoon.formatter.*;
import org.apache.cocoon.framework.*;

/**
 * This class implements a DOM->HTML formatter using XSLP.
 *
 * @author <a href="mailto:balld@webslingerZ.com">Donald Ball</a>
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:26 $
 */

public class XSLPHTMLFormatter extends XSLPFormatter {

    public static final String HTMLDOCTYPE = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.0//EN\" \"http://www.w3.org/TR/RED-html40/strict.dtd\">";

    public void format(Document document, Writer writer, Dictionary parameters) throws Exception {
        PrintWriter pw = (writer instanceof PrintWriter) ? (PrintWriter) writer : new PrintWriter(writer);
        pw.println(HTMLDOCTYPE);
        HTMLPrinter printer = new HTMLPrinter(pw, spaces);
        printer.print(document);
    }
    
    public String getMIMEType() {
        return "text/html";
    }

    public String getStatus() {
        return "XSL:P HTML Formatter";
    }
}