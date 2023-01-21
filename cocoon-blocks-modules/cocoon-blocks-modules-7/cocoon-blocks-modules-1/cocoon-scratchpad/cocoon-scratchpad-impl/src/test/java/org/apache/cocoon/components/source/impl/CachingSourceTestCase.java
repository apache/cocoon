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

import java.io.File;
import java.io.InputStream;
import java.util.Iterator;

import org.apache.avalon.framework.context.DefaultContext;
import org.apache.cocoon.Constants;
import org.apache.cocoon.caching.IdentifierCacheKey;
import org.apache.cocoon.core.container.ContainerTestCase;
import org.apache.cocoon.environment.mock.MockContext;
import org.apache.cocoon.xml.LoggingContentHandler;
import org.apache.cocoon.xml.SaxBuffer;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;

/**
 * TODO describe class
 * 
 */
public class CachingSourceTestCase extends ContainerTestCase {
    
    public void testResolveURI() throws Exception {

        SourceResolver resolver = (SourceResolver) lookup(SourceResolver.ROLE);

        String scheme = "async-caching";
        String uri = "resource://org/apache/cocoon/components/" +
            "source/impl/cachingsourcetest.xml?foo=bar";

        // resolve CachingSource
        Source source = resolver.resolveURI(
            scheme + ":" + uri + "&cocoon:cache-expires=10");
        super.assertTrue(source instanceof CachingSource);
        
        CachingSource cachingSource = (CachingSource) source;
        assertEquals(uri, cachingSource.getSourceURI());
        assertEquals(scheme, cachingSource.getScheme());
        assertEquals(10 * 1000, cachingSource.getExpiration());
        assertEquals(new IdentifierCacheKey("source:" + uri,false), cachingSource.getCacheKey());
        
        cachingSource = (CachingSource) resolver.resolveURI(scheme + ":" + uri + "&cocoon:cache-name=test");
        
        assertEquals(cachingSource.getCacheKey(),new IdentifierCacheKey("source:"+uri+":test",false));
        
        resolver.release(source);
        
        String parentURI = "file://c:/temp";
        String childURI = parentURI + "/test";
        
        // resolve TraversableCachingSource
        source = resolver.resolveURI(scheme + ":" + childURI + "?cocoon:cache-expires=1");
        assertTrue(source instanceof TraversableCachingSource);
        
        TraversableCachingSource child = (TraversableCachingSource) source;
        assertEquals("test",child.getName());
        
        assertTrue( child.getParent() instanceof TraversableCachingSource);
        TraversableCachingSource parent = (TraversableCachingSource) child.getParent();
        assertEquals("temp",parent.getName());
        //assertEquals(parentURI, parent.getSourceURI());
        assertTrue(parent.isCollection());
        
        child = (TraversableCachingSource) parent.getChild("test");
        assertEquals("test", child.getName());
        //assertEquals(childURI, child.getSourceURI());
        
        Iterator children = parent.getChildren().iterator();
        while (children.hasNext()) {
            child = (TraversableCachingSource) children.next();
        }
        
        resolver.release(source);
    }
    
    public void testGetContents() throws Exception {
        
        SourceResolver resolver = (SourceResolver) lookup(SourceResolver.ROLE);
        
        // resolve AsyncCachingSource
        String scheme = "caching";
        String uri = "resource://org/apache/cocoon/components/" +
            "source/impl/cachingsourcetest.xml";
        CachingSource source = (CachingSource) resolver.resolveURI(scheme + ":" + uri);
        
        InputStream stream = source.getInputStream();
        String contents = new String();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = stream.read(buffer)) > 0) {
            contents += new String(buffer,0,len);
        }
        
        resolver.release(source);
        
        uri = "xml:" + uri;
        source = (CachingSource) resolver.resolveURI(scheme + ":" + uri);
        
        SaxBuffer saxbuffer = new SaxBuffer();
        LoggingContentHandler handler = new LoggingContentHandler("test",saxbuffer);
        handler.enableLogging(getLogger().getChildLogger("handler"));
        source.toSAX(handler);
    }
    
//    public void testDelayRefresher() throws Exception {
//    	Parameters parameters = new Parameters();
//    	parameters.setParameter("cache-expires",String.valueOf(10));
//   
//    	Refresher refresher = (Refresher) lookup(Refresher.ROLE);
//    	refresher.refresh(new SimpleCacheKey("test",false),
//                          "http://www.hippo.nl/index.html",
//                          Cache.ROLE,
//                          parameters);
//    }
    
    //source.getSource();
//    InputStream stream = source.getInputStream();
//    String contents = new String();
//    byte[] buffer = new byte[1024];
//    int len;
//    while((len = stream.read(buffer)) > 0) {
//        contents += new String(buffer,0,len);
//    }
//    getLogger().debug("contents: " + contents);
    //getLogger().debug("");
    protected void addContext(DefaultContext ctx) {
        ctx.put("work-directory",new File("build/work"));
        ctx.put(Constants.CONTEXT_ENVIRONMENT_CONTEXT,new MockContext());
    }

}
