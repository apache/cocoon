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
import java.io.Serializable;
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
 */
public class SimpleLuceneCriterionBean implements SimpleLuceneCriterion, Cloneable, Serializable {

	/**
	 * The Bean's ID.
	 */
	protected Long id;

	/**
	 * The Bean's index field to seach in.
	 */
	protected String field;

	/**
	 * The Bean's match value.
	 */
	protected String match;

	/**
	 * The Bean's search term.
	 */
	protected String term;
	
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
	 * @param term the terms to search for
	 */
	public SimpleLuceneCriterionBean(String field, String match, String term) {
		this.field = field;
		this.match = match;
		this.term = term;
	}

	public Object clone() throws CloneNotSupportedException {
		SimpleLuceneCriterionBean criterion = (SimpleLuceneCriterionBean)super.clone();
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
		String f = this.field;
		Query query = null;
		if (ANY_FIELD.equals(this.field)) f = LuceneXMLIndexer.BODY_FIELD;
		// extract Terms from the query string
    TokenStream tokens = analyzer.tokenStream(f, new StringReader (this.term));
    Vector words = new Vector();
    Token token;
    while (true) {
      try {
        token = tokens.next();
      } catch (IOException e) {
        token = null;
      }
      if (token == null) break;
      words.addElement(token.termText ());
    }
    try {
      tokens.close();
    } catch (IOException e) {} // ignore 
		
		// assemble the different matches
		
		if (ANY_MATCH.equals(this.match)) {
			if (words.size() > 1) {
				query = new BooleanQuery();
				for (int i = 0; i < words.size(); i++) {
					((BooleanQuery)query).add(new TermQuery(new Term(f, (String)words.elementAt(i))), false, false);
				}
			} else if (words.size() == 1) {
				query = new TermQuery(new Term(f, (String)words.elementAt(0)));
			}
		} 
		
		if (ALL_MATCH.equals(this.match)) {
			if (words.size() > 1) {
				query = new BooleanQuery();
				for (int i = 0; i < words.size(); i++) {
					((BooleanQuery)query).add(new TermQuery(new Term (f, (String)words.elementAt(i))), true, false);
				}
			} else if (words.size() == 1) {
				query = new TermQuery(new Term(f, (String)words.elementAt(0)));
			}
		} 
		
		if (NOT_MATCH.equals(this.match)) {
			if (words.size() > 1) {
				query = new BooleanQuery();
				for (int i = 0; i < words.size(); i++) {
					((BooleanQuery)query).add(new TermQuery(new Term(f, (String)words.elementAt(i))), true, true);
				}
			} else if (words.size() == 1) {
				query = new TermQuery(new Term(f, (String)words.elementAt(0)));
			}
		} 
		
		if (LIKE_MATCH.equals(this.match)) {
			if (words.size() > 1) {
				query = new BooleanQuery();
				for (int i = 0; i < words.size(); i++) {
					((BooleanQuery)query).add(new FuzzyQuery(new Term(f, (String)words.elementAt(i))), false, false);
				}
			} else if (words.size() == 1) {
				query = new FuzzyQuery(new Term(f, (String)words.elementAt(0)));
			}
		}
		
		if (PHRASE_MATCH.equals (this.match)) {
			if (words.size() > 1) {
				query = new PhraseQuery();
				((PhraseQuery)query).setSlop(0);
				for (int i = 0; i < words.size(); i++) {
					((PhraseQuery)query).add(new Term(f, (String)words.elementAt(i)));
				}
			} else if (words.size() == 1) {
				query = new TermQuery(new Term(f, (String)words.elementAt(0)));
			}
		}
		return query;
	}
	
	/**
	 * Gets the prohibited status from the Criterion
	 */
	public boolean isProhibited () {
		if (NOT_MATCH.equals(this.match)) return true;
		return false;
	}
	
	
	// Bean
	
	/**
	 * Gets the Bean's ID
	 *
	 * @return the <code>Long</code> ID of the Bean. 
	 */
	public Long getId() {
		return this.id;
	}
	
	/**
	 * Sets the Bean's ID
	 *
	 * @param id the <code>Long</code> ID of the Bean. 
	 */
	public void setId(Long id) {
		this.id = id;
	}
	
	/**
	 * Gets the Bean's field
	 *
	 * @return the <code>String</code> field of the Bean. 
	 */
	public String getField() {
		return this.field;
	}
	
	/**
	 * Sets the Bean's field.<br/>
	 * ie. which field would you like this Criterion to search in.
	 *
	 * @param field the <code>String</code> field of the Bean. 
	 */
	public void setField(String field) {
		this.field = field;
	}
	
	/**
	 * Gets the Bean's match
	 *
	 * @return the <code>String</code> match of the Bean. 
	 */
	public String getMatch() {
		return this.match;
	}
	
	/**
	 * Sets the Bean's match.<br/>
	 * ie. what kind of match do you want performed by this Criterion.
	 *
	 * @param match the <code>String</code> match of the Bean. 
	 */
	public void setMatch(String match) {
		this.match = match;
	}
	
	/**
	 * Gets the Bean's term
	 *
	 * @return the <code>String</code> term of the Bean. 
	 */
	public String getTerm() {
		return this.term;
	}
	
	/**
	 * Sets the Bean's term.<br/>
	 * ie. the string of search terms for this <code>Criterion</code>.
	 *
	 * @param term the <code>String</code> term of the Bean. 
	 */
	public void setTerm(String term) {
		this.term = term;
	}

}
