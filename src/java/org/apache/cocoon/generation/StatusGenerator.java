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

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.Constants;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.flow.ContinuationsManager;
import org.apache.cocoon.components.flow.WebContinuationDataBean;
import org.apache.cocoon.core.Core;
import org.apache.cocoon.core.Settings;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.xml.XMLUtils;

import org.apache.commons.lang.SystemUtils;
import org.apache.excalibur.store.Store;
import org.apache.excalibur.store.StoreJanitor;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

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
 * @author <a href="mailto:paul@luminas.co.uk">Paul Russell</a> (Luminas Limited)
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @author <a href="mailto:skoechlin@ivision.fr">S&eacute;bastien K&oelig;chlin</a> (iVision)
 * @author <a href="mailto:g-froehlich@gmx.de">Gerhard Froehlich</a>
 * @version $Id$
 */
public class StatusGenerator 
    extends ServiceableGenerator {

    
    private static final String SHOW_CONTINUATIONS_INFO = "show-cont";    
    
    /**
     * The ContinuationManager
     */
    protected ContinuationsManager continuationsManager;
    
    /**
     * add infos about continuations?
     */
    protected boolean showContinuationsInformation = false;
    
    /**
     * The StoreJanitor used to get cache statistics
     */
    protected StoreJanitor storejanitor;
    protected Store store_persistent;

    /**
     * The XML namespace for the output document.
     */
    protected static final String namespace =
        "http://apache.org/cocoon/status/2.0";

    /**
     * The XML namespace for xlink
     */
    protected static final String xlinkNamespace =
        "http://www.w3.org/1999/xlink";

    /**
     * The namespace prefix for xlink namespace
     */
    protected static final String xlinkPrefix = "xlink";

    /**
     * Set the current <code>ServiceManager</code> instance used by this
     * <code>Serviceable</code>.
     * Need to get statistics about cache hits
     */
    public void service(ServiceManager manager) throws ServiceException {
        super.service(manager);
        if (this.manager.hasService(StoreJanitor.ROLE)) {
            this.storejanitor = (StoreJanitor)manager.lookup(StoreJanitor.ROLE);
        } else {
            getLogger().info("StoreJanitor is not available. Sorry, no cache statistics");
        }
        if (this.manager.hasService(Store.PERSISTENT_STORE)) {
            this.store_persistent = (Store)this.manager.lookup(Store.PERSISTENT_STORE);
        } else {
            getLogger().info("Persistent Store is not available. Sorry no cache statistics about it.");
        }
        if(this.manager.hasService(ContinuationsManager.ROLE)) {
            continuationsManager = (ContinuationsManager) this.manager.lookup(ContinuationsManager.ROLE);
        } else {
            getLogger().info("ContinuationsManager is not available. Sorry no overview of created continuations");
        }        
    }

    /**
     * @see org.apache.cocoon.sitemap.SitemapModelComponent#setup(org.apache.cocoon.environment.SourceResolver, java.util.Map, java.lang.String, org.apache.avalon.framework.parameters.Parameters)
     */
    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters parameters) 
        throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, parameters);
        this.showContinuationsInformation = parameters.getParameterAsBoolean(SHOW_CONTINUATIONS_INFO, false);
    }   
    
    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if (this.manager != null) {
            this.manager.release(this.store_persistent);
            this.manager.release(this.storejanitor);
            this.store_persistent = null;
            this.storejanitor = null;
        }
        super.dispose();
    }

    /** Generate the status information in XML format.
     * @throws SAXException
     *         when there is a problem creating the output SAX events.
     */
    public void generate() throws SAXException {

        // Start the document and set the namespace.
        this.contentHandler.startDocument();
        this.contentHandler.startPrefixMapping("", namespace);
        this.contentHandler.startPrefixMapping(xlinkPrefix, xlinkNamespace);

        genStatus();

        // End the document.
        this.contentHandler.endPrefixMapping(xlinkPrefix);
        this.contentHandler.endPrefixMapping("");
        this.contentHandler.endDocument();
    }

    /** Generate the main status document.
     */
    private void genStatus() throws SAXException {
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
        atts.addAttribute(namespace, "date", "date", "CDATA", dateTime);
        atts.addAttribute(namespace, "host", "host", "CDATA", localHost);
        atts.addAttribute(namespace, "cocoon-version", "cocoon-version", "CDATA", Constants.VERSION);
        this.xmlConsumer.startElement(namespace, "statusinfo", "statusinfo", atts);

        if(this.showContinuationsInformation) {
            genContinuationsTree();        
        }          
        genSettings();
        genVMStatus();
        genProperties();

        // End root element.
        this.xmlConsumer.endElement(namespace, "statusinfo", "statusinfo");
    }
    
    private void genContinuationsTree() throws SAXException {
        startGroup("Continuations");
        List continuationsAsDataBeansList = this.continuationsManager.getWebContinuationsDataBeanList();
        for(Iterator it = continuationsAsDataBeansList.iterator(); it.hasNext();) {
            displayContinuation((WebContinuationDataBean) it.next());
        }
        endGroup();
    }
    

    private void displayContinuation(WebContinuationDataBean wc) throws SAXException {
        AttributesImpl ai = new AttributesImpl(); 
        ai.addAttribute(namespace, "id", "id", "CDATA", wc.getId());
        ai.addAttribute(namespace, "interpreter", "interpreter", "CDATA", wc.getInterpreterId());
        ai.addAttribute(namespace, "expire-time", "expire-time", "CDATA", wc.getExpireTime());
        ai.addAttribute(namespace, "time-to-live", "time-to-live", "CDATA", wc.getTimeToLive());
        ai.addAttribute(namespace, "last-access-time", "last-access-time", "CDATA", wc.getLastAccessTime());   

        this.xmlConsumer.startElement(namespace, "cont", "cont", ai);
        List children = wc.get_children();
        for (int i = 0; i < children.size(); i++) {
            displayContinuation((WebContinuationDataBean) children.get(i));
        }        
        this.xmlConsumer.endElement(namespace, "cont", "cont");
    }       

    private void genVMStatus() throws SAXException {
        AttributesImpl atts = new AttributesImpl();

        startGroup("vm");
        // BEGIN Memory status
        startGroup("memory");
        addValue("total", String.valueOf(Runtime.getRuntime().totalMemory()));
        addValue("free", String.valueOf(Runtime.getRuntime().freeMemory()));
        endGroup();
        // END Memory status

        // BEGIN JRE
        startGroup("jre");
        addValue("version", SystemUtils.JAVA_VERSION);
        atts.clear();
        // qName = prefix + ':' + localName
        atts.addAttribute(xlinkNamespace, "type", xlinkPrefix + ":type", "CDATA", "simple");
        atts.addAttribute(xlinkNamespace, "href", xlinkPrefix + ":href", "CDATA", SystemUtils.JAVA_VENDOR_URL);
        addValue("java-vendor", SystemUtils.JAVA_VENDOR, atts);
        endGroup();
        // END JRE

        // BEGIN Operating system
        startGroup("operating-system");
        addValue("name", SystemUtils.OS_NAME);
        addValue("architecture", SystemUtils.OS_ARCH);
        addValue("version", SystemUtils.OS_VERSION);
        endGroup();
        // END operating system

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

        // BEGIN Cache
        if (this.storejanitor != null) {
            startGroup("Store-Janitor");

            // For each element in StoreJanitor
            Iterator i = this.storejanitor.iterator();
            while (i.hasNext()) {
                Store store = (Store) i.next();
                startGroup(store.getClass().getName() + " (hash = 0x" + Integer.toHexString(store.hashCode()) + ")" );
                int size = 0;
                int empty = 0;
                atts.clear();
                atts.addAttribute(namespace, "name", "name", "CDATA", "cached");
                this.xmlConsumer.startElement(namespace, "value", "value", atts);

                atts.clear();
                Enumeration e = store.keys();
                while (e.hasMoreElements()) {
                    size++;
                    Object key = e.nextElement();
                    Object val = store.get(key);
                    String line = null;
                    if (val == null) {
                        empty++;
                    } else {
                        line = key + " (class: " + val.getClass().getName() + ")";
                        this.xmlConsumer.startElement(namespace, "line", "line", atts);
                        this.xmlConsumer.characters(line.toCharArray(), 0, line.length());
                        this.xmlConsumer.endElement(namespace, "line", "line");
                    }
                }
                if (size == 0) {
                    this.xmlConsumer.startElement(namespace, "line", "line", atts);
                    String value = "[empty]";
                    this.xmlConsumer.characters(value.toCharArray(), 0, value.length());
                    this.xmlConsumer.endElement(namespace, "line", "line");
                }
                this.xmlConsumer.endElement(namespace, "value", "value");

                addValue("size", String.valueOf(size) + " items in cache (" + empty + " are empty)");
                endGroup();
            }
            endGroup();        
        }

        if (this.store_persistent != null) {
            startGroup(store_persistent.getClass().getName() + " (hash = 0x" + Integer.toHexString(store_persistent.hashCode()) + ")");
            int size = 0;
            int empty = 0;
            atts.clear();
            atts.addAttribute(namespace, "name", "name", "CDATA", "cached");
            this.xmlConsumer.startElement(namespace, "value", "value", atts);

            atts.clear();
            Enumeration e = this.store_persistent.keys();
            while (e.hasMoreElements()) {
                size++;
                Object key = e.nextElement();
                Object val = store_persistent.get(key);
                String line = null;
                if (val == null) {
                    empty++;
                } else {
                    line = key + " (class: " + val.getClass().getName() + ")";
                    this.xmlConsumer.startElement(namespace, "line", "line", atts);
                    this.xmlConsumer.characters(line.toCharArray(), 0, line.length());
                    this.xmlConsumer.endElement(namespace, "line", "line");
                }
            }
            if (size == 0) {
                this.xmlConsumer.startElement(namespace, "line", "line", atts);
                String value = "[empty]";
                this.xmlConsumer.characters(value.toCharArray(), 0, value.length());
                this.xmlConsumer.endElement(namespace, "line", "line");
            }
            this.xmlConsumer.endElement(namespace, "value", "value");

            addValue("size", size + " items in cache (" + empty + " are empty)");
            endGroup();
        }
        // END Cache

        // BEGIN OS info
        endGroup();
    }

    private void genSettings() throws SAXException {
        Core core = null;
        try {
            core = (Core)this.manager.lookup(Core.ROLE);
        } catch (ServiceException se) {
            // this can never happen
            throw new RuntimeException("Unable to lookup Cocoon core.");
        }
        final Settings s = core.getSettings();
        this.startGroup("Base Settings");
        
        this.addValue(Settings.KEY_CONFIGURATION, s.getConfiguration());
        this.addValue(Settings.KEY_EXTRA_CLASSPATHS, s.getExtraClasspaths());
        this.addValue(Settings.KEY_LOAD_CLASSES, s.getLoadClasses());
        this.addValue(Settings.KEY_FORCE_PROPERTIES, s.getForceProperties());
        this.addValue(Settings.KEY_LOGGING_CONFIGURATION, s.getLoggingConfiguration());
        this.addValue(Settings.KEY_LOGGING_BOOTSTRAP_LOGLEVEL, s.getBootstrapLogLevel());
        this.addValue(Settings.KEY_LOGGING_MANAGER_CLASS, s.getLoggerManagerClassName());
        this.addValue(Settings.KEY_PARENT_SERVICE_MANAGER, s.getParentServiceManagerClassName());
        this.addValue(Settings.KEY_LOGGING_COCOON_LOGGER, s.getCocoonLogger());
        this.addValue(Settings.KEY_INIT_CLASSLOADER, s.isInitClassloader());
        this.addValue(Settings.KEY_LOGGING_ENVIRONMENT_LOGGER, s.getEnvironmentLogger());
        this.addValue(Settings.KEY_LOGGING_OVERRIDE_LOGLEVEL, s.getOverrideLogLevel());
        this.addValue(Settings.KEY_MANAGE_EXCEPTIONS, s.isManageExceptions());
        this.addValue(Settings.KEY_UPLOADS_DIRECTORY, s.getUploadDirectory());
        this.addValue(Settings.KEY_CACHE_DIRECTORY, s.getCacheDirectory());
        this.addValue(Settings.KEY_WORK_DIRECTORY, s.getWorkDirectory());
        this.addValue(Settings.KEY_FORM_ENCODING, s.getFormEncoding());
        
        this.endGroup();

        this.startGroup("Dynamic Settings");

        this.addValue(Settings.KEY_CONFIGURATION_RELOAD_DELAY, s.getConfigurationReloadDelay());
        this.addValue(Settings.KEY_ALLOW_RELOAD, s.isAllowReload());
        this.addValue(Settings.KEY_UPLOADS_AUTOSAVE, s.isAutosaveUploads());
        this.addValue(Settings.KEY_UPLOADS_ENABLE, s.isEnableUploads());
        this.addValue(Settings.KEY_UPLOADS_MAXSIZE, s.getMaxUploadSize());
        this.addValue(Settings.KEY_UPLOADS_OVERWRITE, s.isAllowOverwrite());
        this.addValue(Settings.KEY_SHOWTIME, s.isShowTime());
        this.addValue(Settings.KEY_HIDE_SHOWTIME, s.isHideShowTime());
        this.addValue(Settings.KEY_SHOW_COCOON_VERSION, s.isShowCocoonVersion());
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

    /** Utility function to begin a <code>group</code> tag pair. */
    private void startGroup(String name) throws SAXException {
        startGroup(name, null);
    }

    /** Utility function to begin a <code>group</code> tag pair with added attributes. */
    private void startGroup(String name, Attributes atts)
    throws SAXException {
        AttributesImpl ai = (atts == null) ? new AttributesImpl() : new AttributesImpl(atts); 
        ai.addAttribute(namespace, "name", "name", "CDATA", name);
        this.xmlConsumer.startElement(namespace, "group", "group", ai);
    }

    /** Utility function to end a <code>group</code> tag pair. */
    private void endGroup() throws SAXException {
        this.xmlConsumer.endElement(namespace, "group", "group");
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
        ai.addAttribute(namespace, "name", "name", "CDATA", name);
        this.xmlConsumer.startElement(namespace, "value", "value", ai);
        this.xmlConsumer.startElement(namespace, "line", "line", XMLUtils.EMPTY_ATTRIBUTES);

        if (value != null) {
            this.xmlConsumer.characters(value.toCharArray(), 0, value.length());
        }

        this.xmlConsumer.endElement(namespace, "line", "line");
        this.xmlConsumer.endElement(namespace, "value", "value");
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
        ai.addAttribute(namespace, "name", "name", "CDATA", name);
        this.xmlConsumer.startElement(namespace, "value", "value", ai);

        for (int i = 0; i < values.size(); i++) {
            String value = (String) values.get(i);
            if (value != null) {
                this.xmlConsumer.startElement(namespace, "line", "line", XMLUtils.EMPTY_ATTRIBUTES);
                this.xmlConsumer.characters(value.toCharArray(), 0, value.length());
                this.xmlConsumer.endElement(namespace, "line", "line");
            }
        }
        this.xmlConsumer.endElement(namespace, "value", "value");
    }
}
