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
package org.apache.cocoon.portal.wsrp.consumer;

import java.util.HashMap;
import java.util.Map;

import org.apache.cocoon.util.AbstractLogEnabled;
import org.apache.wsrp4j.consumer.URLGenerator;
import org.apache.wsrp4j.consumer.URLRewriter;
import org.apache.wsrp4j.util.Constants;

/**
 * Implements the URLRewriter interface providing a method
 * to rewrite urls (Consumer URL Rewriting).<br/>
 *
 * @version $Id$
 */
public class URLRewriterImpl
    extends AbstractLogEnabled
    implements URLRewriter {

    /** The url generator. */
    protected URLGenerator urlGenerator;

    /**
     * @see org.apache.wsrp4j.consumer.URLRewriter#setURLGenerator(org.apache.wsrp4j.consumer.URLGenerator)
     */
    public void setURLGenerator(URLGenerator urlGenerator) {
        this.urlGenerator = urlGenerator;
    }

    /**
     * Rewriting: get url from URLGenerator and append it<br/>
     * 
     * @param markup
     * @param rewriteURL
     */
    protected void rewrite(StringBuffer markup, String rewriteURL) {
    	if ( rewriteURL.startsWith(Constants.REWRITE_START + Constants.PARAMS_START) ) {
    		// handle URL rewriting        		
			Map params = createParameterMap(rewriteURL);

			// What kind of link has to be rewritten?
			if (rewriteURL.indexOf(Constants.URL_TYPE_BLOCKINGACTION) != -1) {
                markup.append(urlGenerator.getBlockingActionURL(params));
			} else if (rewriteURL.indexOf(Constants.URL_TYPE_RENDER) != -1) {
				markup.append(urlGenerator.getRenderURL(params));
			} else if (rewriteURL.indexOf(Constants.URL_TYPE_RESOURCE) != -1) {
				markup.append(urlGenerator.getResourceURL(params));
			}
    	} else if (rewriteURL.startsWith(Constants.REWRITE_START + Constants.NAMESPACE_START) ) {
    		markup.append(urlGenerator.getNamespacedToken(""));
    	} else {
            this.getLogger().error("No valid rewrite expression found in: " + rewriteURL);
    	}
    }

    /**
     * Extracts parameters from url to be rewritten copies them into a map.<br/>
     * 
     * @param rewriteURL
     * @return Map
     */
    protected Map createParameterMap(String rewriteURL) {

        Map params = new HashMap();

        if (rewriteURL.indexOf(Constants.URL_TYPE_BLOCKINGACTION) != -1) {
            params.put(Constants.URL_TYPE, Constants.URL_TYPE_BLOCKINGACTION);
        } else if (rewriteURL.indexOf(Constants.URL_TYPE_RENDER) != -1) {
            params.put(Constants.URL_TYPE, Constants.URL_TYPE_RENDER);
        } else if (rewriteURL.indexOf(Constants.URL_TYPE_RESOURCE) != -1) {
            params.put(Constants.URL_TYPE, Constants.URL_TYPE_RESOURCE);
        } else {
            this.getLogger().error("no valid url-type: " + rewriteURL);
        }

        // begin parsing
        int equals = 0;
        int next = 0;
        int end = rewriteURL.indexOf(Constants.REWRITE_END);
        int index = rewriteURL.indexOf(Constants.NEXT_PARAM);
        int lengthNext = 0;
        String subNext = null;

        while (index != -1) {
			// support "&amp;" as parameter seperator
			// see if &amp; was used
			subNext = rewriteURL.substring(index, index + Constants.NEXT_PARAM_AMP.length());
			if (subNext.equals(Constants.NEXT_PARAM_AMP)) {
				lengthNext = Constants.NEXT_PARAM_AMP.length();
			}
			else {
				lengthNext = Constants.NEXT_PARAM.length();
			}
			 
            equals = rewriteURL.indexOf(Constants.EQUALS, index + lengthNext);
            next = rewriteURL.indexOf(Constants.NEXT_PARAM, equals);

            if (equals != -1) {
                if (next != -1) {
                    params.put(rewriteURL.substring(index + lengthNext, equals), rewriteURL.substring(equals + 1, next));
                } else {
                    params.put(rewriteURL.substring(index + lengthNext, equals), rewriteURL.substring(equals + 1, end));
                }
            }
            index = next;
        }

        return params;

    }

    /**
     * Parses markup and performs URL rewriting.<br/>
     *
     * Principle:<br/>
     * - Iterate over markup-string once and copy processed markup to result
     *   buffer (StringBuffer)<br/>
     * - If url to be rewritten found (during markup iteration),<br/>
     *   ... append markup before url to result buffer,<br/>
     *   ... perform rewriting (call URLGenerator) and append rewritten url to result buffer.<br/>
     *
     * Incomplete rewrite-pairs (e.g. a rewrite-begin-token not followed by a
     * rewrite-end-token) are considered as 'normal' markup.<br/>
     *
     * @param markup String representing the markup to be processed.
     *
     * @return String representing the processed markup.
     *    
	 * @see org.apache.wsrp4j.consumer.URLRewriter#rewriteURLs(java.lang.String)
	 */
	public String rewriteURLs(String markup) {
		StringBuffer resultMarkup = new StringBuffer("");
		int markupIndex = 0;
		int rewriteStartPos = -1;
		int rewriteEndPos = -1;
		int currentPos = 0;
		String exprType = null;
		
		// loop through the markup, find rewrite expressions, rewrite them 
		while ( markupIndex < markup.length() ) {
			rewriteStartPos = -1;
			rewriteEndPos = -1;

			// get fist occurance of wsrp rewrite expression
			rewriteStartPos = markup.indexOf(Constants.REWRITE_START,markupIndex);
			
			if (! ( rewriteStartPos == -1 ||
					( rewriteStartPos + Constants.REWRITE_START.length() - 1 ) >
					( markup.length() - 2 ) ) ) {
				// found a rewrite start token, and token is not at the end of markup so we can
				// determine the rewrite type, i.e. there is at least 1 char after the rewrite start token
					
				// namespace or URL? The single char string after the token decides
				exprType = markup.substring(rewriteStartPos+Constants.REWRITE_START.length() - 1 + 1,
				   						    rewriteStartPos+Constants.REWRITE_START.length() - 1 + 2);
			
				if ( exprType.equals(Constants.NAMESPACE_START)) {
					// ok, we have a namespace rewrite here						
					rewriteEndPos = rewriteStartPos + Constants.REWRITE_START.length()
									+ Constants.NAMESPACE_START.length() - 1;
				} else if ( exprType.equals(Constants.PARAMS_START) ) {
					// ok, we have a URL rewrite here
					// get the position of the end token
					rewriteEndPos = markup.indexOf(Constants.REWRITE_END,markupIndex);
					if (rewriteEndPos != -1) {
						// now let's see if we find a rewrite start token nearer to the end token
						currentPos = rewriteStartPos;

						while ((currentPos != -1) && (currentPos < rewriteEndPos)) {
							 // update rewriteStartPos with position of found rewrite begin token being 'nearer' 
							rewriteStartPos = currentPos;
							// look for next URL rewrite start expression
							currentPos = markup.indexOf(Constants.REWRITE_START+Constants.PARAMS_START,
														rewriteStartPos + Constants.REWRITE_START.length()
														+ Constants.PARAMS_START.length());
						}
						rewriteEndPos = rewriteEndPos + Constants.REWRITE_END.length() - 1;
					}
				}
			}
			
			if ( (rewriteStartPos != -1) && (rewriteEndPos != -1) ) {
				// append markup before rewrite expression
				resultMarkup.append(markup.substring(markupIndex,rewriteStartPos));
				// append rewritten expression
				rewrite(resultMarkup,markup.substring(rewriteStartPos,rewriteEndPos+1));
				// set markup index after the last char of the rewriteExpression
				markupIndex = rewriteEndPos + 1;
			} else {
				// append rest of markup
				resultMarkup.append(markup.substring(markupIndex,markup.length()));
				markupIndex = markup.length();
			}
		}

		return resultMarkup.toString();
	}
}


