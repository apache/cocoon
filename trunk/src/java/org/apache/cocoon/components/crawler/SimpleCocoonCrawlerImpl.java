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

import org.apache.avalon.excalibur.pool.Recyclable;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.cocoon.Constants;
import org.apache.cocoon.util.Tokenizer;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * A simple cocoon crawler.
 *
 * @author     <a href="mailto:berni_huber@a1.net">Bernhard Huber</a>
 * @version CVS $Id: SimpleCocoonCrawlerImpl.java,v 1.4 2003/12/06 21:22:09 cziegeler Exp $
 * 
 * @avalon.component
 * @avalon.service type="CocoonCrawler"
 * @x-avalon.lifestyle type="pooled"
 * @x-avalon.info name="cocoon-crawler"
 */
public class SimpleCocoonCrawlerImpl extends AbstractLogEnabled
        implements CocoonCrawler, Configurable, Disposable, Recyclable {

    /**
     * Config element name specifying expected link content-typ.
     * <p>
     *   Its value is <code>link-content-type</code>.
     * </p>
     */
    public final static String LINK_CONTENT_TYPE_CONFIG = "link-content-type";

    /**
     * Default value of <code>link-content-type</code> configuration value.
     * <p>
     *   Its value is <code>application/x-cocoon-links</code>.
     * </p>
     */
    public final String LINK_CONTENT_TYPE_DEFAULT = Constants.LINK_CONTENT_TYPE;

    /**
     * Config element name specifying query-string appendend for requesting links
     * of an URL.
     * <p>
     *  Its value is <code>link-view-query</code>.
     * </p>
     */
    public final static String LINK_VIEW_QUERY_CONFIG = "link-view-query";

    /**
     * Default value of <code>link-view-query</code> configuration option.
     * <p>
     *   Its value is <code>?cocoon-view=links</code>.
     * </p>
     */
    public final static String LINK_VIEW_QUERY_DEFAULT = "cocoon-view=links";

    /**
     * Config element name specifying excluding regular expression pattern.
     * <p>
     *  Its value is <code>exclude</code>.
     * </p>
     */
    public final static String EXCLUDE_CONFIG = "exclude";

    /**
     * Config element name specifying including regular expression pattern.
     * <p>
     *  Its value is <code>include</code>.
     * </p>
     */
    public final static String INCLUDE_CONFIG = "include";

    /**
     * Config element name specifying http header value for user-Agent.
     * <p>
     *  Its value is <code>user-agent</code>.
     * </p>
     */
    public final static String USER_AGENT_CONFIG = "user-agent";

    /**
     * Default value of <code>user-agent</code> configuration option.
     * @see Constants#COMPLETE_NAME
     */
    public final static String USER_AGENT_DEFAULT = Constants.COMPLETE_NAME;

    /**
     * Config element name specifying http header value for accept.
     * <p>
     *  Its value is <code>accept</code>.
     * </p>
     */
    public final static String ACCEPT_CONFIG = "accept";

    /**
     * Default value of <code>accept</code> configuration option.
     * <p>
     *   Its value is <code>* / *</code>
     * </p>
     */
    public final static String ACCEPT_DEFAULT = "*/*";


    private String linkViewQuery = LINK_VIEW_QUERY_DEFAULT;
    private String linkContentType = LINK_CONTENT_TYPE_DEFAULT;
    private HashSet excludeCrawlingURL;
    private HashSet includeCrawlingURL;
    private String userAgent = USER_AGENT_DEFAULT;
    private String accept = ACCEPT_DEFAULT;

	private int depth;

    private HashSet crawled;
    private HashSet urlsToProcess;
    private HashSet urlsNextDepth;
    
	
	
    /**
     * Constructor for the SimpleCocoonCrawlerImpl object
     */
    public SimpleCocoonCrawlerImpl() {
        // by default include everything
        includeCrawlingURL = null;
        // by default exclude common image patterns
        excludeCrawlingURL = null;
    }


    /**
     * Configure the crawler component.
     * <p>
     *  Configure can specify which URI to include, and which URI to exclude
     *  from crawling. You specify the patterns as regular expressions.
     * </p>
     * <p>
     *  Morover you can configure
     *  the required content-type of crawling request, and the
     *  query-string appended to each crawling request.
     * </p>
     * <pre><tt>
     * &lt;include&gt;.*\.html?&lt;/exclude&gt; or &lt;exclude&gt;.*\.html?, .*\.xsp&lt;/exclude&gt;
     * &lt;exclude&gt;.*\.gif&lt;/exclude&gt; or &lt;exclude&gt;.*\.gif, .*\.jpe?g&lt;/exclude&gt;
     * &lt;link-content-type&gt; application/x-cocoon-links &lt;/link-content-type&gt;
     * &lt;link-view-query&gt; ?cocoon-view=links &lt;/link-view-query&gt;
     * &lt;crawl-domain&gt; host | web &lt;/crawl-domain&gt;
     * </tt></pre>
     *
     * @param  configuration               XML configuration of this avalon component.
     * @exception  ConfigurationException  is throwing if configuration is invalid.
     */
    public void configure(Configuration configuration)
            throws ConfigurationException {

        Configuration[] children;
        children = configuration.getChildren(INCLUDE_CONFIG);
        if (children.length > 0) {
            includeCrawlingURL = new HashSet();
            for (int i = 0; i < children.length; i++) {
                String pattern = children[i].getValue();
                try {
                    Tokenizer t = new Tokenizer(pattern, ", ");
                    while (t.hasMoreTokens()) {
                        String tokenized_pattern = t.nextToken();
                        this.includeCrawlingURL.add(new RE(tokenized_pattern));
                    }
                } catch (RESyntaxException rese) {
                    getLogger().error("Cannot create including regular-expression for " +
                            pattern, rese);
                }
            }
        } else {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Include all URLs");
            }
        }

        children = configuration.getChildren(EXCLUDE_CONFIG);
        if (children.length > 0) {
            excludeCrawlingURL = new HashSet();
            for (int i = 0; i < children.length; i++) {
                String pattern = children[i].getValue();
                try {
                    Tokenizer t = new Tokenizer(pattern, ", ");
                    while (t.hasMoreTokens()) {
                        String tokenized_pattern = t.nextToken();
                        this.excludeCrawlingURL.add(new RE(tokenized_pattern));
                    }
                } catch (RESyntaxException rese) {
                    getLogger().error("Cannot create excluding regular-expression for " +
                            pattern, rese);
                }
            }
        } else {
            excludeCrawlingURL = new HashSet();
            setDefaultExcludeFromCrawling();
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Exclude default URLs only");
            }
        }

        Configuration child;
        String value;
        child = configuration.getChild(LINK_CONTENT_TYPE_CONFIG, false);
        if (child != null) {
            value = child.getValue();
            if (value != null && value.length() > 0) {
                this.linkContentType = value.trim();
            }
        }
        child = configuration.getChild(LINK_VIEW_QUERY_CONFIG, false);
        if (child != null) {
            value = child.getValue();
            if (value != null && value.length() > 0) {
                this.linkViewQuery = value.trim();
            }
        }

        child = configuration.getChild(USER_AGENT_CONFIG, false);
        if (child != null) {
            value = child.getValue();
            if (value != null && value.length() > 0) {
                this.userAgent = value;
            }
        }

        child = configuration.getChild(ACCEPT_CONFIG, false);
        if (child != null) {
            value = child.getValue();
            if (value != null && value.length() > 0) {
                this.accept = value;
            }
        }
        
    }


    /**
     * dispose at end of life cycle, releasing all resources.
     */
    public void dispose() {
        crawled = null;
        urlsToProcess = null;
        urlsNextDepth = null;
        excludeCrawlingURL = null;
        includeCrawlingURL = null;
    }


    /**
     * recylcle this object, relasing resources
     */
    public void recycle() {
        crawled = null;
        urlsToProcess = null;
        urlsNextDepth = null;
        depth = -1;
    }


    /**
     * The same as calling crawl(url,-1);
     * 
	 * @param  url  Crawl this URL, getting all links from this URL.
     */
    public void crawl(URL url) {
		crawl(url, -1);
    }

	/**
	 * Start crawling a URL.
	 *
	 * <p>
	 *   Use this method to start crawling.
	 *   Get the this url, and all its children  by using <code>iterator()</code>.
	 *   The Iterator object will return URL objects.
	 * </p>
	 * <p>
	 *  You may use the crawl(), and iterator() methods the following way:
	 * </p>
	 * <pre><tt>
	 *   SimpleCocoonCrawlerImpl scci = ....;
	 *   scci.crawl( "http://foo/bar" );
	 *   Iterator i = scci.iterator();
	 *   while (i.hasNext()) {
	 *     URL url = (URL)i.next();
	 *     ...
	 *   }
	 * </tt></pre>
	 * <p>
	 *   The i.next() method returns a URL, and calculates the links of the
	 *   URL before return it.
	 * </p>
	 *
	 * @param  url  Crawl this URL, getting all links from this URL.
	 * @param  maxDepth  maximum depth to crawl to. -1 for no maximum.
	 */
	public void crawl(URL url, int maxDepth) {
		crawled = new HashSet();
		urlsToProcess = new HashSet();
		urlsNextDepth = new HashSet();
		depth = maxDepth;

		if (getLogger().isDebugEnabled()) {
			getLogger().debug("crawl URL " + url + " to depth " + maxDepth);
		}
		
		urlsToProcess.add(url);
	}


    /**
     * Return iterator, iterating over all links of the currently crawled URL.
     * <p>
     *   The Iterator object will return URL objects at its <code>next()</code>
     *   method.
     * </p>
     *
     * @return    Iterator iterator of all links from the crawl URL.
     * @since
     */
    public Iterator iterator() {
        return new CocoonCrawlerIterator(this);
    }


    /**
     * Default exclude patterns.
     * <p>
     *   By default URLs matching following patterns are excluded:
     * </p>
     * <ul>
     *   <li>.*\\.gif(\\?.*)?$ - exclude gif images</li>
     *   <li>.*\\.png(\\?.*)?$ - exclude png images</li>
     *   <li>.*\\.jpe?g(\\?.*)?$ - exclude jpeg images</li>
     *   <li>.*\\.js(\\?.*)?$ - exclude javascript </li>
     *   <li>.*\\.css(\\?.*)?$ - exclude cascaded stylesheets</li>
     * </ul>
     *
     * @since
     */
    private void setDefaultExcludeFromCrawling() {
        String[] EXCLUDE_FROM_CRAWLING_DEFAULT = {
            ".*\\.gif(\\?.*)?$",
            ".*\\.png(\\?.*)?$",
            ".*\\.jpe?g(\\?.*)?$",
            ".*\\.js(\\?.*)?$",
            ".*\\.css(\\?.*)?$"
        };

        for (int i = 0; i < EXCLUDE_FROM_CRAWLING_DEFAULT.length; i++) {
            String pattern = EXCLUDE_FROM_CRAWLING_DEFAULT[i];
            try {
                excludeCrawlingURL.add(new RE(pattern));
            } catch (RESyntaxException rese) {
                getLogger().error("Cannot create excluding regular-expression for " +
                        pattern, rese);
            }
        }
    }


    /**
     * Compute list of links from the url.
     * <p>
     *   Check for include, exclude pattern, content-type, and if url
     *   has been craweled already.
     * </p>
     *
     * @param  url  Crawl this URL
     * @return      List of URLs, which are links from url, asserting the conditions.
     * @since
     */
    private List getLinks(URL url) {
        ArrayList url_links = null;
        String sURL = url.toString();

        if (!isIncludedURL(sURL) || isExcludedURL(sURL)) {
            return null;
        }

        // don't try to get links for url which has been crawled already
        if (crawled.contains(sURL)) {
            return null;
        }

        // mark it as crawled
        crawled.add(sURL);

        // get links of url
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Getting links of URL " + sURL);
        }
        BufferedReader br = null;
        try {
            sURL = url.getFile();
            URL links = new URL(url, sURL
                    + ((sURL.indexOf("?") == -1) ? "?" : "&")
                    + linkViewQuery);
            URLConnection links_url_connection = links.openConnection();
            links_url_connection.setRequestProperty("Accept", accept);
            links_url_connection.setRequestProperty("User-Agent", userAgent);
			links_url_connection.connect();
            InputStream is = links_url_connection.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));

            String contentType = links_url_connection.getContentType();
            if (contentType == null) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Ignoring " + sURL + " (no content type)");
                }
                // there is a check on null in the calling method
                return null;
            }

            int index = contentType.indexOf(';');
            if (index != -1) {
                contentType = contentType.substring(0, index);
            }

            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Content-type: " + contentType);
            }

            if (contentType.equals(linkContentType)) {
                url_links = new ArrayList();

                // content is supposed to be a list of links,
                // relative to current URL
                String line;
                while ((line = br.readLine()) != null) {
                    URL new_url = new URL(url, line);
                    boolean add_url = true;
                    // don't add new_url twice
                    if (add_url) {
                        add_url &= !url_links.contains(new_url);
                    }

                    // don't add new_url if it has been crawled already
                    if (add_url) {
                        add_url &= !crawled.contains(new_url.toString());
                    }
                    
                    // don't add if is not matched by existing include definition
                    if (add_url) {
                        add_url &= isIncludedURL(new_url.toString());
                    }

                    // don't add if is matched by existing exclude definition
                    if (add_url) {
                        add_url &= !isExcludedURL(new_url.toString());
                    }
                    if (add_url) {
                        if (getLogger().isDebugEnabled()) {
                            getLogger().debug("Add URL: " + new_url.toString());
                        }
                        url_links.add(new_url);
                    }
                }
                // now we have a list of URL which should be examined
            }
        } catch (IOException ioe) {
            getLogger().warn("Problems get links of " + url, ioe);
        } finally {
            if (br != null) {
                try {
                    br.close();
                    br = null;
                } catch (IOException ignored) {
                }
            }
        }
        return url_links;
    }

	
    /**
     * check if URL is a candidate for indexing
     *
     * @param  url  the URL to check
     * @return      The excludedURL value
     */
    private boolean isExcludedURL(String url) {
        // by default do not exclude URL for crawling
        if (excludeCrawlingURL == null) {
            return false;
        }

        final String s = url.toString();
        Iterator i = excludeCrawlingURL.iterator();
        while (i.hasNext()) {
            RE pattern = (RE) i.next();
            if (pattern.match(s)) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Excluded URL " + url);
                }
                return true;
            }
        }
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Not excluded URL " + url);
        }
        return false;
    }


    /**
     * check if URL is a candidate for indexing
     *
     * @param  url  Description of Parameter
     * @return      The includedURL value
     */
    private boolean isIncludedURL(String url) {
        // by default include URL for crawling
        if (includeCrawlingURL == null) {
            return true;
        }

        final String s = url.toString();
        Iterator i = includeCrawlingURL.iterator();
        while (i.hasNext()) {
            RE pattern = (RE) i.next();
            if (pattern.match(s)) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Included URL " + url);
                }
                return true;
            }
        }
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Not included URL " + url);
        }
        return false;
    }
        

    /**
     * Helper class implementing an Iterator
     * <p>
     *   This Iterator implementation calculates the links of an URL
     *   before returning in the next() method.
     * </p>
     *
     * @author     <a href="mailto:berni_huber@a1.net">Bernhard Huber</a>
     * @version    $Id: SimpleCocoonCrawlerImpl.java,v 1.4 2003/12/06 21:22:09 cziegeler Exp $
     */
    public static class CocoonCrawlerIterator implements Iterator {
        private SimpleCocoonCrawlerImpl cocoonCrawler;


        /**
         * Constructor for the CocoonCrawlerIterator object
         *
         * @param  cocoonCrawler  the containing CocoonCrawler instance.
         */
        CocoonCrawlerIterator(SimpleCocoonCrawlerImpl cocoonCrawler) {
            this.cocoonCrawler = cocoonCrawler;
        }


        /**
         * check if crawling is finished.
         *
         * @return    <code>true</code> if crawling has finished,
         * else <code>false</code>.
         */
        public boolean hasNext() {
            return cocoonCrawler.urlsToProcess.size() > 0 
            || cocoonCrawler.urlsNextDepth.size() > 0;
        }


        /**
         * @return    the next URL
         */
        public Object next() {
        	if (cocoonCrawler.urlsToProcess.size() == 0 
        	    && cocoonCrawler.urlsNextDepth.size() > 0) {
        	    // process queued urls belonging to the next depth level
				cocoonCrawler.urlsToProcess = cocoonCrawler.urlsNextDepth;
				cocoonCrawler.urlsNextDepth = new HashSet();
				cocoonCrawler.depth--;
    	    }
            URL url = null;
            Iterator i = cocoonCrawler.urlsToProcess.iterator();
            if (i.hasNext()) {
                // fetch a URL
                url = (URL) i.next();

                // remove it from the to-do list
                cocoonCrawler.urlsToProcess.remove(url);

				if (cocoonCrawler.depth == -1 || cocoonCrawler.depth > 0) {
					// calc all links from this url
					List url_links = cocoonCrawler.getLinks(url);
					if (url_links != null) {
						// add links of this url to the to-do list
						cocoonCrawler.urlsNextDepth.addAll(url_links);
					}					
				}
            }
            // finally return this url
            return url;
        }


        /**
         * remove is not implemented
         */
        public void remove() {
            throw new UnsupportedOperationException("remove is not implemented");
        }
    }
}

