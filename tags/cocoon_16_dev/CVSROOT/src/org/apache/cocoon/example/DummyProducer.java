package org.apache.cocoon.example;

import java.io.*;
import java.net.*;
import javax.servlet.http.*;
import org.apache.cocoon.producer.*;
import org.apache.cocoon.framework.*;

/**
 * Stupid producer to show how this functionality works.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:27 $
 */

public class DummyProducer extends AbstractProducer implements Status {
    
    String dummy = "<?xml version=\"1.0\"?>" 
        + "<?cocoon:format type=\"text/html\"?>"
        + "<html><body>"
        + "<h1 align=\"center\">"
            + "Hello from a dummy page"
        + "</h1>"
        + "</body></html>";
        
    public Reader getStream(HttpServletRequest request) throws IOException {
        return new StringReader(dummy);
    }

    public String getPath(HttpServletRequest request) {
        return "";
    }
    
    public String getStatus() {
        return "Dummy Producer";
    }
}