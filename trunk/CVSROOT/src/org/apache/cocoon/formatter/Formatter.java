package org.apache.cocoon.formatter;

import java.io.*;
import java.util.*;
import org.w3c.dom.*;

/**
 * This class must be implemented by the printers that format a DOM tree
 * into streams.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:19 $
 */

public interface Formatter {

    /**
     * Prints the give document into a page.
     */
    public void format(Document document, Writer writer, Dictionary parameters) throws Exception;
    
    /**
     * Returns the MIME type used by this formatter for output.
     */
    public String getMIMEType();
    
}