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

import java.io.IOException;
import java.io.StringReader;
import java.util.Vector;
import org.apache.cocoon.components.search.LuceneXMLIndexer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;


/**
 * The criterion bean.
 * <p>
 *   This object defines a <code>Bean</code> for holding a query criterion.<br/>
 *   The idea is to abstract the process of searching into a Bean to be manipulated by CForms.<br/>
 *   This Bean is designed to be persistable.
 * </p>
 *
 * @version CVS $Id: SimpleLuceneCriterionBean.java,v 1.1 2004/06/21 10:00:20 jeremy Exp $
 */
public class SimpleLuceneCriterionBean implements SimpleLuceneCriterion {

	/**
	 * The Bean's ID.
	 */
	private Long mId;

	/**
	 * The Bean's index field to seach in.
	 */
	private String mField;

	/**
	 * The Bean's match value.
	 */
	private String mMatch;

	/**
	 * The Bean's search term.
	 */
	private String mValue;
	
	/**
	 * Default constructor.
	 */
	public SimpleLuceneCriterionBean() {
	}


	/**
	 * Utility constructor.
	 *
	 * @param match the kind of match to use
	 * @param field the field to search
	 * @param value the terms to search for
	 */
	public SimpleLuceneCriterionBean(String field, String match, String value) {
		mField = field;
		mMatch = match;
		mValue = value;
	}
	
	/**
	 * Clones the Bean for the history
	 *
	 * @return a deep copy of this Bean. 
	 */
	public SimpleLuceneCriterionBean copy () {
		SimpleLuceneCriterionBean criterion = new SimpleLuceneCriterionBean ();
		if (mField != null) criterion.setField (new String (mField));
		if (mMatch != null) criterion.setMatch (new String (mMatch));
		if (mValue != null) criterion.setValue (new String (mValue));
		return criterion;
	}

	/**
	 * Gets the <code>org.apache.lucene.search.Query</code> from the Criterion
	 * <p>
	 *   The analyzer specifies which <code>org.apache.lucene.analysis.Analyzer</code> to use for this search.
	 * </p>
	 *
	 * @param  analyzer  The <code>org.apache.lucene.analysis.Analyzer</code> to use to extract the Terms from this Criterion
	 */
	public Query getQuery (Analyzer analyzer) {
		String field = mField;
		Query query = null;
		if (ANY_FIELD.equals (mField)) field = LuceneXMLIndexer.BODY_FIELD;
		// extract Terms from the query string
    TokenStream tokens = analyzer.tokenStream (field, new StringReader (mValue));
    Vector words = new Vector ();
    Token token;
    while (true) {
      try {
        token = tokens.next ();
      } catch (IOException e) {
        token = null;
      }
      if (token == null) break;
      words.addElement (token.termText ());
    }
    try {
      tokens.close ();
    } catch (IOException e) {} // ignore 
		
		// assemble the different matches
		
		if (ANY_MATCH.equals (mMatch)) {
			if (words.size () > 1) {
				query = new BooleanQuery ();
				for (int i = 0; i < words.size (); i++) {
					((BooleanQuery)query).add (new TermQuery (new Term (field, (String)words.elementAt(i))), false, false);
				}
			} else if (words.size () == 1) {
				query = new TermQuery (new Term (field, (String)words.elementAt(0)));
			}
		} 
		
		if (ALL_MATCH.equals (mMatch)) {
			if (words.size () > 1) {
				query = new BooleanQuery ();
				for (int i = 0; i < words.size (); i++) {
					((BooleanQuery)query).add (new TermQuery (new Term (field, (String)words.elementAt(i))), true, false);
				}
			} else if (words.size () == 1) {
				query = new TermQuery (new Term (field, (String)words.elementAt(0)));
			}
		} 
		
		if (NOT_MATCH.equals (mMatch)) {
			if (words.size () > 1) {
				query = new BooleanQuery ();
				for (int i = 0; i < words.size (); i++) {
					((BooleanQuery)query).add (new TermQuery (new Term (field, (String)words.elementAt(i))), true, true);
				}
			} else if (words.size () == 1) {
				query = new TermQuery (new Term (field, (String)words.elementAt(0)));
			}
		} 
		
		if (LIKE_MATCH.equals (mMatch)) {
			if (words.size () > 1) {
				query = new BooleanQuery ();
				for (int i = 0; i < words.size (); i++) {
					((BooleanQuery)query).add (new FuzzyQuery (new Term (field, (String)words.elementAt(i))), false, false);
				}
			} else if (words.size () == 1) {
				query = new FuzzyQuery (new Term (field, (String)words.elementAt(0)));
			}
		}
		
		if (PHRASE_MATCH.equals (mMatch)) {
			if (words.size () > 1) {
				query = new PhraseQuery ();
				((PhraseQuery)query).setSlop (0);
				for (int i = 0; i < words.size (); i++) {
					((PhraseQuery)query).add (new Term (field, (String)words.elementAt(i)));
				}
			} else if (words.size () == 1) {
				query = new TermQuery (new Term (field, (String)words.elementAt(0)));
			}
		}
		return query;
	}
	
	/**
	 * Gets the prohibited status from the Criterion
	 */
	public boolean isProhibited () {
		if (NOT_MATCH.equals (mMatch)) return true;
		return false;
	}
	
	
	// Bean
	
	/**
	 * Gets the Bean's ID
	 *
	 * @return the <code>Long</code> ID of the Bean. 
	 */
	public Long getId() {
		return mId;
	}
	
	/**
	 * Sets the Bean's ID
	 *
	 * @param id the <code>Long</code> ID of the Bean. 
	 */
	public void setId(Long id) {
		mId = id;
	}
	
	/**
	 * Gets the Bean's field
	 *
	 * @return the <code>String</code> field of the Bean. 
	 */
	public String getField() {
		return mField;
	}
	
	/**
	 * Sets the Bean's field.<br/>
	 * ie. which field would you like this Criterion to search in.
	 *
	 * @param field the <code>String</code> field of the Bean. 
	 */
	public void setField(String field) {
		mField = field;
	}
	
	/**
	 * Gets the Bean's match
	 *
	 * @return the <code>String</code> match of the Bean. 
	 */
	public String getMatch() {
		return mMatch;
	}
	
	/**
	 * Sets the Bean's match.<br/>
	 * ie. what kind of match do you want performed by this Criterion.
	 *
	 * @param match the <code>String</code> match of the Bean. 
	 */
	public void setMatch(String match) {
		mMatch = match;
	}
	
	/**
	 * Gets the Bean's value
	 *
	 * @return the <code>String</code> value of the Bean. 
	 */
	public String getValue() {
		return mValue;
	}
	
	/**
	 * Sets the Bean's value.<br/>
	 * ie. the string of search terms for this <code>Criterion</code>.
	 *
	 * @param value the <code>String</code> value of the Bean. 
	 */
	public void setValue(String value) {
		mValue = value.trim ();
	}

}
