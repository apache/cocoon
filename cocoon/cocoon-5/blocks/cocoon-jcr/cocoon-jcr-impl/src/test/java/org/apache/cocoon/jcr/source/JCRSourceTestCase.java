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
package org.apache.cocoon.jcr.source;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.service.ServiceSelector;

import org.apache.cocoon.core.container.ContainerTestCase;

import org.apache.excalibur.source.ModifiableSource;
import org.apache.excalibur.source.ModifiableTraversableSource;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceFactory;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.TraversableSource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;

/**
 * @version $Id$
 */
public class JCRSourceTestCase extends ContainerTestCase {

    private SourceResolver resolver;

    private File tempDir;

    /**
     * @see org.apache.cocoon.core.container.ContainerTestCase#addContext(org.apache.avalon.framework.context.DefaultContext)
     */
    protected void addContext(DefaultContext context) {
        super.addContext(context);
        // Create a temp file
        try {
            tempDir = File.createTempFile("jcr-test", null);
        } catch (IOException e) {
            throw new CascadingRuntimeException("Cannot setup temp dir", e);
        }
        // and turn it to a directory
        tempDir.delete();
        tempDir.mkdir();
        tempDir.deleteOnExit();

        // Setup context root as the temp dir so that relative URI used in the
        // repository configuration go there
        context.put("context-root", tempDir);

        // Make VariableResolver used in repository configuration happy
        context.put("object-model", Collections.EMPTY_MAP);
    }

