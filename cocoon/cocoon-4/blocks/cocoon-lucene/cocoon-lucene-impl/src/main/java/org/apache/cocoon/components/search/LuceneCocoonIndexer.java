/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import org.apache.cocoon.ProcessingException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.store.Directory;

import java.net.URL;

/**
 * The avalon behavioural component interface of an indexer.
 *
 * @version $Id$
 */
public interface LuceneCocoonIndexer {
    
    String ROLE = LuceneCocoonIndexer.class.getName();


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
