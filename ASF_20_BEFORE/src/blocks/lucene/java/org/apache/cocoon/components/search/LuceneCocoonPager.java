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
package org.apache.cocoon.components.search;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Hits;

import java.io.IOException;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * This class should help you to manage paging of hits.
 *
 * @author <a href="mailto:berni_huber@a1.net">Bernhard Huber</a>
 * @version CVS $Id: LuceneCocoonPager.java,v 1.3 2003/10/26 00:56:05 vgritsenko Exp $
 */
public class LuceneCocoonPager implements ListIterator
{
    /**
     * Default count of hits per page.
     */
    public final static int COUNT_OF_HITS_PER_PAGE_DEFAULT = 5;

    /**
     * Default starting index
     */
    public final static int HITS_INDEX_START_DEFAULT = 0;

    /**
     * Current index of hit to return by next()
     */
    int hitsIndex = HITS_INDEX_START_DEFAULT;

    /**
     * Maximum count of hits to return by next(), and previous()
     */
    int countOfHitsPerPage = COUNT_OF_HITS_PER_PAGE_DEFAULT;

    /**
     * Hits to iterate upon
     */
    private Hits hits;


    /**
     * @param  hits  Description of Parameter
     */
    public LuceneCocoonPager(Hits hits) {
        setHits(hits);
    }


    /**
     * Constructor for the LuceneCocoonPager object
     */
    public LuceneCocoonPager() {
    }


    /**
     * Sets the hits attribute of the LuceneCocoonPager object
     *
     * @param  hits  The new hits value
     */
    public void setHits(Hits hits) {
        this.hits = hits;
        this.hitsIndex = HITS_INDEX_START_DEFAULT;
    }


    /**
     * Set count of hits displayed per single page
     *
     * @param  countOfHitsPerPage  The new countOfHitsPerPage value
     */
    public void setCountOfHitsPerPage(int countOfHitsPerPage) {
        this.countOfHitsPerPage = countOfHitsPerPage;
        if (this.countOfHitsPerPage <= 0) {
            this.countOfHitsPerPage = 1;
        }
    }


    /**
     * Get starting index for retrieving hits
     *
     * @param  start_index  The new startIndex value
     */
    public void setStartIndex(int start_index) {
        this.hitsIndex = start_index;
    }


    /**
     * Replaces the last element returned by next or previous with the
     * specified element (optional operation).
     *
     * @param  o  Description of Parameter
     */
    public void set(Object o) {
        throw new UnsupportedOperationException();
    }


    /**
     * Get count of hits
     *
     * @return    The count of hits
     */
    public int getCountOfHits() {
        return hits.length();
    }

    /**
     * Get count of hits displayed per single page
     *
     * @return    The countOfHitsPerPage value
     */
    public int getCountOfHitsPerPage() {
        return this.countOfHitsPerPage;
    }

    /**
     * Caluclate count of pages for displaying all hits
     *
     * @return    The countOfPages value
     */
    public int getCountOfPages() {
        int count_of_pages = hits.length() / this.countOfHitsPerPage;
        int remainder = hits.length() % this.countOfHitsPerPage;
        if (remainder != 0) {
            count_of_pages += 1;
        }
        return count_of_pages;
    }


    /**
     * Set starting index for retrieving hits
     *
     * @return    The startIndex value
     */
    public int getStartIndex() {
        return this.hitsIndex;
    }

    /**
     * Inserts the specified element into the list (optional operation).
     *
     * @param  o                                  Description of Parameter
     * @exception  UnsupportedOperationException  Description of Exception
     */
    public void add(Object o) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns true if this list iterator has more elements when traversing
     * the list in the forward direction.
     *
     * @return    Description of the Returned Value
     */
    public boolean hasNext() {
        return hitsIndex < hits.length();
    }

    /**
     * Returns true if this list iterator has more elements when traversing
     * the list in the reverse direction.
     *
     * @return    Description of the Returned Value
     */
    public boolean hasPrevious() {
        return hitsIndex > countOfHitsPerPage;
    }

    /**
     * Returns the next element in the list.
     *
     * @return    Description of the Returned Value
     */
    public Object next() {
        ArrayList hitsPerPageList = new ArrayList();
        int endIndex = Math.min(hits.length(), hitsIndex + countOfHitsPerPage);
        if (hitsIndex < endIndex) {
            while (hitsIndex < endIndex) {
                try {
                    HitWrapper hit = new HitWrapper(hits.score(hitsIndex),
                                                    hits.doc(hitsIndex));
                    hitsPerPageList.add(hit);
                } catch (IOException ioe) {
                    throw new NoSuchElementException("no more hits: " + ioe.getMessage());
                }
                hitsIndex++;
            }
        } else {
            throw new NoSuchElementException();
        }
        return hitsPerPageList;
    }

    /**
     * Returns the index of the element that would be returned by a
     * subsequent call to next.
     *
     * @return    Description of the Returned Value
     */
    public int nextIndex() {
        return Math.min(hitsIndex, hits.length());
    }

    /**
     * Returns the previous element in the list.
     *
     * @return    Description of the Returned Value
     */
    public Object previous() {
        ArrayList hitsPerPageList = new ArrayList();

        int startIndex = Math.max(0, hitsIndex - 2 * countOfHitsPerPage);
        int endIndex = Math.min(hits.length() - 1, hitsIndex - countOfHitsPerPage);

        if (startIndex < endIndex) {
            while (startIndex < endIndex) {
                try {
                    HitWrapper hit = new HitWrapper(hits.score(startIndex),
                                                    hits.doc(startIndex));
                    hitsPerPageList.add(hit);
                } catch (IOException ioe) {
                    throw new NoSuchElementException("no more hits: " + ioe.getMessage());
                }
                startIndex++;
            }
            hitsIndex = endIndex;
        } else {
            throw new NoSuchElementException();
        }
        return hitsPerPageList;
    }

    /**
     * Returns the index of the element that would be returned by a
     * subsequent call to previous.
     *
     * @return    Description of the Returned Value
     */
    public int previousIndex() {
        return Math.max(0, hitsIndex - 2 * countOfHitsPerPage);
    }

    /**
     * Removes from the list the last element that was returned by next or
     * previous (optional operation).
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }

    /**
     * A helper class encapsulating found document, and its score
     *
     * @author     <a href="mailto:berni_huber@a1.net">Bernhard Huber</a>
     * @version    CVS $Id: LuceneCocoonPager.java,v 1.3 2003/10/26 00:56:05 vgritsenko Exp $
     */
    public static class HitWrapper {
        float score;
        Document document;

        /**
         * Constructor for the HitWrapper object
         *
         * @param  score     Description of Parameter
         * @param  document  Description of Parameter
         */
        public HitWrapper(float score, Document document) {
            this.document = document;
            this.score = score;
        }

        /**
         * Gets the document attribute of the HitWrapper object
         *
         * @return    The document value
         */
        public Document getDocument() {
            return document;
        }

        /**
         * Gets the score attribute of the HitWrapper object
         *
         * @return    The score value
         */
        public float getScore() {
            return score;
        }

        /**
         * Gets the field attribute of the HitWrapper object
         *
         * @param  field  Description of Parameter
         * @return        The field value
         */
        public String getField(String field) {
            return document.get(field);
        }
    }
}
