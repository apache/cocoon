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
package org.apache.cocoon.bean.helpers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.cocoon.bean.Target;

/**
 * A simple Cocoon crawler.
 *
 * @version $Id$
 */

public class Crawler {

    private Map allTranslatedLinks;
    private Map stillNotVisited;
    private Map visitedAlready;
    
    public Crawler() {
        visitedAlready = new HashMap();
        stillNotVisited = new HashMap();
        allTranslatedLinks = new HashMap();
    }
    
    /**
     * Add a target for future processing
     */
    public boolean addTarget(Target target) {
        String targetString = target.toString();
        if (!visitedAlready.containsKey(targetString)) {
            if (!stillNotVisited.containsKey(targetString)) {
                stillNotVisited.put(targetString, target);
                return true;
            }
            Target existingTarget = (Target)stillNotVisited.get(targetString);
            existingTarget.addReferringURI(target.getReferringURI());
        } else {
            Target visitedTarget = (Target)visitedAlready.get(targetString);
            visitedTarget.addReferringURI(target.getReferringURI());
        }
        
        return false;
    }

    /**
     * Returns the number of targets for processing
     */
    public int getRemainingCount() {
        return stillNotVisited.size();
    }
    
    public int getProcessedCount() {
        return visitedAlready.size();
    }
    
    public int getTranslatedCount() {
        return allTranslatedLinks.size();
    }
    
    public void addTranslatedLink(Target target) {
        allTranslatedLinks.put(target.getSourceURI(), target);
    }
    
    public boolean hasTranslatedLink(Target link) {
        return allTranslatedLinks.get(link.getSourceURI())!=null;
    }
    
    public Target getTranslatedLink(Target link) {
        return (Target) allTranslatedLinks.get(link.getSourceURI());
    }
    
    /**
     * Returns an iterator for reading targets
     */
    public CrawlingIterator iterator() {
        return new CrawlingIterator(visitedAlready, stillNotVisited);
    }
    
    public class CrawlingIterator implements Iterator {

        private Map stillNotVisited;
        private Map visitedAlready;
        
        public CrawlingIterator(Map visitedAlready, Map stillNotVisited) {
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
         *   Removing objects is not supported, and will always throw
         *   a <code>UnsupportedOperationException</code>.
         */
        public void remove(){ 
            throw new UnsupportedOperationException();
        }

        /**
         *   Get next not visited URIs
         *
         * @return    object from list of not visited URIs, move it immediatly
         *   to set of visited URIs
         */
        public Object next() {
            // could this be simpler:
            Object nextKey = stillNotVisited.keySet().toArray()[0];
            Object nextElement = stillNotVisited.remove(nextKey);
            visitedAlready.put(nextKey, nextElement);
            return nextElement;
        }
    }
}
