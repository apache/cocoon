/*-- $Id: LinkEncodingProcessor.java,v 1.5 2000-12-12 17:47:59 greenrd Exp $ --

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) @year@ The Apache Software Foundation. All rights reserved.

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

 4. The names "Cocoon" and  "Apache Software Foundation"  must not be used to
    endorse  or promote  products derived  from this  software without  prior
    written permission. For written permission, please contact
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

package org.apache.cocoon.processor;

import java.net.*;
import java.util.Dictionary;
import org.w3c.dom.*;
import javax.servlet.http.*;
import org.apache.cocoon.collections.Predicate;
import org.apache.cocoon.framework.*;

/**
 * This class implements a LinkEncodingProcessor which encodes all URLs
 * found in attributes specified by a Predicate. It is useful for use
 * with cookie-less clients. You could use Xalan extension functions instead,
 * but this is probably easier and faster, and doesn't require the use of
 * Xalan-specific features in your stylesheet.
 *
 * @author <a href="mailto:greenrd@hotmail.com">Robin Green</a>
 * @version $Revision: 1.5 $ $Date: 2000-12-12 17:47:59 $
 */

public class LinkEncodingProcessor implements Processor, Status {

    public void init (Director director) {
    }

    public Document process(Document document, Dictionary parameters) throws Exception {
        HttpServletRequest request = (HttpServletRequest) parameters.get ("request");
        HttpServletResponse response = (HttpServletResponse) parameters.get ("response");
        recurse (document.getDocumentElement (), 
                 new DefaultLinkPredicate (request), 
                 response);
        return document;
    }

    protected void recurse 
      (Element e, Predicate linkPredicate, HttpServletResponse response) {
        NodeList nl = e.getElementsByTagName ("*"); // all elements, recursively
        int n = nl.getLength ();
        for (int j = 0; j < n; j++) {
            NamedNodeMap atts = e.getAttributes ();
            int m = atts.getLength ();
            for (int i = 0; i < m; i++) {
                Attr att = (Attr) atts.item (i);
                if (linkPredicate.matches (att)) {
                    // Have to use deprecated method for servlet-2.1 API compatibility
                    att.setValue (response.encodeUrl (att.getValue ()));
                }
            }
        }
    }

    /**
     * Matches href attributes which point to the same hostname as the request
     * (implicitly or explicitly) and are well-formed URLs.
     */
    private class DefaultLinkPredicate implements Predicate {

        protected URL requestBase;
        protected String hostName;
 
        public DefaultLinkPredicate (HttpServletRequest request)
        throws MalformedURLException {
            this.requestBase = new URL 
              (HttpUtils.getRequestURL (request.getRequestURI ()).toString ());
            this.hostName = requestBase.getHost ();
        }

        public boolean matches (Object x) {
             Attr attr = (Attr) x;
             String name = attr.getName ();
             if (!name.equalsIgnoreCase ("href") 
                 || !name.equalsIgnoreCase ("action")) {
               return false;
             }
             String href = attr.getValue ();
             try {
                 URL full = new URL (requestBase, href);
                 String hrefHost = full.getHost ();
                 // Allow for not-fully--qualified domain names in hrefs
                 return (hrefHost.indexOf ('.') == -1) 
                     ? (hostName + '.').startsWith (hrefHost + '.')
                     : hostName.equals (hrefHost);
             }
             catch (MalformedURLException ex) {
                 return false;
             }
        }
    }

    public String getStatus () {
        return "Link Encoding Processor";
    }

    public boolean hasChanged (Object x) {
        return false;
    }
}
