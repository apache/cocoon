package org.apache.cocoon.parser;

import java.io.*;
import org.w3c.dom.*;
import org.apache.cocoon.framework.*;

/**
 * This class must be implemented by the services that implement XML 
 * parsing capabilites in order to be used by Cocoon.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:15 $
 */

public interface Parser extends Actor {
    
    /**
     * Creates a DOM tree parsing the given input stream.
     */
    public Document parse(Reader in, String sourceURI) throws IOException;
    
    /**
     * Creates an empty DOM tree.
     */
    public Document createEmptyDocument();
    
}