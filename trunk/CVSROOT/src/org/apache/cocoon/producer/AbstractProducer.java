package org.apache.cocoon.producer;

import java.io.*;
import org.w3c.dom.*;
import javax.servlet.http.*;
import org.apache.cocoon.parser.*;
import org.apache.cocoon.framework.*;

/**
 * This abstract class implements the Producer interface and provides
 * utitity methods to convert the generated streams into DOM tress
 * that are used inside the processor pipeline. This class must be
 * seen as a transparent "mediator" between stream and DOM realms.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:12 $
 */

public abstract class AbstractProducer extends AbstractActor implements Producer {
    
    /**
     * This method is the only one called by the Cocoon engine. Producers
     * are allowed to create streams and this class "mediates"
     * translating these streams into DOM trees. For producers willing
     * to generate DOM trees automatically, they should override this method
     * and may well ignore to implement the getStream() method since it's
     * never called directly by Cocoon.
     */
    public Document getDocument(HttpServletRequest request) throws Exception {
        return ((Parser) director.getActor("parser")).parse(getStream(request), getPath(request));
    }
    
    /**
     * This method always returns true to reduce the evaluation overhead to
     * a minimum. Producer are highly encouradged to overwrite this method
     * if they can provide a fast way to evaluate the response change.
     */
    public boolean hasChanged(Object request) {
        return true;
    }
}