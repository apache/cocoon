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
package org.apache.cocoon.bean;

/**
 * Interface allowing caller to install a listener so that it can be informed
 * as the bean makes progress through the links to be called.
 *
 * @author <a href="mailto:uv@upaya.co.uk">Upayavira</a>
 * @version CVS $Id: BeanListener.java,v 1.5 2004/01/23 11:07:28 cziegeler Exp $
 */
public interface BeanListener {

    /**
     * Report a page as successfully generated
     * @param sourceURI      URI of the page to generate
     * @param destinationURI URI of page that has been generated
     * @param pageSize       The size of the page
     * @param linksInPage    Number of links found in this page
     * @param pagesRemaining Number of pages still to be generated
     * @param pagesComplete  Number of pages already generated
     * @param timeTaken      Length of time for processing in millis
     */
    void pageGenerated(String sourceURI,
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
     * @param uri      The uri for the report
     * @param message  The message for skipping
     */
    void pageSkipped(String uri, String message);

    /**
     * Report a general message about operation of the bean
     * @param msg            The message to be reported
     */
    void messageGenerated(String msg);

    /**
     * Report a warning about something non-fatal that happened within
     * the bean.
     * @param uri            The page being generated when warning was triggered
     * @param warning        The warning to be reported
     */
    void warningGenerated(String uri, String warning);

    /**
     * Report a broken link
     * @param uri            The URI that failed to be generated
     * @param message        A reason why the link was not generated
     */
    void brokenLinkFound(String uri, String parentURI, String message, Throwable t);

    /**
     * Signals completion of the generation process. This method can
     * be used to write out reports, display time generation duration,
     * etc.
     */
    void complete();
}
