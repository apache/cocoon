package org.apache.cocoon.formatter.html;

import org.apache.cocoon.framework.*;
import org.apache.cocoon.formatter.*;

/**
 * This class implements a DOM->HTML formatter usign OpenXML publishing API.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:26 $
 */

public class OpenXMLHTMLFormatter extends OpenXMLFormatter {
    
    public void init(Configurations conf) {
        type = "HTML";
        super.init(conf);
    }
    
    public String getMIMEType() {
        return "text/html";
    }

    public String getStatus() {
        return "OpenXML HTML Formatter [" + format + "," + width + "," + spaces + "]";
    }
}