/*
 * Copyright 1999-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.transformation;

import org.apache.excalibur.xml.xpath.XPathProcessor;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.components.sax.XMLDeserializer;
import org.apache.cocoon.components.sax.XMLSerializer;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.transformation.helpers.IncludeCacheManager;
import org.apache.cocoon.transformation.helpers.IncludeCacheManagerSession;
import org.apache.cocoon.xml.IncludeXMLConsumer;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceParameters;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.xml.dom.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

/**
* @cocoon.sitemap.component.documentation
 * This transformer triggers for the element <code>include</code> in the
 * namespace "http://apache.org/cocoon/include/1.0".
 * The <code>src</code> attribute contains the url which points to
 * an xml resource which is include instead of the element.
 * With the attributes <code>element</code>, <code>ns</code> and
 * <code>prefix</code> it is possible to specify an element
 * which surrounds the included content.
 * 
 * @cocoon.sitemap.component.name   cinclude
 * @cocoon.sitemap.component.logger sitemap.transformer.cinclude
 * @cocoon.sitemap.component.documentation.caching
 *               See documentation for further information.
 * 
 * @cocoon.sitemap.component.pooling.min   2
 * @cocoon.sitemap.component.pooling.max  16
 * @cocoon.sitemap.component.pooling.grow  2
 *
 * This transformer also supports a more verbose but flexible version:
 * <cinclude:includexml xmlns:cinclude="http://apache.org/cocoon/include/1.0" ignoreErrors="false">
 *     <cinclude:src>THE SRC URI</cinclude:src>
 *     <!-- This is an optional configuration block -->
 *     <cinclude:configuration>
 *         <!-- For example if you want to make a HTTP POST -->
 *         <cinclude:parameter>
 *             <cinclude:name>method</cinclude:name>
 *             <cinclude:value>POST</cinclude:value>
 *         </cinclude:parameter>
 *     </cinclude:configuration>
 *     <!-- The following are optional parameters appended to the URI -->
 *     <cinclude:parameters>
 *         <cinclude:parameter>
 *             <cinclude:name>a name</cinclude:name>
 *             <cinclude:value>a value</cinclude:value>
 *         </cinclude:parameter>
 *         <!-- more can follow -->
 *     </cinclude:parameters>
 * </cinclude:includexml>
 *
 *
 * This transformer also supports caching of the included content.
 * Therefore it triggers for the element <code>cached-include</code> in the
 * namespace "http://apache.org/cocoon/include/1.0".
 * The <code>src</code> attribute contains the url which points to
 * an xml resource which is include instead of the element.
 * First, it works like the usual include command. But it can be
 * configured with various parameters:
 * The most important one is the <code>expires</code> parameter.
 * If (and only if) this is set to a value greater than zero,
 * all included content is cached for the given period of time.
 * So if any other request includes the same URI, the content
 * is fetched from the cache. The expires value is in seconds.
 * Usually the content is cached in the usual store, but you
 * can also define a writeable source with the <code>source</code> parameter,
 * e.g. "file:/c:/temp". Then the cached content is written into this
 * directory.
 * With the optional <code>purge</code> set to <code>true</code>
 * the cache is purged which means the cached content is regarded as
 * invalid nevertheless if it has expired or not.
 * With the optional parameter <code>parallel</code> the various
 * included contents are processed (included) in parallel rather than
 * in a series.
 * With the optional parameter <code>preemptive</code> set to <code>true</code>
 * a pre-emptive caching is activated. When a resource is requested with
 * pre-emptive caching, this transformer always attempts to get the
 * content from the cache. If the content is not in the cache, it is
 * of course retrieved from the original source and cached.
 * If the cached resource has expired, it is still provided. The cache
 * is updated by a background task. This task has to be started
 * beforehand.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @author <a href="mailto:acoliver@apache.org">Andrew C. Oliver</a>
 * @version CVS $Id: CIncludeTransformer.java,v 1.12 2004/06/17 14:55:24 cziegeler Exp $
 */
