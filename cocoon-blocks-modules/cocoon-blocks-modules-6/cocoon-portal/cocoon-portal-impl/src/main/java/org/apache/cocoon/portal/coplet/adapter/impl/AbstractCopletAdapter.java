/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.portal.coplet.adapter.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.cocoon.portal.PortalException;
import org.apache.cocoon.portal.event.CopletInstanceEvent;
import org.apache.cocoon.portal.event.Receiver;
import org.apache.cocoon.portal.om.CopletAdapter;
import org.apache.cocoon.portal.om.CopletDefinition;
import org.apache.cocoon.portal.om.CopletInstance;
import org.apache.cocoon.portal.om.CopletInstanceFeatures;
import org.apache.cocoon.portal.util.AbstractBean;
import org.apache.cocoon.portal.util.SaxBuffer;
import org.apache.cocoon.thread.RunnableManager;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import EDU.oswego.cs.dl.util.concurrent.CountDown;

/**
 * This is the abstract base adapter which could be used by any adapter
 * implementation. It already provides several base features which can
 * be used by sub classes.
 *
 * This adapter adds a caching mechanism. The result of the called coplet is cached until a
 * {@link org.apache.cocoon.portal.event.CopletInstanceEvent} for that coplet instance
 * is received. The content can eiter be cached in the user session or globally. The default
 * is the user session.
 *
 * <h2>Configuration</h2>
 * <table><tbody>
 * <tr>
 *   <th>buffer</th>
 *   <td>Shall the content of the coplet be buffered? If a coplet is
 *       buffered, errors local to the coplet are caught and a not
 *       availability notice is delivered instead. Buffering does not
 *       cache responses for subsequent requests.</td>
 *   <td></td>
 *   <td>boolean</td>
 *   <td><code>false</code></td>
 *  </tr>
 *  <tr>
 *   <th>timeout</th>
 *   <td>Max time in seconds content delivery may take. After a timeout,
 *       a not availability notice is delivered. Setting a timeout automatically
 *       turns on buffering.</td>
 *   <td></td>
 *   <td>int</td>
 *   <td><code>null</code></td>
 *  </tr>
 *  <tr>
 *   <th>cache-enabled</th>
 *   <td>Enable/disable the caching of the coplet.</td>
 *   <td></td>
 *   <td>boolean</td>
 *   <td><code>true</code></td>
 *  </tr>
 *  <tr>
 *   <th>ignore-sizing-events</th>
 *   <td>
 *     The cached content of a coplet is invalidated if an event for this coplet is received.
 *     If sizing events should be ignored, turn on this configuration.
 *   </td>
 *   <td></td>
 *   <td>boolean</td>
 *   <td><code>true</code></td>
 *  </tr>
 *  <tr>
 *   <th>ignore-simple-sizing-events</th>
 *   <td>
 *     This configuration has only an effect if ignore-sizing-events is turned on. If this
 *     configuration is set to true, then only sizing events for minimizing and setting
 *     the coplet state to back to normal are ignored. This allows the coplet to provide
 *     different contents for maximized or full screen.
 *   </td>
 *   <td></td>
 *   <td>boolean</td>
 *   <td><code>false</code></td>
 *  </tr>
 *  <tr>
 *   <th>cache-global</th>
 *   <td>With global caching the result of a coplet is cached independently of the user. If
 *       global caching is not used, the contents is cached on a per user base.</td>
 *   <td></td>
 *   <td>boolean</td>
 *   <td><code>false</code></td>
 *  </tr>
 *  <tr>
 *   <th>cache-global-use-attributes</th>
 *   <td>If global caching is used, the contents is cached per coplet data definition. If you
 *     turn on this configuration the contents is cached taking the current values of the
 *     attributes of a coplet instance data into account.
 *   </td>
 *   <td></td>
 *   <td>boolean</td>
 *   <td><code>false</code></td>
 *  </tr>
 * </tbody></table>
 *
 * @version $Id$
 */
