package org.apache.cocoon.formatter.xhtml;

import org.apache.cocoon.framework.*;
import org.apache.cocoon.formatter.*;

/**
 * This class implements a DOM->XHTML formatter usign OpenXML publishing API.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:25 $
 */

public class OpenXMLXHTMLFormatter extends OpenXMLFormatter {
    
    public void init(Configurations conf) {
        type = "XHTML";
        super.init(conf);
    }
    
    public String getMIMEType() {
        return "text/xhtml";
    }

    public String getStatus() {
        return "OpenXML XHTML Formatter [" + format + "," + width + "," + spaces + "]";
    }
}