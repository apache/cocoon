package org.apache.cocoon.formatter.xml;

import java.io.*;
import java.util.*;
import org.w3c.dom.*;
import com.kvisco.xml.*;
import org.apache.cocoon.formatter.*;
import org.apache.cocoon.framework.*;

/**
 * This class implements a DOM->XML formatter using XSLP.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:21 $
 */

public class XSLPXMLFormatter extends XSLPFormatter {

    public void format(Document document, Writer writer, Dictionary parameters) throws Exception {
        XMLPrinter printer = new XMLPrinter(new PrintWriter(writer), spaces);
        printer.print(document);
    }

    public String getMIMEType() {
        return "text/xml";
    }

    public String getStatus() {
        return "XSL:P XML Formatter";
    }
}