/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.components.language.markup;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.ContentHandler;

import java.io.IOException;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;

import org.apache.log.Logger;
import org.apache.avalon.logger.Loggable;

import org.apache.cocoon.util.TraxErrorHandler;

/**
 * A logicsheet-based implementation of <code>MarkupCodeGenerator</code>
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @author <a href="mailto:dims@yahoo.com">Davanum Srinivas</a>
 * @version CVS $Revision: 1.1.2.14 $ $Date: 2001-04-20 20:49:52 $
 */
public class LogicsheetCodeGenerator implements MarkupCodeGenerator, Loggable {

    protected Logger log;

    private Logicsheet corelogicsheet;

    private ContentHandler serializerContentHandler;

    private XMLReader rootReader;

    private TransformerHandler currentParent;

    private StringWriter writer;

    /**
    * The default constructor
    */
    public LogicsheetCodeGenerator() {

        SAXTransformerFactory factory = (SAXTransformerFactory) TransformerFactory.newInstance();
        factory.setErrorListener(new TraxErrorHandler(log));
        Properties format = new Properties();

        try {
            // set the serializer which would act as ContentHandler for the last transformer
            // FIXME (SSA) change a home made content handler, that extract the PCDATA
            // from the last remaining element
            TransformerHandler handler = factory.newTransformerHandler();
            handler.getTransformer().setErrorListener(new TraxErrorHandler(log));

            // Set the output properties
            format.put(OutputKeys.METHOD,"text");
            // FIXME (SSA) remove the nice identing. For debug purpose only.
            format.put(OutputKeys.INDENT,"yes");
            handler.getTransformer().setOutputProperties(format);

            this.writer = new StringWriter();
            handler.setResult(new StreamResult(writer));
            this.serializerContentHandler = handler;
        } catch (TransformerConfigurationException tce) {
            log.error("LogicsheetCodeGenerator: unable to get TransformerHandler", tce);
        }
    }

    public void setLogger(Logger logger) {
        if (this.log == null) {
            this.log = logger;
        }
    }

    /**
    * Add a logicsheet to the logicsheet list
    *
    * @param logicsheet The logicsheet to be added
    */
    public void addLogicsheet(Logicsheet logicsheet) {
        if (this.currentParent==null) {
            // Setup the first transformer of the chain.
            this.currentParent = logicsheet.getTransformerHandler();

            // the parent is the rootReader
            this.rootReader.setContentHandler(this.currentParent);;

            // Set content handler for the end of the chain : serializer
            this.currentParent.setResult(new SAXResult(this.serializerContentHandler));

        } else {
            // Build the transformer chain on the fly
            TransformerHandler newParent=logicsheet.getTransformerHandler();

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
        // set the root XMLReader of the transformer chain
        this.rootReader = reader;
        // start the parsing
        this.rootReader.parse(input);
        return this.writer.toString();
    }

}

