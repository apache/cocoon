package org.apache.cocoon.processor.xslt;

import java.io.*;
import java.net.*;
import java.util.*;
import org.w3c.dom.*;

import org.apache.cocoon.parser.*;
import org.apache.cocoon.processor.*;
import org.apache.cocoon.framework.*;

import com.jclark.xsl.dom.Transform;
import com.jclark.xsl.dom.TransformEngine;
import com.jclark.xsl.dom.TransformException;
import com.jclark.xsl.dom.XSLTransformEngine;

import org.xml.sax.SAXException;

/**
 * This class implements the processor interface for the
 * James Calrk's XT XSLT processor using it's DOM API.
 *
 * @author <a href="mailto:zvi@usa.net?cc=zvia@netmanage.co.il">Zvi Avraham</a>
 */

public class XTProcessor extends AbstractXSLTProcessor {

    public Document process(Document document, Dictionary parameters) throws Exception {
        try {
            Document result=parser.createEmptyDocument();
            
            Transform transform = new XSLTransformEngine().createTransform(getStylesheet(document, parameters));

            transform.transform(document, result);
    
            return result;
        } catch(PINotFoundException e) {
            return document;
        }
    }

    public String getStatus() {
        return "XT XSLT Processor";
    }
}