public abstract class AbstractCopletAdapter
    extends AbstractBean
    implements CopletAdapter, Receiver {

    /** The configuration name for buffering. */
    public static final String CONFIGURATION_BUFFERING = "buffer";

    /** The configuration name for timeout. */
    public static final String CONFIGURATION_TIMEOUT = "timeout";

    /** The configuration name for enabling/disabling the cache. */
    public static final String CONFIGURATION_ENABLE_CACHING = "cache-enabled";

    /** The configuration name for using the global cache. */
    public static final String CONFIGURATION_CACHE_GLOBAL= "cache-global";

    /** The configuration name for querying instance attributes to generate the key
     * for the global cache. */
    public static final String CONFIGURATION_CACHE_GLOBAL_USE_ATTRIBUTES= "cache-global-use-attributes";

    /** The configuration name for ignoring all sizing events to clear the cache. */
    public static final String CONFIGURATION_IGNORE_SIZING_EVENTS = "ignore-sizing-events";

    /** The configuration name for ignoring just min/normal sizing events to clear the cache. */
    public static final String CONFIGURATION_IGNORE_SIMPLE_SIZING_EVENTS = "ignore-simple-sizing-events";

    /** The temporary attribute name for the storing the cached coplet content. */
    public static final String CACHE = "cacheData";

    /** This temporary attribute can be set on the instance to not cache the current response. */
    public static final String DO_NOT_CACHE = "doNotCache";

    /** The runnable manager for starting background tasks. */
    protected RunnableManager runnableManager;

    public void setRunnableManager(RunnableManager runnableManager) {
        this.runnableManager = runnableManager;
    }

    /**
     * Get a configuration value
     * First the coplet data is queried and if it doesn't provide an
     * attribute with the given name, the coplet base data is used.
     */
    protected Object getConfiguration(CopletInstance coplet, String key) {
        final CopletDefinition copletDefinition = coplet.getCopletDefinition();
        Object data = copletDefinition.getAttribute( key );
        if ( data == null) {
            data = copletDefinition.getCopletType().getCopletConfig().get( key );
        }
        return data;
    }

    /**
     * Get a configuration value
     * First the coplet data is queried and if it doesn't provide an
     * attribute with the given name, the coplet base data is used.
     * If no value is found the default value is returned.
     */
    protected Object getConfiguration(CopletInstance coplet,
                                      String key,
                                      Object defaultValue) {
        Object data = this.getConfiguration(coplet, key);
        if ( data == null ) {
            data = defaultValue;
        }
        return data;
    }

    /**
     * Implement this and not toSAX().
     */
    protected abstract void streamContent(CopletInstance coplet,
                                          ContentHandler contentHandler)
    throws SAXException;

    /**
     * This method streams the content of a coplet instance data.
     * It handles buffering and timeout setting and calls
     * {@link #streamContent(CopletInstance, ContentHandler)}
     * for creating the content.
     *
     * @see org.apache.cocoon.portal.om.CopletAdapter#toSAX(org.apache.cocoon.portal.om.CopletInstance, org.xml.sax.ContentHandler)
     */
    public void toSAX(CopletInstance coplet, ContentHandler contentHandler)
    throws SAXException {
        final long startTime = System.currentTimeMillis();
        Boolean bool = (Boolean) this.getConfiguration( coplet, CONFIGURATION_BUFFERING );
        Integer timeout = (Integer) this.getConfiguration( coplet, CONFIGURATION_TIMEOUT);
        if ( timeout != null ) {
            // if timeout is set we have to buffer!
            bool = Boolean.TRUE;
        }

        if ( bool != null && bool.booleanValue() ) {
            boolean read = false;
            SaxBuffer buffer = new SaxBuffer();
            Exception error = null;
            try {

                if ( timeout != null ) {
                    final int milli = timeout.intValue() * 1000;
                    final LoaderThread loader = new LoaderThread(this, coplet, buffer);
                    this.runnableManager.execute( this.getLoaderRunnable(loader) );
                    try {
                        read = loader.join( milli );
                    } catch (InterruptedException ignore) {
                        // ignored
                    }
                    error = loader.exception;
                    if ( error != null && this.getLogger().isWarnEnabled() ) {
                        this.getLogger().warn("Unable to get content of coplet: " + coplet.getId(), error);
                    }
                } else {
                    this.streamContentAndCache( coplet, buffer );
                    read = true;
                }
            } catch (Exception exception ) {
                error = exception;
                this.getLogger().error("Unable to get content of coplet: " + coplet.getId(), exception);
            } catch (Throwable t ) {
                error = new PortalException("Unable to get content of coplet: " + coplet.getId(), t);
                this.getLogger().error("Unable to get content of coplet: " + coplet.getId(), t);
            }

            if ( read ) {
                buffer.toSAX( contentHandler );
            } else {
                if ( !this.renderErrorContent(coplet, contentHandler, error)) {
                    // FIXME - get correct error message
                    contentHandler.startDocument();
                    contentHandler.startElement("", "p", "p", new AttributesImpl());
                    final char[] msg = ("The coplet " + coplet.getId() + " is currently not available.").toCharArray();
                    contentHandler.characters(msg, 0, msg.length);
                    contentHandler.endElement("", "p", "p");
                    contentHandler.endDocument();
                }
            }
        } else {
            this.streamContentAndCache( coplet, contentHandler );
        }
        if ( this.getLogger().isInfoEnabled() ) {
            final long msecs = System.currentTimeMillis() - startTime;
            this.getLogger().info("Streamed coplet " + coplet.getCopletDefinition().getId() +
                                  " (instance " + coplet.getId() + ") in " + msecs + "ms.");
        }
    }

    /**
     * This method does the caching (if enabled).
     */
    public void streamContentAndCache( final CopletInstance coplet,
                                       final ContentHandler contentHandler)
    throws SAXException {
        // Is caching enabled?
        boolean cachingEnabled = ((Boolean)this.getConfiguration(coplet, CONFIGURATION_ENABLE_CACHING, Boolean.TRUE)).booleanValue();
        // do we cache globally?
        boolean cacheGlobal = ((Boolean)this.getConfiguration(coplet, CONFIGURATION_CACHE_GLOBAL, Boolean.FALSE)).booleanValue();

        SaxBuffer data = null;
        // If caching is enabed and the cache is still valid, then use the cache
        if (cachingEnabled) {
            if ( cacheGlobal ) {
                data = (SaxBuffer) coplet.getCopletDefinition().getTemporaryAttribute(this.getCacheKey(coplet));
            } else {
                data = (SaxBuffer) coplet.getTemporaryAttribute(CACHE);
            }
        }
        if (data == null) {
            // if caching is permanently or temporary disabled, flush the cache and invoke coplet
            if ( !cachingEnabled || coplet.getTemporaryAttribute(DO_NOT_CACHE) != null ) {
                coplet.removeTemporaryAttribute(DO_NOT_CACHE);
                if ( cacheGlobal ) {
                    coplet.getCopletDefinition().removeTemporaryAttribute(this.getCacheKey(coplet));
                } else {
                    coplet.removeTemporaryAttribute(CACHE);
                }
                this.streamContent(coplet, contentHandler);
            } else {

                SaxBuffer buffer = new SaxBuffer();

                this.streamContent(coplet, buffer);
                data = buffer;
                if (coplet.removeTemporaryAttribute(DO_NOT_CACHE) == null) {
                    if ( cacheGlobal ) {
                        coplet.getCopletDefinition().setTemporaryAttribute(this.getCacheKey(coplet), data);
                    } else {
                        coplet.setTemporaryAttribute(CACHE, data);
                    }
                }
            }
        }
        // and now stream the data
        if ( data != null ) {
            data.toSAX(contentHandler);
        }
    }

    /**
     * @see org.apache.cocoon.portal.om.CopletAdapter#init(org.apache.cocoon.portal.om.CopletDefinition)
     */
    public void init(CopletDefinition coplet) {
        if ( this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("Initializing coplet definition: " + coplet);
        }
        // nothing to do here, can be overwritten in subclasses
    }

    /**
     * @see org.apache.cocoon.portal.om.CopletAdapter#destroy(org.apache.cocoon.portal.om.CopletDefinition)
     */
    public void destroy(CopletDefinition coplet) {
        // nothing to do here, can be overwritten in subclasses
        if ( this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("Destroying coplet definition: " + coplet);
        }
    }

    /**
     * @see org.apache.cocoon.portal.om.CopletAdapter#login(org.apache.cocoon.portal.om.CopletInstance)
     */
    public void login(CopletInstance coplet) {
        if ( this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("Login into coplet " + coplet);
        }
        // copy temporary attributes from the coplet data
        final Iterator iter = coplet.getCopletDefinition().getAttributes().entrySet().iterator();
        while ( iter.hasNext() ) {
            final Map.Entry entry = (Map.Entry)iter.next();
            if ( entry.getKey().toString().startsWith("temporary:") ) {
                final String name = entry.getKey().toString().substring(10);
                if ( this.getLogger().isDebugEnabled() ) {
                    this.getLogger().debug("Setting temporary attribute '" + name + "' on coplet " + coplet + " : " + entry.getValue());
                }
                coplet.setTemporaryAttribute(name, entry.getValue());
            }
        }
    }

    /**
     * @see org.apache.cocoon.portal.om.CopletAdapter#logout(org.apache.cocoon.portal.om.CopletInstance)
     */
    public void logout(CopletInstance coplet) {
        if ( this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("Logout from coplet " + coplet);
        }
        // nothing to do here, can be overwritten in subclasses
    }

    /**
     * Render the error content for a coplet
     * @param coplet  The coplet instance data
     * @param handler The content handler
     * @param error   The exception that occured
     * @return True if the error content has been rendered, otherwise false
     * @throws SAXException
     */
    protected boolean renderErrorContent(CopletInstance coplet,
                                         ContentHandler     handler,
                                         Exception          error)
    throws SAXException {
        return false;
    }
    /**
     * This adapter listens for CopletInstanceEvents. Each event sets the cache invalid.
     * @see org.apache.cocoon.portal.event.Receiver
     */
    public void inform(CopletInstanceEvent event) {
        final CopletInstance coplet = event.getTarget();

        // do we ignore SizingEvents
        boolean ignoreSizing = ((Boolean)this.getConfiguration(coplet, CONFIGURATION_IGNORE_SIZING_EVENTS, Boolean.TRUE)).booleanValue();

        if ( !ignoreSizing || !CopletInstanceFeatures.isSizingEvent(event)) {
            boolean cleanupCache = true;
            boolean ignoreSimpleSizing = ((Boolean)this.getConfiguration(coplet, CONFIGURATION_IGNORE_SIMPLE_SIZING_EVENTS, Boolean.FALSE)).booleanValue();
            if ( ignoreSimpleSizing && CopletInstanceFeatures.isSizingEvent(event) ) {
                int newSize = CopletInstanceFeatures.getSize(event);
                int oldSize = coplet.getSize();
                if (  (oldSize == CopletInstance.SIZE_NORMAL || oldSize == CopletInstance.SIZE_MINIMIZED )
                   && (newSize == CopletInstance.SIZE_NORMAL || newSize == CopletInstance.SIZE_MINIMIZED )) {
                    cleanupCache = false;
                }
            }
            if ( cleanupCache ) {
                // do we cache globally?
                boolean cacheGlobal = ((Boolean)this.getConfiguration(coplet, CONFIGURATION_CACHE_GLOBAL, Boolean.FALSE)).booleanValue();
                boolean cacheGlobalUseAttributes = ((Boolean)this.getConfiguration(coplet, CONFIGURATION_CACHE_GLOBAL_USE_ATTRIBUTES, Boolean.FALSE)).booleanValue();
                if ( cacheGlobal ) {
                    if ( !cacheGlobalUseAttributes ) {
                        coplet.getCopletDefinition().removeAttribute(CACHE);
                    }
                } else {
                    coplet.removeTemporaryAttribute(CACHE);
                }
            }
        }
    }

    /**
     * Build the key for the global cache.
     */
    protected String getCacheKey(CopletInstance coplet) {
        final Boolean useAttributes = (Boolean)this.getConfiguration(coplet,
                                                            CONFIGURATION_CACHE_GLOBAL_USE_ATTRIBUTES,
                                                            Boolean.FALSE);
        if ( !useAttributes.booleanValue() ) {
            return "coplet:" + coplet.getCopletDefinition().getId();
        }
        final StringBuffer buffer = new StringBuffer("coplet:");
        buffer.append(coplet.getCopletDefinition().getId());
        boolean hasParams = false;
        // first add attributes:
        // sort the keys
        List keyList = new ArrayList(coplet.getAttributes().keySet());
        Collections.sort(keyList);
        Iterator i = keyList.iterator();
        while ( i.hasNext() ) {
            final Object name = i.next();
            final Object value = coplet.getAttribute(name.toString());
            if ( hasParams ) {
                buffer.append('&');
            } else {
                buffer.append('?');
                hasParams = true;
            }
            buffer.append(name.toString());
            buffer.append('=');
            if ( value != null ) {
                try {
                    buffer.append(URLEncoder.encode(value.toString(), "utf-8"));
                } catch (UnsupportedEncodingException ignore) {
                    // we ignore this
                }
            }
        }
        // second add temporary attributes
        keyList = new ArrayList(coplet.getTemporaryAttributes().keySet());
        Collections.sort(keyList);
        i = keyList.iterator();
        while ( i.hasNext() ) {
            final Object name = i.next();
            final Object value = coplet.getTemporaryAttribute(name.toString());
            if ( hasParams ) {
                buffer.append('&');
            } else {
                buffer.append('?');
                hasParams = true;
            }
            buffer.append(name.toString());
            buffer.append('=');
            if ( value != null ) {
                try {
                    buffer.append(URLEncoder.encode(value.toString(), "utf-8"));
                } catch (UnsupportedEncodingException ignore) {
                    // we ignore this
                }
            }
        }
        return buffer.toString();
    }

    protected Runnable getLoaderRunnable(Runnable loader) {
        return loader;
    }
}

final class LoaderThread implements Runnable {

    private final AbstractCopletAdapter adapter;
    private final ContentHandler        handler;
    private final CopletInstance        coplet;
    private final CountDown             finished;
    Exception exception;

    public LoaderThread(AbstractCopletAdapter adapter,
                         CopletInstance coplet,
                         ContentHandler handler) {
        this.adapter = adapter;
        this.coplet  = coplet;
        this.handler = handler;
        this.finished = new CountDown( 1 );
    }

    public void run() {
        try {
            adapter.streamContentAndCache( this.coplet, this.handler );
        } catch (Exception local) {
            this.exception = local;
        } finally {
            this.finished.release();
        }
    }

    boolean join( final long milis )
    throws InterruptedException {
        return this.finished.attempt( milis );
    }
}
