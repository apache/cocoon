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
package org.apache.cocoon.components.flow;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.thread.RunnableManager;

import org.apache.excalibur.instrument.CounterInstrument;
import org.apache.excalibur.instrument.Instrument;
import org.apache.excalibur.instrument.Instrumentable;
import org.apache.excalibur.instrument.ValueInstrument;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * The default implementation of {@link ContinuationsManager}.
 *
 * @author <a href="mailto:ovidiu@cup.hp.com">Ovidiu Predescu</a>
 * @author <a href="mailto:Michael.Melhem@managesoft.com">Michael Melhem</a>
 * @since March 19, 2002
 * @see ContinuationsManager
 * @version CVS $Id$
 */
public class ContinuationsManagerImpl
        extends AbstractLogEnabled
        implements ContinuationsManager, Component, Configurable,
                   ThreadSafe, Instrumentable, Serviceable {

    static final int CONTINUATION_ID_LENGTH = 20;
    static final String EXPIRE_CONTINUATIONS = "expire-continuations";

    /**
     * Random number generator used to create continuation ID
     */
    protected SecureRandom random;
    protected byte[] bytes;

    /**
     * How long does a continuation exist in memory since the last
     * access? The time is in miliseconds, and the default is 1 hour.
     */
    protected int defaultTimeToLive;

    /**
     * Maintains the forest of <code>WebContinuation</code> trees.
     * This set is used only for debugging puroses by
     * {@link #displayAllContinuations()} method.
     */
    protected Set forest = Collections.synchronizedSet(new HashSet());

    /**
     * Association between <code>WebContinuation</code> IDs and the
     * corresponding <code>WebContinuation</code> objects.
     */
    protected Map idToWebCont = Collections.synchronizedMap(new HashMap());

    /**
     * Sorted set of <code>WebContinuation</code> instances, based on
     * their expiration time. This is used by the background thread to
     * invalidate continuations.
     */
    protected SortedSet expirations = Collections.synchronizedSortedSet(new TreeSet());

    private String instrumentableName;
    private ValueInstrument continuationsCount;
    private ValueInstrument forestSize;
    private ValueInstrument expirationsSize;
    private CounterInstrument continuationsCreated;
    private CounterInstrument continuationsInvalidated;

    private ServiceManager serviceManager;

    public ContinuationsManagerImpl() throws Exception {
        try {
            random = SecureRandom.getInstance("SHA1PRNG");
        } catch(java.security.NoSuchAlgorithmException nsae) {
            // Maybe we are on IBM's SDK
            random = SecureRandom.getInstance("IBMSecureRandom");
        }
        random.setSeed(System.currentTimeMillis());
        bytes = new byte[CONTINUATION_ID_LENGTH];

        continuationsCount = new ValueInstrument("count");
        forestSize = new ValueInstrument("forest-size");
        expirationsSize = new ValueInstrument("expirations-size");
        continuationsCreated = new CounterInstrument("creates");
        continuationsInvalidated = new CounterInstrument("invalidates");
    }

    /**
     * Get the command sink so that we can be notified of changes
     */
    public void service(final ServiceManager manager) throws ServiceException {
        this.serviceManager = manager;
    }

    public void configure(Configuration config) {
        this.defaultTimeToLive = config.getAttributeAsInteger("time-to-live", (3600 * 1000));

        final Configuration expireConf = config.getChild("expirations-check");
        final long initialDelay = expireConf.getChild("offset", true).getValueAsLong(180000);
        final long interval = expireConf.getChild("period", true).getValueAsLong(180000);
        try {
            final RunnableManager runnableManager = (RunnableManager)serviceManager.lookup(RunnableManager.ROLE);
            runnableManager.execute( new Runnable() {
                public void run()
                {
                    expireContinuations();
                }
            }, initialDelay, interval);
            serviceManager.release(runnableManager);
        } catch (Exception e) {
            getLogger().warn("Could not enqueue continuations expiration task. " +
                             "Continuations will not automatically expire.", e);
        }
    }

    public void setInstrumentableName(String instrumentableName) {
        this.instrumentableName = instrumentableName;
    }

    public String getInstrumentableName() {
        return instrumentableName;
    }

    public Instrument[] getInstruments() {
        return new Instrument[]{
            continuationsCount,
            continuationsCreated,
            continuationsInvalidated,
            forestSize
        };
    }

    public Instrumentable[] getChildInstrumentables() {
        return Instrumentable.EMPTY_INSTRUMENTABLE_ARRAY;
    }

    public WebContinuation createWebContinuation(Object kont,
                                                 WebContinuation parent,
                                                 int timeToLive,
                                                 ContinuationsDisposer disposer) {
        int ttl = (timeToLive == 0 ? defaultTimeToLive : timeToLive);

        WebContinuation wk = generateContinuation(kont, parent, ttl, disposer);
        wk.enableLogging(getLogger());

        if (parent == null) {
            forest.add(wk);
            forestSize.setValue(forest.size());
        } else {
            // REVISIT: This places only the "leaf" nodes in the expirations Sorted Set.
            // do we really want to do this?
            if (parent.getChildren().size() < 2) {
                expirations.remove(parent);
            }
        }

        expirations.add(wk);
        expirationsSize.setValue(expirations.size());

        // No need to add the WebContinuation in idToWebCont as it was
        // already done during its construction.

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("WK: Created continuation " + wk.getId());
        }

        return wk;
    }

    public WebContinuation lookupWebContinuation(String id) {
        // REVISIT: Is the following check needed to avoid threading issues:
        // return wk only if !(wk.hasExpired) ?
        return (WebContinuation) idToWebCont.get(id);
    }

    /**
     * Create <code>WebContinuation</code> and generate unique identifier
     * for it. The identifier is generated using a cryptographically strong
     * algorithm to prevent people to generate their own identifiers.
     *
     * <p>It has the side effect of interning the continuation object in
     * the <code>idToWebCont</code> hash table.
     *
     * @param kont an <code>Object</code> value representing continuation
     * @param parent value representing parent <code>WebContinuation</code>
     * @param ttl <code>WebContinuation</code> time to live
     * @param disposer <code>ContinuationsDisposer</code> instance to use for
     * cleanup of the continuation.
     * @return the generated <code>WebContinuation</code> with unique identifier
     */
    private WebContinuation generateContinuation(Object kont,
                                                 WebContinuation parent,
                                                 int ttl,
                                                 ContinuationsDisposer disposer) {

        char[] result = new char[bytes.length * 2];
        WebContinuation wk = null;

        while (true) {
            random.nextBytes(bytes);

            for (int i = 0; i < CONTINUATION_ID_LENGTH; i++) {
                byte ch = bytes[i];
                result[2 * i] = Character.forDigit(Math.abs(ch >> 4), 16);
                result[2 * i + 1] = Character.forDigit(Math.abs(ch & 0x0f), 16);
            }

            final String id = new String(result);
            synchronized (idToWebCont) {
                if (!idToWebCont.containsKey(id)) {
                    wk = new WebContinuation(id, kont, parent, ttl, disposer);
                    idToWebCont.put(id, wk);
                    continuationsCount.setValue(idToWebCont.size());
                    break;
                }
            }
        }

        continuationsCreated.increment();
        return wk;
    }

    public void invalidateWebContinuation(WebContinuation wk) {
        _detach(wk);
        _invalidate(wk);
    }

    private void _invalidate(WebContinuation wk) {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("WK: Manual expire of continuation " + wk.getId());
        }
        disposeContinuation(wk);
        expirations.remove(wk);
        expirationsSize.setValue(expirations.size());

        // Invalidate all the children continuations as well
        List children = wk.getChildren();
        int size = children.size();
        for (int i = 0; i < size; i++) {
            _invalidate((WebContinuation) children.get(i));
        }
    }

    /**
     * Detach this continuation from parent. This method removes
     * continuation from {@link #forest} set, or, if it has parent,
     * from parent's children collection.
     * @param wk Continuation to detach from parent.
     */
    private void _detach(WebContinuation wk) {
        WebContinuation parent = wk.getParentContinuation();
        if (parent == null) {
            forest.remove(wk);
            forestSize.setValue(forest.size());
        } else {
            List parentKids = parent.getChildren();
            parentKids.remove(wk);
        }
    }

    /**
     * Makes the continuation inaccessible for lookup, and triggers possible needed
     * cleanup code through the ContinuationsDisposer interface.
     *
     * @param wk the continuation to dispose.
     */
    private void disposeContinuation(WebContinuation wk) {
        idToWebCont.remove(wk.getId());
        continuationsCount.setValue(idToWebCont.size());
        wk.dispose();
        continuationsInvalidated.increment();
    }

    /**
     * Removes an expired leaf <code>WebContinuation</code> node
     * from its continuation tree, and recursively removes its
     * parent(s) if it they have expired and have no (other) children.
     *
     * @param wk <code>WebContinuation</code> node
     */
    private void removeContinuation(WebContinuation wk) {
        if (wk.getChildren().size() != 0) {
            return;
        }

        // remove access to this contination
        disposeContinuation(wk);
        _detach(wk);

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("WK: Deleted continuation: " + wk.getId());
        }

        // now check if parent needs to be removed.
        WebContinuation parent = wk.getParentContinuation();
        if (null != parent && parent.hasExpired()) {
            removeContinuation(parent);
        }
    }

    /**
     * Dump to Log file the current contents of
     * the expirations <code>SortedSet</code>
     */
    private void displayExpireSet() {
        StringBuffer wkSet = new StringBuffer("\nWK; Expire set size: " + expirations.size());
        Iterator i = expirations.iterator();
        while (i.hasNext()) {
            final WebContinuation wk = (WebContinuation) i.next();
            final long lat = wk.getLastAccessTime() + wk.getTimeToLive();
            wkSet.append("\nWK: ")
                    .append(wk.getId())
                    .append(" ExpireTime [");

            if (lat < System.currentTimeMillis()) {
                wkSet.append("Expired");
            } else {
                wkSet.append(lat);
            }
            wkSet.append("]");
        }

        getLogger().debug(wkSet.toString());
    }

    /**
     * Dump to Log file all <code>WebContinuation</code>s
     * in the system
     */
    public void displayAllContinuations() {
        final Iterator i = forest.iterator();
        while (i.hasNext()) {
            ((WebContinuation) i.next()).display();
        }
    }

    /**
     * Remove all continuations which have already expired.
     */
    protected void expireContinuations() {
        long now = 0;
        if (getLogger().isDebugEnabled()) {
            now = System.currentTimeMillis();

            /* Continuations before clean up:
            getLogger().debug("WK: Forest before cleanup: " + forest.size());
            displayAllContinuations();
            displayExpireSet();
            */
        }

        // Clean up expired continuations
        int count = 0;
        WebContinuation wk;
        Iterator i = expirations.iterator();
        while (i.hasNext() && ((wk = (WebContinuation) i.next()).hasExpired())) {
            i.remove();
            removeContinuation(wk);
            count++;
        }
        expirationsSize.setValue(expirations.size());

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("WK Cleaned up " + count + " continuations in " +
                              (System.currentTimeMillis() - now));

            /* Continuations after clean up:
            getLogger().debug("WK: Forest after cleanup: " + forest.size());
            displayAllContinuations();
            displayExpireSet();
            */
        }
    }
}
