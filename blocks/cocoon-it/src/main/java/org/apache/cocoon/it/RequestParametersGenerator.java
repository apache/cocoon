package org.apache.cocoon.it;

import java.io.IOException;
import java.util.Enumeration;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.generation.AbstractGenerator;
import org.apache.cocoon.xml.AttributesImpl;
import org.xml.sax.SAXException;

public class RequestParametersGenerator extends AbstractGenerator {

    public void generate() throws IOException, SAXException, ProcessingException {
        Request request = ObjectModelHelper.getRequest(this.objectModel);

        this.contentHandler.startDocument();
        this.contentHandler.startElement("", "request-paramters", "request-paramters", new AttributesImpl());

        Enumeration parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String name = (String) parameterNames.nextElement();
            String value = (String) request.getParameter(name);
            this.contentHandler.startElement("", name, name, new AttributesImpl());
            this.contentHandler.characters(value.toCharArray(), 0, value.length());
            this.contentHandler.endElement("", name, name);
        }

        this.contentHandler.endElement("", "request-paramters", "request-paramters");
        this.contentHandler.endDocument();
    }

}
