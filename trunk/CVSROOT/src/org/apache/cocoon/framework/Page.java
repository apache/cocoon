/*-- $Id: Page.java,v 1.7 2001-03-01 16:05:47 greenrd Exp $ -- 

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
package org.apache.cocoon.framework;

import java.util.*;
import javax.servlet.http.HttpServletRequest;
import org.w3c.dom.*;

/**
 * The Page wrapper class.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.7 $ $Date: 2001-03-01 16:05:47 $
 */

public class Page implements java.io.Serializable, Changeable, Cacheable {
	
    private byte[] content;
    private String contentType;

    private boolean cached = false;
    private Vector changeables = new Vector(3);

    public byte[] getContent() {
	return this.content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
	
    public String getContentType() {
	return this.contentType;
    }

    public void setContentType(String type) {
        if (type != null) this.contentType = type;
    }
    
    public boolean isText() {
    	return this.contentType.startsWith("text");
    }

    public boolean allowsMarkup() {
        return contentType.equals("text/xml")
	        || contentType.equals("text/html")
	        || contentType.equals("text/svg")
	        || contentType.equals("text/mathml");
    }
    
    public Enumeration getChangeables() {
        return this.changeables.elements();
    }
    
    public void setChangeable(Changeable change) {
        this.changeables.addElement(change);
    }
    
    public boolean isCached() {
        return cached;
    }

    public boolean hasChanged (Object context) {
        HttpServletRequest request = (HttpServletRequest) context;

        Enumeration e = getChangeables();
        while (e.hasMoreElements()) {
            Changeable c = (Changeable) e.nextElement();
            if (c.hasChanged (request)) {
                return true;
            }
        }
        return false;
    }

    public boolean isCacheable (HttpServletRequest request) {

        // Certain types of requests should never be cached
        // Surprisingly, according to HTTP 1.1 spec, POST is
        // not one of them!
        String method = request.getMethod ();
        if (method != null
            && (method.equals ("OPTIONS") || method.equals ("PUT") ||
                method.equals ("DELETE") || method.equals ("TRACE")))
            return false;

        Enumeration e = getChangeables();
        while (e.hasMoreElements()) {
            Object x = e.nextElement ();
            if (!(x instanceof Cacheable)) return false;

            Cacheable c = (Cacheable) x;
            if (!c.isCacheable(request)) return false;
        }
        return true;
    }
    
    public void setCached(boolean cached) {
        this.cached = cached;
    }
}
