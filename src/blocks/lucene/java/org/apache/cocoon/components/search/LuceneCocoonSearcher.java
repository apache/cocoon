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
package org.apache.cocoon.components.search;

import org.apache.avalon.framework.component.Component;
import org.apache.cocoon.ProcessingException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.Hits;
import org.apache.lucene.store.Directory;

/**
 * The avalon behavioural component interface of a searcher.
 * <p>
 *   This component defines an interface for searching.
 *   The idea is to abstract the process of searching having a query string,
 *   and an index, and generating hits which matches the query string in the index.
 * </p>
 *
 * @author <a href="mailto:berni_huber@a1.net">Bernhard Huber</a>
 * @version CVS $Id: LuceneCocoonSearcher.java,v 1.3 2004/03/05 13:01:59 bdelacretaz Exp $
 */
public interface LuceneCocoonSearcher extends Component
{
    /**
     * The ROLE name of this avalon component.
     * <p>
     *   Its value if the FQN of this interface,
     *   ie. <code>org.apache.cocoon.components.search.LuceneCocoonSearcher</code>.
     * </p>
     *
     * @since
     */
    String ROLE = "org.apache.cocoon.components.search.LuceneCocoonSearcher";


    /**
     * Sets the analyzer attribute of the LuceneCocoonSearcher object
     * <p>
     *   The analyzer determines the tokenization of the query,
     *   and strategy of matching.
     * </p>
     * <p>
     *   The analyzer class defined here should be equivalent to the analyzer
     *   class used when creating the index used for searching.
     * </p>
     *
     * @param  analyzer  The new analyzer value
     * @since
     */
    void setAnalyzer(Analyzer analyzer);


    /**
     * Sets the directory attribute of the LuceneCocoonSearcher object
     * <p>
     *   The directory specifies the directory used for looking up the
     *   index. It defines the physical place of the index
     * </p>
     *
     * @param  directory  The new directory value
     * @since
     */
    void setDirectory(Directory directory);


    /**
     * Search a query-string, returning zero, or more hits.
     * <p>
     * </p>
     *
     * @param  query_string             A query string parsable by a query parser.
     * @param  default_field            The default field of the query string.
     * @return                          Hits zero or more hits matching the query string
     * @exception  ProcessingException  throwing due to processing errors while
     *   looking up the index directory, parsing the query string, generating the hits.
     * @since
     */
    Hits search(String query_string, String default_field) throws ProcessingException;
}

