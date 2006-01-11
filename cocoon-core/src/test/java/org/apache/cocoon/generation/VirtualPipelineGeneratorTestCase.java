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
package org.apache.cocoon.generation;

import org.apache.cocoon.SitemapTestCase;

public class VirtualPipelineGeneratorTestCase extends SitemapTestCase {
    public void testSimplePipe() throws Exception {
        pipeTest("test", "vpc-test.xml");
    }

    public void testVirtualPipe() throws Exception {
        pipeTest("v1", "vpc-test.xml");
    }

    public void testVirtualPipeParam() throws Exception {
        pipeTest("v2", "vpc-param-expected.xml");
    }

    public void testVirtualPipeSourceParam() throws Exception {
        pipeTest("v3", "vpc-source-param-expected.xml");
    }

    public void testVirtualSubPipeSourceParam() throws Exception {
        pipeTest("sub/v3", "vpc-source-param-expected.xml");
    }

    public void testVirtualPipeSrc() throws Exception {
        pipeTest("v4", "vpc-test.xml");
    }

    public void testVirtualSubPipeSrc() throws Exception {
        pipeTest("sub/v4", "vpc-test.xml");
    }
}
