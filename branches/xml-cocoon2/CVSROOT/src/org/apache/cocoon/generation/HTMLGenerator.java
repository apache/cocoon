/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.generation;

import org.apache.avalon.Poolable;
import org.apache.cocoon.components.parser.Parser;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.Roles;

import org.xml.sax.SAXException;
import org.xml.sax.InputSource;

import java.net.URL;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;

import org.w3c.tidy.Tidy;

/**
 * @author <a href="mailto:dims@yahoo.com">Davanum Srinivas</a>
 * @version CVS $Revision: 1.1.2.4 $ $Date: 2000-12-08 20:39:36 $
 */
public class HTMLGenerator extends ComposerGenerator implements Poolable {
    /**
     * Generate XML data.
     */
    public void generate()
    throws IOException, SAXException, ProcessingException {
        try
        {
            URL url = new URL(this.source);
            ByteArrayOutputStream ostream = new ByteArrayOutputStream();

             // Setup an instance of Tidy.
            Tidy tidy = new Tidy();
            tidy.setXmlOut(true);
            tidy.setXHTML(true);

            // FIXME (DIMS): Using DOMStreamer will eliminate the need for an
            // intermediate ByteArrayOutput Stream. But the document created
            // by JTidy has problems. So for now we use the ByteArrayOutputStream.
            tidy.parseDOM(new BufferedInputStream(url.openStream()),
                            new BufferedOutputStream(ostream));

            log.debug("Looking up " + Roles.PARSER);
            // Pipe the results into the parser
            Parser parser=(Parser) this.manager.lookup(Roles.PARSER);
            parser.setContentHandler(this.contentHandler);
            parser.setLexicalHandler(this.lexicalHandler);
            parser.parse(new InputSource
                            (new ByteArrayInputStream
                            (ostream.toByteArray())));
        } catch (IOException e){
            log.error("HTMLGenerator.generate()", e);
            throw(e);
        } catch (SAXException e){
            log.error("HTMLGenerator.generate()", e);
            throw(e);
        } catch (Exception e){
            log.error("Could not get parser", e);
            throw new ProcessingException(e.getMessage());
        }
    }
}
