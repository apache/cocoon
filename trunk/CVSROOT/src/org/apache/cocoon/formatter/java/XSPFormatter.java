package org.apache.cocoon.formatter.java;

import java.io.*;
import java.util.*;
import org.w3c.dom.*;
import org.apache.cocoon.formatter.*;
import org.apache.cocoon.framework.*;

/**
 * The formatter for the eXtensible Server Pages format.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:19 $
 */
public class XSPFormatter implements Formatter, Status {

    public void format(Document doc, Writer writer, Dictionary parameters) throws Exception {
        PrintWriter out = (writer instanceof PrintWriter) ? (PrintWriter) writer : new PrintWriter(writer);

    }
    
    public String getMIMEType() {
        return "text/plain";
    }        
    
    public String getStatus() {
        return "XSP Formatter";
    }
}