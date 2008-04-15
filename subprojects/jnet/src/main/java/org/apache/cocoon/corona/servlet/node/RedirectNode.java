/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.cocoon.corona.servlet.node;

import org.apache.cocoon.corona.sitemap.Invocation;
import org.apache.cocoon.corona.sitemap.node.AbstractSitemapNode;
import org.apache.cocoon.corona.sitemap.node.InvocationResult;
import org.apache.cocoon.corona.sitemap.node.Node;

@Node(name="redirect-to")
public class RedirectNode extends AbstractSitemapNode {

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.corona.sitemap.node.AbstractSitemapNode#invoke(org.apache.cocoon.corona.sitemap.Invocation)
     */
    @Override
    public InvocationResult invoke(Invocation invocation) {
        // install the component
        invocation.installComponent("redirector", this.getParameters());

        // signal that we did some processing
        return InvocationResult.PROCESSED;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.corona.sitemap.node.AbstractSitemapNode#toString()
     */
    @Override
    public String toString() {
        return "RedirectNode(" + this.getParameters().get("uri") + ")";
    }
}
