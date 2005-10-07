/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
package org.apache.cocoon.generation;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;

import org.apache.cocoon.Constants;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.xml.XMLUtils;

import org.apache.commons.lang.SystemUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.TraversableSource;
import org.apache.excalibur.store.Store;
import org.apache.excalibur.store.StoreJanitor;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

/**
 * @cocoon.sitemap.component.documentation
 * Generates an XML representation of the current status of Cocoon.
 *
 * @cocoon.sitemap.component.name   status
 * @cocoon.sitemap.component.label  content
 * @cocoon.sitemap.component.logger sitemap.generator.status
 *
 * @cocoon.sitemap.component.pooling.max  16
 *
 * Potted DTD:
 *
 * <code>
 * &lt;!ELEMENT statusinfo (group|value)*&gt;
 *
 * &lt;!ATTLIST statusinfo
 *     date CDATA #IMPLIED
 *     host CDATA #IMPLIED
       cocoon-version CDATA #IMPLIED
 * &gt;
 *
 * &lt;!ELEMENT group (group|value)*&gt;
 * &lt;!ATTLIST group
 *     name CDATA #IMPLIED
 * &gt;
 *
 * &lt;!ELEMENT value (line)+&gt;
 * &lt;!ATTLIST value
 *     name CDATA #REQUIRED
 *
 * &lt;!ELEMENT line (#PCDATA)+&gt;
 * &gt;
 * </code>
 *
 * @author <a href="mailto:paul@luminas.co.uk">Paul Russell</a> (Luminas Limited)
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @author <a href="mailto:skoechlin@ivision.fr">S&eacute;bastien K&oelig;chlin</a> (iVision)
 * @author <a href="mailto:g-froehlich@gmx.de">Gerhard Froehlich</a>
 * @version $Id$
 */
