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
package org.apache.cocoon.components.language.markup;

import org.apache.avalon.framework.logger.AbstractLogEnabled;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.util.TraxErrorHandler;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.Properties;

/**
 * A logicsheet-based implementation of <code>MarkupCodeGenerator</code>
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @author <a href="mailto:dims@yahoo.com">Davanum Srinivas</a>
 * @version CVS $Id: LogicsheetCodeGenerator.java,v 1.1 2003/03/09 00:08:53 pier Exp $
 */
public class LogicsheetCodeGenerator extends AbstractLogEnabled implements MarkupCodeGenerator {

    private ContentHandler serializerContentHandler;

    private XMLReader rootReader;

    private TransformerHandler currentParent;

    private StringWriter writer;

    /** The trax TransformerFactory */
    private SAXTransformerFactory tfactory = null;

    /**
     * Initialize the LogicsheetCodeGenerator.
     */
    public void initialize() {
        Properties format = new Properties();
        try {
            // Set the serializer which would act as ContentHandler for the last transformer
            // FIXME (SSA) change a home made content handler, that extract the PCDATA
            // from the last remaining element
            TransformerHandler handler = getTransformerFactory().newTransformerHandler();

            // Set the output properties
            format.put(OutputKeys.METHOD,"text");
            // FIXME (SSA) remove the nice identing. For debug purpose only.
            format.put(OutputKeys.INDENT,"yes");
            handler.getTransformer().setOutputProperties(format);

            this.writer = new StringWriter();
            handler.setResult(new StreamResult(writer));
            this.serializerContentHandler = handler;
        } catch (TransformerConfigurationException tce) {
            getLogger().error("LogicsheetCodeGenerator: unable to get TransformerHandler", tce);
        }
    }

    /**
     * Helper for TransformerFactory.
     */
    private SAXTransformerFactory getTransformerFactory()
    {
        if(tfactory == null)  {
            tfactory = (SAXTransformerFactory) TransformerFactory.newInstance();
            tfactory.setErrorListener(new TraxErrorHandler(getLogger()));
        }
        return tfactory;
    }

    /**
    * Add a logicsheet to the logicsheet list
    *
    * @param logicsheet The logicsheet to be added
    */
    public void addLogicsheet(Logicsheet logicsheet) throws ProcessingException {
        if (this.currentParent == null) {
            // Setup the first transformer of the chain.
            this.currentParent = logicsheet.getTransformerHandler();

            // the parent is the rootReader
            this.rootReader.setContentHandler(this.currentParent);

            // Set content handler for the end of the chain : serializer
            this.currentParent.setResult(new SAXResult(this.serializerContentHandler));
        } else {
            // Build the transformer chain on the fly
            TransformerHandler newParent = logicsheet.getTransformerHandler();

            // the currentParent is the parent of the new logicsheet filter
            this.currentParent.setResult(new SAXResult(newParent));

            // reset the new parent and the contentHanlder
            this.currentParent = newParent;
            this.currentParent.setResult(new SAXResult(this.serializerContentHandler));
        }
    }

    /**
    * Generate source code from the input document. Filename information is
    * ignored in the logicsheet-based code generation approach.
    *
    * @param reader The reader
    * @param input The input source
    * @param filename The input source original filename
    * @return The generated source code
    * @exception Exception If an error occurs during code generation
    */
    public String generateCode(XMLReader reader, InputSource input, String filename) throws Exception {
        try {
            // set the root XMLReader of the transformer chain
            this.rootReader = reader;
            // start the parsing
            this.rootReader.parse(input);
            return this.writer.toString();
        } catch (SAXException e) {
            if(e.getException() != null) {
                getLogger().debug("Got SAXException; Rethrowing cause exception", e);
                throw e.getException();
            }
            getLogger().debug("Got SAXException", e);
            throw e;
        }
    }
}
