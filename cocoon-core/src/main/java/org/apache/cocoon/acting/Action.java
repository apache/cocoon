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
package org.apache.cocoon.acting;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;

import java.util.Map;

/**
 *
 * @version $Id$
 */
public interface Action {

    String ROLE = Action.class.getName();

    /**
     * Controls the processing against some values of the
     * <code>Dictionary</code> objectModel and returns a
     * <code>Map</code> object with values used in subsequent
     * sitemap substitution patterns.
     *
     * NOTE: This interface is designed so that implentations can be <code>ThreadSafe<code>.
     * When an action is ThreadSafe, only one instance serves all requests : this
     * reduces memory usage and avoids pooling.
     *
     * @param resolver    The <code>SourceResolver</code> in charge
     * @param objectModel The <code>Map</code> with object of the
     *                    calling environment which can be used
     *                    to select values this controller may need
     *                    (ie Request, Response).
     * @param source      A source <code>String</code> to the Action
     * @param parameters  The <code>Parameters</code> for this invocation
     * @return Map        The returned <code>Map</code> object with
     *                    sitemap substitution values which can be used
     *                    in subsequent elements attributes like src=
     *                    using a xpath like expression: src="mydir/{myval}/foo"
     *                    If the return value is null the processing inside
     *                    the <map:act> element of the sitemap will
     *                    be skipped.
     * @exception Exception Indicates something is totally wrong
     */
    Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters)
            throws Exception;
}
