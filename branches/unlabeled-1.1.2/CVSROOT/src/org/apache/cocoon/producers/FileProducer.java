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
import org.apache.cocoon.Job;
import org.apache.cocoon.sax.XMLConsumer;
import org.apache.cocoon.sax.XMLProducer;
import org.apache.cocoon.sax.XMLSource;
import org.apache.cocoon.framework.AbstractComponent;
import org.apache.cocoon.framework.Configurations;
import org.apache.cocoon.framework.ConfigurationException;
import org.apache.cocoon.parsers.ParserFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * The <code>FileProducer</code> produces XML data from source files.
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>, 
 *         Exoffice Technologies, INC.</a>
 * @author Copyright 1999 &copy; <a href="http://www.apache.org">The Apache
 *         Software Foundation</a>. All rights reserved.
 * @version CVS $Revision: 1.1.2.3 $ $Date: 2000-02-09 08:34:46 $
 * @since Cocoon 2.0
 */
public class FileProducer extends AbstractComponent implements Producer {
    /** The current parserFactory instance */
    private ParserFactory parserFactory=null;

    /**
     * Return an <code>XMLSource</code> instance producing XML data from a
     * file.
     *
     * @param job The current <code>Job</code>.
     * @param source The sitemap-translate source URI that will be parsed.
     */
    public XMLSource getXMLSource(Job job, String source) {
        return(Source.create(this.parserFactory,source));
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
        this.parserFactory=cocoon.getParserFactory();
    }
    
    /** The XMLSource implementation for this producer */
    private static class Source implements XMLSource {
        /** The XMLProducer (from Cocoon parser) */
        private XMLProducer producer=null;
        /** The source file */
        private File file=null;

        /** Create this Source object */
        private static Source create(ParserFactory parser, String source) {
            Source s=new Source();
            s.producer=parser.getXMLProducer(new InputSource(source));
            s.file=new File(source);
            return(s);
        }

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
