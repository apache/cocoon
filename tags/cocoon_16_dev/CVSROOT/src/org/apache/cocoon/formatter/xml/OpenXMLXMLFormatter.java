package org.apache.cocoon.formatter.xml;

import org.apache.cocoon.framework.*;
import org.apache.cocoon.formatter.*;

/**
 * This class implements a DOM->XML formatter usign OpenXML publishing API.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:22 $
 */

public class OpenXMLXMLFormatter extends OpenXMLFormatter {
    
    public void init(Configurations conf) {
        type = "XML";
        super.init(conf);
    }
    
    public String getMIMEType() {
        return "text/xml";
    }
    
    public String getStatus() {
        return "OpenXML XML Formatter [" + format + "," + width + "," + spaces + "]";
    }
}