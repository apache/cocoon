package org.apache.cocoon.parser;

import java.io.*;
import java.net.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import oracle.xml.parser.v2.*;
import org.apache.cocoon.framework.*;

/**
 * This class implements an XML parser using the Oracle XML Parser v2.
 * Should be updated as Oracle nears full release.
 *
 * @author <a href="mailto:chris_conway@mail.scp.com">Christopher Conway</a>
 * @version $Revision: 1.0
 */

public class OracleXMLParser extends AbstractActor implements Parser, Status {
    
    DOMParser parser;

    OracleXMLParser() {
        this.parser = new DOMParser();
    }

    public Document parse(Reader in, String sourceURI) throws IOException {
        try {
            this.parser.setBaseURL(new URL("file", null, sourceURI));
            this.parser.parse(in);
        } catch (XMLParseException e) {
            throw new IOException("XMLParseException: Exiting with " + e.getNumMessages() + " errors.");
        } catch (SAXException e) {
            throw new IOException(e.getMessage());
        }
        return this.parser.getDocument();
    }

    public Document createEmptyDocument() {
        return new XMLDocument();
    }

    public String getStatus() {
        return new String("<b>Oracle XML Parser Early Adopter v2</b>") ;
    }
}