package org.apache.cocoon.producer;

import javax.servlet.http.*;
import org.apache.cocoon.parser.*;
import org.apache.cocoon.framework.*;

/**
 * This abstract class implements the common Producer methods used
 * by the XSP page compiler to simplify XSP creation.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:12 $
 */

public abstract class AbstractXSPProducer extends AbstractProducer implements Status {
    
    protected Parser parser;
        
    /**
     * Initialize the actor by indicating their director.
     */
    public void init(Director director) {
        this.parser = (Parser) director.getActor("parser");
    }
    
    /**
     * Returns the path where the resource is found, or an empty string if
     * no path can be applied to the resource.
     * Warning, null values are not valid.
     */
    public String getPath(HttpServletRequest request) {
        return "";
    }
        
    /**
     * Returns a status string with the info of this producer.
     */
    public String getStatus() {
        return "XSP-generated Producer";
    }
}