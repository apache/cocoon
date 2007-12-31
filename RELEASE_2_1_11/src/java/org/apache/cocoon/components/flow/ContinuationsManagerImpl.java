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
package org.apache.cocoon.components.flow;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.components.thread.RunnableManager;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;

import org.apache.excalibur.instrument.CounterInstrument;
import org.apache.excalibur.instrument.Instrument;
import org.apache.excalibur.instrument.Instrumentable;
import org.apache.excalibur.instrument.ValueInstrument;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

/**
 * The default implementation of {@link ContinuationsManager}. <br/>There are
 * two modes of work: <br/>
 * <ul>
 * <li><b>standard mode </b>- continuations are stored in single holder. No
 * security is applied to continuation lookup. Anyone can invoke a continuation
 * only knowing the ID. Set "session-bound-continuations" configuration option
 * to false to activate this mode.</li>
 * <li><b>secure mode </b>- each session has it's own continuations holder. A
 * continuation is only valid for the same session it was created for. Session
 * invalidation causes all bound continuations to be invalidated as well. Use
 * this setting for web applications. Set "session-bound-continuations"
 * configuration option to true to activate this mode.</li>
 * </ul>
 * 
 * @author <a href="mailto:ovidiu@cup.hp.com">Ovidiu Predescu </a>
 * @author <a href="mailto:Michael.Melhem@managesoft.com">Michael Melhem </a>
 * @since March 19, 2002
 * @see ContinuationsManager
 * @version CVS $Id$
 */
