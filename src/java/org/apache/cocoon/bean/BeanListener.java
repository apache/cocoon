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
package org.apache.cocoon.bean;

/**
 * Interface allowing caller to install a listener so that it can be informed
 * as the bean makes progress through the links to be called.
 *
 * @author <a href="mailto:uv@upaya.co.uk">Upayavira</a>
 * @version CVS $Id: BeanListener.java,v 1.5 2004/03/05 13:02:45 bdelacretaz Exp $
 */
public interface BeanListener {

    /**
     * Report a page as successfully generated
     * @param sourceURI
     * @param destinationURI
     * @param pageSize
     * @param linksInPage    Number of links found in this page
     * @param newLinksinPage
     * @param pagesRemaining Number of pages still to be generated
     * @param pagesComplete
     * @param timeTaken
     */
    public void pageGenerated(String sourceURI,
                              String destinationURI, 
                              int pageSize,
                              int linksInPage, 
                              int newLinksinPage, 
                              int pagesRemaining, 
                              int pagesComplete, 
                              long timeTaken);
                              
    /**
     * Report a that was skipped because its URI matched an
     * include/exclude pattern.
     * @param uri
     * @param message            
     */
    public void pageSkipped(String uri, String message);
    
    /**
     * Report a general message about operation of the bean
     * @param msg            The message to be reported
     */
    public void messageGenerated(String msg);

    /**
     * Report a warning about something non-fatal that happened within
     * the bean.
     * @param uri            The page being generated when warning was triggered
     * @param warning        The warning to be reported
     */
    public void warningGenerated(String uri, String warning);

    /**
     * Report a broken link
     * @param uri            The URI that failed to be generated
     * @param message        A reason why the link was not generated
     */
    public void brokenLinkFound(String uri, String parentURI, String message, Throwable t);
    
    /**
     * Signals completion of the generation process. This method can
     * be used to write out reports, display time generation duration,
     * etc.
     */
    public void complete();
}
