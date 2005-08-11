package org.apache.cocoon.transformation;

import java.io.IOException;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.RedundantNamespacesFilter;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class BrowserUpdateTransformer extends AbstractTransformer {
    
    public static final String AJAXMODE_PARAM = "cocoon-ajax";
    
    public static final String BU_NSURI = "http://apache.org/cocoon/browser-update/1.0";
    
    private boolean ajaxMode = false;
    
    private int replaceDepth = 0;

    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par) throws ProcessingException, SAXException, IOException {

        Request request = ObjectModelHelper.getRequest(objectModel);
        this.ajaxMode = request.getParameter(AJAXMODE_PARAM) != null;
    }

    public void startDocument() throws SAXException {
        
        if (ajaxMode) {
            // Add the namespace filter to our own output.
            // This is needed as flattening bu:* elements can lead to some weird reordering of
            // namespace declarations...
            RedundantNamespacesFilter nsPipe = new RedundantNamespacesFilter();
            if (this.xmlConsumer != null) {
                nsPipe.setConsumer(this.xmlConsumer);
            } else {
                nsPipe.setContentHandler(this.contentHandler);
            }
            setConsumer(nsPipe);
        }
        
        super.startDocument();
        if (ajaxMode) {
            // Add a root element. The original one is very likely to be stripped
            super.startPrefixMapping("bu", BU_NSURI);
            super.startElement(BU_NSURI, "document", "bu:document", new AttributesImpl());
        }
    }
    
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        // Passthrough if not in ajax mode or if in a bu:replace
        if (!this.ajaxMode || this.replaceDepth > 0) {
            super.startPrefixMapping(prefix, uri);
        }
    }

    public void startElement(String uri, String loc, String raw, Attributes a) throws SAXException {
        if (BU_NSURI.equals(uri) && "replace".equals(loc)) {
            if (this.ajaxMode && this.replaceDepth == 0) {
                // Pass this element through
                super.startElement(uri, loc, raw, a);
            }
            replaceDepth++;
        } else {
            // Passthrough if not in ajax mode or if in a bu:replace
            if (!this.ajaxMode || this.replaceDepth > 0) {
                super.startElement(uri, loc, raw, a);
            }
        }
    }

    public void characters(char[] buffer, int offset, int len) throws SAXException {
        if (!this.ajaxMode || this.replaceDepth > 0) {
            super.characters(buffer, offset, len);
        }
    }
    
    public void endElement(String uri, String loc, String raw) throws SAXException {
        if (BU_NSURI.equals(uri) && "replace".equals(loc)) {
            replaceDepth--;
            if (this.ajaxMode && this.replaceDepth == 0) {
                // Pass this element through
                super.endElement(uri, loc, raw);
            }
        } else {
            // Passthrough if not in ajax mode or if in a bu:replace
            if (!this.ajaxMode || this.replaceDepth > 0) {
                super.endElement(uri, loc, raw);
            }
        }
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        // Passthrough if not in ajax mode or if in a bu:replace
        if (!this.ajaxMode || this.replaceDepth > 0) {
            super.endPrefixMapping(prefix);
        }
    }

    public void endDocument() throws SAXException {
        if (ajaxMode) {
            super.endElement(BU_NSURI, "document", "bu:document");
            super.endPrefixMapping("bu");
        }
        super.endDocument();
    }
}
