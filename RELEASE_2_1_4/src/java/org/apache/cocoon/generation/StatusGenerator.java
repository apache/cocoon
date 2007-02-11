/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.generation;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.excalibur.store.Store;
import org.apache.excalibur.store.StoreJanitor;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Generates an XML representation of the current status of Cocoon.
 * Potted DTD:
 *
 * <code>
 * &lt;!ELEMENT statusinfo (group|value)*&gt;
 *
 * &lt;!ATTLIST statusinfo
 *     date CDATA #IMPLIED
 *     host CDATA #IMPLIED
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
 * @version CVS $Id: StatusGenerator.java,v 1.3 2003/12/08 11:45:16 antonio Exp $
 */
public class StatusGenerator extends ServiceableGenerator {

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
        try {
            this.storejanitor = (StoreJanitor)manager.lookup(StoreJanitor.ROLE);
            this.store_persistent = (Store)this.manager.lookup(Store.PERSISTENT_STORE);
        } catch(ServiceException ce) {
            getLogger().info("StoreJanitor is not available. Sorry, no cache statistics");
        }
    }
    
    public void dispose() {
        if ( this.manager != null ) {
            this.manager.release( this.store_persistent );
            this.manager.release( this.storejanitor );
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

        genStatus(this.contentHandler);

        // End the document.
        this.contentHandler.endPrefixMapping(xlinkPrefix);
        this.contentHandler.endPrefixMapping("");
        this.contentHandler.endDocument();
    }

    /** Generate the main status document.
     */
    private void genStatus(ContentHandler ch) throws SAXException {
        // Root element.

        // The current date and time.
        String dateTime = DateFormat.getDateTimeInstance().format(new Date());

        String localHost;

        // The local host.
        try {
            localHost = InetAddress.getLocalHost().getHostName();
        } catch ( UnknownHostException e ) {
            getLogger().debug("StatusGenerator:UnknownHost", e);
            localHost = "";
        } catch ( SecurityException e ) {
            getLogger().debug("StatusGenerator:Security", e);
            localHost = "";
        }

        AttributesImpl atts = new AttributesImpl();
        atts.addAttribute(namespace, "date", "date", "CDATA", dateTime);
        atts.addAttribute(namespace, "host", "host", "CDATA", localHost);
        ch.startElement(namespace, "statusinfo", "statusinfo", atts);

        genVMStatus(ch);

        // End root element.
        ch.endElement(namespace, "statusinfo", "statusinfo");
    }

    private void genVMStatus(ContentHandler ch) throws SAXException {
        AttributesImpl atts = new AttributesImpl();

        startGroup(ch, "vm");
        // BEGIN Memory status
        startGroup(ch, "memory");
        addValue(ch, "total", String.valueOf(Runtime.getRuntime().totalMemory()));
        addValue(ch, "free", String.valueOf(Runtime.getRuntime().freeMemory()));
        endGroup(ch);
        // END Memory status

        // BEGIN JRE
        startGroup(ch, "jre");
        addValue(ch, "version", System.getProperty("java.version"));
        atts.clear();
        // qName = prefix + ':' + localName
        atts.addAttribute(xlinkNamespace, "type", xlinkPrefix + ":type", "CDATA", "simple");
        atts.addAttribute(xlinkNamespace, "href", xlinkPrefix + ":href", "CDATA",
            System.getProperty("java.vendor.url") );
        addValue(ch, "java-vendor", System.getProperty("java.vendor"), atts);
        endGroup(ch);
        // END JRE

        // BEGIN Operating system
        startGroup(ch, "operating-system");
        addValue(ch, "name", System.getProperty("os.name"));
        addValue(ch, "architecture", System.getProperty("os.arch"));
        addValue(ch, "version", System.getProperty("os.version"));
        endGroup(ch);
        // END operating system

        String classpath = System.getProperty("java.class.path");
        List paths = new ArrayList();
        StringTokenizer tokenizer = new StringTokenizer(classpath, System.getProperty("path.separator"));
        while (tokenizer.hasMoreTokens()) {
            paths.add(tokenizer.nextToken());
        }
        addMultilineValue(ch, "classpath", paths);
        // END ClassPath

        // BEGIN Cache
        startGroup(ch, "Store-Janitor");

        // For each element in StoreJanitor
        Iterator i = this.storejanitor.iterator();
        while (i.hasNext()) {
            Store store = (Store) i.next();
            startGroup(ch, store.getClass().getName()+" (hash = 0x"+Integer.toHexString(store.hashCode())+")" );
            int size = 0;
            int empty = 0;
            atts.clear();
            atts.addAttribute(namespace, "name", "name", "CDATA", "cached");
            ch.startElement(namespace, "value", "value", atts);
            // For each element in Store
            Enumeration e = store.keys();
            atts.clear();
            while( e.hasMoreElements() ) {
                size++;
                Object key  = e.nextElement();
                Object val  = store.get( key );
                String line = null;
                if (val == null) {
                    empty++;
                } else {
                    line = key + " (class: " + val.getClass().getName() + ")";
                    ch.startElement(namespace, "line", "line", atts);
                    ch.characters(line.toCharArray(), 0, line.length());
                    ch.endElement(namespace, "line", "line");
                }
            }

            if (size == 0) {
                ch.startElement(namespace, "line", "line", atts);
                String value = "[empty]";
                ch.characters(value.toCharArray(), 0, value.length());
                ch.endElement(namespace, "line", "line");
            }
            ch.endElement(namespace, "value", "value");

            addValue(ch, "size", String.valueOf(size) + " items in cache (" + empty + " are empty)");
            endGroup(ch);
        }
        endGroup(ch);        

        startGroup(ch, store_persistent.getClass().getName()+" (hash = 0x"+Integer.toHexString(store_persistent.hashCode())+")");
        int size = 0;
        int empty = 0;
        atts.clear();
        atts.addAttribute(namespace, "name", "name", "CDATA", "cached");
        ch.startElement(namespace, "value", "value", atts);
        Enumeration enum = this.store_persistent.keys();
        while (enum.hasMoreElements()) {
            size++;

            Object key  = enum.nextElement();
            Object val  = store_persistent.get (key);
            String line = null;
            if (val == null) {
                empty++;
            } else {
                line = key + " (class: " + val.getClass().getName() +  ")";
                ch.startElement(namespace, "line", "line", atts);
                ch.characters(line.toCharArray(), 0, line.length());
                ch.endElement(namespace, "line", "line");
            }
        }

        if (size == 0) {
            ch.startElement(namespace, "line", "line", atts);
            String value = "[empty]";
            ch.characters(value.toCharArray(), 0, value.length());
            ch.endElement(namespace, "line", "line");
        }
        ch.endElement(namespace, "value", "value");

        addValue(ch, "size", String.valueOf(size) + " items in cache (" + empty + " are empty)");
        endGroup(ch);
        // END Cache

        // BEGIN OS info
        endGroup(ch);
    }

    /** Utility function to begin a <code>group</code> tag pair. */
    private void startGroup(ContentHandler ch, String name) throws SAXException {
        startGroup(ch, name, null);
    }

    /** Utility function to begin a <code>group</code> tag pair with added attributes. */
    private void startGroup(ContentHandler ch, String name, Attributes atts) throws SAXException {
        AttributesImpl ai;
        if (atts == null) {
            ai = new AttributesImpl();
        } else {
            ai = new AttributesImpl(atts);
        }
        ai.addAttribute(namespace, "name", "name", "CDATA", name);
        ch.startElement(namespace, "group", "group", ai);
    }

    /** Utility function to end a <code>group</code> tag pair. */
    private void endGroup(ContentHandler ch) throws SAXException {
        ch.endElement(namespace, "group", "group");
    }

    /** Utility function to begin and end a <code>value</code> tag pair. */
    private void addValue(ContentHandler ch, String name, String value) throws SAXException {
        addValue(ch, name, value, null);
    }

    /** Utility function to begin and end a <code>value</code> tag pair with added attributes. */
    private void addValue(ContentHandler ch, String name, String value, Attributes atts) throws SAXException {
        AttributesImpl ai;
        if (atts == null) {
            ai = new AttributesImpl();
        } else {
            ai = new AttributesImpl(atts);
        }
        ai.addAttribute(namespace, "name", "name", "CDATA", name);
        ch.startElement(namespace, "value", "value", ai);
        ch.startElement(namespace, "line", "line", new AttributesImpl());

        if (value != null) {
            ch.characters(value.toCharArray(), 0, value.length());
        }

        ch.endElement(namespace, "line", "line");
        ch.endElement(namespace, "value", "value");
    }

    /** Utility function to begin and end a <code>value</code> tag pair. */
    private void addMultilineValue(ContentHandler ch, String name, List values) throws SAXException {
        addMultilineValue(ch, name, values, null);
    }

    /** Utility function to begin and end a <code>value</code> tag pair with added attributes. */
    private void addMultilineValue(ContentHandler ch, String name, List values, Attributes atts) throws SAXException {
        AttributesImpl ai;
        if (atts == null) {
            ai = new AttributesImpl();
        } else {
            ai = new AttributesImpl(atts);
        }
        ai.addAttribute(namespace, "name", "name", "CDATA", name);
        ch.startElement(namespace, "value", "value", ai);

        for (int i = 0; i < values.size(); i++) {
            String value = (String) values.get(i);
            if (value != null) {
                ch.startElement(namespace, "line", "line", new AttributesImpl());
                ch.characters(value.toCharArray(), 0, value.length());
                ch.endElement(namespace, "line", "line");
            }
        }

        ch.endElement(namespace, "value", "value");
    }
}
