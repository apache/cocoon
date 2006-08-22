/*
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

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
 * @version CVS $Id$
 */
public class SimpleLuceneQueryBean implements SimpleLuceneQuery, Cloneable, Serializable {

	/**
	 * The DEFAULT_PAGE_SIZE of this bean.
	 * ie. <code>20</code>
	 */
	public static Long DEFAULT_PAGE_SIZE = new Long (20);

	/**
	 * The DEFAULT_PAGE of this bean.
	 * ie. <code>0</code>
	 */
	public static Long DEFAULT_PAGE = new Long (0);

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
	private Date date; 

	/**
	 * The Bean's list of Criteria.
	 */
	private List criteria;

	/**
	 * The Bean's ID.
	 */
	private Long id;

	/**
	 * The Bean's current page.
	 */
	private Long page; 

	/**
	 * The Bean's page isze.
	 */
	private Long size; 

	/**
	 * The Bean's hit count.
	 */
	private Long total; 

	/**
	 * The Bean's query boolean.
	 */
	private String bool;

	/**
	 * The Bean's query name.
	 */
	private String name;

	/**
	 * The Bean's query type.
	 */
	private String type;

	/**
	 * The Bean's owner.
	 */
	private String user;

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
		this.name = "My Query";
		this.type = type;
		this.bool = bool;
		this.size = DEFAULT_PAGE_SIZE;
		this.page = DEFAULT_PAGE;
		this.total = null;
		this.user = null;
		this.id = null;
		this.addCriterion(new SimpleLuceneCriterionBean(field, match, query));
	}
	
	public Object clone() throws CloneNotSupportedException {
		SimpleLuceneQueryBean query = (SimpleLuceneQueryBean)super.clone();
		query.setCriteria(new ArrayList(this.criteria.size()));
		Iterator it = this.getCriteria().iterator();
		while (it.hasNext()) query.addCriterion((SimpleLuceneCriterionBean)((SimpleLuceneCriterionBean)it.next()).clone());
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
	public List search (LuceneCocoonSearcher searcher) throws IOException, ProcessingException {
		BooleanQuery query = new BooleanQuery();
		Iterator it = criteria.iterator();
		boolean required = false;
		if (AND_BOOL.equals(this.bool)) required = true;
		while (it.hasNext()) {
			SimpleLuceneCriterion criterion = (SimpleLuceneCriterion)it.next();
			Query subquery = criterion.getQuery (searcher.getAnalyzer());
			query.add(subquery, required, criterion.isProhibited());
		}
		Hits hits = searcher.search(query);
		this.total = new Long (hits.length());
		this.date = new Date();
		return page(hits);
	}

	/**
	 * Outputs part of a Hit List according to the Bean's paging properties.
	 *
	 * @param  hits  The Lucene Hits you want to page
	 * @return a List of Maps, each representing a Hit. 
	 * @exception  IOException thrown when the searcher's directory cannot be found
	 */
	private List page (Hits hits)  throws java.io.IOException {
		ArrayList results = new ArrayList();
		int start = getPage().intValue() * getSize().intValue();
		if (start > this.total.intValue()) start = this.total.intValue(); 
		int end = start + getSize().intValue();
		if (end > this.total.intValue()) end = this.total.intValue(); 
		for (int i = start; i < end; i++) {
			HashMap hit = new HashMap();
			hit.put(SCORE_FIELD, new Float(hits.score (i)));
			hit.put(INDEX_FIELD, new Long(i));
			Document doc = hits.doc(i);
			for (Enumeration e = doc.fields(); e.hasMoreElements(); ) {
				Field field = (Field)e.nextElement();
				if (field.name().equals(SCORE_FIELD)) continue;
				if (field.name().equals(INDEX_FIELD)) continue;
				hit.put(field.name(), field.stringValue());
      }
			results.add(hit);
		}
		return (results);
	}
	
	/**
	 * Gets the Bean's ID.
	 *
	 * @return the <code>Long</code> ID of the Bean. 
	 */
	public Long getId() {
		return this.id;
	}
	
	/**
	 * Sets the Bean's ID.
	 *
	 * @param id the <code>Long</code> ID of the Bean. 
	 */
	public void setId(Long id) {
		this.id = id;
	}
	
	/**
	 * Gets the Bean's name.
	 *
	 * @return the <code>String</code> name of the Bean. 
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Sets the Bean's Name.
	 *
	 * @param name the <code>String</code> name of the Bean. 
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the Bean's type.
	 *
	 * @return the <code>String</code> type of the Bean. 
	 */
	public String getType() {
		return this.type;
	}
	
	/**
	 * Sets the Bean's type.
	 *
	 * @param type the <code>String</code> type of the Bean. 
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Gets the Bean's boolean operator.
	 *
	 * @return the <code>String</code> boolean of the Bean. 
	 */
	public String getBool() {
		return this.bool;
	}
	
	/**
	 * Sets the Bean's boolean operator.
	 * ie. which kind of boolean operation do you want performed on each <code>Criterion</code>.
	 *
	 * @param bool the <code>String</code> boolean of the Bean. 
	 */
	public void setBool(String bool) {
		this.bool = bool;
	}

	/**
	 * Gets the Bean's owner.
	 *
	 * @return the <code>String</code> owner of the Bean. 
	 */
	public String getUser() {
		return this.user;
	}
	
	/**
	 * Sets the Bean's owner.
	 *
	 * @param user the <code>String</code> owner of the Bean. 
	 */
	public void setUser(String user) {
		this.user = user;
	}
	
	/**
	 * Gets the Bean's page size
	 *
	 * @return the <code>Long</code> page size of the Bean. 
	 */
	public Long getSize() {
		if (this.size == null) {
			return DEFAULT_PAGE_SIZE;
		} else {
			return this.size;
		}
	}
	
	/**
	 * Sets the Bean's page size.
	 * ie. how many hits do you want this Bean to show on in page.
	 *
	 * @param size the <code>Long</code> page size of the Bean. 
	 */
	public void setSize(Long size) {
		this.size = size;
	}
	
	/**
	 * Gets the Bean's page index
	 *
	 * @return the <code>Long</code> page index of the Bean. 
	 */
	public Long getPage() {
		if (this.page == null) {
			return DEFAULT_PAGE;
		} else {
			return this.page;
		}
	}
	
	/**
	 * Sets the Bean's page index.
	 * ie. which page do you want this Bean to show.
	 *
	 * @param page the <code>Long</code> page index of the Bean. 
	 */
	public void setPage(Long page) {
		this.page = page;
	}

	/**
	 * Gets the Bean's hit count.
	 *
	 * @return the <code>Long</code> hit count of the Bean. 
	 */
	public Long getTotal() {
		return this.total;
	}
	
	/**
	 * Sets the Bean's hit count.
	 *
	 * @param total the <code>Long</code> hit count of the Bean. 
	 */
	public void setTotal(Long total) {
		this.total = total;
	}

	/**
	 * Gets the Bean's inception date.
	 *
	 * @return the <code>Date</code> of the Bean. 
	 */
	public Date getDate() {
		return this.date;
	}
	
	/**
	 * Sets the Bean's inception date.
	 *
	 * @param date the <code>Date</code> inception date of the Bean. 
	 */
	public void setDate(Date date) {
		this.date = date;
	}

	/**
	 * Gets the Bean's criteria.
	 *
	 * @return the <code>List</code> of Bean Query criteria. 
	 */
	public List getCriteria() {
		return this.criteria;
	}
	
	/**
	 * Sets the Bean's criteria.
	 *
	 * @param criteria the <code>List</code> of Bean Query criteria. 
	 */
	public void setCriteria(List criteria) {
		this.criteria = criteria;
	}

	/**
	 * Adds a <code>Criterion</code> the Bean.
	 *
	 * @param criterion the <code>SimpleLuceneCriterionBean</code> to add to the Bean. 
	 */
	public void addCriterion(SimpleLuceneCriterionBean criterion) {
		if (this.criteria == null) this.criteria = new ArrayList();
		this.criteria.add(criterion);
	}

	/**
	 * Removes a <code>Criterion</code> from the Bean.
	 *
	 * @param criterion the <code>SimpleLuceneCriterionBean</code> to remove from the Bean. 
	 */
	public void removeCriterion(SimpleLuceneCriterionBean criterion) {
		if (this.criteria != null) this.criteria.remove(criterion);
	}
	
}
