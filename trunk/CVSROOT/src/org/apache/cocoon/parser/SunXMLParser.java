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
 * @version $Revision: 1.4 $ $Date: 1999-11-30 16:30:08 $
 */

public class SunXMLParser extends AbstractParser implements Status {

    /**
     * Creates a DOM tree parsing the given input source.
     */
    public Document parse(InputSource input) throws SAXException, IOException {
      	org.xml.sax.Parser parser;
        XmlDocumentBuilder builder;

	    if (validate) {
		    parser = new com.sun.xml.parser.ValidatingParser(true);
	    } else {
		    parser = new com.sun.xml.parser.Parser();
		}
		
	    //parser.setEntityResolver(resolver);
	    builder = new XmlDocumentBuilder();
    	builder.setParser(parser);
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
        return "Sun ProjectX TR2 XML Parser";
    }
}