package org.apache.cocoon.formatter.wml;

import org.apache.cocoon.framework.*;
import org.apache.cocoon.formatter.*;

/**
 * This class implements a DOM->WML formatter usign OpenXML publishing API.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:27 $
 */

public class OpenXMLWMLFormatter extends OpenXMLFormatter {
    
    public void init(Configurations conf) {
        type = "XML";
        publicID = "-//WAPFORUM//DTD WML 1.1//EN";
        systemID = "http://www.wapforum.org/DTD/wml_1.1.xml";
        super.init(conf);
    }
    
    public String getMIMEType() {
        return "text/vnd.wap.wml";
    }
    
    public String getStatus() {
        return "OpenXML WML Formatter [" + format + "," + width + "," + spaces + "]";
    }
}