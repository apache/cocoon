package org.apache.cocoon.processor;

import java.io.*;
import java.util.*;
import org.w3c.dom.*;
import org.apache.cocoon.*;
import org.apache.cocoon.framework.*;

/**
 * This class implements the processing router by identifying the processor
 * associated to the produced document.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:13 $
 */

public class ProcessorFactory extends Router implements Defaults {

    public Processor getProcessor(Document document) throws Exception {
        String type = getType(document);
        Processor processor = (Processor) objects.get(type);
        if (processor == null) throw new Exception("No processor for type \"" + type + "\"");
        return processor;
    }

    public String getType(Document document) {
    	ProcessingInstruction pi = Utils.getFirstPI(document, COCOON_PROCESS_PI, true);
    	if (pi != null) {
	        Hashtable attributes = Utils.getPIPseudoAttributes(pi);
	        String type = (String) attributes.get("type");
	        if (type != null) return type;
        }
        throw new RuntimeException();
    }

    public String getStatus() {
        return "<b>Cocoon Processors</b>" + super.getStatus();
    }
}