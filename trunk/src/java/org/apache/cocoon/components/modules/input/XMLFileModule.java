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

package org.apache.cocoon.components.modules.input;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.SourceValidity;
import org.w3c.dom.Document;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This module provides an Input Module interface to any XML document, by using
 * XPath expressions as attribute keys.
 * The XML can be obtained from any Cocoon <code>Source</code> (e.g.,
 * <code>cocoon:/...</code>, <code>context://..</code>, and regular URLs).
 * Sources can be held in memory for better performance and reloaded if
 * changed.
 *
 * <p>Caching and reloading can be turned on / off (default: on)
 * through <code>&lt;reloadable&gt;false&lt;/reloadable&gt;</code> and
 * <code>&lt;cacheable&gt;false&lt;/cacheable&gt;</code>. The file
 * (source) to use is specified through <code>&lt;file
 * src="protocol:path/to/file.xml" reloadable="true"
 * cacheable="true"/&gt;</code> optionally overriding defaults for
 * caching and or reloading.</p>
 *
 * @author <a href="mailto:jefft@apache.org">Jeff Turner</a>
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version CVS $Id: XMLFileModule.java,v 1.12 2004/02/22 18:10:59 cziegeler Exp $
 */
public class XMLFileModule extends AbstractJXPathModule
    implements Serviceable, ThreadSafe {

    /** Static (cocoon.xconf) configuration location, for error reporting */
    String staticConfLocation;
    /** Cached documents */
    Map documents = null;
    /** Default value for reloadability of sources */
    boolean reloadAll = false;
    /** Default value for cachability of sources */
    boolean cacheAll = true;
    /** Default src */
    String src;

    SourceResolver resolver;
    ServiceManager manager;
    

    /**
     * Takes care of (re-)loading and caching of sources.
     *
     */
    protected class DocumentHelper {

        private boolean reloadable = true;
        private boolean cacheable = true;
        /** source location */
        private String uri = null;
        /** cached DOM */
        private Document document = null;
        private SourceValidity srcVal = null;

        /**
         * Creates a new <code>DocumentHelper</code> instance.
         *
         * @param reload a <code>boolean</code> value, whether this source should be reloaded if changed.
         * @param cache a <code>boolean</code> value, whether this source should be kept in memory.
         * @param src a <code>String</code> value containing the URI
         */
        public DocumentHelper(boolean reload, boolean cache, String src) {
            this.reloadable = reload;
            this.cacheable = cache;
            this.uri = src;
            // deferr loading document
        }

        
        /**
         * Returns the Document belonging to the configured
         * source. Transparently handles reloading and caching.
         *
         * @param manager a <code>ComponentManager</code> value
         * @param resolver a <code>SourceResolver</code> value
         * @return a <code>Document</code> value
         * @exception Exception if an error occurs
         */
        public synchronized Document getDocument(ServiceManager manager, 
                                                 SourceResolver resolver, 
                                                 Logger logger) throws Exception {
            Source src = null;
            Document dom = null;
            try {
                if (this.document == null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Document not cached... reloading uri " + this.uri);
                    }
                    src = resolver.resolveURI(this.uri);
                    this.srcVal = src.getValidity();
                    this.document = SourceUtil.toDOM(src);
                    dom = this.document;
                } else {
                    if (this.reloadable) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Document cached... checking validity of uri " + this.uri);
                        }
                        src = resolver.resolveURI(this.uri);
                        SourceValidity valid = src.getValidity();
                        if (srcVal != null && this.srcVal.isValid(valid) != 1) {
                            if (logger.isDebugEnabled())
                                logger.debug("reloading document... uri "+this.uri);
                            this.srcVal = valid;
                            this.document = SourceUtil.toDOM(src);
                        }
                    }
                    dom = this.document;
                }
            } finally {
                resolver.release(src);
            }

            if (!this.cacheable) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Not caching document cached... uri " + this.uri);
                }
                this.srcVal = null;
                this.document = null;
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Done with document... uri " + this.uri);
            }
            return dom;
        }
    }



    /**
     * Set the current <code>ServiceManager</code> instance used by this
     * <code>Serviceable</code>.
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
        this.resolver = (SourceResolver)manager.lookup(SourceResolver.ROLE);
    }


	/* (non-Javadoc)
	 * @see org.apache.avalon.framework.activity.Disposable#dispose()
	 */
	public void dispose() {
		super.dispose();
        if ( this.manager != null ) {
            this.manager.release( this.resolver );
            this.manager = null;
            this.resolver = null;
        }
	}
    
    /**
     * Static (cocoon.xconf) configuration.
     * Configuration is expected to be of the form:
     * &lt;...>
     *   &lt;reloadable>true|<b>false</b>&lt;/reloadable>
     *   &lt;cacheable><b>true</b>|false&lt;/cacheable>
     *   &lt;file src="<i>src1</i>" reloadable="true|<b>false</b>" cacheable="<b>true</b>|false"/>
     *   &lt;file src="<i>src2</i>" reloadable="true|<b>false</b>" cacheable="<b>true</b>|false"/>
     *   ...
     * &lt;/...>
     * Each &lt;file> pre-loads an XML DOM for querying. Typically only one
     * &lt;file> is specified, and its <i>src</i> is used as a default if not
     * overridden in the {@link #getContextObject dynamic configuration}
     *
     * @param config a <code>Configuration</code> value, as described above.
     * @exception ConfigurationException if an error occurs
     */
    public void configure(Configuration config) throws ConfigurationException {

        this.staticConfLocation = config.getLocation();
        super.configure(config);
        this.reloadAll = config.getChild("reloadable").getValueAsBoolean(this.reloadAll);
        if (config.getChild("cachable", false) != null) {
            throw new ConfigurationException(
                    "Bzzt! Wrong spelling at "+config.getChild("cachable").getLocation()+": please use 'cacheable', not 'cachable'");
        }
        this.cacheAll = config.getChild("cacheable").getValueAsBoolean(this.cacheAll);

        Configuration[] files = config.getChildren("file");
        if (this.documents == null)
            this.documents = Collections.synchronizedMap(new HashMap());

        for (int i = 0; i < files.length; i++) {
            boolean reload = files[i].getAttributeAsBoolean("reloadable", this.reloadAll);
            boolean cache  = files[i].getAttributeAsBoolean("cacheable", this.cacheAll);
            this.src = files[i].getAttribute("src");
            // by assigning the source uri to this.src the last one will be the default
            // OTOH caching / reload parameters can be specified in one central place
            // if multiple file tags are used.
            this.documents.put(files[i], new DocumentHelper(reload, cache, this.src));
        }
    }


    /**
     * Get the DOM object that JXPath will operate on when evaluating
     * attributes.  This DOM is loaded from a Source, specified in the
     * modeConf, or (if modeConf is null) from the {@link #configure static
     * configuration}.
     * @param modeConf The dynamic configuration for the current operation. May
     * be <code>null</code>, in which case static (cocoon.xconf) configuration
     * is used.  Configuration is expected to have a &lt;file> child node, and
     * be of the form:
     * &lt;...>
     *   &lt;file src="..." reloadable="true|false"/>
     * &lt;/...>
     * @param objectModel Object Model for the current module operation.
     */
    protected Object getContextObject(Configuration modeConf,
                                      Map objectModel) throws ConfigurationException {

        String src = this.src;
        boolean reload = this.reloadAll;
        boolean cache = this.cacheAll;
        boolean hasDynamicConf = false; // whether we have a <file src="..."> dynamic configuration
        Configuration fileConf = null;  // the nested <file>, if any
        if (modeConf != null) {
            fileConf = modeConf.getChild("file", false);
            if (fileConf == null) {
                getLogger().warn("Error: missing 'file' child element at "+modeConf.getLocation());
                /*
                throw new ConfigurationException(
                        "Error in dynamic configuration of XMLFileModule: " +
                        "missing 'file' child element at " + 
                        modeConf.getLocation());
                */
            } else {
              hasDynamicConf = true;
            }
        }

        if (hasDynamicConf) {
            src = fileConf.getAttribute("src");
        }

        if (this.documents == null) 
            this.documents = Collections.synchronizedMap(new HashMap());

        if (src==null) {
            throw new ConfigurationException("No source specified"+
                    (modeConf!=null?", either dynamically in "+modeConf.getLocation()+", or ":"")+
                    " statically in "+staticConfLocation
                    );
        }

        if (!this.documents.containsKey(src)) {
            if (hasDynamicConf) {
                reload = fileConf.getAttributeAsBoolean("reloadable",reload);
                cache = fileConf.getAttributeAsBoolean("cacheable",cache);
                if (fileConf.getAttribute("cachable", null) != null) {
                    throw new ConfigurationException(
                            "Bzzt! Wrong spelling at "+fileConf.getLocation()+": please use 'cacheable', not 'cachable'");
                }

            }
            this.documents.put(src, new DocumentHelper(reload, cache, src));
        }

        Document dom = null;

        try{            
            dom = ((DocumentHelper) this.documents.get(src)).getDocument(this.manager, this.resolver, getLogger());
        } catch (Exception e) {
            if (getLogger().isDebugEnabled())
                getLogger().debug("Error using source "+src+"\n"+ e.getMessage(), e);
            throw new ConfigurationException("Error using source "+src, e);
        }
        return dom;

    }

}
