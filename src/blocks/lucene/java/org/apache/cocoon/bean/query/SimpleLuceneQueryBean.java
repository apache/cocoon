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

import java.util.Iterator;
import java.util.List;
import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Enumeration;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.BooleanQuery;
import org.apache.cocoon.components.search.LuceneCocoonSearcher;
import org.apache.cocoon.ProcessingException;


/**
 * The query bean.
 * <p>
 *   This object defines a <code>Bean</code> for searching.<br/>
 *   The idea is to abstract the process of searching into a Bean to be manipulated by CForms.<br/>
 *   This Bean is designed to be persistable.
 * </p>
 *
 * @version CVS $Id: SimpleLuceneQueryBean.java,v 1.1 2004/06/21 10:00:20 jeremy Exp $
 */
public class SimpleLuceneQueryBean implements SimpleLuceneQuery {

	/**
	 * The DEFAULT_PAGE_SIZE of this bean.
	 * ie. <code>20</code>
	 */
	public static Long DEFAULT_PAGE_SIZE = new Long (20);

	/**
	 * The SCORE_FIELD of this bean.
	 * This is the key of the Lucene Score as output by this Bean in each hit.
	 * ie. <code>_lucene-score_</code>
	 */
	public static String SCORE_FIELD = "_lucene-score_";

	/**
	 * The INDEX_FIELD of this bean.
	 * This is the key of the hit index as output by this Bean in each hit.
	 * ie. <code>_lucene-index_</code>
	 */
	public static String INDEX_FIELD = "_lucene-index_";

	/**
	 * The date this Query was created.
	 */
	private Date mDate; 

	/**
	 * The Bean's list of Criteria.
	 */
	private List mCriteria;

	/**
	 * The Bean's ID.
	 */
	private Long mId;

	/**
	 * The Bean's current page.
	 */
	private Long mPage; 

	/**
	 * The Bean's page isze.
	 */
	private Long mSize; 

	/**
	 * The Bean's hit count.
	 */
	private Long mTotal; 

	/**
	 * The Bean's query boolean.
	 */
	private String mBool;

	/**
	 * The Bean's query name.
	 */
	private String mName;

	/**
	 * The Bean's query type.
	 */
	private String mType;

	/**
	 * The Bean's searcher.
	 */
	private LuceneCocoonSearcher searcher;
	
	/**
	 * Default constructor.
	 */
	public SimpleLuceneQueryBean() {
	}

	/**
	 * Utility constructor.
	 *
	 * @param type the type of this query
	 * @param bool the kind of boolean opperation to apply to each of it's Criteria
	 * @param match the kind of match to use for the generated <code>Criterion</code>
	 * @param field the field to search for the generated <code>Criterion</code>
	 * @param query the terms to search for the generated <code>Criterion</code>
	 */
	public SimpleLuceneQueryBean(String type, String bool, String match, String field, String query) {
		mName = "My Query";
		mType = type;
		mBool = bool;
		mSize = DEFAULT_PAGE_SIZE;
		mPage = new Long (0);
		mTotal = null;
		this.addCriterion (new SimpleLuceneCriterionBean (field, match, query));
	}
	
	/**
	 * Clones the Bean for the history
	 *
	 * @return a deep copy of this Bean. 
	 */
	public SimpleLuceneQuery copy() {
		SimpleLuceneQueryBean query = new SimpleLuceneQueryBean ();
		if (mName != null) query.setName (new String (mName));
		if (mType != null) query.setType (new String (mType));
		if (mBool != null) query.setBool (new String (mBool));
		query.setSize (DEFAULT_PAGE_SIZE);
		query.setPage (new Long (0));
		query.setTotal (null);
		if (mDate != null) {
			query.setDate (new Date (mDate.getTime ()));
		} else {
			query.setDate (new Date ());
		}
		Iterator i = this.getCriteria().iterator ();
		while (i.hasNext()) query.addCriterion (((SimpleLuceneCriterionBean)i.next()).copy ());
		return query;
	}
	
	/**
	 * Gets the Bean to perform it's query
	 * <p>
	 *   The searcher specifies which LuceneCocoonSearcher to use for this search.<br/>
	 *   It needs to have been initialised properly before use.<br/>
	 *   Each <code>Map</code> in the <code>List</code> returned by this method contains:
	 *   <ul>
	 *     <li>Each stored field from the Index</li>
	 *     <li><code>SCORE_FIELD</code> the Lucene score</li>
	 *     <li><code>INDEX_FIELD</code> the index of the hit</li>
	 *   </ul>
	 * </p>
	 *
	 * @param  searcher  The <code>LuceneCocoonSearcher</code> to use for this search
	 * @return a List of Maps, each representing a Hit. 
	 * @exception  ProcessingException thrown by the searcher
	 * @exception  IOException thrown when the searcher's directory cannot be found
	 */
	public List search (LuceneCocoonSearcher searcher) throws java.io.IOException, ProcessingException {
		BooleanQuery query = new BooleanQuery ();
		Iterator criteria = mCriteria.iterator ();
		boolean required = false;
		if (AND_BOOL.equals (mBool)) required = true;
		while (criteria.hasNext ()) {
			SimpleLuceneCriterion criterion = (SimpleLuceneCriterion)criteria.next ();
			Query subquery = criterion.getQuery (searcher.getAnalyzer ());
			query.add (subquery, required, criterion.isProhibited ());
		}
		// TODO: how can this bean be both Persistable and LogEnabled ?
		//System.out.println ("SimpleLuceneQueryBean: " + query.toString ());
		Hits hits = searcher.search (query);
		mTotal = new Long (hits.length ());
		mDate = new Date ();
		return page (hits);
	}

