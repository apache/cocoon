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

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.excalibur.event.Queue;
import org.apache.excalibur.event.Sink;
import org.apache.excalibur.event.command.RepeatedCommand;

/**
 * The default implementation of {@link ContinuationsManager}.
 *
 * @author <a href="mailto:ovidiu@cup.hp.com">Ovidiu Predescu</a>
 * @author <a href="mailto:Michael.Melhem@managesoft.com">Michael Melhem</a>
 * @since March 19, 2002
 * @see ContinuationsManager
 * @version CVS $Id: ContinuationsManagerImpl.java,v 1.13 2004/03/08 13:57:36 cziegeler Exp $
 * 
 * @avalon.component
 * @avalon.service type=ContinuationsManager
 * @x-avalon.lifestyle type=singleton
 * @x-avalon.info name=continuations-manager
 */
public class ContinuationsManagerImpl
        extends AbstractLogEnabled
        implements ContinuationsManager, Serviceable, Configurable {

    static final int CONTINUATION_ID_LENGTH = 20;
    static final String EXPIRE_CONTINUATIONS = "expire-continuations";

    protected SecureRandom random = null;
    protected byte[] bytes;

    protected Sink m_commandSink;

    /**
     * How long does a continuation exist in memory since the last
     * access? The time is in miliseconds, and the default is 1 hour.
     */
    protected int defaultTimeToLive;

    /**
     * Maintains the forrest of <code>WebContinuation</code> trees.
     */
    protected Set forrest = Collections.synchronizedSet(new HashSet());

    /**
     * Association between <code>WebContinuation</code> ids and the
     * corresponding <code>WebContinuation</code> object.
     */
    protected Map idToWebCont = Collections.synchronizedMap(new HashMap());

    /**
     * Sorted set of <code>WebContinuation</code> instances, based on
     * their expiration time. This is used by the background thread to
     * invalidate continuations.
     */
    protected SortedSet expirations = Collections.synchronizedSortedSet(new TreeSet());


    public ContinuationsManagerImpl() throws Exception {
        try {
            random = SecureRandom.getInstance("SHA1PRNG");
        }
        catch(java.security.NoSuchAlgorithmException nsae) {
            // maybe we are on IBM's SDK
            random = SecureRandom.getInstance("IBMSecureRandom");
        }
        random.setSeed(System.currentTimeMillis());
        bytes = new byte[CONTINUATION_ID_LENGTH];
    }

    public void service(ServiceManager manager) throws ServiceException {
        m_commandSink = (Sink) manager.lookup(Queue.ROLE);
    }

    public void configure(Configuration config) {
        defaultTimeToLive = config.getAttributeAsInteger("time-to-live", (3600 * 1000));
        Configuration expireConf = config.getChild("expirations-check");

        try {
            ContinuationInterrupt interrupt = new ContinuationInterrupt(expireConf);
            m_commandSink.enqueue(interrupt);
        } catch (Exception ex) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("WK: Exception while configuring WKManager " + ex);
            }
        }
    }

    public WebContinuation createWebContinuation(Object kont,
                                                 WebContinuation parent,
                                                 int timeToLive, 
                                                 ContinuationsDisposer disposer) {
        int ttl = (timeToLive == 0 ? defaultTimeToLive : timeToLive);

        WebContinuation wk = generateContinuation(kont, parent, ttl, disposer);
        wk.enableLogging(getLogger());

        if (parent == null) {
            forrest.add(wk);
        }

        // REVISIT: This Places only the "leaf" nodes in the expirations Sorted Set.
        // do we really want to do this?
        if (parent != null) {
            if (wk.getParentContinuation().getChildren().size() < 2) {
                expirations.remove(wk.getParentContinuation());
            }
        }

        expirations.add(wk);

        // No need to add the WebContinuation in idToWebCont as it was
        // already done during its construction.

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("WK: Just Created New Continuation " + wk.getId());
        }

        return wk;
    }

    public void invalidateWebContinuation(WebContinuation wk) {
        WebContinuation parent = wk.getParentContinuation();
        if (parent == null) {
            forrest.remove(wk);
        } else {
            List parentKids = parent.getChildren();
            parentKids.remove(wk);
        }

        _invalidate(wk);
    }

    private void _invalidate(WebContinuation wk) {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("WK: Manual Expire of Continuation " + wk.getId());
        }
        disposeContinuation(wk);
        expirations.remove(wk);

        // Invalidate all the children continuations as well
        List children = wk.getChildren();
        int size = children.size();
        for (int i = 0; i < size; i++) {
            _invalidate((WebContinuation) children.get(i));
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
           
        // Call specific possible implementation-specific clean-up on this continuation.
        ContinuationsDisposer disposer = wk.getDisposer();
        if (disposer != null) {
          disposer.disposeContinuation(wk);
        }
    }

    public WebContinuation lookupWebContinuation(String id) {
        // REVISIT: Is the folliwing check needed to avoid threading issues:
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
    private WebContinuation generateContinuation( Object kont, 
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

            String id = new String(result);
            synchronized (idToWebCont) {
                if (!idToWebCont.containsKey(id)) {
                    wk = new WebContinuation(id, kont, parent, ttl, disposer);
                    idToWebCont.put(id, wk);
                    break;
                }
            }
        }

        return wk;
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

        WebContinuation parent = wk.getParentContinuation();
        if (parent == null) {
            forrest.remove(wk);
        } else {
            List parentKids = parent.getChildren();
            parentKids.remove(wk);
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("WK: deleted this WK: " + wk.getId());
        }

        // now check if parent needs to be removed.
        if (null != parent && parent.hasExpired()) {
            removeContinuation(parent);
        }
    }

    /**
     * Dump to Log file the current contents of
     * the expirations <code>SortedSet</code>
     */
    private void displayExpireSet() {
        Iterator iter = expirations.iterator();
        StringBuffer wkSet = new StringBuffer("\nWK; Expire Set Size: " + expirations.size());
        while (iter.hasNext()) {
            final WebContinuation wk = (WebContinuation) iter.next();
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
        Iterator iter = forrest.iterator();
        while (iter.hasNext()) {
            ((WebContinuation) iter.next()).display();
        }
    }

    /**
     * Remove all continuations which have
     * already expired
     */
    private void expireContinuations() {
        // log state before continuations clean up
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("WK: Forrest size: " + forrest.size());
            displayAllContinuations();
            displayExpireSet();
        }

        // clean up
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("WK CurrentSystemTime[" + System.currentTimeMillis() +
                              "]: Cleaning up expired Continuations....");
        }
        WebContinuation wk;
        Iterator iter = expirations.iterator();
        while (iter.hasNext() && ((wk = (WebContinuation) iter.next()).hasExpired())) {
            iter.remove();
            this.removeContinuation(wk);
        }

        // log state after continuations clean up
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("WK: Forrest size: " + forrest.size());
            displayAllContinuations();
            displayExpireSet();
        }
    }

    final class ContinuationInterrupt implements RepeatedCommand {
        private final long m_interval;
        private final long m_initialDelay;

        /**
         * @param expireConf
         */
        public ContinuationInterrupt(Configuration expireConf) {
            // only periodic time triggers are supported
            m_initialDelay =
                    expireConf.getChild("offset", true).getValueAsLong(100);
            m_interval =
                    expireConf.getChild("period", true).getValueAsLong(100);
        }

        /**
         * Repeat forever
         */
        public int getNumberOfRepeats() {
            return -1;
        }

        /**
         * Get the number of millis to wait between invocations
         */
        public long getRepeatInterval() {
            return m_interval;
        }

        /**
         * Get the number of millis to wait for the first invocation
         */
        public long getDelayInterval() {
            return m_initialDelay;
        }

        /**
         * expire any continuations that need expiring.
         */
        public void execute() throws Exception {
            expireContinuations();
        }
    }

}