public class CIncludeTransformer
extends AbstractSAXTransformer
implements Disposable, CacheableProcessingComponent {

    public static final String CINCLUDE_NAMESPACE_URI = "http://apache.org/cocoon/include/1.0";
    public static final String CINCLUDE_INCLUDE_ELEMENT = "include";
    public static final String CINCLUDE_INCLUDE_ELEMENT_SRC_ATTRIBUTE = "src";
    public static final String CINCLUDE_INCLUDE_ELEMENT_ELEMENT_ATTRIBUTE = "element";
    public static final String CINCLUDE_INCLUDE_ELEMENT_SELECT_ATTRIBUTE = "select";
    public static final String CINCLUDE_INCLUDE_ELEMENT_NS_ATTRIBUTE = "ns";
    public static final String CINCLUDE_INCLUDE_ELEMENT_PREFIX_ATTRIBUTE = "prefix";

    public static final String CINCLUDE_INCLUDEXML_ELEMENT    = "includexml";
    public static final String CINCLUDE_INCLUDEXML_ELEMENT_IGNORE_ERRORS_ATTRIBUTE = "ignoreErrors";
    public static final String CINCLUDE_SRC_ELEMENT           = "src";
    public static final String CINCLUDE_CONFIGURATION_ELEMENT = "configuration";
    public static final String CINCLUDE_PARAMETERS_ELEMENT    = "parameters";
    public static final String CINCLUDE_PARAMETER_ELEMENT     = "parameter";
    public static final String CINCLUDE_NAME_ELEMENT          = "name";
    public static final String CINCLUDE_VALUE_ELEMENT         = "value";

    public static final String CINCLUDE_CACHED_INCLUDE_ELEMENT = "cached-include";
    protected static final String CINCLUDE_CACHED_INCLUDE_PLACEHOLDER_ELEMENT = "cached-includep";

    private static final int STATE_OUTSIDE   = 0;
    private static final int STATE_INCLUDE   = 1;

    /** The configuration of includexml */
    protected Parameters configurationParameters;

    /** The parameters for includexml */
    protected SourceParameters resourceParameters;

    /** The current state: STATE_ */
    protected int state;

    protected IncludeCacheManager cacheManager;

    protected IncludeCacheManagerSession cachingSession;

    protected boolean compiling;

    protected IncludeXMLConsumer filter;

    protected XMLSerializer recorder;

    protected AttributesImpl srcAttributes = new AttributesImpl();

    protected boolean supportCaching;

    /** Remember the start time of the request for profiling */
    protected long startTime;

   /**
     * Constructor
     * Set the namespace
     */
    public CIncludeTransformer() {
        this.namespaceURI = CINCLUDE_NAMESPACE_URI;
    }

    /**
     * Setup the component.
     */
    public void setup(SourceResolver resolver, Map objectModel,
                      String source, Parameters parameters)
    throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, source, parameters);
        this.state = STATE_OUTSIDE;
        if ( null != this.cacheManager ) {
            this.cachingSession = this.cacheManager.getSession( this.parameters );
        }
        this.compiling = false;
        this.supportCaching = parameters.getParameterAsBoolean("support-caching", false);
        if (this.getLogger().isErrorEnabled()) {
            this.getLogger().debug("Starting CIncludeTransformer with session " + this.cachingSession);
            this.startTime = System.currentTimeMillis();
        }
    }

    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        super.service(manager);
        if (this.manager.hasService(IncludeCacheManager.ROLE)) {
            this.cacheManager = (IncludeCacheManager) this.manager.lookup(IncludeCacheManager.ROLE);
        } else {
            getLogger().warn("The cinclude transformer cannot find the IncludeCacheManager. " +
                             "Therefore caching is turned off for the include transformer.");
        }
    }

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( null != this.manager ) {
            this.manager.release( this.cacheManager );
            this.manager = null;
        }
    }

    /**
     * Recycle the component
     */
    public void recycle() {
        if ( null != this.cachingSession ) {
            this.cacheManager.terminateSession( this.cachingSession );
        }
        this.cachingSession = null;
        if ( null != this.recorder) {
            this.manager.release( this.recorder );
            this.recorder = null;
        }
        super.recycle();
        this.configurationParameters = null;
        this.resourceParameters = null;
        if (getLogger().isErrorEnabled()) {
            getLogger().debug("Finishing CachingCIncludeTransformer, time: " +
                              (System.currentTimeMillis() - this.startTime));
            this.startTime = 0;
        }
        this.filter = null;
    }

    public void startTransformingElement(String uri, String name, String raw, Attributes attr)
    throws ProcessingException ,IOException, SAXException {
        if (name.equals(CINCLUDE_INCLUDE_ELEMENT)) {

            this.processCIncludeElement(attr.getValue("",CINCLUDE_INCLUDE_ELEMENT_SRC_ATTRIBUTE),
                                        attr.getValue("",CINCLUDE_INCLUDE_ELEMENT_ELEMENT_ATTRIBUTE),
                                        attr.getValue("",CINCLUDE_INCLUDE_ELEMENT_SELECT_ATTRIBUTE),
                                        attr.getValue("",CINCLUDE_INCLUDE_ELEMENT_NS_ATTRIBUTE),
                                        attr.getValue("",CINCLUDE_INCLUDE_ELEMENT_PREFIX_ATTRIBUTE),
                                        false);

        // Element: include
        } else if (name.equals(CINCLUDE_INCLUDEXML_ELEMENT)
                   && this.state == STATE_OUTSIDE) {
            this.state = STATE_INCLUDE;
            String ignoreErrors = attr.getValue("", CINCLUDE_INCLUDEXML_ELEMENT_IGNORE_ERRORS_ATTRIBUTE);
            if (ignoreErrors == null || ignoreErrors.length() == 0) {
                ignoreErrors = "false";
            }
            this.stack.push(new Boolean(this.ignoreEmptyCharacters));
            this.stack.push(new Boolean(this.ignoreWhitespaces));
            this.stack.push(ignoreErrors);

            this.ignoreEmptyCharacters = false;
            this.ignoreWhitespaces = true;

        // target
        } else if (name.equals(CINCLUDE_SRC_ELEMENT)
                   && this.state == STATE_INCLUDE) {
            this.startTextRecording();

        // configparameters
        } else if (name.equals(CINCLUDE_CONFIGURATION_ELEMENT)
                   && this.state == STATE_INCLUDE) {
            stack.push("end");

        // parameters
        } else if (name.equals(CINCLUDE_PARAMETERS_ELEMENT)
                   && this.state == STATE_INCLUDE) {
            stack.push("end");

        // parameter
        } else if (name.equals(CINCLUDE_PARAMETER_ELEMENT)
                   && this.state == STATE_INCLUDE) {

        // parameter name
        } else if (name.equals(CINCLUDE_NAME_ELEMENT)
                   && this.state == STATE_INCLUDE) {
            this.startTextRecording();

        // parameter value
        } else if (name.equals(CINCLUDE_VALUE_ELEMENT)
                   && this.state == STATE_INCLUDE) {
            this.startSerializedXMLRecording(XMLUtils.createPropertiesForXML(true));

       } else if (name.equals(CINCLUDE_CACHED_INCLUDE_ELEMENT)) {

           String src = this.processCIncludeElement(attr.getValue("",CINCLUDE_INCLUDE_ELEMENT_SRC_ATTRIBUTE),
                                                    null,
                                                    null,
                                                    null,
                                                    null,
                                                    this.cacheManager != null);
           if (this.compiling) {
               this.srcAttributes.addAttribute("", CINCLUDE_INCLUDE_ELEMENT_SRC_ATTRIBUTE, CINCLUDE_SRC_ELEMENT, "CDATA", src);
               super.startTransformingElement(uri,
                                              CINCLUDE_CACHED_INCLUDE_PLACEHOLDER_ELEMENT,
                                              raw + "p",
                                              this.srcAttributes);
               this.srcAttributes.clear();
           }
        } else {
            super.startTransformingElement(uri, name, raw, attr);
        }
    }

    public void endTransformingElement(String uri, String name, String raw)
    throws ProcessingException, IOException, SAXException {
        if (name.equals(CINCLUDE_INCLUDE_ELEMENT)) {
            // do nothing
            return;

        } else if (name.equals(CINCLUDE_INCLUDEXML_ELEMENT)
                   && this.state == STATE_INCLUDE) {
            // Element: includexml

            this.state = STATE_OUTSIDE;

            final String resource = (String)stack.pop();

            final boolean ignoreErrors = stack.pop().equals("true");

            if (this.getLogger().isDebugEnabled()) {
                getLogger().debug("Processing CIncludexml element: src=" + resource
                              + ", ignoreErrors=" + ignoreErrors
                              + ", configuration=" + this.configurationParameters
                              + ", parameters=" + this.resourceParameters);
            }
            Source source = null;

            try {
                source = SourceUtil.getSource(resource,
                                              this.configurationParameters,
                                              this.resourceParameters,
                                              this.resolver);

                XMLSerializer serializer = null;
                XMLDeserializer deserializer = null;
                try {
                    if ( ignoreErrors ) {
                        serializer = (XMLSerializer) this.manager.lookup(XMLSerializer.ROLE);
                        deserializer = (XMLDeserializer)this.manager.lookup(XMLDeserializer.ROLE);
                        SourceUtil.toSAX(source, serializer, this.configurationParameters, true);
                        deserializer.setConsumer( this.xmlConsumer );
                        deserializer.deserialize( serializer.getSAXFragment() );
                    } else {
                        SourceUtil.toSAX(source, this.xmlConsumer, this.configurationParameters, true);
                    }
                } catch (ProcessingException pe) {
                    if (!ignoreErrors) throw pe;
                } catch (ServiceException ignore) {
                } finally {
                    this.manager.release( serializer );
                    this.manager.release( deserializer );
                }
            } catch (SourceException se) {
                if (!ignoreErrors) throw SourceUtil.handle(se);
            } catch (SAXException se) {
                if (!ignoreErrors) throw se;
            } catch (IOException ioe) {
                if (!ignoreErrors) throw ioe;
            } finally {
                this.resolver.release(source);
            }

            // restore values
            this.ignoreWhitespaces = ((Boolean)stack.pop()).booleanValue();
            this.ignoreEmptyCharacters = ((Boolean)stack.pop()).booleanValue();

        // src element
        } else if (name.equals(CINCLUDE_SRC_ELEMENT)
                   && this.state == STATE_INCLUDE) {

            this.stack.push(this.endTextRecording());

        } else if (name.equals(CINCLUDE_PARAMETERS_ELEMENT)
                   && this.state == STATE_INCLUDE) {
            this.resourceParameters = new SourceParameters();
            // Now get the parameters off the stack
            String label = (String)stack.pop();
            String key = null;
            String value = null;
            while (!label.equals("end")) {
                if (label.equals("name")) key = (String)stack.pop();
                if (label.equals("value")) value = (String)stack.pop();
                if (key != null && value != null) {
                    this.resourceParameters.setParameter(key, value);
                    key = null;
                    value = null;
                }
                label = (String)stack.pop();
            }

        } else if (name.equals(CINCLUDE_CONFIGURATION_ELEMENT) == true
                 && this.state == STATE_INCLUDE) {
            this.configurationParameters = new Parameters();
            // Now get the parameters off the stack
            String label = (String)stack.pop();
            String key = null;
            String value = null;
            while (!label.equals("end")) {
                if (label.equals("name")) key = (String)stack.pop();
                if (label.equals("value")) value = (String)stack.pop();
                if (key != null && value != null) {
                    this.configurationParameters.setParameter(key, value);
                    key = null;
                    value = null;
                }
                label = (String)stack.pop();
            }

        } else if (name.equals(CINCLUDE_PARAMETER_ELEMENT) == true
                   && this.state == STATE_INCLUDE) {

        } else if (name.equals(CINCLUDE_NAME_ELEMENT) == true
                   && this.state == STATE_INCLUDE) {
            stack.push(this.endTextRecording());
            stack.push("name");

        // parameter value
        } else if (name.equals(CINCLUDE_VALUE_ELEMENT) == true
                   && this.state == STATE_INCLUDE) {
            stack.push(this.endSerializedXMLRecording());
            stack.push("value");

        } else if (name.equals(CINCLUDE_CACHED_INCLUDE_ELEMENT)) {
            if (this.compiling) {
               super.endTransformingElement(uri,
                                            CINCLUDE_CACHED_INCLUDE_PLACEHOLDER_ELEMENT,
                                            raw + "p");
            }
            // do nothing else
        } else {
            super.endTransformingElement(uri, name, raw);
        }
    }

    protected String processCIncludeElement(String src, String element,
                                            String select, String ns, String prefix,
                                            boolean cache)
    throws SAXException, IOException {

        if (src == null) {
            throw new SAXException("Missing 'src' attribute on cinclude:include element");
        }

        if (element == null) element="";
        if (select == null) select="";
        if (ns == null) ns="";
        if (prefix == null) prefix="";

        if (this.getLogger().isDebugEnabled()) {
            getLogger().debug("Processing CInclude element: src=" + src
                          + ", element=" + element
                          + ", select=" + select
                          + ", ns=" + ns
                          + ", prefix=" + prefix
                          + ", caching="+cache);
        }

        if (cache) {
            src = this.cacheManager.load(src, this.cachingSession);

            if (this.cachingSession.isParallel() && !this.cachingSession.isPreemptive()) {
                if (!this.compiling) {
                    this.compiling = true;
                    this.startCompiledXMLRecording();
                }
            } else {
                this.cacheManager.stream(src, this.cachingSession, this.filter);
            }

            return src;
        }

        // usual no caching stuff
        if (!"".equals(element)) {
            AttributesImpl attrs = new AttributesImpl();
            if (!ns.equals("")) {
                super.startPrefixMapping(prefix, ns);
            }
            super.startElement(ns,
                               element,
                               (!ns.equals("") && !prefix.equals("") ? prefix+":"+element : element),
                               attrs);
        }

        Source source = null;
        try {
            source = this.resolver.resolveURI(src);

            if (!"".equals(select)) {


                DOMParser parser = null;
                XPathProcessor processor = null;

                try {
                    parser = (DOMParser)this.manager.lookup(DOMParser.ROLE);
                    processor = (XPathProcessor)this.manager.lookup(XPathProcessor.ROLE);

                    InputSource input = SourceUtil.getInputSource(source);

                    Document document = parser.parseDocument(input);
                    NodeList list = processor.selectNodeList(document, select);
                    int length = list.getLength();
                    for (int i=0; i<length; i++) {
                          IncludeXMLConsumer.includeNode(list.item(i),
                                               this,
                                               this);
                    }
                } finally {
                    this.manager.release(parser);
                    this.manager.release(processor);
                }
            } else {
                String mimeType = null;
                if ( null != this.configurationParameters ) {
                    mimeType = this.configurationParameters.getParameter("mime-type", mimeType);
                }
                if ( this.compiling ) {
                    SourceUtil.toSAX(source, mimeType, new IncludeXMLConsumer(this.contentHandler, this.lexicalHandler));
                } else {
                    SourceUtil.toSAX(source, mimeType, this.filter);
                }
            }


        } catch (SourceException se) {
            throw new SAXException("Exception in CIncludeTransformer",se);
        } catch (IOException e) {
            throw new SAXException("CIncludeTransformer could not read resource", e);
        } catch (ProcessingException e){
            throw new SAXException("Exception in CIncludeTransformer",e);
        } catch(ServiceException e) {
            throw new SAXException(e);
        } finally {
            this.resolver.release(source);
        }

        if (!"".equals(element)) {
            super.endElement(ns, element, (!ns.equals("") && !prefix.equals("") ? prefix+":"+element : element));
            if (!ns.equals("")) {
                super.endPrefixMapping(prefix);
            }
        }
        return src;
    }

    /**
     * Start recording of compiled xml.
     * The incomming SAX events are recorded and a compiled representation
     * is created. These events are not forwarded to the next component in
     * the pipeline.
     */
    protected void startCompiledXMLRecording()
    throws SAXException {
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("BEGIN startCompiledXMLRecording");
        }

        try {
            this.recorder = (XMLSerializer)this.manager.lookup(XMLSerializer.ROLE);

            this.addRecorder(recorder);

        } catch (ServiceException ce) {
            throw new SAXException("Unable to lookup xml serializer for compiling xml.", ce);
        }
        if (this.getLogger().isDebugEnabled()) {
           this.getLogger().debug("END startCompiledXMLRecording");
        }
    }

    /**
     * Stop recording of compiled XML.
     * @return The compiled XML.
     */
    protected Object endCompiledXMLRecording()
    throws SAXException {
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("BEGIN endCompiledXMLRecording");
        }

        XMLSerializer recorder = (XMLSerializer)this.removeRecorder();
        Object text = recorder.getSAXFragment();

        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("END endCompiledXMLRecording text="+text);
        }
        return text;
    }

    /**
     * @see org.xml.sax.ContentHandler#startDocument()
     */
    public void startDocument() throws SAXException {
        this.filter = new MyFilter(this.xmlConsumer, this);
        super.startDocument();
    }

    /**
     * @see org.xml.sax.ContentHandler#endDocument()
     */
    public void endDocument() throws SAXException {
        if ( this.compiling ) {
            Object compiledXML = this.endCompiledXMLRecording();
            XMLDeserializer deserializer = null;
            try {
                deserializer = (XMLDeserializer)this.manager.lookup(XMLDeserializer.ROLE);
                deserializer.setConsumer(this.filter);
                deserializer.deserialize(compiledXML);
            } catch (ServiceException ce) {
                throw new SAXException("Unable to lookup xml deserializer.", ce);
            } finally {
                this.manager.release( deserializer );
            }
        }
        super.endDocument();
    }

    /**
     * @see org.apache.cocoon.caching.CacheableProcessingComponent#getKey()
     */
    public Serializable getKey() {
        if (this.supportCaching
            && null != this.cacheManager
            && this.cachingSession.getExpires() > 0) {
            return "1";
        }
        return null;
    }

    /**
     * @see org.apache.cocoon.caching.CacheableProcessingComponent#getValidity()
     */
    public SourceValidity getValidity() {
        if (this.supportCaching
            && null != this.cacheManager
            && this.cachingSession.getExpires() > 0
            && !this.cachingSession.isPurging()) {
            return this.cachingSession.getExpiresValidity();
        }
        return null;
    }

}