public class ContinuationsManagerImpl
        extends AbstractLogEnabled
        implements ContinuationsManager, Component, Configurable,
                   ThreadSafe, Instrumentable, Serviceable, Contextualizable {
    
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
     * Main continuations holder. Used unless continuations are stored in user
     * session.
     */
    protected WebContinuationsHolder continuationsHolder;
    
    /**
     * Sorted set of <code>WebContinuation</code> instances, based on
     * their expiration time. This is used by the background thread to
     * invalidate continuations.
     */
    protected SortedSet expirations = Collections.synchronizedSortedSet(new TreeSet());

    protected String instrumentableName;
    protected ValueInstrument continuationsCount;
    protected int continuationsCounter;
    protected ValueInstrument forestSize;
    protected ValueInstrument expirationsSize;
    protected CounterInstrument continuationsCreated;
    protected CounterInstrument continuationsInvalidated;
    protected boolean isContinuationSharingBugCompatible;
    protected boolean bindContinuationsToSession;

    protected ServiceManager serviceManager;
    protected Context context;

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
        continuationsCounter = 0;
        forestSize = new ValueInstrument("forest-size");
        expirationsSize = new ValueInstrument("expirations-size");
        continuationsCreated = new CounterInstrument("creates");
        continuationsInvalidated = new CounterInstrument("invalidates");
    }

    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(final ServiceManager manager) throws ServiceException {
        this.serviceManager = manager;
    }

    /**
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration config) {
        this.defaultTimeToLive = config.getAttributeAsInteger("time-to-live", (3600 * 1000));
        this.isContinuationSharingBugCompatible = config.getAttributeAsBoolean("continuation-sharing-bug-compatible", false);
        this.bindContinuationsToSession = config.getAttributeAsBoolean( "session-bound-continuations", false );
        
        // create a global ContinuationsHolder if this the "session-bound-continuations" parameter is set to false
        if(!this.bindContinuationsToSession) {
            this.continuationsHolder = new WebContinuationsHolder();
        }        
        
        // create a thread that invalidates the continuations
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

    /**
     * @see org.apache.excalibur.instrument.Instrumentable#setInstrumentableName(java.lang.String)
     */
    public void setInstrumentableName(String instrumentableName) {
        this.instrumentableName = instrumentableName;
    }

    /**
     * @see org.apache.excalibur.instrument.Instrumentable#getInstrumentableName()
     */
    public String getInstrumentableName() {
        return instrumentableName;
    }

    /**
     * @see org.apache.excalibur.instrument.Instrumentable#getInstruments()
     */
    public Instrument[] getInstruments() {
        return new Instrument[]{
            continuationsCount,
            continuationsCreated,
            continuationsInvalidated,
            forestSize
        };
    }

    /**
     * @see org.apache.excalibur.instrument.Instrumentable#getChildInstrumentables()
     */
    public Instrumentable[] getChildInstrumentables() {
        return Instrumentable.EMPTY_INSTRUMENTABLE_ARRAY;
    }

    /**
     * @see org.apache.cocoon.components.flow.ContinuationsManager#createWebContinuation(java.lang.Object, org.apache.cocoon.components.flow.WebContinuation, int, java.lang.String, org.apache.cocoon.components.flow.ContinuationsDisposer)
     */
    public WebContinuation createWebContinuation(Object kont,
                                                 WebContinuation parent,
                                                 int timeToLive,
                                                 String interpreterId, 
                                                 ContinuationsDisposer disposer) {
        int ttl = (timeToLive == 0 ? defaultTimeToLive : timeToLive);

        WebContinuation wk = generateContinuation(kont, parent, ttl, interpreterId, disposer);
        wk.enableLogging(getLogger());

        if (parent == null) {
            forest.add(wk);
            forestSize.setValue(forest.size());
        } else {
            handleParentContinuationExpiration(parent);
        }

        handleLeafContinuationExpiration(wk);

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("WK: Created continuation " + wk.getId());
        }

        return wk;
    }

    /**
     * When a new continuation is created in @link #createWebContinuation(Object, WebContinuation, int, String, ContinuationsDisposer),
     * it is registered in the expiration set in order to be evaluated by the invalidation mechanism.
     */
    protected void handleLeafContinuationExpiration(WebContinuation wk) {
        expirations.add(wk);
        expirationsSize.setValue(expirations.size());
    }

    /**
     * When a new continuation is created in @link #createWebContinuation(Object, WebContinuation, int, String, ContinuationsDisposer),
     * its parent continuation is removed from the expiration set. This way only leaf continuations are part of
     * the expiration set.
     */
    protected void handleParentContinuationExpiration(WebContinuation parent) {
        if (parent.getChildren().size() < 2) {
            expirations.remove(parent);
        }
    }
    

    /**
     * @see org.apache.cocoon.components.flow.ContinuationsManager#lookupWebContinuation(java.lang.String, java.lang.String)
     */
    public WebContinuation lookupWebContinuation(String id, String interpreterId) {
        // REVISIT: Is the following check needed to avoid threading issues:
        // return wk only if !(wk.hasExpired) ?
        WebContinuationsHolder continuationsHolder = lookupWebContinuationsHolder(false);
        if (continuationsHolder == null) {
            return null;
        }
        
        WebContinuation kont = continuationsHolder.get(id);
        if(kont != null) {
            boolean interpreterMatches = kont.interpreterMatches(interpreterId);
            if (!interpreterMatches && getLogger().isWarnEnabled()) {
                getLogger().warn("WK: Continuation (" + kont.getId() 
                                 + ") lookup for wrong interpreter. Bound to: " 
                                 + kont.getInterpreterId() + ", looked up for: " 
                                 + interpreterId);
            }
            return interpreterMatches || isContinuationSharingBugCompatible ? kont : null;
        }
        return null;
    }

    /**
     * Create <code>WebContinuation</code> and generate unique identifier
     * for it. The identifier is generated using a cryptographically strong
     * algorithm to prevent people to generate their own identifiers.
     *
     * @param kont an <code>Object</code> value representing continuation
     * @param parent value representing parent <code>WebContinuation</code>
     * @param ttl <code>WebContinuation</code> time to live
     * @param interpreterId id of interpreter invoking continuation creation
     * @param disposer <code>ContinuationsDisposer</code> instance to use for
     * cleanup of the continuation.
     * @return the generated <code>WebContinuation</code> with unique identifier
     */
    protected WebContinuation generateContinuation(Object kont,
                                                 WebContinuation parent,
                                                 int ttl,
                                                 String interpreterId,
                                                 ContinuationsDisposer disposer) {

        char[] result = new char[bytes.length * 2];
        WebContinuation wk = null;
        WebContinuationsHolder continuationsHolder = lookupWebContinuationsHolder(true);
        while (true) {
            random.nextBytes(bytes);

            for (int i = 0; i < CONTINUATION_ID_LENGTH; i++) {
                byte ch = bytes[i];
                result[2 * i] = Character.forDigit(Math.abs(ch >> 4), 16);
                result[2 * i + 1] = Character.forDigit(Math.abs(ch & 0x0f), 16);
            }

            final String id = new String(result);
            synchronized (continuationsHolder) {
                if (!continuationsHolder.contains(id)) {
                    if (this.bindContinuationsToSession) {
                        wk = new HolderAwareWebContinuation(id, kont, parent,
                                ttl, interpreterId, disposer,
                                continuationsHolder);
                    }
                    else {
                        wk = new WebContinuation(id, kont, parent, ttl,
                                interpreterId, disposer);
                    }
                    continuationsHolder.addContinuation(wk);
                    synchronized (continuationsCount) {
                        continuationsCounter++;
                        continuationsCount.setValue(continuationsCounter);
                    }
                    break;
                }
            }
        }

        continuationsCreated.increment();
        return wk;
    }

    /**
     * @see org.apache.cocoon.components.flow.ContinuationsManager#invalidateWebContinuation(org.apache.cocoon.components.flow.WebContinuation)
     */
    public void invalidateWebContinuation(WebContinuation wk) {
        WebContinuationsHolder continuationsHolder = lookupWebContinuationsHolder(false);
        if (!continuationsHolder.contains(wk)) {
            //TODO this looks like a security breach - should we throw?
            return;
        }
        _detach(wk);
        _invalidate(continuationsHolder, wk);
    }

    protected void _invalidate(WebContinuationsHolder continuationsHolder, WebContinuation wk) {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("WK: Manual expire of continuation " + wk.getId());
        }
        disposeContinuation(continuationsHolder, wk);
        expirations.remove(wk);
        expirationsSize.setValue(expirations.size());

        // Invalidate all the children continuations as well
        List children = wk.getChildren();
        int size = children.size();
        for (int i = 0; i < size; i++) {
            _invalidate(continuationsHolder, (WebContinuation) children.get(i));
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
        } else 
            wk.detachFromParent();
    }

    /**
     * Makes the continuation inaccessible for lookup, and triggers possible needed
     * cleanup code through the ContinuationsDisposer interface.
     * @param continuationsHolder
     *
     * @param wk the continuation to dispose.
     */
    protected void disposeContinuation(WebContinuationsHolder continuationsHolder, WebContinuation wk) {
        continuationsHolder.removeContinuation(wk);
        synchronized( continuationsCount ) {
            continuationsCounter--;
            continuationsCount.setValue(continuationsCounter);
        }
        wk.dispose();
        continuationsInvalidated.increment();
    }

    /**
     * Removes an expired leaf <code>WebContinuation</code> node
     * from its continuation tree, and recursively removes its
     * parent(s) if it they have expired and have no (other) children.
     * @param continuationsHolder
     *
     * @param wk <code>WebContinuation</code> node
     */
    protected void removeContinuation(WebContinuationsHolder continuationsHolder, WebContinuation wk) {
        if (wk.getChildren().size() != 0) {
            return;
        }

        // remove access to this contination
        disposeContinuation(continuationsHolder, wk);
        _detach(wk);

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("WK: Deleted continuation: " + wk.getId());
        }

        // now check if parent needs to be removed.
        WebContinuation parent = wk.getParentContinuation();
        if (null != parent && parent.hasExpired()) {
            //parent must have the same continuations holder, lookup not needed
            removeContinuation(continuationsHolder, parent);
        }
    }

    /**
     * Dump to Log file the current contents of
     * the expirations <code>SortedSet</code>
     */
    protected void displayExpireSet() {
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

            /* Continuations before clean up: */
            getLogger().debug("WK: Forest before cleanup: " + forest.size());
            displayAllContinuations();
            displayExpireSet();

        }

        // Clean up expired continuations
        int count = 0;
        WebContinuation wk;
        Iterator i = expirations.iterator();
        while(i.hasNext() && ((wk = (WebContinuation) i.next()).hasExpired())) {
            i.remove();
            WebContinuationsHolder continuationsHolder = null;
            if(wk instanceof HolderAwareWebContinuation) {
                continuationsHolder = ((HolderAwareWebContinuation) wk).getContinuationsHolder();
            }
            else {
                continuationsHolder = this.continuationsHolder;
            }
            removeContinuation(continuationsHolder, wk);
            count++;
        }
        expirationsSize.setValue(expirations.size());

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("WK Cleaned up " + count + " continuations in " +
                              (System.currentTimeMillis() - now) + " ms");

            /* Continuations after clean up: */
