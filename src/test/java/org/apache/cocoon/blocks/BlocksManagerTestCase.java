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
package org.apache.cocoon.blocks;

import org.apache.cocoon.ServletTestCase;

public class BlocksManagerTestCase extends ServletTestCase {
    
    public void testBlockSource1() throws Exception {
        pipeTest("/test1/test", "test1/COB-INF/test.xml");
    }

    public void testBlockSource2() throws Exception {
        pipeTest("/test1/test2", "test2/COB-INF/test.xml");
    }

    public void testBlockSource3() throws Exception {
        pipeTest("/test1/test3", "test1/COB-INF/test.xml");
    }

    public void testBlockSource4() throws Exception {
        pipeTest("/test1/test4", "test1/META-INF/classes/test.xml");
    }

    public void testBlockSourceSub1() throws Exception {
        pipeTest("/test1/sub/test", "test1/COB-INF/sub/test.xml");
    }

    public void testBlockSourceSub2() throws Exception {
        pipeTest("/test1/sub/test2", "test1/COB-INF/sub/test.xml");
    }

    public void testBlockSourceSub3() throws Exception {
        pipeTest("/test1/sub/test3", "test1/COB-INF/test.xml");
    }

    public void testBlockExtend1() throws Exception {
        pipeTest("/test3/test", "test3/COB-INF/test.xml");
    }

    public void testBlockExtend2() throws Exception {
        pipeTest("/test3/test2", "test2/COB-INF/test.xml");
    }

    public void testBlockExtend3() throws Exception {
        pipeTest("/test3/test3", "test3/COB-INF/test.xml");
    }

    public void testBlockExtend4() throws Exception {
        pipeTest("/test3/test4", "test1/COB-INF/test.xml");
    }

    public void testBlockProperty() throws Exception {
        pipeTest("/test3/prop", "test3/COB-INF/prop-expected.xml");
    }

    public void testAbsolutize() throws Exception {
        pipeTest("/test1/sub/abs", "test1/COB-INF/sub/path-expected.xml");
    }
}
