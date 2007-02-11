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
package org.apache.cocoon.ant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.avalon.framework.logger.AbstractLogEnabled;

import org.apache.cocoon.util.NetUtils;

/**
 *   A simple Cocoon crawler
 *
 * @author    huber@apache.org
 * @version CVS $Id: CocoonCrawling.java,v 1.2 2003/03/16 18:03:54 vgritsenko Exp $
 */
public class CocoonCrawling extends AbstractLogEnabled {
    private Set visitedAlready;
    private List stillNotVisited;
    private String root;


    /**
     * Constructor for the CocoonCrawling object
     */
    public CocoonCrawling() {
        visitedAlready = Collections.synchronizedSet(new HashSet());
        stillNotVisited = Collections.synchronizedList(new ArrayList());
    }


    /**
     *   Sets the root attribute of the CocoonCrawling object
     *
     * @param  uriType  The new root value
     */
    public void setRoot(UriType uriType) {
        this.root = uriType.getPath();
        add(uriType);
    }


    /**
     *   Description of the Method
     *
     * @param  uriType  Description of Parameter
     */
    public void add(UriType uriType) {
        if (root != null) {
            String normalizedUriType = NetUtils.normalize(uriType.getUri());
            if (!normalizedUriType.startsWith(root)) {
                getLogger().warn("Uri " + String.valueOf(normalizedUriType) + " does not match root " + String.valueOf(root));
                return;
            }
        }
        if (!visitedAlready.contains(uriType)) {
            if (!stillNotVisited.contains(uriType)) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Add uri " + String.valueOf(uriType.getUri()) + " for visiting");
                }
                stillNotVisited.add(uriType);
            }
        }
    }


    /**
     *   Description of the Method
     *
     * @return    Description of the Returned Value
     */
    public Iterator iterator() {
        return new CocoonCrawlingIterator(visitedAlready, stillNotVisited);
    }


    /**
     *   Description of the Method
     *
     * @return    Description of the Returned Value
     */
    public Iterator visitedAlreadyIterator() {
        return visitedAlready.iterator();
    }


    /**
     *   An Iterator iterating over URIs which are not visited already,
     *   visited URIs are moved immediatly to a set of visited URIs
     *
     * @author    huber@apache.org
     */
    public static class CocoonCrawlingIterator implements Iterator {
        /**
         * List of URIs waiting to get visited
         */
        private List stillNotVisited;
        /**
         * List of all visited URIs
         */
        private Set visitedAlready;


        /**
         * Constructor for the CocoonCrawlingIterator object
         *
         * @param  visitedAlready   Description of Parameter
         * @param  stillNotVisited  Description of Parameter
         */
        public CocoonCrawlingIterator(Set visitedAlready, List stillNotVisited) {
            this.visitedAlready = visitedAlready;
            this.stillNotVisited = stillNotVisited;
        }


        /**
         *   Check if list of not visited URIs is empty
         *
         * @return    boolean true iff list of not visited URIs is not empty
         */
        public boolean hasNext() {
            return !stillNotVisited.isEmpty();
        }


        /**
         *   Get next not visited URIs
         *
         * @return    object from list of not visited URIs, move it immediatly
         *   to set of visited URIs
         */
        public Object next() {
            Object nextElement = stillNotVisited.remove(0);
            visitedAlready.add(nextElement);
            return nextElement;
        }


        /**
         *   Removing objects is not supported, and will always throw
         *   a <code>UnsupportedOperationException</code>.
         */
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}

