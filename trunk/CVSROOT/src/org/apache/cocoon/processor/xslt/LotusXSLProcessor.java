package org.apache.cocoon.processor.xslt;

import java.io.*;
import java.net.*;
import java.util.*;
import org.w3c.dom.*;
import com.lotus.xml.*;
import com.lotus.xsl.*;
import org.apache.cocoon.*;
import org.apache.cocoon.parser.*;
import org.apache.cocoon.processor.*;
import org.apache.cocoon.framework.*;

/**
 * This class implements the processor interface for the Lotus XSL processor.
 *
 * <p>NOTE: some of this code was taken from the internals of the LotusXSL
 * processors to allow bugfixing and/or loose coupling on versions</p>
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:14 $
 */

public class LotusXSLProcessor extends AbstractXSLTProcessor {

    XSLProcessor styler;

    public void init(Director director) {
        super.init(director);
        this.styler = new XSLProcessor(new XMLParser(this.parser));
    }

    public Document process(Document document, Dictionary parameters) throws Exception {
        try {
            return styler.process(document, getStylesheet(document, parameters), null);
        } catch (PINotFoundException e) {
            return document;
        }
    }

    public String getStatus() {
        return "Lotus XSLT Processor";
    }

    class XMLParser extends XMLParserLiaisonDefault {
        Parser parser;

        public XMLParser(Parser parser) {
            this.parser = parser;
        }

        public Document createDocument() {
            return this.parser.createEmptyDocument();
        }

        public boolean isIgnorableWhitespace(Text text) {
            return false;
        }

        public Document parseXMLStream(Reader in, String filename) {
            try {
                return this.parser.parse(in, null);
            } catch (IOException e) {
                return null;
            }
        }

        public Document parseXMLStream(URL url) {
            try {
                return parser.parse(new InputStreamReader(url.openStream()), null);
            } catch (IOException e) {
                return null;
            }
        }
    }
}