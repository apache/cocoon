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
package org.apache.butterfly.source;

import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.butterfly.source.impl.FileSource;
import org.apache.butterfly.test.SitemapComponentTestCase;


/**
 * Description of SourceResolverTestCase.
 * 
 * @version CVS $Id: SourceResolverTestCase.java,v 1.2 2004/07/24 20:31:57 ugo Exp $
 */
public class SourceResolverTestCase extends SitemapComponentTestCase {

    public SourceResolverTestCase(String name) {
        super(name);
    }
    
    public void testFileSourceExists() throws MalformedURLException, IOException {
        SourceResolver resolver = (SourceResolver) getBean("sourceResolver");
        Source src = resolver.resolveURI("testdata/test1.xml");
        assertTrue("Source is not a FileSource", src instanceof FileSource);
        assertTrue("FileSource does not exist", src.exists());
    }
    
    public void testFileSourceLength() throws MalformedURLException, IOException {
        SourceResolver resolver = (SourceResolver) getBean("sourceResolver");
        Source src = resolver.resolveURI("testdata/test1.xml");
        assertEquals("FileSource is not of the expected length", 739, src.getContentLength());
    }
}