    /**
     * @see org.apache.cocoon.core.container.ContainerTestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        resolver = (SourceResolver)getManager().lookup(SourceResolver.ROLE);
    }

    private void write(ModifiableSource src, String text) throws Exception {
        byte[] data = text.getBytes("ISO-8859-1");
        OutputStream os = src.getOutputStream();
        os.write(data);
        os.close();
    }

    private String read(Source src) throws Exception {
        byte[] data = new byte[(int)src.getContentLength()];
        InputStream is = src.getInputStream();
        assertEquals(data.length, is.read(data));
        is.close();
        return new String(data, "ISO-8859-1");
    }

    protected void deleteFile(File file) {
        File[] children = file.listFiles();
        if (children != null) {
            for (int i = 0; i < children.length; i++) {
                deleteFile(children[i]);
            }
        }
        file.delete();
    }

    /**
     * @see org.apache.cocoon.core.container.ContainerTestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        deleteFile(tempDir);
    }

    public void testJCRSourceInitialization() throws Exception {
        ServiceSelector selector = (ServiceSelector)getManager().lookup(SourceFactory.ROLE + "Selector");
        Object jcrSourceFactory = selector.select("jcr");

        assertEquals("Wrong class name for jcr protocol", jcrSourceFactory.getClass(), JCRSourceFactory.class);
    }

    public void testGetRootNode() throws Exception {

        JCRNodeSource source = (JCRNodeSource)resolver.resolveURI("jcr://");

        assertTrue("Root node should exist", source.exists());
        System.err.println("Root node type = " + source.getNode().getPrimaryNodeType().getName());
        assertTrue("Root node should be a collection", source.isCollection());
    }

    public void testCreateFirstLevelFile() throws Exception {

        String someText = "Some text";

        JCRNodeSource root = (JCRNodeSource)resolver.resolveURI("jcr://");

        JCRNodeSource firstChild = (JCRNodeSource)root.getChild("child1");

        assertFalse(firstChild.exists());
        assertEquals(firstChild.getURI(), "jcr://child1");

        write(firstChild, someText);

        assertTrue(firstChild.exists());

        // Check content
        Source child1 = resolver.resolveURI("jcr://child1");
        assertTrue(child1.exists());

        int len = (int)child1.getContentLength();
        assertEquals(someText.length(), len);
        assertEquals(someText, read(child1));

    }

    public void testCreateDeepFile() throws Exception {
        String anotherText = "another text";

        JCRNodeSource source = (JCRNodeSource)resolver.resolveURI("jcr://some/deep/path/to/file");
        assertFalse(source.exists());

        write(source, anotherText);

        // Lookup again, using the parent, doing some traversal
        TraversableSource dir = (TraversableSource)resolver.resolveURI("jcr://some/deep");
        assertTrue(dir.isCollection());
        dir = (TraversableSource)dir.getChild("path");
        assertTrue(dir.isCollection());
        dir = (TraversableSource)dir.getChild("to");
        assertTrue(dir.isCollection());

        source = (JCRNodeSource)dir.getChild("file");
        assertTrue(source.exists());

        assertEquals(anotherText, read(source));
    }

    public void testDeleteFile() throws Exception {
        String text = "Yeah! Some content!";
        ModifiableSource source = (ModifiableSource)resolver.resolveURI("jcr://yet/another/deep/file");

        assertFalse(source.exists());
        write(source, text);

        // Lookup a fresh source
        source = (ModifiableSource)resolver.resolveURI("jcr://yet/another/deep/file");
        assertTrue(source.exists());
        source.delete();
        assertFalse(source.exists());

        // Lookup again to check it was really deleted
        source = (ModifiableSource)resolver.resolveURI("jcr://yet/another/deep/file");
        assertFalse(source.exists());
    }

    public void testDeleteDir() throws Exception {
        String text = "Wow, a lot of data going there";
        ModifiableTraversableSource source = (ModifiableTraversableSource)resolver.resolveURI("jcr://and/again/a/deep/node");

        assertFalse(source.exists());
        write(source, text);

        // Lookup 'a' node
        source = (ModifiableTraversableSource)resolver.resolveURI("jcr://and/again/a/");
        assertTrue(source.isCollection());
        source.delete();
        assertFalse(source.exists());

        // Double check with a fresh source
        source = (ModifiableTraversableSource)resolver.resolveURI("jcr://and/again/a/");
        assertFalse(source.exists());

        // Check on children
        source = (ModifiableTraversableSource)resolver.resolveURI("jcr://and/again/a/deep/node");
        assertFalse(source.exists());
    }

    public void testTraverseDir() throws Exception {
        String text = "Look Ma, more data!";

        ModifiableTraversableSource dir = (ModifiableTraversableSource)resolver.resolveURI("jcr://path/to/dir");
        dir.makeCollection();

        for (int i = 0; i < 10; i++) {
            ModifiableTraversableSource src = (ModifiableTraversableSource)dir.getChild("file" + i);
            write(src, text + i);
        }

        // Lookup dir again, and inspect children
        dir = (ModifiableTraversableSource)resolver.resolveURI("jcr://path/to/dir");
        Collection children = dir.getChildren();

        assertEquals(10, children.size());

        for (int i = 0; i < 10; i++) {
            Source src = dir.getChild("file" + i);
            assertTrue(src.exists());
            assertEquals(text + i, read(src));
        }
    }

    public void testCrawlUp() throws Exception {
        String text = "Look Pa, some more!";

        ModifiableTraversableSource src = (ModifiableTraversableSource)resolver.resolveURI("jcr://path/to/very/deep/content");
        write(src, text);

        // Do a fresh lookup
        src = (ModifiableTraversableSource)resolver.resolveURI("jcr://path/to/very/deep/content");

        ModifiableTraversableSource parent = (ModifiableTraversableSource)src.getParent();
        assertTrue(parent.exists());
        assertEquals("jcr://path/to/very/deep", parent.getURI());

        parent = (ModifiableTraversableSource)parent.getParent();
        assertTrue(parent.exists());
        assertEquals("jcr://path/to/very", parent.getURI());

        parent = (ModifiableTraversableSource)parent.getParent();
        assertTrue(parent.exists());
        assertEquals("jcr://path/to", parent.getURI());

        parent = (ModifiableTraversableSource)parent.getParent();
        assertTrue(parent.exists());
        assertEquals("jcr://path", parent.getURI());

        parent = (ModifiableTraversableSource)parent.getParent();
        assertTrue(parent.exists());
        assertEquals("jcr://", parent.getURI());

        // Root node has no parent
        parent = (ModifiableTraversableSource)parent.getParent();
        assertNull(parent);
    }
}