//            getLogger().debug("WK: Forest after cleanup: " + forest.size());
//            displayAllContinuations();
//            displayExpireSet();

        }
    }

    /**
     * Method used by WebContinuationsHolder to notify the continuations manager
     * about session invalidation. Invalidates all continuations held by passed
     * continuationsHolder.
     */
    protected void invalidateContinuations(
            WebContinuationsHolder continuationsHolder) {
        // TODO: this avoids ConcurrentModificationException, still this is not
        // the best solution and should be changed
        Object[] continuationIds = continuationsHolder.getContinuationIds()
                .toArray();
        
        for (int i = 0; i < continuationIds.length; i++) {
            WebContinuation wk = continuationsHolder.get(continuationIds[i]);
            if (wk != null) {
                _detach(wk);
                _invalidate(continuationsHolder, wk);
            }
        }
    }

    /**
     * Lookup a proper web continuations holder. 
     * @param createNew
     *            should the manager create a continuations holder in session
     *            when none found?
     */
    public WebContinuationsHolder lookupWebContinuationsHolder(boolean createNew) {
        //there is only one holder if continuations are not bound to session
        if (!this.bindContinuationsToSession)
            return this.continuationsHolder;
        
        //if continuations bound to session lookup a proper holder in the session
        Map objectModel = ContextHelper.getObjectModel(this.context);
        Request request = ObjectModelHelper.getRequest(objectModel);

        if (!createNew && request.getSession(false) == null)
            return null;

        Session session = request.getSession(true);
        WebContinuationsHolder holder = 
            (WebContinuationsHolder) session.getAttribute(
                    WebContinuationsHolder.CONTINUATIONS_HOLDER);
        if (!createNew)
            return holder;

        if (holder != null)
            return holder;

        holder = new WebContinuationsHolder();
        session.setAttribute(WebContinuationsHolder.CONTINUATIONS_HOLDER,
                holder);
        return holder;
    }

    /**
     * A holder for WebContinuations. When bound to session notifies the
     * continuations manager of session invalidation.
     */
    protected class WebContinuationsHolder implements HttpSessionBindingListener {
        private final static String CONTINUATIONS_HOLDER = "o.a.c.c.f.SCMI.WebContinuationsHolder";

        private Map holder = Collections.synchronizedMap(new HashMap());

        public WebContinuation get(Object id) {
            return (WebContinuation) this.holder.get(id);
        }

        public void addContinuation(WebContinuation wk) {
            this.holder.put(wk.getId(), wk);
        }

        public void removeContinuation(WebContinuation wk) {
            this.holder.remove(wk.getId());
        }

        public Set getContinuationIds() {
            return holder.keySet();
        }
        
        public boolean contains(String continuationId) {
            return this.holder.containsKey(continuationId);
        }
        
        public boolean contains(WebContinuation wk) {
            return contains(wk.getId());
        }

        public void valueBound(HttpSessionBindingEvent event) {
        }

        public void valueUnbound(HttpSessionBindingEvent event) {
            invalidateContinuations(this);
        }
    }

    /**
     * WebContinuation extension that holds also the information about the
     * holder. This information is needed to cleanup a proper holder after
     * continuation's expiration time.
     */
    protected static class HolderAwareWebContinuation extends WebContinuation {
        private WebContinuationsHolder continuationsHolder;

        public HolderAwareWebContinuation(String id, Object continuation,
                WebContinuation parentContinuation, int timeToLive,
                String interpreterId, ContinuationsDisposer disposer,
                WebContinuationsHolder continuationsHolder) {
            super(id, continuation, parentContinuation, timeToLive,
                    interpreterId, disposer);
            this.continuationsHolder = continuationsHolder;
        }

        public WebContinuationsHolder getContinuationsHolder() {
            return continuationsHolder;
        }

        //retain comparation logic from parent
        public int compareTo(Object other) {
            return super.compareTo(other);
        }
    }

    /**
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context context) throws ContextException {
        this.context = context;        
    }

    /**
     * Get a list of all web continuations (data only)
     */
    public List getWebContinuationsDataBeanList() {
        List beanList = new ArrayList();
        for(Iterator it = this.forest.iterator(); it.hasNext();) {
            beanList.add(new WebContinuationDataBean((WebContinuation) it.next()));
        }
        return beanList;
    }

}
