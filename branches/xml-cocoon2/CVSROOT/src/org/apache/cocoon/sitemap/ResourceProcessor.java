/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.sitemap;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Vector;
import org.apache.avalon.Component;
import org.apache.avalon.ComponentManager;
import org.apache.avalon.Composer;
import org.apache.avalon.Configurable;
import org.apache.avalon.Configuration;
import org.apache.avalon.ConfigurationException;
import org.apache.cocoon.Processor;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.Request;
import org.apache.cocoon.Response;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.sitemap.patterns.PatternException;
import org.apache.cocoon.sitemap.patterns.PatternMatcher;
import org.apache.cocoon.sitemap.patterns.PatternTranslator;
import org.xml.sax.SAXException;

/**
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @version CVS $Revision: 1.1.2.4 $ $Date: 2000-07-20 21:57:14 $
 */
public class ResourceProcessor implements Composer, Configurable, Processor {

    /** The component manager instance */
    private ComponentManager manager=null;
    /** The source->uri translator */
    private PatternTranslator sourceTranslator=null;
    /** The uri->source translator */
    private PatternTranslator targetTranslator=null;

    /**
     * Create a new <code>SitemapPartition</code> instance.
     */
    public ResourceProcessor() {
        super();
    }

    /**
     * Set the current <code>ComponentManager</code> instance used by this
     * <code>Composer</code>.
     */
    public void setComponentManager(ComponentManager manager) {
        this.manager=manager;
    }

    /**
     * Pass a <code>Configuration</code> instance to this
     * <code>Configurable</code> class.
     */
    public void setConfiguration(Configuration conf)
    throws ConfigurationException {
        String uri=conf.getAttribute("uri");
        String source=conf.getAttribute("source");
        try {
            this.sourceTranslator=new PatternTranslator(source,uri);
            this.targetTranslator=new PatternTranslator(uri,source);
        } catch (PatternException e) {
            throw new ConfigurationException(e.getMessage(),conf);
        }
    }

    /**
     * Process the given <code>Request</code> producing the output to the
     * specified <code>Response</code> and <code>OutputStream</code>.
     */
    public boolean process(Environment environment, OutputStream out) {return true;}
    public boolean process(Request req, Response res, OutputStream out)
    throws SAXException, IOException, ProcessingException {
        if (!this.targetTranslator.match(req.getUri())) return(false);
        throw new ProcessingException("NOT YET IMPLEMENTED");
    }
}
