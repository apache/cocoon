package org.apache.cocoon.it;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.generation.AbstractGenerator;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.NOPValidity;
import org.xml.sax.SAXException;

public class DateGenerator extends AbstractGenerator implements CacheableProcessingComponent {

    public void generate() throws IOException, SAXException, ProcessingException {
        this.contentHandler.startDocument();
        this.contentHandler.startElement("", "date", "date", new AttributesImpl());
        SimpleDateFormat format = new SimpleDateFormat("E, yyyy MMMM dd, hh:mm:ss.SSS, z");
        String now = format.format(new Date());
        this.contentHandler.characters(now.toCharArray(), 0, now.length());
        this.contentHandler.endElement("", "date", "date");
        this.contentHandler.endDocument();
    }

    public Serializable getKey() {
        return "1";
    }

    public SourceValidity getValidity() {
        return NOPValidity.SHARED_INSTANCE;
    }

}
