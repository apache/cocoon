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
package org.apache.cocoon.components.crawler;

import java.net.URL;
import java.util.Iterator;

/**
 * The avalon behavioural component interface of crawling.
 *
 * @version $Id$
 */
public interface CocoonCrawler {
    /**
     * Role name of this avalon component.
     * Its value is <code>org.apache.cocoon.components.crawler.CocoonCrawler</code>.
     */
    String ROLE = CocoonCrawler.class.getName();


    /**
     * This is the same as calling crawl(url,-1);
     *
     * @param  url  The URL to start crawling from.
     */
    void crawl(URL url);
    
    
	/**
     * start crawling the URL.
     * <p>
     *   Calling this method initiates the crawling and tells the
     *   crawler not to crawl beyond a maximum depth.
     * </p>
     * 
	 * @param url  The URL to start crawling from
	 * @param maxDepth  The maximum depth to crawl to. -1 for no maxiumum.
	 */
    void crawl(URL url, int maxDepth);
    

    /**
     * Iterate over crawling URLs.
     * <p>
     *   This iterator will returns URL as result of crawling
     *   the base URL passed via crawling().
     * </p>
     *
     * @return    Iterator iterates over crawling URLs.
     */
    Iterator iterator();
}

