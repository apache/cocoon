/*
  Copyright 1999-2004 The Apache Software Foundation

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package org.apache.cocoon.bean.query;

import org.apache.lucene.search.Query;
import org.apache.lucene.analysis.Analyzer;


/**
 * The interface of a criterion bean.
 * <p>
 *   This component defines an interface for searching.
 *   The idea is to abstract the process of searching into a Bean to be manipulated by CForms.
 * </p>
 *
 * @version CVS $Id: SimpleLuceneCriterion.java,v 1.1 2004/06/21 10:00:20 jeremy Exp $
 */
public interface SimpleLuceneCriterion {

    /**
     * The ANY_FIELD name of this bean.
     * <p>
     *   The value representing a query on any field in the index.
     *   ie. <code>any</code>
     * </p>
     */
    public static final String ANY_FIELD = "any";
	
    /**
     * The ANY_MATCH name of this bean.
     * <p>
     *   The value representing a match on any of the terms in this criterion.
     *   ie. <code>any</code>
     * </p>
     */
    public static final String ANY_MATCH = "any";
	
    /**
     * The ALL_MATCH name of this bean.
     * <p>
     *   The value representing a match on all of the terms in this criterion.
     *   ie. <code>all</code>
     * </p>
     */
    public static final String ALL_MATCH = "all";
	
    /**
     * The LIKE_MATCH name of this bean.
     * <p>
     *   The value representing a fuzzy match on any of the terms in this criterion.
     *   ie. <code>like</code>
     * </p>
     */
    public static final String LIKE_MATCH = "like";
	
    /**
     * The NOT_MATCH name of this bean.
     * <p>
     *   The value representing a prohibition on any of the terms in this criterion.
     *   ie. <code>like</code>
     * </p>
     */
    public static final String NOT_MATCH = "not";
	
    /**
     * The PHRASE_MATCH name of this bean.
     * <p>
     *   The value representing a phrase match using all of the terms in this criterion.
     *   ie. <code>like</code>
     * </p>
     */
    public static final String PHRASE_MATCH = "phrase";

    /**
     * Gets the <code>org.apache.lucene.search.Query</code> from the Criterion
     * <p>
     *   The analyzer specifies which <code>org.apache.lucene.analysis.Analyzer</code> to use for this search
     * </p>
     *
     * @param  analyzer  The <code>org.apache.lucene.analysis.Analyzer</code> to use to extract the Terms from this Criterion
     */
    public Query getQuery (Analyzer analyzer);

    /**
     * Gets the prohibited status from the Criterion
     */
    public boolean isProhibited ();
	
}
