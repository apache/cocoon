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
package org.apache.cocoon.components.blocks;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.SitemapTestCase;
import org.apache.cocoon.environment.mock.MockEnvironment;

public class BlocksManagerTestCase extends SitemapTestCase {

    public void testCreate() throws ServiceException {
        BlocksManager blocks = (BlocksManager)this.lookup(BlocksManager.ROLE);
        this.release(blocks);
    }

    public void testMount() throws Exception {
        BlocksManager blocks = (BlocksManager)this.lookup(BlocksManager.ROLE);
        MockEnvironment env = getEnvironment("/test1/test");
        blocks.process(env);
        getLogger().info("Output: " + new String(env.getOutput(), "UTF-8"));
        this.release(blocks);
    }

    public void testBlockId() throws Exception {
        BlocksManager blocks = (BlocksManager)this.lookup(BlocksManager.ROLE);
        MockEnvironment env = getEnvironment("test");
        blocks.process("test1id", env);
        getLogger().info("Output: " + new String(env.getOutput(), "UTF-8"));
        this.release(blocks);
    }

    public void testBlockSource1() throws Exception {
        pipeTest("test", "test1/test.xml");
    }

    public void testBlockSource2() throws Exception {
        pipeTest("test2", "test2/test.xml");
    }

    public void testBlockSource3() throws Exception {
        pipeTest("test3", "test1/test.xml");
    }

    public void testBlockSource4() throws Exception {
        pipeTest("test4", "test1/COB-INF/classes/test.xml");
    }

    public void testBlockSourceSub1() throws Exception {
        pipeTest("sub/test", "test1/sub/test.xml");
    }

    public void testBlockSourceSub2() throws Exception {
        pipeTest("sub/test2", "test1/sub/test.xml");
    }

    public void testBlockSourceSub3() throws Exception {
        pipeTest("sub/test3", "test1/test.xml");
    }

    public void testBlockExtend1() throws Exception {
        pipeTest("ext/test", "test3/test.xml");
    }

    public void testBlockExtend2() throws Exception {
        pipeTest("ext/test2", "test2/test.xml");
    }

    public void testBlockExtend3() throws Exception {
        pipeTest("ext/test3", "test3/test.xml");
    }

    public void testBlockExtend4() throws Exception {
        pipeTest("ext/test4", "test1/test.xml");
    }

    public void testBlockProperty() throws Exception {
        pipeTest("ext/prop", "test3/prop-expected.xml");
    }

    public void testAbsolutize() throws Exception {
        pipeTest("sub/abs", "test1/sub/path-expected.xml");
    }
}
