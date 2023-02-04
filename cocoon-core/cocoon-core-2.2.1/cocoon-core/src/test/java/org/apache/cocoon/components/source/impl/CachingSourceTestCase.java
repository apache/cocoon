/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.cocoon.Constants;
import org.apache.cocoon.SitemapComponentTestCase;
import org.apache.cocoon.core.container.spring.avalon.ComponentInfo;
import org.apache.cocoon.core.container.spring.avalon.ConfigurationInfo;
import org.apache.cocoon.environment.mock.MockContext;
import org.apache.cocoon.xml.LoggingContentHandler;
import org.apache.cocoon.xml.SaxBuffer;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceFactory;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.SourceUtil;
import org.apache.excalibur.source.impl.AbstractSource;

/**
 * CachingSource (cached: protocol) tests
 *
 * @version $Id$
 */
public class CachingSourceTestCase extends SitemapComponentTestCase {

    private static final String URI = "resource://org/apache/cocoon/components/source/impl/cachingsourcetest.xml?foo=bar";

    SourceResolver resolver;

    public void setUp() throws Exception {
        super.setUp();
        resolver = (SourceResolver) lookup(SourceResolver.ROLE);
    }
    
    @Override
    protected void addComponents(ConfigurationInfo info) throws Exception {
        super.addComponents(info);
        
        //register MockSourceFactory
        ComponentInfo componentInfo = new ComponentInfo();
        componentInfo.setComponentClassName(MockSourceFactory.class.getName());
        componentInfo.setRole(SourceFactory.ROLE + "/mock");
        componentInfo.setConfiguration(new DefaultConfiguration("-"));
        
        info.addComponent(componentInfo);
    }

    public void testResolveURI() throws Exception {
        testResolveURI("cached", URI);
    }

    public void testResolveAsyncURI() throws Exception {
        testResolveURI("async-cached", URI);
    }

    private void testResolveURI(final String scheme, final String uri) throws Exception {
        // resolve CachingSource
        Source source = resolver.resolveURI(scheme + ":" + uri + "&cocoon:cache-expires=10");
        assertTrue(source instanceof CachingSource);

        CachingSource cachingSource = (CachingSource) source;
        assertEquals(uri, cachingSource.getSourceURI());
        assertEquals(scheme, cachingSource.getScheme());
        assertEquals(10 * 1000, cachingSource.getExpiration());
        assertEquals("source:" + uri, cachingSource.getCacheKey());
        resolver.release(source);

        // resolve CachingSource with specified cache name
        cachingSource = (CachingSource) resolver.resolveURI(scheme + ":" + uri + "&cocoon:cache-name=test");
        assertEquals("source:" + uri + ":test", cachingSource.getCacheKey());
        resolver.release(source);
    }

    public void testCachingURI() throws Exception {
        String uri = "cached:mock://dummyaddress.org/?cocoon:cache-expires=1";

        CachingSource source;

        source = (CachingSource) resolver.resolveURI(uri);
        CachingSource.SourceMeta meta1 = source.getResponseMeta();
        resolver.release(source);

        source = (CachingSource) resolver.resolveURI(uri);
        CachingSource.SourceMeta meta2 = source.getResponseMeta();
        resolver.release(source);

        // Source is cached -- same meta data
        assertSame(meta1, meta2);

        source = (CachingSource) resolver.resolveURI(uri);
        source.refresh();
        CachingSource.SourceMeta meta3 = source.getResponseMeta();
        resolver.release(source);

        // Source is still cached -- still same meta data
        assertSame(meta1, meta3);

        Thread.sleep(1100);

        source = (CachingSource) resolver.resolveURI(uri);
        source.refresh();
        CachingSource.SourceMeta meta4 = source.getResponseMeta();
        resolver.release(source);

        // Source is refreshed -- meta data changes
        assertNotSame(meta1, meta4);
        assertEquals(meta1.getContentLength(), meta4.getContentLength());
        assertTrue(meta1.getLastModified() != meta4.getLastModified());
        assertEquals(meta1.getMimeType(), meta4.getMimeType());
    }

    public void testRefreshSyncURI() throws Exception {
        testRefreshURI("cached", "mock://dummyaddress.org/");
    }

//    public void testRefreshAsyncURI() throws Exception {
//        testRefreshURI("async-caching", "http://slashdot.org/");
//    }

