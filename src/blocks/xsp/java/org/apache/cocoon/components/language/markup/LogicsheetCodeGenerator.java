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
package org.apache.cocoon.components.language.markup;

import org.apache.avalon.framework.logger.AbstractLogEnabled;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.xml.AbstractXMLPipe;
import org.apache.cocoon.util.TraxErrorHandler;
import org.apache.excalibur.source.Source;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

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
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: LogicsheetCodeGenerator.java,v 1.2 2004/06/11 20:03:35 vgritsenko Exp $
 */
public class LogicsheetCodeGenerator extends AbstractLogEnabled implements MarkupCodeGenerator {

    private ContentHandler serializerContentHandler;

    private AbstractXMLPipe end;

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
    private SAXTransformerFactory getTransformerFactory() {
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
            this.end.setContentHandler(this.currentParent);

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
     * Generate source code from the given source. Filename information is
     * ignored in the logicsheet-based code generation approach.
     *
     * @param source The source of the markup
     * @return The generated source code
     * @exception Exception If an error occurs during code generation
     */
    public String generateCode(Source source, AbstractXMLPipe filter)
    throws Exception {
        try {
            // set the root XMLReader of the transformer chain
            this.end = filter;
            // start the parsing
            SourceUtil.toSAX(source, filter);
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
