package org.apache.cocoon.processor.xslt;

import java.io.*;
import java.net.*;
import java.util.*;
import org.w3c.dom.*;
import com.kvisco.xsl.*;
import org.apache.cocoon.parser.*;
import org.apache.cocoon.processor.*;
import org.apache.cocoon.framework.*;

/**
 * This class implements the processor interface for the XSL:P processor.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @author <a href="mailto:kvisco@ziplink.net">Keith Visco</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:14 $
 */

public class XSLPProcessor extends AbstractXSLTProcessor {

    XSLProcessor styler = new XSLProcessor();

    public Document process(Document document, Dictionary parameters) throws Exception {
        try {
            return styler.process(document, getStylesheet(document, parameters));
        } catch (PINotFoundException e) {
            return document;
        }
    }

    public String getStatus() {
        return "XSL:P XSLT Processor";
    }
}