    private void testRefreshURI(final String scheme, final String uri) throws Exception {
        CachingSource source;

        source = (CachingSource) resolver.resolveURI(scheme + ":" + uri + "?cocoon:cache-expires=1");
        CachingSource.SourceMeta meta1 = source.getResponseMeta();
        resolver.release(source);

        source = (CachingSource) resolver.resolveURI(scheme + ":" + uri + "?cocoon:cache-expires=1");
        CachingSource.SourceMeta meta2 = source.getResponseMeta();
        resolver.release(source);

        assertSame(meta1, meta2);
        assertEquals(meta1.getContentLength(), meta2.getContentLength());
        assertEquals(meta1.getLastModified(), meta2.getLastModified());

        Thread.sleep(1200);

        source = (CachingSource) resolver.resolveURI(scheme + ":" + uri + "?cocoon:cache-expires=1");
        CachingSource.SourceMeta meta3 = source.getResponseMeta();
        resolver.release(source);

        assertNotSame(meta1, meta3);
        assertEquals(meta1.getContentLength(), meta3.getContentLength());
        assertTrue(meta1.getLastModified() != meta3.getLastModified());
        assertEquals(meta1.getMimeType(), meta3.getMimeType());
    }

    public void testCachingTraversableSource() throws Exception {
        String scheme = "cached";
        File cwd = new File(".").getCanonicalFile();
        String childURI = cwd.toURL().toString();
        String parentURI = cwd.getParentFile().toURL().toString();
        String childName = cwd.getName();
        String parentName = cwd.getParentFile().getName();

        // resolve TraversableCachingSource
        Source source = resolver.resolveURI(scheme + ":" + childURI + "?cocoon:cache-expires=1");
        assertTrue(source instanceof TraversableCachingSource);

        TraversableCachingSource child = (TraversableCachingSource) source;
        assertEquals(childName, child.getName());
        assertTrue(child.getParent() instanceof TraversableCachingSource);

        TraversableCachingSource parent = (TraversableCachingSource) child.getParent();
        assertEquals(parentName, parent.getName());
        assertEquals(parentURI, parent.getSourceURI());
        assertTrue(parent.isCollection());

        child = (TraversableCachingSource) parent.getChild(childName);
        assertEquals(childName, child.getName());
        assertEquals(childURI, child.getSourceURI());

        boolean found = false;
        Iterator children = parent.getChildren().iterator();
        while (children.hasNext()) {
            child = (TraversableCachingSource) children.next();
            if (child.getName().equals(childName)) {
                found = true;
            }
        }
        assertTrue(found);

        resolver.release(source);
    }

    public void testGetContents() throws Exception {
        // resolve CachingSource
        String scheme = "cached";
        String uri = "resource://org/apache/cocoon/components/source/impl/cachingsourcetest.xml";
        CachingSource source = (CachingSource) resolver.resolveURI(scheme + ":" + uri);

        InputStream stream = source.getInputStream();
        String contents = "";
        byte[] buffer = new byte[1024];
        int len;
        while ((len = stream.read(buffer)) > 0) {
            contents += new String(buffer, 0, len);
        }

        resolver.release(source);

        uri = "xml:" + uri;
        source = (CachingSource) resolver.resolveURI(scheme + ":" + uri);

        SaxBuffer saxbuffer = new SaxBuffer();
        LoggingContentHandler handler = new LoggingContentHandler("test", saxbuffer);
        source.toSAX(handler);
    }

//    public void testDelayRefresher() throws Exception {
//    	Parameters parameters = new Parameters();
//    	parameters.setParameter("cache-expires",String.valueOf(10));
//
//    	SourceRefresher refresher = (SourceRefresher) lookup(SourceRefresher.ROLE);
//    	refresher.refresh(new SimpleCacheKey("test",false),
//                          "http://www.hippo.nl/index.html",
//                          Cache.ROLE,
//                          parameters);
//    }

    protected void addContext(DefaultContext ctx) {
        ctx.put("work-directory", new File("build/work"));
        ctx.put(Constants.CONTEXT_ENVIRONMENT_CONTEXT, new MockContext());
    }
    
    //------------------------ Mock objects ---------------------------
    
    /**
     * Simple source that does not provide anything meaningful apart from lastModification date which is passed
     * to it from outside.
     */
    private static class MockSource extends AbstractSource {
        
        public MockSource(String uri, long lastModified) {
            setSystemId(uri);
            setScheme(SourceUtil.getScheme(uri));
            setLastModified(lastModified);
        }

        public boolean exists() { 
            return true; 
        }
        
        @Override
        protected void getInfos() {
            //overrode this method because it wipes out last modification date
        }
    }
    
    /**
     * This simple SourceFactory produces instances of MockSource with current lastModification date.
     */
    private static final class MockSourceFactory implements SourceFactory {
        
        public MockSourceFactory() { }

        @SuppressWarnings("unchecked")
        public Source getSource(String location, Map parameters)
                throws IOException, MalformedURLException {
            return new MockSource(location, (new Date()).getTime());
        }

        public void release(Source source) {
            //it's mock source, do nothing
        }
        
    }
    
}
