package org.apache.cocoon.formatter;

import java.io.*;
import java.util.*;
import org.w3c.dom.*;
import org.apache.cocoon.*;
import org.apache.cocoon.framework.*;

/**
 * This class implements the formatter factory. This looks for a
 * processing instruction telling which document type we have to work on.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:19 $
 */

public class FormatterFactory extends Router implements Defaults {

    public Formatter getFormatter(Document document) throws Exception {
        return getFormatterForType(getType(document));
    }

    public Formatter getFormatterForType(String type) throws Exception {
        Formatter formatter = (Formatter) objects.get(type);
        if (formatter == null) throw new Exception("No formatter for type \"" + type + "\"");
        return formatter;
    };

    public String getType(Document document) {
    	ProcessingInstruction pi = Utils.getFirstPI(document, COCOON_FORMAT_PI, true);
    	if (pi != null) {
	        Hashtable attributes = Utils.getPIPseudoAttributes(pi);
	        String type = (String) attributes.get("type");
	        return (type != null) ? type : defaultType;
	    } else {
	    	return defaultType;
	    }
    }

	public String getStatus() {
        return "<b>Cocoon Formatters</b>" + super.getStatus();
    }
}