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
package org.apache.cocoon.components.xscript;

import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.SourceNotFoundException;

import java.io.IOException;
import java.io.InputStream;

/**
 * An <code>XScriptObject</code> created from the contents of a URL.
 *
 * @author <a href="mailto:ovidiu@cup.hp.com">Ovidiu Predescu</a>
 * @version CVS $Id: XScriptObjectFromURL.java,v 1.3 2004/02/07 15:20:09 joerg Exp $
 * @since August 30, 2001
 */
public class XScriptObjectFromURL extends XScriptObject {

    /**
     * The content obtained from this URL becomes the content of this
     * instance.
     */
    String systemId;

    /**
     * When was the content of the URL last modified.
     */
    long lastModified;


    public XScriptObjectFromURL(XScriptManager manager, String systemId) {
        super(manager);
        this.systemId = systemId;
    }

    public InputStream getInputStream() throws IOException, SourceNotFoundException {
        SourceResolver resolver = null;
        Source source = null;
        try {
            resolver = (SourceResolver) serviceManager.lookup(SourceResolver.ROLE);
            source = resolver.resolveURI(this.systemId);
            return source.getInputStream();
        } catch (Exception e) {
            throw new SourceException("Exception during processing of " + this.systemId, e);
        } finally {
            if (resolver != null) {
                resolver.release(source);
                serviceManager.release(resolver);
            }
        }
    }

    public long getContentLength() {
        return -1;
    }

    public long getLastModified() {
        return 0;
    }

    public String toString() {
        return new StringBuffer("XScriptObjectFromURL(systemId = ").append(systemId).append(")").toString();
    }

    public String getURI() {
        // FIXME: generate a real system id to represent this object
        return "xscript:url:" + systemId;
    }
}
