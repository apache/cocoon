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
import org.apache.arch.Component;
import org.apache.arch.ComponentManager;
import org.apache.arch.Composer;
import org.apache.arch.config.Configurable;
import org.apache.arch.config.Configuration;
import org.apache.arch.config.ConfigurationException;
import org.apache.cocoon.Processor;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.Request;
import org.apache.cocoon.Response;
import org.apache.cocoon.sitemap.patterns.PatternException;
import org.apache.cocoon.sitemap.patterns.PatternMatcher;
import org.apache.cocoon.sitemap.patterns.PatternTranslator;
import org.xml.sax.SAXException;

/**
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-02-27 01:25:39 $
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
    public boolean process(Request req, Response res, OutputStream out)
    throws SAXException, IOException, ProcessingException {
        if (!this.targetTranslator.match(req.getUri())) return(false);
        throw new ProcessingException("NOT YET IMPLEMENTED");
    }
}
