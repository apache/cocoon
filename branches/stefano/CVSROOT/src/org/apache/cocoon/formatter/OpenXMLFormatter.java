package org.apache.cocoon.formatter;

import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import org.openxml.*;
import org.w3c.dom.*;
import org.openxml.x3p.*;
import org.apache.cocoon.framework.*;

/**
 * This class implements an abstract formatter based on OpenXML publishing API.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:19 $
 */

public abstract class OpenXMLFormatter implements Formatter, Configurable, Status {

    private StreamFormat sf = StreamFormat.XML;
    
    protected String type = "XML";
    protected String publicID = null;
    protected String systemID = null;
    
    protected String format;
    protected String width;
    protected String spaces;
    protected String printer;
    
	public void init(Configurations conf) {
        
        format = (String) conf.get("style", "normal");
        width = (String) conf.get("line_width", "120");
        spaces = (String) conf.get("indent_spaces", "1");
        printer = type;
        
        if (!format.toLowerCase().equals("normal")) printer += "_" + format.toUpperCase();
        
		try {
            sf = (StreamFormat) sf.getClass().getField(printer).get(sf);
            sf = sf.changeLineWrap(Integer.parseInt(width));   
            sf = sf.changeIndentSpaces(Integer.parseInt(spaces));
            if (systemID != null) {
				sf = sf.changeExternalDTD(publicID, systemID);
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not create formatter: " + e);
        }
	}
    
	public void format(Document document, Writer writer, Dictionary parameters) throws Exception {
		Publisher publisher = PublisherFactory.createPublisher(writer, sf);
		publisher.publish(document);
	}
}