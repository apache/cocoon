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

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.XMLFilter;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.XMLFilterImpl;

import java.io.IOException;
import org.xml.sax.SAXException;

import serialize.SerializerFactory;
import serialize.Method;
import serialize.Serializer;
import serialize.OutputFormat;

import trax.Transformer;

/**
 * A logicsheet-based implementation of <code>MarkupCodeGenerator</code>
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version CVS $Revision: 1.1.2.4 $ $Date: 2000-10-12 16:43:16 $
 */
public class LogicsheetCodeGenerator implements MarkupCodeGenerator {

    private Logicsheet corelogicsheet;

    private Serializer serializer;

    private ContentHandler serializerContentHandler;

    private XMLReader rootReader;

    private XMLFilter currentParent;

    /**
    * The default constructor
    */
    public LogicsheetCodeGenerator() {
        // set the serializer which would act as ContentHandler for the last transformer
        // FIXME (SSA) change a home made content handler, that extract the PCDATA
        // from the last remaining element
        this.serializer = SerializerFactory.getSerializer(Method.Text);
        OutputFormat outformat = new OutputFormat();
        // FIXME (SSA) set the right encoding set
        //outformat.setEncoding("");
        // FIXME (SSA) remove the nice identing. For debug purpose only.
        outformat.setIndent(true);
        outformat.setPreserveSpace(true);
        this.serializer.setOutputFormat(outformat);

        this.serializer.setWriter(new StringWriter());
        try {
            this.serializerContentHandler = this.serializer.asContentHandler();
        } catch (IOException ioe) {
            // This should never happen, because we're not dealing with IO file,
            // but rather with StringWriter
            ioe.printStackTrace();
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
            this.currentParent = logicsheet.getXMLFilter();

            // the parent is the rootReader
            this.currentParent.setParent(this.rootReader);

            // Set content handler for the end of the chain : serializer
            this.currentParent.setContentHandler(this.serializerContentHandler);

        } else {
            // Build the transformer chain on the fly
            XMLFilter newParent=logicsheet.getXMLFilter();

            // the currentParent is the parent of the new logicsheet filter
            newParent.setParent(this.currentParent);

            // reset the new parent and the contentHanlder
            this.currentParent = newParent;
            this.currentParent.setContentHandler(this.serializerContentHandler);
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
        return this.serializer.getWriter().toString();
    }

}

