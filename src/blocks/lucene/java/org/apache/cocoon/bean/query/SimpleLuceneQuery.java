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
import java.util.List;
import org.apache.cocoon.components.search.LuceneCocoonSearcher;
import org.apache.cocoon.ProcessingException;

/**
 * The interface of a query bean.
 * <p>
 *   This component defines an interface for searching.
 *   The idea is to abstract the process of searching into a Bean to be manipulated by CForms.
 * </p>
 *
 * @version CVS $Id: SimpleLuceneQuery.java,v 1.2 2004/06/24 11:31:17 cziegeler Exp $
 */
public interface SimpleLuceneQuery {

    /**
     * The AND_BOOL name of this bean.
     * <p>
     *   The value representing a Boolean AND operation.
     *   ie. <code>and</code>
     * </p>
     */
    public static final String AND_BOOL = "and";

    /**
     * The OR_BOOL name of this bean.
     * <p>
     *   The value representing a Boolean OR operation.
     *   ie. <code>or</code>
     * </p>
     */
    public static final String OR_BOOL = "or";

    /**
     * Gets the Bean to perform it's query
     * <p>
     *   The searcher specifies which LuceneCocoonSearcher to use for this search
     *   It needs to have been initialised properly before use
     * </p>
     *
     * @param  searcher  The <code>LuceneCocoonSearcher</code> to use for this search
     * @return a List of Maps, each representing a Hit. 
     * @exception  ProcessingException thrown by the searcher
     * @exception  IOException thrown when the searcher's directory cannot be found
     */
    public List search (LuceneCocoonSearcher searcher)  throws IOException, ProcessingException;

}
