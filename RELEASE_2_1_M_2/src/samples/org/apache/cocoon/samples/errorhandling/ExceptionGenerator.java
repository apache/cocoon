/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

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
 * @author <a href="mailto:bluetkemeier@s-und-n.de">Björn Lütkemeier</a>
 * @version CVS $Id: ExceptionGenerator.java,v 1.2 2003/05/08 10:13:02 stephan Exp $
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
        this.contentHandler.startElement(null, "html", "html", noAttrs);
        this.contentHandler.startElement(null, "body", "body", noAttrs);
        this.contentHandler.startElement(null, "p", "p", noAttrs);
        this.contentHandler.characters(text.toCharArray(), 0, text.length());
        this.contentHandler.endElement(null, "p", "p");
        this.contentHandler.endElement(null, "body", "body");
        this.contentHandler.endElement(null, "html", "html");
        this.contentHandler.endDocument();
    }
}
