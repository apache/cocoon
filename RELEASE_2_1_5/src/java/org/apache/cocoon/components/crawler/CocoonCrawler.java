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
package org.apache.cocoon.components.crawler;


import org.apache.avalon.framework.component.Component;

import java.net.URL;
import java.util.Iterator;

/**
 * The avalon behavioural component interface of crawling.
 *
 * @author <a href="mailto:berni_huber@a1.net">Bernhard Huber</a>
 * @version CVS $Id: CocoonCrawler.java,v 1.5 2004/03/05 13:02:46 bdelacretaz Exp $
 */
public interface CocoonCrawler extends Component
{
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

