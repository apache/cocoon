package org.apache.cocoon.parser;

import java.io.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import com.sun.xml.tree.*;
import com.sun.xml.parser.*;
import org.apache.cocoon.framework.*;

/**
 * This class implements an XML parser using the Sun ProjectX parser
 * 'Technology Release 1'.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.5 $ $Date: 2000-01-31 21:53:15 $
 */

public class SunXMLParser extends AbstractParser implements Status {

    /**
     * Creates a DOM tree parsing the given input source.
     */
    public Document parse(InputSource input, boolean validate) throws SAXException, IOException {
      	org.xml.sax.Parser parser;
        XmlDocumentBuilder builder;

	    if (validate) {
		    parser = new com.sun.xml.parser.ValidatingParser(true);
	    } else {
		    parser = new com.sun.xml.parser.Parser();
		}
		
	    builder = new XmlDocumentBuilder();
    	builder.setParser(parser);
	    parser.setErrorHandler(this);
    	parser.parse(input);
	    return builder.getDocument();
    }
    
    /**
     * Creates an empty DOM tree.
     */
    public Document createEmptyDocument() {
        return new XmlDocument();
    }

    public String getStatus() {
        return "Sun ProjectX TR2 XML Parser (validation = " + validation + ")";
    }
}