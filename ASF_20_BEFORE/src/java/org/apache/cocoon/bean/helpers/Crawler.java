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
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
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
package org.apache.cocoon.bean.helpers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.cocoon.bean.Target;
import org.apache.cocoon.ProcessingException;

/**
 *   A simple Cocoon crawler
 *
 * @author <a href="mailto:uv@upaya.co.uk">Upayavira</a>
 * @version CVS $Id: Crawler.java,v 1.3 2003/10/21 21:48:32 upayavira Exp $
 */

public class Crawler {

    private Map allTranslatedLinks;
    private Map stillNotVisited;
    private Set visitedAlready;
    
    public Crawler() {
        visitedAlready = new HashSet();
        stillNotVisited = new HashMap();
        allTranslatedLinks = new HashMap();
    }
    
    /**
     * Add a target for future processing
     */
    public boolean addTarget(Target target) {
        String targetString = target.toString();
        if (!visitedAlready.contains(targetString)) {
            if (!stillNotVisited.containsKey(targetString)) {
                stillNotVisited.put(targetString, target);
                return true;
            }
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
    
    public void addTranslatedLink(Target target) throws ProcessingException {
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
        private Set visitedAlready;
        
        public CrawlingIterator(Set visitedAlready, Map stillNotVisited) {
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
            visitedAlready.add(nextKey);
            return nextElement;
        }
    }
}
