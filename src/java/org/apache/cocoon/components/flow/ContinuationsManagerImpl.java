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

*/
package org.apache.cocoon.components.flow;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.Composable;

import org.apache.avalon.framework.activity.Disposable;

import org.apache.avalon.cornerstone.services.scheduler.TimeScheduler;
import org.apache.avalon.cornerstone.services.scheduler.Target;
import org.apache.avalon.cornerstone.services.scheduler.TimeTriggerFactory;

import java.security.SecureRandom;
import java.util.*;

/**
 * The default implementation of {@link ContinuationsManager}.
 *
 * @author <a href="mailto:ovidiu@cup.hp.com">Ovidiu Predescu</a>
 * @author <a href="mailto:Michael.Melhem@managesoft.com">Michael Melhem</a>
 * @since March 19, 2002
 * @see ContinuationsManager
 */
public class ContinuationsManagerImpl
  extends AbstractLogEnabled
  implements ContinuationsManager, Component, Configurable, 
  ThreadSafe, Composable, Disposable, Target {

  static final int CONTINUATION_ID_LENGTH = 20;
  static final String EXPIRE_CONTINUATIONS="expire-continuations"; 


  protected SecureRandom random = null;
  protected byte[] bytes;

  protected TimeScheduler m_scheduler;
  protected ComponentManager m_manager;

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

  public ContinuationsManagerImpl()
    throws Exception
  {
    random = SecureRandom.getInstance("SHA1PRNG");
    random.setSeed(System.currentTimeMillis());
    bytes = new byte[CONTINUATION_ID_LENGTH];
  }

  public void configure(Configuration config)
  {
    defaultTimeToLive = config.getAttributeAsInteger("time-to-live", (3600 * 1000));
    Configuration expireConf = config.getChild("expirations-check");

    try {
      m_scheduler = (TimeScheduler)this.m_manager.lookup(TimeScheduler.ROLE);
      TimeTriggerFactory    triggerFac = new TimeTriggerFactory();

      m_scheduler.addTrigger(EXPIRE_CONTINUATIONS,
                             triggerFac.createTimeTrigger(expireConf),
                             this);
    }
    catch (Exception ex) {
      if (this.getLogger().isDebugEnabled()) {
        getLogger().debug("WK: Exception while configuring WKManager " + ex);
      }
    }
  }

  public WebContinuation createWebContinuation(Object kont,
                                               WebContinuation parentKont,
                                               int timeToLive)
  {
    int ttl = (timeToLive == 0 ? defaultTimeToLive : timeToLive);

    WebContinuation wk = new WebContinuation(kont, parentKont, this, ttl);
    wk.enableLogging(getLogger());

    if (parentKont == null)
      forrest.add(wk);

    // REVISIT: This Places only the "leaf" nodes in the expirations Sorted Set.
    // do we really want to do this?
    if (parentKont != null) {
        if (wk.getParentContinuation().getChildren().size() < 2) {
            expirations.remove(wk.getParentContinuation());
        }
    }

    expirations.add(wk);

    // No need to add the WebContinuation in idToWebCont as it was
    // already done during its construction.

    if (this.getLogger().isDebugEnabled()) {
      getLogger().debug("WK: Just Created New Continuation " + wk.getId());
    }

    return wk;
  }

  public void invalidateWebContinuation(WebContinuation wk)
  {
    WebContinuation parent = wk.getParentContinuation();
    if (parent == null)
        forrest.remove(wk);
    else {
      List parentKids = parent.getChildren();
      parentKids.remove(wk);
    }

    _invalidate(wk);
  }

  protected void _invalidate(WebContinuation wk)
  {
    if (this.getLogger().isDebugEnabled()) {
      getLogger().debug("WK: Manual Expire of Continuation " + wk.getId());
    }
    idToWebCont.remove(wk.getId());
    expirations.remove(wk);
    
    // Invalidate all the children continuations as well
    List children = wk.getChildren();
    int size = children.size();
    for (int i = 0; i < size; i++)
      _invalidate((WebContinuation)children.get(i));
  }

  public WebContinuation lookupWebContinuation(String id)
  {
    // REVISIT: Is the folliwing check needed to avoid threading issues:
    // return wk only if !(wk.hasExpired) ?
    return (WebContinuation)idToWebCont.get(id);
  }

  /**
   * Generate a unique identifier for a
   * <code>WebContinuation</code>. The identifier is generated using a
   * cryptographically strong algorithm to prevent people to generate
   * their own identifiers.
   *
   * <p>It has the side effect of interning the continuation object in
   * the <code>idToWebCont</code> hash table.
   *
   * @param wk a <code>WebContinuation</code> object for which the
   * identifier should be generated.
   * @return the <code>String</code> identifier of the
   * <code>WebContinuation</code>
   */
  public String generateContinuationId(WebContinuation wk)
  {
    char[] result = new char[bytes.length * 2];
    String continuationId = null;
    
    while (true) {
      random.nextBytes(bytes);
    
      for (int i = 0; i < CONTINUATION_ID_LENGTH; i++) {
        byte ch = bytes[i];      
        result[2 * i] = Character.forDigit(Math.abs(ch >> 4), 16);
        result[2 * i + 1] = Character.forDigit(Math.abs(ch & 0x0f), 16);
      }
      continuationId = new String(result);
      synchronized(idToWebCont) {
        if (!idToWebCont.containsKey(continuationId)) {
          idToWebCont.put(continuationId, wk);
          break;
        }
      }
    }

    return continuationId;
  }


  /**
   * Removes an expired leaf <code>WebContinuation</code> node
   * from its continuation tree, and recursively removes its
   * parent(s) if it they have expired and have no (other) children.
   * 
   * @param  a <code>WebContinuation</code> node 
   */
  public void removeContinuation(WebContinuation wk) {
    if (wk.getChildren().size() != 0) {
      return;
    }

    // remove access to this contination
    idToWebCont.remove(wk.getId());
	
    WebContinuation parent = wk.getParentContinuation();
    if (parent == null) {
      forrest.remove(wk);
    }
    else {
      List parentKids = parent.getChildren();
      parentKids.remove(wk);
    }

    if (this.getLogger().isDebugEnabled()) {
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
  public void displayExpireSet() {
    Iterator iter = expirations.iterator();
    StringBuffer wkSet = new StringBuffer("\nWK; Expire Set Size: "+ expirations.size());
    while (iter.hasNext()) {
      final WebContinuation wk = (WebContinuation)iter.next();
      final long lat = wk.getLastAccessTime() + wk.getTimeToLive();
      wkSet.append("\nWK: ")
           .append(wk.getId())
           .append(" ExpireTime [");

      if (lat <  System.currentTimeMillis()) {
        wkSet.append("Expired");
      }
      else {
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
  public void displayAllContinuations()
  {
    Iterator iter = forrest.iterator();
    while (iter.hasNext()) {
      ((WebContinuation)iter.next()).display();
    }
  }
 
  public void compose(ComponentManager manager) throws ComponentException
  {
      this.m_manager = manager;
  }

  /**
   * Remove all continuations which have 
   * already expired
   */
  public void expireContinuations() {
    // log state before continuations clean up
    if (this.getLogger().isDebugEnabled()) {
      getLogger().debug("WK: Forrest size: " + forrest.size());  
      displayAllContinuations();
      displayExpireSet();
    }

    // clean up
    if (this.getLogger().isDebugEnabled()) {
      getLogger().debug("WK CurrentSytemTime[" + System.currentTimeMillis() + 
                        "]: Cleaning up expired Continuations....");
    }
    WebContinuation wk;
    Iterator iter = expirations.iterator();
    while (iter.hasNext() && ((wk=(WebContinuation)iter.next()).hasExpired())) {
      iter.remove();
      this.removeContinuation(wk);
    }

    // log state after continuations clean up
    if (this.getLogger().isDebugEnabled()) {
      getLogger().debug("WK: Forrest size: " + forrest.size());  
      displayAllContinuations();
      displayExpireSet();
    }
  } 

  /**
   * Handle cornerstone triggers 
   *
   * @param trigger an <code>String</code> value
   */
   public void targetTriggered(String trigger)   
   {
     // Expire continuations whenever this
     // trigger goes off.
     if (trigger.equals(EXPIRE_CONTINUATIONS)) {
       if (this.getLogger().isDebugEnabled()) {
         getLogger().debug("WK: ExpireContinuations clean up triggered:");
       }
        this.expireContinuations();
     }
   }

  /**
   * dispose of this component
   */
  public void dispose() {
    this.m_scheduler.removeTrigger(EXPIRE_CONTINUATIONS);
    this.m_manager.release((Component)m_scheduler);
    this.m_manager = null;
  }
}
