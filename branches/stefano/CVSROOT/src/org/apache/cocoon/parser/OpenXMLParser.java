package org.apache.cocoon.parser;

import java.io.*;
import org.w3c.dom.*;
import org.openxml.*;
import org.openxml.parser.*;
import org.apache.cocoon.framework.*;

/**
 * This class implements an XML parser using the OpenXML parser.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:15 $
 */

public class OpenXMLParser extends AbstractActor implements Parser, Status {

    public Document parse(Reader in, String sourceURI) throws IOException {
    	return new XMLParser(in, sourceURI).parseDocument();
    }
    
    public Document createEmptyDocument() {
        return new XMLDocument();
    }
    
    public String getStatus() {
        return "<b>OpenXML Parser</b>";
    }
}