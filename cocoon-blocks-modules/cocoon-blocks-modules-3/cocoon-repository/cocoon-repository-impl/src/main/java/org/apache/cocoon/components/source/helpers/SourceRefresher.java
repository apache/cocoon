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
package org.apache.cocoon.components.source.helpers;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.excalibur.source.SourceException;

/**
 * A SourceRefresher is a component that invokes the {@link org.apache.excalibur.source.Source#refresh}
 * method on a configured set of Sources.
 *
 * <p>Implementations can for instance trigger refresh based on a timeout value or
 * in response to an external event.</p>
 *
 * @since 2.1.1
 * @version $Id$
 */
public interface SourceRefresher extends Component {

    String ROLE = SourceRefresher.class.getName();

    /**
     * Refresh interval for the Source.
     * Parameter is used by {@link org.apache.cocoon.components.source.helpers.DelaySourceRefresher}.
     */
    String PARAM_CACHE_INTERVAL = "interval";

    /**
     * Add a uri to the SourceRefresher.
     *
     * @param name      Uniquely identifying name for the source.
     * @param uri       The uri to refresh. Every valid protocol can be used.
     * @param params    Additional parameters such as an interval between refresh runs.
     */
    void refresh(String name,
                 String uri,
                 Parameters params)
    throws SourceException;

}