final class MyFilter extends IncludeXMLConsumer {

    private CIncludeTransformer transformer;

    /**
     * This filter class post-processes the parallel fetching
     * @param consumer
     */
    public MyFilter(XMLConsumer consumer, CIncludeTransformer transformer) {
        super(consumer);
        this.transformer = transformer;
    }


    public void endElement(String uri, String local, String qName)
    throws SAXException {
        if (uri != null
            && uri.equals(CIncludeTransformer.CINCLUDE_NAMESPACE_URI)
            && local.equals(CIncludeTransformer.CINCLUDE_CACHED_INCLUDE_PLACEHOLDER_ELEMENT)) {
            // this is the placeholder element: do nothing
        } else {
            super.endElement(uri, local, qName);
        }
    }

    public void startElement(String uri,
                                String local,
                                String qName,
                                Attributes attr)
    throws SAXException {
        if (uri != null
            && uri.equals(CIncludeTransformer.CINCLUDE_NAMESPACE_URI)
            && local.equals(CIncludeTransformer.CINCLUDE_CACHED_INCLUDE_PLACEHOLDER_ELEMENT)) {
            // this is a placeholder
            try {
                final String src = attr.getValue("",CIncludeTransformer.CINCLUDE_INCLUDE_ELEMENT_SRC_ATTRIBUTE);
                this.transformer.cacheManager.stream(src, this.transformer.cachingSession, this);
            } catch (IOException ioe) {
                throw new SAXException("IOException", ioe);
            }
        } else {
            super.startElement(uri, local, qName, attr);
        }
    }

}