	/**
	 * Outputs part of a Hit List according to the Bean's paging properties.
	 *
	 * @param  hits  The Lucene Hits you want to page
	 * @return a List of Maps, each representing a Hit. 
	 * @exception  IOException thrown when the searcher's directory cannot be found
	 */
	private List page (Hits hits)  throws java.io.IOException {
		ArrayList results = new ArrayList ();
		int start = mPage.intValue () * mSize.intValue ();
		if (start > mTotal.intValue ()) start = mTotal.intValue (); 
		int end = start + mSize.intValue ();
		if (end > mTotal.intValue ()) end = mTotal.intValue (); 
		for (int i = start; i < end; i++) {
			HashMap hit = new HashMap ();
			hit.put (SCORE_FIELD, new Float (hits.score (i)));
			hit.put (INDEX_FIELD, new Long (i));
			Document doc = hits.doc (i);
			for (Enumeration e = doc.fields (); e.hasMoreElements (); ) {
				Field field = (Field)e.nextElement ();
				if (field.name ().equals (SCORE_FIELD)) continue;
				if (field.name ().equals (INDEX_FIELD)) continue;
				hit.put (field.name (), field.stringValue ());
      }
			results.add (hit);
		}
		return (results);
	}
	
	/**
	 * Gets the Bean's ID.
	 *
	 * @return the <code>Long</code> ID of the Bean. 
	 */
	public Long getId() {
		return mId;
	}
	
	/**
	 * Sets the Bean's ID.
	 *
	 * @param id the <code>Long</code> ID of the Bean. 
	 */
	public void setId(Long id) {
		mId = id;
	}
	
	/**
	 * Gets the Bean's name.
	 *
	 * @return the <code>String</code> name of the Bean. 
	 */
	public String getName() {
		return mName;
	}
	
	/**
	 * Sets the Bean's Name.
	 *
	 * @param id the <code>String</code> name of the Bean. 
	 */
	public void setName(String name) {
		mName = name;
	}

	/**
	 * Gets the Bean's type.
	 *
	 * @return the <code>String</code> type of the Bean. 
	 */
	public String getType() {
		return mType;
	}
	
	/**
	 * Sets the Bean's type.
	 *
	 * @param type the <code>String</code> type of the Bean. 
	 */
	public void setType(String type) {
		mType = type;
	}

	/**
	 * Gets the Bean's boolean operator.
	 *
	 * @return the <code>String</code> boolean of the Bean. 
	 */
	public String getBool() {
		return mBool;
	}
	
	/**
	 * Sets the Bean's boolean operator.
	 * ie. which kind of boolean operation do you want performed on each <code>Criterion</code>.
	 *
	 * @param bool the <code>String</code> boolean of the Bean. 
	 */
	public void setBool(String bool) {
		mBool = bool;
	}
	
	/**
	 * Gets the Bean's page size
	 *
	 * @return the <code>Long</code> page size of the Bean. 
	 */
	public Long getSize() {
		return mSize;
	}
	
	/**
	 * Sets the Bean's page size.
	 * ie. how many hits do you want this Bean to show on in page.
	 *
	 * @param size the <code>Long</code> page size of the Bean. 
	 */
	public void setSize(Long size) {
		mSize = size;
	}
	
	/**
	 * Gets the Bean's page index
	 *
	 * @return the <code>Long</code> page index of the Bean. 
	 */
	public Long getPage() {
		return mPage;
	}
	
	/**
	 * Sets the Bean's page index.
	 * ie. which page do you want this Bean to show.
	 *
	 * @param page the <code>Long</code> page index of the Bean. 
	 */
	public void setPage(Long page) {
		mPage = page;
	}

	/**
	 * Gets the Bean's hit count.
	 *
	 * @return the <code>Long</code> hit count of the Bean. 
	 */
	public Long getTotal() {
		return mTotal;
	}
	
	/**
	 * Sets the Bean's hit count.
	 *
	 * @param total the <code>Long</code> hit count of the Bean. 
	 */
	public void setTotal(Long total) {
		mTotal = total;
	}

	/**
	 * Gets the Bean's inception date.
	 *
	 * @return the <code>Date</code> of the Bean. 
	 */
	public Date getDate() {
		return mDate;
	}
	
	/**
	 * Sets the Bean's inception date.
	 *
	 * @param date the <code>Date</code> inception date of the Bean. 
	 */
	public void setDate(Date date) {
		mDate = date;
	}

	/**
	 * Gets the Bean's criteria.
	 *
	 * @return the <code>List</code> of Bean Query criteria. 
	 */
	public List getCriteria() {
		return mCriteria;
	}
	
	/**
	 * Sets the Bean's criteria.
	 *
	 * @param criteria the <code>List</code> of Bean Query criteria. 
	 */
	public void setCriteria(List criteria) {
		mCriteria = criteria;
	}

	/**
	 * Adds a <code>Criterion</code> the Bean.
	 *
	 * @param criterion the <code>SimpleLuceneCriterionBean</code> to add to the Bean. 
	 */
	public void addCriterion(SimpleLuceneCriterionBean criterion) {
		if (mCriteria == null) mCriteria = new ArrayList ();
		mCriteria.add (criterion);
	}

	/**
	 * Removes a <code>Criterion</code> from the Bean.
	 *
	 * @param criterion the <code>SimpleLuceneCriterionBean</code> to remove from the Bean. 
	 */
	public void removeCriterion(SimpleLuceneCriterionBean criterion) {
		if (mCriteria != null) mCriteria.remove (criterion);
	}
	
}
