package org.apache.cocoon.parser;

import java.io.*;
import org.w3c.dom.*;
import com.sun.xml.tree.*;
import org.xml.sax.*;
import org.apache.cocoon.framework.*;

/**
 * This class implements an XML parser using the Sun ProjectX parser.
 * Update to work with the 'Technology Release 1, Feb. 24, 1999'
 * version of the Sun XML parser.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:15 $
 */

public class SunXMLParser extends AbstractActor implements Parser, Status {

    XmlDocument factory;
    
    public SunXMLParser() {
        this.factory = new XmlDocument();
    }
    
    public Document parse(Reader in, String sourceURI) throws IOException {
        try  {
            return factory.createXmlDocument(new InputSource(in), false);
        } catch (SAXException e) {
            throw new IOException(e.getMessage());
        }
    }
    
    public Document createEmptyDocument() {
        return new XmlDocument();
    }
    
    public String getStatus() {
        return "<b>Sun Java ProjectX XML Parser</b>";
    }
}