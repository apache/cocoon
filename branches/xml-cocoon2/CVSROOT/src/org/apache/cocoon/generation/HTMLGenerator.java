/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.generation;

import org.apache.avalon.Poolable;
import java.io.IOException;
import org.apache.cocoon.components.parser.Parser;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.Roles;
import org.apache.cocoon.xml.dom.DOMStreamer;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;

import java.net.URL;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.w3c.dom.Document;

/**
 *
 * @author <a href="mailto:dims@yahoo.com">Davanum Srinivas</a>
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-10-27 20:58:03 $
 */
public class HTMLGenerator extends ComposerGenerator implements Poolable {

    /**
     * Generate XML data.
     */
    public void generate()
    throws IOException, SAXException {
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        URL url = new URL(this.source);

        // Get a spruced up document.
        org.w3c.tidy.Tidy tidy = new org.w3c.tidy.Tidy();
        System.out.println(">>>> Opening URL: " + url.toString());
        tidy.setXmlOut(true);
        tidy.setXHTML(true);
		Document newdoc = tidy.parseDOM(url.openStream(),ostream);

        byte[] bytes = ostream.toByteArray();

        // pipe the results into the parser
        Parser parser=(Parser) this.manager.lookup(Roles.PARSER);
        parser.setContentHandler(this.contentHandler);
        parser.setLexicalHandler(this.lexicalHandler);
        parser.parse(new InputSource(new ByteArrayInputStream(bytes)));
    }    
}
