package org.apache.cocoon.producer;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.servlet.http.*;
import org.apache.cocoon.*;
import org.apache.cocoon.framework.*;

/**
 * This class implements the producer interface in order to produce a document
 * based on the path provided as "PathTranslated". This should work on most
 * of the servlet engine available.
 * 
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:12 $
 */

public class FileProducer extends AbstractProducer implements Status {
    
    private Monitor monitor = new Monitor(10);
    
    public Reader getStream(HttpServletRequest request) throws IOException {
        File file = new File(this.getBasename(request));
        this.monitor.watch(Utils.encode(request), file);
        return new InputStreamReader(new FileInputStream(file));
    }

    public String getPath(HttpServletRequest request) {
        String basename = this.getBasename(request);
        return basename.substring(0, basename.lastIndexOf('/') + 1);
    }
    
    public boolean hasChanged(Object context) {
        return this.monitor.hasChanged(Utils.encode((HttpServletRequest) context));
    }
    
    private String getBasename(HttpServletRequest request) {
        return ((request.getPathInfo() == null)
            ? request.getRealPath(request.getRequestURI())
            : request.getPathTranslated()).replace('\\','/');
    }
    
    public String getStatus() {
        return "File Producer";
    }
}