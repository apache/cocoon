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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import org.apache.avalon.excalibur.testcase.ExcaliburTestCase;
import org.apache.cocoon.components.source.impl.WebDAVSource;
import org.apache.excalibur.source.ModifiableTraversableSource;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.TraversableSource;
import org.apache.webdav.lib.WebdavResource;

/**
 * @version $Id: WebDAVSourceTestCase.java,v 1.3 2004/03/27 17:40:11 unico Exp $
 */
public class WebDAVSourceTestCase extends ExcaliburTestCase {
    
    private String m_scheme = "webdav";
    private String m_credentials = "usr:pwd";
    private String m_authority = "localhost:8888";
    private String m_path = "/webdav/";
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

    public void testTraversal() throws Exception {
//        SourceResolver resolver = (SourceResolver) lookup(SourceResolver.ROLE);
//        String uri = m_location + m_options;
//        TraversableSource source = (TraversableSource) resolver.resolveURI(uri);
//        assertTrue(source.isCollection());
//        assertTrue(source.exists());
//        Iterator children = source.getChildren().iterator();
//        if (children.hasNext()) {
//            TraversableSource child = (TraversableSource) children.next();
//            assertEquals(m_scheme, child.getScheme());
//            TraversableSource parent = (TraversableSource) child.getParent();
//            assertEquals(m_scheme, parent.getScheme());
//            assertEquals(m_name, parent.getName());
//            assertTrue(parent.isCollection());
//            resolver.release(child);
//        }
//        resolver.release(source);
    }

    public void testModification() throws Exception {
//        SourceResolver resolver = (SourceResolver) lookup(SourceResolver.ROLE);
//        String uri = m_location + m_options;
//        ModifiableTraversableSource source = (ModifiableTraversableSource) resolver.resolveURI(uri);
//        ModifiableTraversableSource child = (ModifiableTraversableSource) source.getChild("newcol");
//        
//        assertTrue(!child.exists());
//        child.makeCollection();
//        assertTrue(child.exists());
//        child.delete();
//        assertTrue(!child.exists());
//        
//        resolver.release(child);
//        resolver.release(source);
//        
//        source = (ModifiableTraversableSource) resolver.resolveURI(uri);
//        child = (ModifiableTraversableSource) source.getChild("newdoc.txt");
//        assertTrue(!child.exists());
//        
//        // create document
//        String hello = "hello world";
//        OutputStream out = child.getOutputStream();
//        out.write(hello.getBytes());
//        out.close();
//        
//        assertTrue(child.exists());
//        
//        // read contents
//        byte[] read = new byte[hello.length()];
//        InputStream in = child.getInputStream();
//        in.read(read);
//        
//        // compare
//        assertEquals(hello, new String(read));
//        
//        child.delete();
//        assertTrue(!child.exists());
//        
//        resolver.release(source);
//        resolver.release(child);
    }
}
