/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.components.saxconnector;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.Stack;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.Roles;
import org.apache.cocoon.components.pipeline.EventPipeline;
import org.apache.cocoon.components.pipeline.StreamPipeline;
import org.apache.cocoon.components.url.URLFactory;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.sitemap.Sitemap;
import org.apache.cocoon.xml.AbstractXMLPipe;
import org.apache.cocoon.xml.IncludeXMLConsumer;
import org.apache.cocoon.xml.XMLProducer;
import org.apache.avalon.excalibur.pool.Recyclable;
import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Copy of code from CIncludeTransformer as a starting point for CIncludeSAXConnector.
 * @author <a href="dims@yahoo.com">Davanum Srinivas</a>
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2001-05-10 02:47:01 $
 */
public class CIncludeSAXConnector extends AbstractXMLPipe implements Composable, Recyclable, SAXConnector {

    /** The sitemap we manipulate */
    private Sitemap sitemap;

    /** The current <code>ComponentManager</code>. */
    protected ComponentManager manager = null;

    public static final String CINCLUDE_NAMESPACE_URI = "http://apache.org/cocoon/include/1.0";
    public static final String CINCLUDE_INCLUDE_ELEMENT = "include";
    public static final String CINCLUDE_INCLUDE_ELEMENT_SRC_ATTRIBUTE = "src";
    public static final String CINCLUDE_INCLUDE_ELEMENT_ELEMENT_ATTRIBUTE = "element";
    public static final String CINCLUDE_INCLUDE_ELEMENT_NS_ATTRIBUTE = "ns";
    public static final String CINCLUDE_INCLUDE_ELEMENT_PREFIX_ATTRIBUTE = "prefix";

    /** The current <code>Environment</code>. */
    protected Environment environment;

    public void setup(EntityResolver resolver, Map objectModel,
                      String source, Parameters parameters)
            throws ProcessingException, SAXException, IOException {
        environment = (Environment) resolver;
    }

    public final void setSitemap(final Sitemap sitemap) {
        this.sitemap = sitemap;
    }

    public final void compose(final ComponentManager manager) {
        this.manager = manager;
    }

    public void startElement(String uri, String name, String raw, Attributes attr) throws SAXException {
        if (uri != null && name != null && uri.equals(CINCLUDE_NAMESPACE_URI) && name.equals(CINCLUDE_INCLUDE_ELEMENT)) {
            String src = attr.getValue("",CINCLUDE_INCLUDE_ELEMENT_SRC_ATTRIBUTE);
            String element = attr.getValue("",CINCLUDE_INCLUDE_ELEMENT_ELEMENT_ATTRIBUTE);
            String ns = attr.getValue("",CINCLUDE_INCLUDE_ELEMENT_NS_ATTRIBUTE);
            String prefix = attr.getValue("",CINCLUDE_INCLUDE_ELEMENT_PREFIX_ATTRIBUTE);
            try {
                processCIncludeElement(src, element, ns, prefix);
            } catch (MalformedURLException e) {
                getLogger().debug("CIncludeSAXConnector", e);
                throw new SAXException(e);
            } catch (IOException e) {
                getLogger().debug("CIncludeSAXConnector", e);
                throw new SAXException(e);
            }

        } else {
            super.startElement(uri, name, raw, attr);
        }
    }

    public void endElement(String uri, String name, String raw) throws SAXException {
        if (uri != null && name != null && uri.equals(CINCLUDE_NAMESPACE_URI) && name.equals(CINCLUDE_INCLUDE_ELEMENT)) {
            return;
        }
        super.endElement(uri, name, raw);
    }

    private void startElem(String namespaceURI, String prefix, String name) throws SAXException {
        AttributesImpl attrs = new AttributesImpl();
        String qname = name;
        if (!namespaceURI.equals("")) {
            super.startPrefixMapping(prefix, namespaceURI);
        }
        super.startElement(namespaceURI, name, name, attrs);
    }

    private void endElem(String namespaceURI, String prefix, String name) throws SAXException {
        super.endElement(namespaceURI, name, name);
        if (!namespaceURI.equals("")) {
            super.endPrefixMapping(prefix);
        }
    }

    public void setDocumentLocator(Locator locator) {
        super.setDocumentLocator(locator);
    }

    protected void processCIncludeElement(String src, String element, String ns, String prefix)
        throws SAXException,MalformedURLException,IOException {

        EventPipeline eventPipeline = null;
        StreamPipeline pipeline = null;
        if (element == null) element="";
        if (ns == null) ns="";
        if (prefix == null) prefix="";

        try {
            getLogger().debug("Processing CInclude element: src=" + src
                                + ", sitemap=" + this.sitemap
                                + ", element=" + element
                                + ", ns=" + ns
                                + ", prefix=" + prefix);

            eventPipeline = (EventPipeline)this.manager.lookup(Roles.EVENT_PIPELINE);
            pipeline = (StreamPipeline)this.manager.lookup(Roles.STREAM_PIPELINE);
            pipeline.setEventPipeline(eventPipeline);

            IncludeXMLConsumer consumer = new IncludeXMLConsumer(this);

            if (!"".equals(element))
                this.startElem(ns, prefix, element);

            ((XMLProducer)eventPipeline).setConsumer(consumer);

            this.environment.pushURI(src);
            this.sitemap.process(this.environment, pipeline, eventPipeline);
            eventPipeline.process(this.environment);
            this.environment.popURI();

            if (!"".equals(element))
                this.endElem(ns, prefix, element);
        } catch (Exception e) {
            getLogger().error("Error selecting sitemap",e);
        } finally {
            if(eventPipeline != null)
                this.manager.release((Component)eventPipeline);
            if(pipeline != null)
                this.manager.release((Component)pipeline);
        }
    }

    /**
     * Recycle the producer by removing references
     */
    public void recycle () {
        this.sitemap = null;
    }
}
