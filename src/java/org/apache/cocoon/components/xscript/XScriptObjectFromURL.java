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
 * @version CVS $Id: XScriptObjectFromURL.java,v 1.4 2004/03/05 13:02:54 bdelacretaz Exp $
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
