package org.apache.cocoon.processor.xslt;

import java.io.*;
import java.net.*;
import java.util.*;
import org.w3c.dom.*;
import oracle.xml.parser.v2.*;
import org.apache.cocoon.parser.*;
import org.apache.cocoon.processor.*;
import org.apache.cocoon.framework.*;

/**
 * This class implements the processor interface for the Oracle XSL Processor.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:14 $
 */

public class OracleXSLProcessor extends AbstractXSLTProcessor {

    Parser parser;
    XSLProcessor styler;

    public void init(Director director) {
        try {
            this.parser = (OracleXMLParser) director.getActor("parser");
            this.styler = new XSLProcessor();
        } catch (ClassCastException e) {
            throw new RuntimeException("The Oracle XSP processor may be used only with the Oracle XML parser");
        }
    }

    /**
     * The Oracle XSL processor doesn't cache the stylesheets
     */    
    public Document process(Document document, Dictionary parameters) throws Exception {
        try {
            XSLStylesheet stylesheet = new XSLStylesheet((XMLDocument) getStylesheet(document, parameters), new URL("file:" + (String) parameters.get("path")));
            return (Document) styler.processXSL(stylesheet, (XMLDocument) document);
        } catch (PINotFoundException e) {
            return document;
        }
    }

    public String getStatus() {
        return "Oracle XSLT Processor";
    }
}