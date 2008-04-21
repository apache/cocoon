package org.apache.excalibur.sourceresolve.jnet.source;

import javax.xml.transform.sax.SAXResult;

import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceFactory;
import org.apache.excalibur.xml.sax.XMLizable;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class SourceSAXResult extends SAXResult {

    protected final Source source;

    protected final SourceFactory factory;

    protected final XMLizable xmlizable;

    protected boolean closed = false;

    public SourceSAXResult(final SourceFactory f, final Source s, final XMLizable x) {
        this.factory = f;
        this.source = s;
        this.xmlizable = x;
    }

    public void setHandler(ContentHandler handler) {
        if ( !this.closed ) {
            try {
                this.xmlizable.toSAX(handler);
            } catch (SAXException se) {
                throw new RuntimeException(se);
            } finally {
                this.closed = true;
                this.factory.release(this.source);
            }
        } else {
            throw new RuntimeException("Source already closed.");
        }
    }


}
