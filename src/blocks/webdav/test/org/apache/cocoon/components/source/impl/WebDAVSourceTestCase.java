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
package org.apache.cocoon.components.source.impl;

import java.util.Iterator;

import org.apache.avalon.excalibur.testcase.ExcaliburTestCase;
import org.apache.cocoon.components.source.impl.WebDAVSource;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.TraversableSource;
import org.apache.webdav.lib.WebdavResource;

/**
 * @version $Id: WebDAVSourceTestCase.java,v 1.1 2004/03/27 15:59:16 unico Exp $
 */
public class WebDAVSourceTestCase extends ExcaliburTestCase {
    
    private String m_scheme = "webdav";
    private String m_credentials = "site:site";
    private String m_authority = "localhost:8888/webdav";
    private String m_path = "/";
    private String m_name = "files";
    private String m_qs = "?foo=bar";
    private String m_location = m_scheme + "://" + m_credentials + "@" + m_authority + m_path + m_name + m_qs;
    private String m_secure = m_scheme + "://" + m_authority + m_path + m_name + m_qs;
    private String m_options = "&cocoon:webdav-action=" + WebdavResource.NOACTION + "&cocoon:webdav-depth=0";
    
    
    public WebDAVSourceTestCase(String name) {
        super(name);
    }
    
    public void testResolve() throws Exception {
        SourceResolver resolver = (SourceResolver) lookup(SourceResolver.ROLE);
        String uri = m_location + m_options;
        WebDAVSource source = (WebDAVSource) resolver.resolveURI(uri);
        assertEquals(m_location, source.getURI());
        assertEquals(m_scheme, source.getScheme());
        assertEquals(m_name, source.getName());
        assertEquals(m_secure, source.getSecureURI());
        resolver.release(source);
    }

    public void testCollection() throws Exception {
        SourceResolver resolver = (SourceResolver) lookup(SourceResolver.ROLE);
        String uri = m_location + m_options;
        TraversableSource source = (TraversableSource) resolver.resolveURI(uri);
        assertTrue(source.isCollection());
        assertTrue(source.exists());
        Iterator children = source.getChildren().iterator();
        if (children.hasNext()) {
            TraversableSource child = (TraversableSource) children.next();
            assertEquals(m_scheme, child.getScheme());
            TraversableSource parent = (TraversableSource) child.getParent();
            assertEquals(m_scheme, parent.getScheme());
            assertEquals(m_name, parent.getName());
            assertTrue(parent.isCollection());
        }
        resolver.release(source);
    }
    
}
