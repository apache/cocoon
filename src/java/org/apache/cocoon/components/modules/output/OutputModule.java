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

package org.apache.cocoon.components.modules.output;

import java.util.Map;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.configuration.Configuration;

/**
 * Communicate results to other components. This could be done via
 * request attributes, session attribute etc. Implementors should obey
 * the transactional nature and e.g. queue values as request
 * attributes and do the real communication e.g. to a bean only when
 * the transaction completes successfully.
 *
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version CVS $Id: OutputModule.java,v 1.3 2004/03/05 13:02:49 bdelacretaz Exp $
 */
public interface OutputModule extends Component {

    String ROLE = OutputModule.class.getName();

    /**
     * communicate an attribute value to further processing logic. OutputModules
     * work in implicit transaction mode, thus setting an attribute starts an
     * transaction and sttributes are only visible after the transaction is
     * successfully completed with a call to commit
     * @param modeConf column's mode configuration from resource
     * description. This argument is optional.
     * @param objectModel The objectModel
     * @param name The attribute's label, consisting of "table.column"
     * or "table.column[index]" in case of multiple attributes of the
     * same spec.
     * @param value The attriute's value.
     * */
    void setAttribute( Configuration modeConf, Map objectModel, String name, Object value );


    /**
     * If a database transaction needs to rollback, this is called to
     * inform the further processing logic about this fact. All
     * already set attribute values are invalidated. <em>This is difficult
     * because only the request object can be used to synchronize this
     * and build some kind of transaction object. Beaware that sending
     * your data straight to some beans or other entities could result
     * in data corruption!</em>
     * */
    void rollback( Configuration modeConf, Map objectModel, Exception e );


    /**
     * Signal that the database transaction completed
     * successfully. See notes on {@link #rollback(Configuration, Map, Exception)}.
     * */
    void commit( Configuration modeConf, Map objectModel );


}
