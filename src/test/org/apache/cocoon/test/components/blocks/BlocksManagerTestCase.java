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
package org.apache.cocoon.test.components.blocks;

import org.apache.cocoon.Constants;
import org.apache.cocoon.components.blocks.BlocksManager;
import org.apache.cocoon.test.SitemapTestCase;

public class BlocksManagerTestCase extends SitemapTestCase {
    
    protected void setUp() throws Exception {
        this.processorClassName = BlocksManager.class.getName();
        super.setUp();
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.test.SitemapTestCase#getConfiguration()
     */
    protected String getConfiguration() {
        return Constants.WIRING;
    }

    public void testBlockSource1() throws Exception {
        pipeTest("/test1/test", "test1/test.xml");
    }

    public void testBlockSource2() throws Exception {
        pipeTest("/test1/test2", "test2/test.xml");
    }

    public void testBlockSource3() throws Exception {
        pipeTest("/test1/test3", "test1/test.xml");
    }
    /*
    public void testBlockSource4() throws Exception {
        pipeTest("/test1/test4", "test1/COB-INF/classes/test.xml");
    }
    */
    public void testBlockSourceSub1() throws Exception {
        pipeTest("/test1/sub/test", "test1/sub/test.xml");
    }

    public void testBlockSourceSub2() throws Exception {
        pipeTest("/test1/sub/test2", "test1/sub/test.xml");
    }

    public void testBlockSourceSub3() throws Exception {
        pipeTest("/test1/sub/test3", "test1/test.xml");
    }

    public void testBlockExtend1() throws Exception {
        pipeTest("/test3/test", "test3/test.xml");
    }

    public void testBlockExtend2() throws Exception {
        pipeTest("/test3/test2", "test2/test.xml");
    }

    public void testBlockExtend3() throws Exception {
        pipeTest("/test3/test3", "test3/test.xml");
    }

    public void testBlockExtend4() throws Exception {
        pipeTest("/test3/test4", "test1/test.xml");
    }

    public void testBlockProperty() throws Exception {
        pipeTest("/test3/prop", "test3/prop-expected.xml");
    }

    public void testAbsolutize() throws Exception {
        pipeTest("/test1/sub/abs", "test1/sub/path-expected.xml");
    }
}