public class StatusGenerator extends ServiceableGenerator
                             implements Configurable {

    /**
     * The XML namespace for the output document.
     */
    public static final String NAMESPACE = "http://apache.org/cocoon/status/2.0";

    /**
     * The XML namespace for xlink
     */
    protected static final String XLINK_NS = "http://www.w3.org/1999/xlink";

    /**
     * The namespace prefix for xlink namespace
     */
    protected static final String XLINK_PREFIX = "xlink";

    /**
     * The StoreJanitor used to get cache statistics
     */
    protected StoreJanitor storeJanitor;

    /**
     * The persistent store
     */
    protected Store storePersistent;

    /**
     * List & show the contents of WEB/lib
     */
    private boolean showLibrary;

    /**
     * WEB-INF/lib directory
     */
    private Source libDirectory;


    public void configure(Configuration configuration) throws ConfigurationException {
        this.showLibrary = configuration.getChild("show-libraries").getValueAsBoolean(true);
    }

    /**
     * Set the current <code>ServiceManager</code> instance used by this
     * <code>Serviceable</code>.
     * Need to get statistics about cache hits
     */
    public void service(ServiceManager manager) throws ServiceException {
        super.service(manager);

        if (this.manager.hasService(StoreJanitor.ROLE)) {
            this.storeJanitor = (StoreJanitor) manager.lookup(StoreJanitor.ROLE);
        } else {
            getLogger().info("StoreJanitor is not available. Sorry, no cache statistics");
        }
        if (this.manager.hasService(Store.PERSISTENT_STORE)) {
            this.storePersistent = (Store) this.manager.lookup(Store.PERSISTENT_STORE);
        } else {
            getLogger().info("Persistent Store is not available. Sorry no cache statistics about it.");
        }
    }

    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par)
    throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, par);

        if (this.showLibrary) {
            try {
                this.libDirectory = super.resolver.resolveURI("context://WEB-INF/lib");
            } catch (SourceException e) {
                throw SourceUtil.handle(e);
            }
        }
    }

    public void dispose() {
        if (this.manager != null) {
            this.manager.release(this.storePersistent);
            this.manager.release(this.storeJanitor);
            this.storePersistent = null;
            this.storeJanitor = null;
        }

        if (this.libDirectory != null) {
            super.resolver.release(this.libDirectory);
            this.libDirectory = null;
        }

        super.dispose();
    }

    /** Generate the status information in XML format.
     * @throws SAXException
     *         when there is a problem creating the output SAX events.
     */
    public void generate() throws SAXException, ProcessingException {

        // Start the document and set the namespace.
        this.contentHandler.startDocument();
        this.contentHandler.startPrefixMapping("", NAMESPACE);
        this.contentHandler.startPrefixMapping(XLINK_PREFIX, XLINK_NS);

        genStatus(this.contentHandler);

        // End the document.
        this.contentHandler.endPrefixMapping(XLINK_PREFIX);
        this.contentHandler.endPrefixMapping("");
        this.contentHandler.endDocument();
    }

    /**
     * Generate the main status document.
     */
    private void genStatus(ContentHandler ch) throws SAXException, ProcessingException {
        // Root element.

        // The current date and time.
        String dateTime = DateFormat.getDateTimeInstance().format(new Date());
        String localHost;

        // The local host.
        try {
            localHost = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            getLogger().debug("StatusGenerator:UnknownHost", e);
            localHost = "";
        } catch (SecurityException e) {
            getLogger().debug("StatusGenerator:Security", e);
            localHost = "";
        }

        AttributesImpl atts = new AttributesImpl();
        atts.addAttribute(NAMESPACE, "date", "date", "CDATA", dateTime);
        atts.addAttribute(NAMESPACE, "host", "host", "CDATA", localHost);
        atts.addAttribute(NAMESPACE, "cocoon-version", "cocoon-version", "CDATA", Constants.VERSION);
        ch.startElement(NAMESPACE, "statusinfo", "statusinfo", atts);

        genVMStatus(ch);
        if (this.showLibrary) {
            this.genLibrarylist(ch);
        }

        // End root element.
        ch.endElement(NAMESPACE, "statusinfo", "statusinfo");
    }

    private void genVMStatus(ContentHandler ch) throws SAXException {
        AttributesImpl atts = new AttributesImpl();
        startGroup(ch, "VM");

        // BEGIN ClassPath
        String classpath = SystemUtils.JAVA_CLASS_PATH;
        if (classpath != null) {
            List paths = new ArrayList();
            StringTokenizer tokenizer = new StringTokenizer(classpath, SystemUtils.PATH_SEPARATOR);
            while (tokenizer.hasMoreTokens()) {
                paths.add(tokenizer.nextToken());
            }
            addMultilineValue(ch, "classpath", paths);
        }
        // END ClassPath

        // BEGIN Memory status
        startGroup(ch, "Memory");
        addValue(ch, "total", String.valueOf(Runtime.getRuntime().totalMemory()));
        addValue(ch, "free", String.valueOf(Runtime.getRuntime().freeMemory()));
        endGroup(ch);
        // END Memory status

        // BEGIN JRE
        startGroup(ch, "JRE");
        addValue(ch, "version", SystemUtils.JAVA_VERSION);
        atts.clear();
        // qName = prefix + ':' + localName
        atts.addAttribute(XLINK_NS, "type", XLINK_PREFIX + ":type", "CDATA", "simple");
        atts.addAttribute(XLINK_NS, "href", XLINK_PREFIX + ":href", "CDATA", SystemUtils.JAVA_VENDOR_URL);
        addValue(ch, "java-vendor", SystemUtils.JAVA_VENDOR, atts);
        endGroup(ch);
        // END JRE

        // BEGIN Operating system
        startGroup(ch, "Operating System");
        addValue(ch, "name", SystemUtils.OS_NAME);
        addValue(ch, "architecture", SystemUtils.OS_ARCH);
        addValue(ch, "version", SystemUtils.OS_VERSION);
        endGroup(ch);
        // END operating system

        // BEGIN Cache
        if (this.storeJanitor != null) {
            startGroup(ch, "Store Janitor");

            // For each element in StoreJanitor
            Iterator i = this.storeJanitor.iterator();
            while (i.hasNext()) {
                Store store = (Store) i.next();
                startGroup(ch, store.getClass().getName() + " (hash = 0x" + Integer.toHexString(store.hashCode()) + ")");
                int size = 0;
                int empty = 0;
                atts.clear();
                atts.addAttribute(NAMESPACE, "name", "name", "CDATA", "cached");
                ch.startElement(NAMESPACE, "value", "value", atts);

                atts.clear();
                Enumeration e = store.keys();
                while (e.hasMoreElements()) {
                    size++;
                    Object key = e.nextElement();
                    Object val = store.get(key);
                    String line;
                    if (val == null) {
                        empty++;
                    } else {
                        line = key + " (class: " + val.getClass().getName() + ")";
                        ch.startElement(NAMESPACE, "line", "line", atts);
                        ch.characters(line.toCharArray(), 0, line.length());
                        ch.endElement(NAMESPACE, "line", "line");
                    }
                }
                if (size == 0) {
                    ch.startElement(NAMESPACE, "line", "line", atts);
                    String value = "[empty]";
                    ch.characters(value.toCharArray(), 0, value.length());
                    ch.endElement(NAMESPACE, "line", "line");
                }
                ch.endElement(NAMESPACE, "value", "value");

                addValue(ch, "size", String.valueOf(size) + " items in cache (" + empty + " are empty)");
                endGroup(ch);
            }
            endGroup(ch);
        }

        if (this.storePersistent != null) {
            startGroup(ch, storePersistent.getClass().getName() + " (hash = 0x" + Integer.toHexString(storePersistent.hashCode()) + ")");
            int size = 0;
            int empty = 0;
            atts.clear();
            atts.addAttribute(NAMESPACE, "name", "name", "CDATA", "cached");
            ch.startElement(NAMESPACE, "value", "value", atts);

            atts.clear();
            Enumeration e = this.storePersistent.keys();
            while (e.hasMoreElements()) {
                size++;
                Object key = e.nextElement();
                Object val = storePersistent.get(key);
                String line;
                if (val == null) {
                    empty++;
                } else {
                    line = key + " (class: " + val.getClass().getName() + ")";
                    ch.startElement(NAMESPACE, "line", "line", atts);
                    ch.characters(line.toCharArray(), 0, line.length());
                    ch.endElement(NAMESPACE, "line", "line");
                }
            }
            if (size == 0) {
                ch.startElement(NAMESPACE, "line", "line", atts);
                String value = "[empty]";
                ch.characters(value.toCharArray(), 0, value.length());
                ch.endElement(NAMESPACE, "line", "line");
            }
            ch.endElement(NAMESPACE, "value", "value");

            addValue(ch, "size", size + " items in cache (" + empty + " are empty)");
            endGroup(ch);
        }
        // END Cache

        endGroup(ch);
    }

    private void genLibrarylist(ContentHandler ch) throws SAXException,ProcessingException {
        try {
            if (this.libDirectory instanceof TraversableSource) {
                startGroup(ch, "WEB-INF/lib");

                Set files = new TreeSet();
                Collection kids = ((TraversableSource) this.libDirectory).getChildren();
                try {
                    for (Iterator i = kids.iterator(); i.hasNext(); ) {
                        final Source lib = (Source) i.next();
                        final String name = lib.getURI().substring(lib.getURI().lastIndexOf('/'));
                        files.add(name);
                    }
                } finally {
                    for (Iterator i = kids.iterator(); i.hasNext(); ) {
                        final Source lib = (Source) i.next();
                        super.resolver.release(lib);
                    }
                }

                for (Iterator i = files.iterator(); i.hasNext(); ) {
                    addValue(ch, "file", (String) i.next());
                }

                endGroup(ch);
            }
        } catch (SourceException e) {
            throw new ResourceNotFoundException("Could not read directory", e);
        }
    }

    /** Utility function to begin a <code>group</code> tag pair. */
    private void startGroup(ContentHandler ch, String name) throws SAXException {
        startGroup(ch, name, null);
    }

    /** Utility function to begin a <code>group</code> tag pair with added attributes. */
    private void startGroup(ContentHandler ch, String name, Attributes atts)
    throws SAXException {
        AttributesImpl ai = (atts == null) ? new AttributesImpl() : new AttributesImpl(atts);
        ai.addAttribute(NAMESPACE, "name", "name", "CDATA", name);
        ch.startElement(NAMESPACE, "group", "group", ai);
    }

    /** Utility function to end a <code>group</code> tag pair. */
    private void endGroup(ContentHandler ch) throws SAXException {
        ch.endElement(NAMESPACE, "group", "group");
    }

    /** Utility function to begin and end a <code>value</code> tag pair. */
    private void addValue(ContentHandler ch, String name, String value)
    throws SAXException {
        addValue(ch, name, value, null);
    }

    /** Utility function to begin and end a <code>value</code> tag pair with added attributes. */
    private void addValue(ContentHandler ch, String name, String value, Attributes atts)
    throws SAXException {
        AttributesImpl ai = (atts == null) ? new AttributesImpl() : new AttributesImpl(atts);
        ai.addAttribute(NAMESPACE, "name", "name", "CDATA", name);
        ch.startElement(NAMESPACE, "value", "value", ai);
        ch.startElement(NAMESPACE, "line", "line", XMLUtils.EMPTY_ATTRIBUTES);

        if (value != null) {
            ch.characters(value.toCharArray(), 0, value.length());
        }

        ch.endElement(NAMESPACE, "line", "line");
        ch.endElement(NAMESPACE, "value", "value");
    }

    /** Utility function to begin and end a <code>value</code> tag pair. */
    private void addMultilineValue(ContentHandler ch, String name, List values)
    throws SAXException {
        addMultilineValue(ch, name, values, null);
    }

    /** Utility function to begin and end a <code>value</code> tag pair with added attributes. */
    private void addMultilineValue(ContentHandler ch, String name, List values, Attributes atts)
    throws SAXException {
        AttributesImpl ai = (atts == null) ? new AttributesImpl() : new AttributesImpl(atts);
        ai.addAttribute(NAMESPACE, "name", "name", "CDATA", name);
        ch.startElement(NAMESPACE, "value", "value", ai);

        for (int i = 0; i < values.size(); i++) {
            String value = (String) values.get(i);
            if (value != null) {
                ch.startElement(NAMESPACE, "line", "line", XMLUtils.EMPTY_ATTRIBUTES);
                ch.characters(value.toCharArray(), 0, value.length());
                ch.endElement(NAMESPACE, "line", "line");
            }
        }
        ch.endElement(NAMESPACE, "value", "value");
    }
}
