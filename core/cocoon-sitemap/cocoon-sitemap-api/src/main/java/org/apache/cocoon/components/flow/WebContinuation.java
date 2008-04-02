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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.iterators.IteratorEnumeration;
import org.apache.commons.lang.StringUtils;

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
 * @since March 19, 2002
 * @version $Id$
 */
public class WebContinuation implements Comparable, Cloneable {

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
     * Interpreter id that this continuation is bound to
     */
    protected String interpreterId;

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
     * The attributes of this continuation
     */
    private Map attributes;

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
                    String interpreterId,
                    ContinuationsDisposer disposer) {
        this.id = id;
        this.continuation = continuation;
        this.parentContinuation = parentContinuation;
        this.updateLastAccessTime();
        this.timeToLive = timeToLive;
        this.interpreterId = interpreterId;
        this.disposer = disposer;

        if (parentContinuation != null) {
            this.parentContinuation.children.add(this);
        }
    }

    /**
     * Get an attribute of this continuation
     * 
     * @param name the attribute name.
     */
    public Object getAttribute(String name) {
        if (this.attributes == null) {
            return null;
        }
        
        return this.attributes.get(name);
    }
    
    /**
     * Set an attribute of this continuation
     * 
     * @param name the attribute name
     * @param value its value
     */
    public void setAttribute(String name, Object value) {
        if (this.attributes == null) {
            this.attributes = Collections.synchronizedMap(new HashMap());
        }
        
        this.attributes.put(name, value);
    }
    
    /**
     * Remove an attribute of this continuation
     * 
     * @param name the attribute name
     */
    public void removeAttribute(String name) {
        if (this.attributes == null)
            return;
        
        this.attributes.remove(name);
    }
    
    /**
     * Enumerate the attributes of this continuation.
     * 
     * @return an enumeration of strings
     */
    public Enumeration getAttributeNames() {
        if (this.attributes == null)
            return new IteratorEnumeration();
        
        ArrayList keys = new ArrayList(this.attributes.keySet());
        return new IteratorEnumeration(keys.iterator());
    }

    /**
     * Return the continuation object.
     *
     * @return an <code>Object</code> value
     */
    public Object getContinuation() {
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
     * Returns the string identifier of the interpreter to which
     * this <code>WebContinuation</code> is bound.
     *
     * @return a <code>String</code> value
     */
    public String getInterpreterId() {
        return interpreterId;
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

    /**
     * Dispose this continuation. Should be called on invalidation.
     */
    public void dispose() {
        // Call possible implementation-specific clean-up on this continuation.
        if (this.disposer != null) {
            this.disposer.disposeContinuation(this);
        }
        // Remove continuation object - will also serve as "disposed" flag
        this.continuation = null;
    }

    /**
     * Return true if this continuation was disposed of
     */
    public boolean disposed() {
        return this.continuation == null;
    }
    
    public boolean interpreterMatches( String interpreterId ) {
        return StringUtils.equals(this.interpreterId, interpreterId);
    }

    public void detachFromParent() {
        if (getParentContinuation() != null) {
            getParentContinuation().getChildren().remove(this);
        }
    }

    /**
     * Creates a clone of this WebContinuation without trying to clone the actual continuation, the
     * user object or the disposer.
     * 
     * TODO: Check continuation, user object, disposer for implementing {@link Cloneable} or
     *       {@link java.io.Serializable}.
     */
    public Object clone() {
        
        WebContinuation clone = new WebContinuation(id, continuation, null, timeToLive, interpreterId, disposer);
        // reset last access time
        clone.lastAccessTime = this.lastAccessTime;
        // recreate hierarchy recursively
        for (Iterator iter = this.children.iterator(); iter.hasNext();) {
            WebContinuation child = (WebContinuation) iter.next();
            WebContinuation childClone = (WebContinuation) child.clone();
            // relationships must be fixed manually
            childClone.parentContinuation = clone;
            clone.children.add(childClone);
        }
        return clone;
    }

    /**
     * Debugging method.
     *
     * <p>Assumes the receiving instance as the root of a tree and
     * displays the tree of continuations.
     */
    public String toString() {
        return "\nWK: Tree" + display(0);
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

        if (hasExpired()) {
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

}
