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
import org.apache.cocoon.components.url.URLFactory;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.Roles;
import org.apache.cocoon.xml.dom.DOMStreamer;

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
 * @version CVS $Revision: 1.1.2.13 $ $Date: 2001-02-22 19:08:04 $
 */
public class HTMLGenerator extends ComposerGenerator implements Poolable {

    /**
     * Generate XML data.
     */
    public void generate()
    throws IOException, SAXException, ProcessingException {
        try
        {
            // Setup an instance of Tidy.
            Tidy tidy = new Tidy();
            tidy.setXmlOut(true);
            tidy.setXHTML(true);

            // Extract the document using JTidy and stream it.
            Component urlFactory = this.manager.lookup(Roles.URL_FACTORY);
            URL url = ((URLFactory) urlFactory).getURL(this.source);
            this.manager.release(urlFactory);
            org.w3c.dom.Document doc = tidy.parseDOM(new BufferedInputStream(url.openStream()), null);
            DOMStreamer streamer = new DOMStreamer(this.contentHandler,this.lexicalHandler);
            streamer.stream(doc);
        } catch (IOException e){
            getLogger().warn("HTMLGenerator.generate()", e);
            throw new ResourceNotFoundException("Could not get Resource for HTMLGenerator", e);
        } catch (SAXException e){
            getLogger().error("HTMLGenerator.generate()", e);
            throw(e);
        } catch (Exception e){
            getLogger().error("Could not get parser", e);
            throw new ProcessingException(e.getMessage());
        }
    }
}
