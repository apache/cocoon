/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.components.crawler;


import org.apache.avalon.framework.component.Component;

import java.net.URL;
import java.util.Iterator;

/**
 * The avalon behavioural component interface of crawling.
 *
 * @author <a href="mailto:berni_huber@a1.net">Bernhard Huber</a>
 * @version CVS $Id: CocoonCrawler.java,v 1.4 2003/10/22 22:04:25 unico Exp $
 */
public interface CocoonCrawler
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
	 * @param depth  The maximum depth to crawl to. -1 for no maxiumum.
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

