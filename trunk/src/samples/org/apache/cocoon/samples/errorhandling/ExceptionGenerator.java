/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.samples.errorhandling;

import java.io.IOException;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.generation.AbstractGenerator;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Exception generator.
 *
 * @author <a href="mailto:bluetkemeier@s-und-n.de">Bj&ouml;rn L&uuml;tkemeier</a>
 * @version CVS $Id: ExceptionGenerator.java,v 1.5 2004/03/10 09:54:05 cziegeler Exp $
 */
public class ExceptionGenerator extends AbstractGenerator {

    /** Name of request parameters. */
    public static final String PAR_EXCEPTION = "exception";

    public static final String PAR_CODE = "code";

    /**
     * Overridden from superclass.
     */
    public void generate()
      throws IOException, SAXException, ProcessingException {
        Request request = ObjectModelHelper.getRequest(this.objectModel);
        String exception = request.getParameter(PAR_EXCEPTION);
        String text = null;

        if (exception==null) {
            text = "No exception occured.";
        } else if (exception.equals("validation")) {
            throw new ProcessingException(new ValidationException());
        } else if (exception.equals("application")) {
            throw new ProcessingException(new ApplicationException(Integer.parseInt(request.getParameter(PAR_CODE))));
        } else if (exception.equals("resourceNotFound")) {
            throw new ProcessingException(new ResourceNotFoundException(""));
        } else if (exception.equals("nullPointer")) {
            throw new NullPointerException();
        } else if (exception.equals("error")) {
            throw new Error("Error");
        } else {
            text = "Unknown exception requested.";
        }

        Attributes noAttrs = new AttributesImpl();

        this.contentHandler.startDocument();
        this.contentHandler.startElement("", "html", "html", noAttrs);
        this.contentHandler.startElement("", "body", "body", noAttrs);
        this.contentHandler.startElement("", "p", "p", noAttrs);
        this.contentHandler.characters(text.toCharArray(), 0, text.length());
        this.contentHandler.endElement("", "p", "p");
        this.contentHandler.endElement("", "body", "body");
        this.contentHandler.endElement("", "html", "html");
        this.contentHandler.endDocument();
    }
}
