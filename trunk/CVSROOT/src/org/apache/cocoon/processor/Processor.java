package org.apache.cocoon.processor;

import java.util.*;
import org.w3c.dom.*;
import org.apache.cocoon.framework.*;

/**
 * Every DOM processor must extend this interface to be recognized by
 * Cocoon as a DOM processor.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:13 $
 */

public interface Processor extends Actor, Changeable {

    /**
     * Process the DOM tree. The returned tree is allowed to be either a copy 
     * or the modified input tree.
     */
    public Document process(Document document, Dictionary parameters) throws Exception;

}