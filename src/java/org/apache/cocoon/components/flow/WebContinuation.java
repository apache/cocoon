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

import java.util.ArrayList;
import java.util.List;

import org.apache.avalon.framework.logger.AbstractLogEnabled;

/**
 * Representation of continuations in a Web environment.
 *
 * <p>Because a user may click on the back button of the browser and
 * restart a saved computation in a continuation, each
 * <code>WebContinuation</code> becomes the parent of a subtree of
 * continuations.
 *
 * <p>If there is no parent <code>WebContinuation</code>, the created
 * continuation becomes the root of a tree of
 * <code>WebContinuation</code>s.
 *
 * @author <a href="mailto:ovidiu@cup.hp.com">Ovidiu Predescu</a>
 * @since March 19, 2002
 * @version CVS $Id: WebContinuation.java,v 1.6 2004/01/21 14:31:25 vgritsenko Exp $
 */
public class WebContinuation extends AbstractLogEnabled
                             implements Comparable {

    /**
     * The continuation this object represents.
     */
    protected Object continuation;

    /**
     * The parent <code>WebContinuation</code> from which processing
     * last started. If null, there is no parent continuation
     * associated, and this is the first one to be created in a
     * processing. In this case this <code>WebContinuation</code>
     * instance becomes the root of the tree maintained by the
     * <code>ContinuationsManager</code>.
     *
     * @see ContinuationsManager
     */
    protected WebContinuation parentContinuation;

    /**
     * The children continuations. These are continuations created by
     * resuming the processing from the point stored by
     * <code>continuation</code>.
     */
    protected List children = new ArrayList();

    /**
     * The continuation id used to represent this instance in Web pages.
     */
    protected String id;

    /**
     * A user definable object. This is present for convenience, to
     * store any information associated with this
     * <code>WebContinuation</code> a particular implementation might
     * need.
     */
    protected Object userObject;

    /**
     * When was this continuation accessed last time. Each time the
     * continuation is accessed, this time is set to the time of the
     * access.
     */
    protected long lastAccessTime;

    /**
     * Indicates how long does this continuation will live (in
     * seconds). The continuation will be removed once the current time
     * is bigger than <code>lastAccessTime + timeToLive</code>.
     */
    protected int timeToLive;

    /**
     * Holds the <code>ContinuationsDisposer</code> to call when this continuation
     * gets invalidated.
     */
    protected ContinuationsDisposer disposer;


    /**
     * Create a <code>WebContinuation</code> object. Saves the object in
     * the hash table of continuations maintained by
     * <code>manager</code> (this is done as a side effect of obtaining
     * and identifier from it).
     *
     * @param continuation an <code>Object</code> value
     * @param parentContinuation a <code>WebContinuation</code> value
     * @param timeToLive time this continuation should live
     * @param disposer a <code>ContinuationsDisposer</code> to call when this
     * continuation gets invalidated.
     */
    WebContinuation(String id,
                    Object continuation,
                    WebContinuation parentContinuation,
                    int timeToLive,
                    ContinuationsDisposer disposer) {
        this.id = id;
        this.continuation = continuation;
        this.parentContinuation = parentContinuation;
        this.updateLastAccessTime();
        this.timeToLive = timeToLive;
        this.disposer = disposer;

        if (parentContinuation != null) {
            this.parentContinuation.children.add(this);
        }
    }

    /**
     * Return the continuation object.
     *
     * @return an <code>Object</code> value
     */
    public Object getContinuation() {
        updateLastAccessTime();
        return continuation;
    }

    /**
     * Return the ancestor continuation situated <code>level</code>s
     * above the current continuation. The current instance is
     * considered to be at level 0. The parent continuation of the
     * receiving instance at level 1, its parent is at level 2 relative
     * to the receiving instance. If <code>level</code> is bigger than
     * the depth of the tree, the root of the tree is returned.
     *
     * @param level an <code>int</code> value
     * @return a <code>WebContinuation</code> value
     */
    public WebContinuation getContinuation(int level) {
        if (level <= 0) {
            updateLastAccessTime();
            return this;
        } else if (parentContinuation == null) {
            return this;
        } else {
            return parentContinuation.getContinuation(level - 1);
        }
    }

    /**
     * Return the parent <code>WebContinuation</code>. Equivalent with
     * <code>getContinuation(1)</code>.
     *
     * @return a <code>WebContinuation</code> value
     */
    public WebContinuation getParentContinuation() {
        return parentContinuation;
    }

    /**
     * Return the children <code>WebContinuation</code> which were
     * created as a result of resuming the processing from the current
     * <code>continuation</code>.
     *
     * @return a <code>List</code> value
     */
    public List getChildren() {
        return children;
    }

    /**
     * Returns the string identifier of this
     * <code>WebContinuation</code>.
     *
     * @return a <code>String</code> value
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the last time this
     * <code>WebContinuation</code> was accessed.
     *
     * @return a <code>long</code> value
     */
    public long getLastAccessTime() {
        return lastAccessTime;
    }

    /**
     * Returns the the timetolive for this
     * <code>WebContinuation</code>.
     *
     * @return a <code>long</code> value
     */
    public long getTimeToLive() {
        return this.timeToLive;
    }

    /**
     * Sets the user object associated with this instance.
     *
     * @param obj an <code>Object</code> value
     */
    public void setUserObject(Object obj) {
        this.userObject = obj;
    }

    /**
     * Obtains the user object associated with this instance.
     *
     * @return an <code>Object</code> value
     */
    public Object getUserObject() {
        return userObject;
    }

    /**
     * Obtains the <code>ContinuationsDisposer</code> to call when this continuation
     * is invalidated.
     *
     * @return a <code>ContinuationsDisposer</code> instance or null if there are
     * no specific clean-up actions required.
     */
    ContinuationsDisposer getDisposer() {
        return this.disposer;
    }

    /**
     * Returns the hash code of the associated identifier.
     *
     * @return an <code>int</code> value
     */
    public int hashCode() {
        return id.hashCode();
    }

    /**
     * True if the identifiers are the same, false otherwise.
     *
     * @param another an <code>Object</code> value
     * @return a <code>boolean</code> value
     */
    public boolean equals(Object another) {
        if (another instanceof WebContinuation) {
            return id.equals(((WebContinuation) another).id);
        }
        return false;
    }

    /**
     * Compares the expiration time of this instance with that of the
     * WebContinuation passed as argument.
     *
     * <p><b>Note:</b> this class has a natural ordering that is
     * inconsistent with <code>equals</code>.</p>.
     *
     * @param other an <code>Object</code> value, which should be a
     * <code>WebContinuation</code> instance
     * @return an <code>int</code> value
     */
    public int compareTo(Object other) {
        WebContinuation wk = (WebContinuation) other;
        return (int) ((lastAccessTime + timeToLive)
                - (wk.lastAccessTime + wk.timeToLive));
    }

    /**
     * Debugging method.
     *
     * <p>Assumes the receiving instance as the root of a tree and
     * displays the tree of continuations.
     */
    public void display() {
        getLogger().debug("\nWK: Tree" + display(0));
    }

    /**
     * Debugging method.
     *
     * <p>Displays the receiving instance as if it is at the
     * <code>indent</code> depth in the tree of continuations. Each
     * level is indented 2 spaces.
     *
     * @param depth an <code>int</code> value
     */
    protected String display(int depth) {
        StringBuffer tree = new StringBuffer("\n");
        for (int i = 0; i < depth; i++) {
            tree.append("  ");
        }

        tree.append("WK: WebContinuation ")
                .append(id)
                .append(" ExpireTime [");

        if ((lastAccessTime + timeToLive) < System.currentTimeMillis()) {
            tree.append("Expired");
        } else {
            tree.append(lastAccessTime + timeToLive);
        }

        tree.append("]");

        // REVISIT: is this needed for some reason?
        // System.out.print(spaces); System.out.println("WebContinuation " + id);

        int size = children.size();
        depth++;

        for (int i = 0; i < size; i++) {
            tree.append(((WebContinuation) children.get(i)).display(depth));
        }

        return tree.toString();
    }

    /**
     * Update the continuation in the
     */
    protected void updateLastAccessTime() {
        lastAccessTime = System.currentTimeMillis();
    }

    /**
     * Determines whether this continuation has expired
     *
     * @return a <code>boolean</code> value
     */
    public boolean hasExpired() {
        long currentTime = System.currentTimeMillis();
        long expireTime = this.getLastAccessTime() + this.timeToLive;

        return (currentTime > expireTime);
    }
}
