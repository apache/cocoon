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
import org.apache.lucene.store.Directory;

import java.net.URL;

/**
 * The avalon behavioural component interface of an indexer.
 *
 * @author <a href="mailto:berni_huber@a1.net">Bernhard Huber</a>
 * @version CVS $Id: LuceneCocoonIndexer.java,v 1.3 2004/03/05 13:01:59 bdelacretaz Exp $
 */
public interface LuceneCocoonIndexer extends Component
{
    /**
     *Description of the Field
     *
     * @since
     */
    String ROLE = "org.apache.cocoon.components.search.LuceneCocoonIndexer";


    /**
     *Sets the analyzer attribute of the LuceneCocoonIndexer object
     *
     * @param  analyzer  The new analyzer value
     * @since
     */
    void setAnalyzer(Analyzer analyzer);


    /**
     *Description of the Method
     *
     * @param  index                    Description of Parameter
     * @param  create                   Description of Parameter
     * @param  base_url                 Description of Parameter
     * @exception  ProcessingException  Description of Exception
     * @since
     */
    void index(Directory index, boolean create, URL base_url)
             throws ProcessingException;
}
