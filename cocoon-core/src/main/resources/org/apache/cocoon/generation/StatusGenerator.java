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
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;

import org.apache.cocoon.Constants;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.components.flow.ContinuationsManager;
import org.apache.cocoon.components.flow.WebContinuationDataBean;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.core.Core;
import org.apache.cocoon.core.Settings;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;

import org.apache.commons.lang.SystemUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.TraversableSource;
import org.apache.excalibur.store.Store;
import org.apache.excalibur.store.StoreJanitor;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.File;
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
import java.util.Properties;
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
 *     cocoon-version CDATA #IMPLIED
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
 * @version $Id$
 */
public class StatusGenerator extends ServiceableGenerator
                             implements Contextualizable, Configurable {

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
     * The component context.
     */
    protected Context context;

    /**
     * The Cocoon core.
     */
    protected Core core;

    /**
     * The StoreJanitor used to get cache statistics
     */
    protected StoreJanitor storeJanitor;

    /**
     * The persistent store
     */
    protected Store storePersistent;

    /**
     * Show continuations information
     */
    private boolean showContinuations;

    /**
     * The ContinuationManager
     */
    private ContinuationsManager continuationsManager;

    /**
     * List & show the contents of WEB/lib
     */
    private boolean showLibrary;

    /**
     * WEB-INF/lib directory
     */
    private Source libDirectory;


    public void contextualize(Context context) throws ContextException {
        this.context = context;
    }

    public void configure(Configuration configuration) throws ConfigurationException {
        this.showContinuations = configuration.getChild("show-continuations").getValueAsBoolean(true);
        this.showLibrary = configuration.getChild("show-libraries").getValueAsBoolean(true);
    }

    /**
     * Set the current <code>ServiceManager</code> instance used by this
     * <code>Serviceable</code>.
     * Need to get statistics about cache hits
     */
    public void service(ServiceManager manager) throws ServiceException {
        super.service(manager);
        this.core = (Core) this.manager.lookup(Core.ROLE);

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

        if(this.manager.hasService(ContinuationsManager.ROLE)) {
            continuationsManager = (ContinuationsManager) this.manager.lookup(ContinuationsManager.ROLE);
        } else {
            getLogger().info("ContinuationsManager is not available. Sorry no overview of created continuations");
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

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if (this.manager != null) {
            this.manager.release(this.core);
            this.manager.release(this.storePersistent);
            this.manager.release(this.storeJanitor);
            this.manager.release(this.continuationsManager);
            this.core = null;
            this.storePersistent = null;
            this.storeJanitor = null;
            this.continuationsManager = null;
        }

        if (this.libDirectory != null) {
            super.resolver.release(this.libDirectory);
            this.libDirectory = null;
        }

        super.dispose();
    }

    /**
     * Generate the status information in XML format.
     * @throws SAXException
     *         when there is a problem creating the output SAX events.
     */
    public void generate() throws SAXException, ProcessingException {

        // Start the document and set the namespace.
        super.contentHandler.startDocument();
        super.contentHandler.startPrefixMapping("", NAMESPACE);
        super.contentHandler.startPrefixMapping(XLINK_PREFIX, XLINK_NS);

        genStatus();

        // End the document.
        super.contentHandler.endPrefixMapping(XLINK_PREFIX);
        super.contentHandler.endPrefixMapping("");
        super.contentHandler.endDocument();
    }

    /**
     * Generate the main status document.
     */
    private void genStatus() throws SAXException, ProcessingException {
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
        atts.addCDATAAttribute(NAMESPACE, "date", dateTime);
        atts.addCDATAAttribute(NAMESPACE, "host", localHost);
        atts.addCDATAAttribute(NAMESPACE, "cocoon-version", Constants.VERSION);
        dateTime = DateFormat.getDateTimeInstance().format(new Date(this.core.getSettings().getCreationTime()));
        atts.addCDATAAttribute(NAMESPACE, "creation-time", dateTime);
        atts.addCDATAAttribute(NAMESPACE, "build-info", Constants.BUILD_INFO);
        super.contentHandler.startElement(NAMESPACE, "statusinfo", "statusinfo", atts);

        if (this.showContinuations) {
            genContinuationsTree();
        }
        genSettings();
        genVMStatus();
        genProperties();
        if (this.showLibrary) {
            genLibrarylist();
        }

        // End root element.
        super.contentHandler.endElement(NAMESPACE, "statusinfo", "statusinfo");
    }

    private void genContinuationsTree() throws SAXException {
        startGroup("Continuations");
        List continuationsAsDataBeansList = this.continuationsManager.getWebContinuationsDataBeanList();
        for (Iterator i = continuationsAsDataBeansList.iterator(); i.hasNext();) {
            displayContinuation((WebContinuationDataBean) i.next());
        }
        endGroup();
    }

    private void displayContinuation(WebContinuationDataBean wc) throws SAXException {
        AttributesImpl ai = new AttributesImpl();
        ai.addAttribute(NAMESPACE, "id", "id", "CDATA", wc.getId());
        ai.addAttribute(NAMESPACE, "interpreter", "interpreter", "CDATA", wc.getInterpreterId());
        ai.addAttribute(NAMESPACE, "expire-time", "expire-time", "CDATA", wc.getExpireTime());
        ai.addAttribute(NAMESPACE, "time-to-live", "time-to-live", "CDATA", wc.getTimeToLive());
        ai.addAttribute(NAMESPACE, "last-access-time", "last-access-time", "CDATA", wc.getLastAccessTime());

        super.contentHandler.startElement(NAMESPACE, "cont", "cont", ai);
        List children = wc.get_children();
        for (int i = 0; i < children.size(); i++) {
            displayContinuation((WebContinuationDataBean) children.get(i));
        }
        super.contentHandler.endElement(NAMESPACE, "cont", "cont");
    }

    private void genVMStatus() throws SAXException {
        AttributesImpl atts = new AttributesImpl();
        startGroup("VM");

        // BEGIN ClassPath
        String classpath = SystemUtils.JAVA_CLASS_PATH;
        if (classpath != null) {
            List paths = new ArrayList();
            StringTokenizer tokenizer = new StringTokenizer(classpath, SystemUtils.PATH_SEPARATOR);
            while (tokenizer.hasMoreTokens()) {
                paths.add(tokenizer.nextToken());
            }
            addMultilineValue("classpath", paths);
        }
        // END ClassPath

        // BEGIN CONTEXT CLASSPATH
        String contextClassPath = null;
        try {
            contextClassPath = (String) this.context.get(Constants.CONTEXT_CLASSPATH);
        } catch (ContextException e) {
            // we ignore this
        }
        if (contextClassPath != null) {
            List paths = new ArrayList();
            StringTokenizer tokenizer = new StringTokenizer(contextClassPath, File.pathSeparator);
            while (tokenizer.hasMoreTokens()) {
                paths.add(tokenizer.nextToken());
            }
            addMultilineValue("context-classpath", paths);
        }
        // END CONTEXT CLASSPATH

        // BEGIN Memory status
        startGroup("Memory");
        final long totalMemory = Runtime.getRuntime().totalMemory();
        final long freeMemory = Runtime.getRuntime().freeMemory();
        addValue("total", String.valueOf(totalMemory));
        addValue("used", String.valueOf(totalMemory - freeMemory));
        addValue("free", String.valueOf(freeMemory));
        endGroup();
        // END Memory status

        // BEGIN JRE
        startGroup("JRE");
        addValue("version", SystemUtils.JAVA_VERSION);
        atts.clear();
        // qName = prefix + ':' + localName
        atts.addAttribute(XLINK_NS, "type", XLINK_PREFIX + ":type", "CDATA", "simple");
        atts.addAttribute(XLINK_NS, "href", XLINK_PREFIX + ":href", "CDATA", SystemUtils.JAVA_VENDOR_URL);
        addValue("java-vendor", SystemUtils.JAVA_VENDOR, atts);
        endGroup();
        // END JRE

        // BEGIN Operating system
        startGroup("Operating System");
        addValue("name", SystemUtils.OS_NAME);
        addValue("architecture", SystemUtils.OS_ARCH);
        addValue("version", SystemUtils.OS_VERSION);
        endGroup();
        // END operating system

        // BEGIN Cache
        if (this.storeJanitor != null) {
            startGroup("Store Janitor");

            // For each element in StoreJanitor
            Iterator i = this.storeJanitor.iterator();
            while (i.hasNext()) {
                Store store = (Store) i.next();
                startGroup(store.getClass().getName() + " (hash = 0x" + Integer.toHexString(store.hashCode()) + ")" );
                int size = 0;
                int empty = 0;
                atts.clear();
                atts.addAttribute(NAMESPACE, "name", "name", "CDATA", "cached");
                super.contentHandler.startElement(NAMESPACE, "value", "value", atts);

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
                        super.contentHandler.startElement(NAMESPACE, "line", "line", atts);
                        super.contentHandler.characters(line.toCharArray(), 0, line.length());
                        super.contentHandler.endElement(NAMESPACE, "line", "line");
                    }
                }
                if (size == 0) {
                    super.contentHandler.startElement(NAMESPACE, "line", "line", atts);
                    String value = "[empty]";
                    super.contentHandler.characters(value.toCharArray(), 0, value.length());
                    super.contentHandler.endElement(NAMESPACE, "line", "line");
                }
                super.contentHandler.endElement(NAMESPACE, "value", "value");

                addValue("size", String.valueOf(size) + " items in cache (" + empty + " are empty)");
                endGroup();
            }
            endGroup();
        }

        if (this.storePersistent != null) {
            startGroup(storePersistent.getClass().getName() + " (hash = 0x" + Integer.toHexString(storePersistent.hashCode()) + ")");
            int size = 0;
            int empty = 0;
            atts.clear();
            atts.addAttribute(NAMESPACE, "name", "name", "CDATA", "cached");
            super.contentHandler.startElement(NAMESPACE, "value", "value", atts);

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
                    super.contentHandler.startElement(NAMESPACE, "line", "line", atts);
                    super.contentHandler.characters(line.toCharArray(), 0, line.length());
                    super.contentHandler.endElement(NAMESPACE, "line", "line");
                }
            }
            if (size == 0) {
                super.contentHandler.startElement(NAMESPACE, "line", "line", atts);
                String value = "[empty]";
                super.contentHandler.characters(value.toCharArray(), 0, value.length());
                super.contentHandler.endElement(NAMESPACE, "line", "line");
            }
            super.contentHandler.endElement(NAMESPACE, "value", "value");

            addValue("size", size + " items in cache (" + empty + " are empty)");
            endGroup();
        }
        // END Cache

        endGroup();
    }

    private void genSettings() throws SAXException {
        final Settings s = core.getSettings();
        this.startGroup("Base Settings");

        this.addValue("Running mode", s.getProperty(Settings.PROPERTY_RUNNING_MODE,
                                                    Settings.DEFAULT_RUNNING_MODE));
        this.addValue(Settings.KEY_CONFIGURATION, s.getConfiguration());
        this.addMultilineValue(Settings.KEY_EXTRA_CLASSPATHS, s.getExtraClasspaths());
        this.addMultilineValue(Settings.KEY_LOAD_CLASSES, s.getLoadClasses());
        this.addValue(Settings.KEY_FORCE_PROPERTIES, s.getForceProperties());
        this.addValue(Settings.KEY_PROPERTY_PROVIDER, s.getPropertyProviders());
        this.addValue(Settings.KEY_LOGGING_CONFIGURATION, s.getLoggingConfiguration());
        this.addValue(Settings.KEY_LOGGING_BOOTSTRAP_LOGLEVEL, s.getBootstrapLogLevel());
        this.addValue(Settings.KEY_LOGGING_MANAGER_CLASS, s.getLoggerManagerClassName());
        this.addValue(Settings.KEY_PARENT_SERVICE_MANAGER, s.getParentServiceManagerClassName());
        this.addValue(Settings.KEY_LOGGING_COCOON_LOGGER, s.getCocoonLogger());
        this.addValue(Settings.KEY_LOGGING_ENVIRONMENT_LOGGER, s.getEnvironmentLogger());
        this.addValue(Settings.KEY_LOGGING_OVERRIDE_LOGLEVEL, s.getOverrideLogLevel());
        this.addValue(Settings.KEY_MANAGE_EXCEPTIONS, s.isManageExceptions());
        this.addValue(Settings.KEY_UPLOADS_DIRECTORY, s.getUploadDirectory());
        this.addValue(Settings.KEY_CACHE_DIRECTORY, s.getCacheDirectory());
        this.addValue(Settings.KEY_WORK_DIRECTORY, s.getWorkDirectory());
        this.addValue(Settings.KEY_FORM_ENCODING, s.getFormEncoding());

        this.endGroup();

        this.startGroup("Dynamic Settings");

        this.addValue(Settings.KEY_RELOADING, s.isReloadingEnabled(null));
        this.addValue(Settings.KEY_RELOAD_DELAY, s.getReloadDelay(null));
        Iterator i = s.getProperties("org.apache.cocoon." + Settings.KEY_RELOADING + '.').iterator();
        while ( i.hasNext() ) {
            final String key = (String)i.next();
            final String value = s.getProperty(key);
            this.addValue(key.substring(18), value);
        }
        i = s.getProperties("org.apache.cocoon." + Settings.KEY_RELOAD_DELAY + '.').iterator();
        while ( i.hasNext() ) {
            final String key = (String)i.next();
            final String value = s.getProperty(key);
            this.addValue(key.substring(18), value);
        }
        this.addValue(Settings.KEY_UPLOADS_AUTOSAVE, s.isAutosaveUploads());
        this.addValue(Settings.KEY_UPLOADS_ENABLE, s.isEnableUploads());
        this.addValue(Settings.KEY_UPLOADS_MAXSIZE, s.getMaxUploadSize());
        this.addValue(Settings.KEY_UPLOADS_OVERWRITE, s.isAllowOverwrite());
        this.addValue(Settings.KEY_SHOWTIME, s.isShowTime());
        this.addValue(Settings.KEY_HIDE_SHOWTIME, s.isHideShowTime());
        this.addValue(Settings.KEY_SHOW_VERSION, s.isShowVersion());
        this.addValue(Settings.KEY_LAZY_MODE, s.isLazyMode());

        this.endGroup();
    }

    private void genProperties() throws SAXException {
        this.startGroup("System-Properties");
        final Properties p = System.getProperties();
        final Enumeration e = p.keys();
        while ( e.hasMoreElements() ) {
            final String key = (String)e.nextElement();
            final String value = p.getProperty(key);
            this.addValue(key, value);
        }
        this.endGroup();
    }

    private void genLibrarylist() throws SAXException,ProcessingException {
        try {
            if (this.libDirectory instanceof TraversableSource) {
                startGroup("WEB-INF/lib");

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
                    addValue("file", (String) i.next());
                }

                endGroup();
            }
        } catch (SourceException e) {
            throw new ResourceNotFoundException("Could not read directory", e);
        }
    }

    /** Utility function to begin a <code>group</code> tag pair. */
    private void startGroup(String name) throws SAXException {
        startGroup(name, null);
    }

    /** Utility function to begin a <code>group</code> tag pair with added attributes. */
    private void startGroup(String name, Attributes atts)
    throws SAXException {
        AttributesImpl ai = (atts == null) ? new AttributesImpl() : new AttributesImpl(atts);
        ai.addAttribute(NAMESPACE, "name", "name", "CDATA", name);
        super.contentHandler.startElement(NAMESPACE, "group", "group", ai);
    }

    /** Utility function to end a <code>group</code> tag pair. */
    private void endGroup() throws SAXException {
        super.contentHandler.endElement(NAMESPACE, "group", "group");
    }

    /** Utility function to begin and end a <code>value</code> tag pair. */
    private void addValue(String name, String value)
    throws SAXException {
        addValue(name, value, null);
    }

    /** Utility function to begin and end a <code>value</code> tag pair. */
    private void addValue(String name, boolean value) throws SAXException {
        addValue(name, String.valueOf(value), null);
    }

    /** Utility function to begin and end a <code>value</code> tag pair. */
    private void addValue(String name, int value) throws SAXException {
        addValue(name, String.valueOf(value), null);
    }

    /** Utility function to begin and end a <code>value</code> tag pair. */
    private void addValue(String name, long value) throws SAXException {
        addValue(name, String.valueOf(value), null);
    }

    /** Utility function to begin and end a <code>value</code> tag pair. */
    private void addValue(String name, List value) throws SAXException {
        addValue(name, value.iterator());
    }

    /** Utility function to begin and end a <code>value</code> tag pair. */
    private void addValue(String name, Iterator value) throws SAXException {
        final StringBuffer buffer = new StringBuffer();
        boolean first = true;
        while ( value.hasNext() ) {
            if ( !first ) {
                buffer.append(',');
            } else {
                first = false;
            }
            buffer.append(value.next());
        }
        addValue(name, buffer.toString(), null);
    }

    /** Utility function to begin and end a <code>value</code> tag pair. */
    private void addValue(String name, Map value) throws SAXException {
        final StringBuffer buffer = new StringBuffer();
        final Iterator i = value.entrySet().iterator();
        boolean first = true;
        while ( i.hasNext() ) {
            if ( !first ) {
                buffer.append(',');
            } else {
                first = false;
            }
            Map.Entry current = (Map.Entry)i.next();
            buffer.append(current.getKey()).append('=').append(current.getValue());
        }
        addValue(name, buffer.toString(), null);
    }

    /** Utility function to begin and end a <code>value</code> tag pair with added attributes. */
    private void addValue(String name, String value, Attributes atts)
    throws SAXException {
        AttributesImpl ai = (atts == null) ? new AttributesImpl() : new AttributesImpl(atts);
        ai.addAttribute(NAMESPACE, "name", "name", "CDATA", name);
        super.contentHandler.startElement(NAMESPACE, "value", "value", ai);
        super.contentHandler.startElement(NAMESPACE, "line", "line", XMLUtils.EMPTY_ATTRIBUTES);

        if (value != null) {
            super.contentHandler.characters(value.toCharArray(), 0, value.length());
        }

        super.contentHandler.endElement(NAMESPACE, "line", "line");
        super.contentHandler.endElement(NAMESPACE, "value", "value");
    }

    /** Utility function to begin and end a <code>value</code> tag pair. */
    private void addMultilineValue(String name, List values)
    throws SAXException {
        addMultilineValue(name, values, null);
    }

    /** Utility function to begin and end a <code>value</code> tag pair with added attributes. */
    private void addMultilineValue(String name, List values, Attributes atts)
    throws SAXException {
        AttributesImpl ai = (atts == null) ? new AttributesImpl() : new AttributesImpl(atts);
        ai.addAttribute(NAMESPACE, "name", "name", "CDATA", name);
        super.contentHandler.startElement(NAMESPACE, "value", "value", ai);

        for (int i = 0; i < values.size(); i++) {
            String value = (String) values.get(i);
            if (value != null) {
                super.contentHandler.startElement(NAMESPACE, "line", "line", XMLUtils.EMPTY_ATTRIBUTES);
                super.contentHandler.characters(value.toCharArray(), 0, value.length());
                super.contentHandler.endElement(NAMESPACE, "line", "line");
            }
        }
        super.contentHandler.endElement(NAMESPACE, "value", "value");
    }
}
