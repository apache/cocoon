/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon.sitemap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.HashMap;

import org.apache.avalon.context.Contextualizable;
import org.apache.avalon.context.Context;
import org.apache.avalon.component.ComponentManager;
import org.apache.avalon.component.Composable;
import org.apache.avalon.configuration.Configurable;
import org.apache.avalon.configuration.Configuration;
import org.apache.avalon.context.ContextException;
import org.apache.avalon.logger.AbstractLoggable;

import org.apache.cocoon.Constants;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.pipeline.StreamPipeline;
import org.apache.cocoon.components.pipeline.EventPipeline;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.sitemap.Handler;
import org.apache.cocoon.sitemap.XSLTFactoryLoader;

import org.xml.sax.SAXException;

/**
 * This class manages all sub <code>Sitemap</code>s of a <code>Sitemap</code>
 * Invokation of sub sitemaps will be done by this instance as well
 * checking regeneration of the sub <code>Sitemap</code>
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.2.17 $ $Date: 2001-04-24 12:14:46 $
 */
public class Manager extends AbstractLoggable implements Configurable, Composable, Contextualizable {

    private Context context;

    /** The vectors of sub sitemaps */
    private HashMap sitemaps = new HashMap();

    /** The configuration */
    private Configuration conf;

    /** The component manager */
    private ComponentManager manager;

    /** get a configuration
     * @param conf the configuration
     */
    public void configure (Configuration conf) {
        this.conf = conf;
    }

    /** get a context
     * @param context the context object
     */
    public void contextualize (Context context) throws ContextException {
        this.context = context;
    }

    /** get a component manager
     * @param manager the component manager
     */
    public void compose (ComponentManager manager) {
        this.manager = manager;
    }

    /** invokes the sitemap handler to process a request
     * @param environment the environment
     * @param uri_prefix the prefix to the URI
     * @param source the source of the sitemap
     * @param check_reload should the sitemap be automagically reloaded
     * @param reload_asynchron should the sitemap be reloaded asynchron
     * @throws Exception there may be several excpetions thrown
     * @return states if the requested resource was produced
     */
    public boolean invoke (Environment environment, String uri_prefix,
                           String source, boolean check_reload, boolean reload_asynchron)
    throws Exception {

        // make sure the uri_prefix ends with a slash
        String prefix = this.getPrefix(uri_prefix);

        // get a sitemap handler
        Handler sitemapHandler = getHandler(environment, source, check_reload, reload_asynchron);

        // setup to invoke the processing
        setupProcessing(environment, sitemapHandler, uri_prefix, source);
        return sitemapHandler.process(environment);
    }

    /** invokes the sitemap handler to process a request
     * @param environment the environment
     * @param uri_prefix the prefix to the URI
     * @param source the source of the sitemap
     * @param check_reload should the sitemap be automagically reloaded
     * @param reload_asynchron should the sitemap be reloaded asynchron
     * @throws Exception there may be several excpetions thrown
     * @return states if the requested resource was produced
     */
    public boolean invoke (Environment environment, String uri_prefix,
                           String source, boolean check_reload, boolean reload_asynchron,
                           StreamPipeline pipeline, EventPipeline eventPipeline)
    throws Exception {

        // make sure the uri_prefix ends with a slash
        String prefix = this.getPrefix(uri_prefix);

        // get a sitemap handler
        Handler sitemapHandler = getHandler(environment, source, check_reload, reload_asynchron);

        // setup to invoke the processing
        setupProcessing(environment, sitemapHandler, uri_prefix, source);
        return sitemapHandler.process(environment, pipeline, eventPipeline);
    }

    /** has the sitemap changed
     * @return whether the sitemap file has changed
     */
    public boolean hasChanged() {
        Handler sitemapHandler = null;
        Iterator iter = sitemaps.values().iterator();
        while (iter.hasNext()) {
            sitemapHandler = (Handler) iter.next();
            if ((sitemapHandler != null) && (sitemapHandler.hasChanged())) {
                return true;
            }
        }
        return false;
    }

    /** make sure the uri_prefix ends with a slash */
    private String getPrefix (String uri_prefix) {
        if (uri_prefix.length() > 0)
            return (uri_prefix.charAt(uri_prefix.length() - 1) == '/' ? uri_prefix : uri_prefix + "/");
        else
            return uri_prefix;
    }

    private Handler getHandler(final Environment environment,
                               final String source,
                               final boolean check_reload,
                               final boolean reload_asynchron)
            throws Exception {
        Handler sitemapHandler = (Handler) sitemaps.get(source);

        if (sitemapHandler != null) {
            if (sitemapHandler.available()) {
                if (check_reload
                 && sitemapHandler.hasChanged()
                 && !sitemapHandler.isRegenerating()) {
                    if (reload_asynchron == true) {
                        sitemapHandler.regenerateAsynchronously(environment);
                    } else {
                        sitemapHandler.regenerate(environment);
                    }
                }
            } else {
                sitemapHandler.regenerate(environment);
            }
        } else {
            sitemapHandler = new Handler(source, check_reload);
            sitemapHandler.contextualize(this.context);
            sitemapHandler.setLogger(getLogger());
            sitemapHandler.compose(this.manager);
            sitemapHandler.configure(this.conf);
            sitemapHandler.regenerate(environment);
            sitemaps.put(source, sitemapHandler);
        }
        return sitemapHandler;
    }

    private void setupProcessing (Environment environment, Handler sitemapHandler, String uri_prefix, String source)
            throws Exception {
        environment.changeContext(uri_prefix, source);
        if (! sitemapHandler.available())
            throw new ProcessingException("The sitemap handler's sitemap is not available.", sitemapHandler.getException());
    }
}
