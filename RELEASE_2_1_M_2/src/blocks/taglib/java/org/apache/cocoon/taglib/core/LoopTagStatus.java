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

package org.apache.cocoon.taglib.core;

/**
 * <p>Provides an interface for objects representing the current status of
 * an iteration.  Cocoon taglibrary provides a mechanism for LoopTags to
 * return information about the current index of the iteration and
 * convenience methods to determine whether or not the current round is
 * either the first or last in the iteration.  It also lets authors
 * use the status object to obtain information about the iteration range,
 * step, and current object.</p>
 *
 * <p>Environments that require more status can extend this interface.</p>
 * 
 * This Interface is a copy from JSTL1.0
 * @see javax.servlet.jsp.jstl.core.LoopTagStatus
 *
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * @version CVS $Id: LoopTagStatus.java,v 1.2 2003/03/16 17:49:08 vgritsenko Exp $
 */
public interface LoopTagStatus {

    /**
     * Retrieves the current item in the iteration.  Behaves
     * idempotently; calling getCurrent() repeatedly should return the same
     * Object until the iteration is advanced.  (Specifically, calling
     * getCurrent() does <b>not</b> advance the iteration.)
     *
     * @return the current item as an object
     */
    public Object getCurrent();

    /**
     * Retrieves the index of the current round of the iteration.  If
     * iteration is being performed over a subset of an underlying
     * array, java.lang.Collection, or other type, the index returned
     * is absolute with respect to the underlying collection.  Indices
     * are 0-based.
     *
     * @return the 0-based index of the current round of the iteration
     */
    public int getIndex();

    /**
     * <p>Retrieves the "count" of the current round of the iteration.  The
     * count is a relative, 1-based sequence number identifying the
     * current "round" of iteration (in context with all rounds the
     * current iteration will perform).</p>
     *
     * <p>As an example, an iteration with begin = 5, end = 15, and step =
     * 5 produces the counts 1, 2, and 3 in that order.</p>
     *
     * @return the 1-based count of the current round of the iteration
     */
    public int getCount();

    /**
     * Returns information about whether the current round of the
     * iteration is the first one.  This current round may be the 'first'
     * even when getIndex() != 0, for 'index' refers to the absolute
     * index of the current 'item' in the context of its underlying
     * collection.  It is always that case that a true result from
     * isFirst() implies getCount() == 1.
     * 
     * @return <tt>true</tt> if the current round is the first in the
     * iteration, <tt>false</tt> otherwise.
     */
    public boolean isFirst();

    /**
     * Returns information about whether the current round of the
     * iteration is the last one.  As with isFirst(), subsetting is
     * taken into account.  isLast() doesn't necessarily refer to the
     * status of the underlying Iterator; it refers to whether or not
     * the current round will be the final round of iteration for the
     * tag associated with this LoopTagStatus.
     * 
     * @return <tt>true</tt> if the current round is the last in the
     * iteration, <tt>false</tt> otherwise.
     */
    public boolean isLast();

    /**
     * Returns the value of the 'begin' attribute for the associated tag,
     * or null if no 'begin' attribute was specified.
     *
     * @return the 'begin' value for the associated tag, or null
     * if no 'begin' attribute was specified
     */
    public Integer getBegin();

    /**
     * Returns the value of the 'end' attribute for the associated tag,
     * or null if no 'end' attribute was specified.
     *
     * @return the 'end' value for the associated tag, or null
     * if no 'end' attribute was specified
     */
    public Integer getEnd();

    /**
     * Returns the value of the 'step' attribute for the associated tag,
     * or null if no 'step' attribute was specified.
     *
     * @return the 'step' value for the associated tag, or null
     * if no 'step' attribute was specified
     */
    public Integer getStep();

}
