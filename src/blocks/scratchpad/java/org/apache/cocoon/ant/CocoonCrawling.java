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
 * @version CVS $Id: CocoonCrawling.java,v 1.2 2004/03/05 10:07:25 bdelacretaz Exp $
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

