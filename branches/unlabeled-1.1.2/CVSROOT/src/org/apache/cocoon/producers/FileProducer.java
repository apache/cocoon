/*****************************************************************************
 * Copyright (C) 1999 The Apache Software Foundation.   All rights reserved. *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1,  a copy of wich has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.producers;

import java.io.File;
import java.io.IOException;
import org.apache.cocoon.Cocoon;
import org.apache.cocoon.sitemap.Request;
import org.apache.cocoon.sitemap.Response;
import org.apache.cocoon.sax.XMLConsumer;
import org.apache.cocoon.sax.XMLProducer;
import org.apache.cocoon.sax.XMLSource;
import org.apache.cocoon.framework.AbstractComponent;
import org.apache.cocoon.framework.Configurations;
import org.apache.cocoon.framework.ConfigurationException;
import org.apache.cocoon.parsers.Parser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * The <code>FileProducer</code> produces XML data from source files.
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>, 
 *         Exoffice Technologies, INC.</a>
 * @author Copyright 1999 &copy; <a href="http://www.apache.org">The Apache
 *         Software Foundation</a>. All rights reserved.
 * @version CVS $Revision: 1.1.2.5 $ $Date: 2000-02-12 00:33:00 $
 * @since Cocoon 2.0
 */
public class FileProducer extends AbstractComponent implements Producer {
    /** The current Parser instance */
    private Parser parser=null;

    /**
     * Return an <code>XMLSource</code> instance producing XML data from a
     * file.
     *
     * @param req The cocoon <code>Request</code>.
     * @param res The cocoon <code>Response</code>.
     */
    public XMLSource getXMLSource(Request req, Response res) {
        Source s=new Source();
        String source=req.getPathTranslated();
        s.producer=parser.getXMLProducer(new InputSource(source));
        s.file=new File(source);
        return(s);
    }

    /**
     * Configure this <code>FileProducer</code>
     */
    public void configure(Configurations conf)
    throws ConfigurationException {
        // Check and prepare the parser factory
        Cocoon cocoon=this.getCocoonInstance();
        if (cocoon==null)
            throw new ConfigurationException("Cannot access current 'Cocoon'"+
                                             "instance",this.getClass());
        this.parser=cocoon.getParser();
    }
    
    /** The XMLSource implementation for this producer */
    private static class Source implements XMLSource {
        /** The XMLProducer (from Cocoon parser) */
        private XMLProducer producer=null;
        /** The source file */
        private File file=null;

        /**
         * Produce XML data.
         */
        public void produce(XMLConsumer cons)
        throws IOException, SAXException {
            this.producer.produce(cons);
        }

        /**
         * Check last modification date of source file.
         */
        public boolean modifiedSince(long date) {
            long modified=this.file.lastModified();
            // If lastModified() returns zero it means we weren't able to 
            // access the file. Return true for safety.
            if (modified==0) return(true);
            else return(modified>date);
        }
    }
}
