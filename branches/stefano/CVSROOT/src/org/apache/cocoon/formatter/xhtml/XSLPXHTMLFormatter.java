package org.apache.cocoon.formatter.xhtml;

import java.io.*;
import java.util.*;
import org.w3c.dom.*;
import com.kvisco.xml.*;
import org.apache.cocoon.formatter.*;
import org.apache.cocoon.framework.*;

/**
 * This class implements a DOM->XHTML formatter using XSLP.
 *
 * @author <a href="mailto:balld@webslingerZ.com">Donald Ball</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:25 $
 */

public class XSLPXHTMLFormatter extends XSLPFormatter {

    public static final String XHTMLDOCTYPE = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/WD-html-in-xml/DTD/xhtml1-strict.dtd\">";

    public void format(Document document, Writer writer, Dictionary parameters) throws Exception {
        PrintWriter pw = (writer instanceof PrintWriter) ? (PrintWriter) writer : new PrintWriter(writer);
        pw.println(XMLDECL);
        pw.println(XHTMLDOCTYPE);
        XMLPrinter printer = new XMLPrinter(pw, spaces);
        printer.print(document.getDocumentElement());
    }

    public String getMIMEType() {
        return "text/xhtml";
    }

    public String getStatus() {
        return "XSL:P XHTML Formatter";
    }
}