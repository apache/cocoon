package org.apache.cocoon.formatter.pdf;

import java.io.*;
import java.util.*;
import org.w3c.dom.*;
import com.jtauber.fop.apps.*;
import org.apache.cocoon.formatter.*;
import org.apache.cocoon.framework.*;

/**
 * This class wraps around FOP classes to perform XSL:FO to PDF formatting.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:25 $
 */

public class FOPFormatter implements Formatter, Status {

    public void format(Document document, Writer writer, Dictionary parameters) throws Exception {
        DOMProcessor fop = new DOMProcessor(document);
        fop.format(new PrintWriter(writer));
    }
    
    public String getMIMEType() {
        return "application/pdf";
    }
    
    public String getStatus() {
        return "James Tauber's " + com.jtauber.fop.Version.getVersion() + " formatter";
    }
}