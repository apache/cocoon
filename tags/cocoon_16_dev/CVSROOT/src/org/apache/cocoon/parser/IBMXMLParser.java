package org.apache.cocoon.parser;

import java.io.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import com.ibm.xml.dom.*;
import com.ibm.xml.parsers.*;
import com.ibm.xml.framework.*;
import org.apache.cocoon.framework.*;

/**
 * This class implements an XML parser using the XML4J 2.0 parser.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:15 $
 */

public class IBMXMLParser extends AbstractActor implements Parser, Status {

    public Document parse(Reader in, String sourceURI) throws IOException {

      DOMParser parser = new DOMParser();

      try {
          parser.parse(new InputSource(in));
      } catch (SAXException e) {
          throw new IOException(e.getMessage());
      }
      
      return parser.getDocument();
    }

    public Document createEmptyDocument() {
        return new DocumentImpl();
    }
    
    public String getStatus() {
        return "<b>" + Version.fVersion + " Parser</b>";
    }
}