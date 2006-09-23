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
package org.apache.cocoon.ojb.odmg.components;

import org.apache.avalon.framework.component.Component;

import org.odmg.Implementation;
import org.odmg.ODMGException;

/**
 * Interface component to the ODMG Implementation. It is used to get
 * the ODMG Implementation object to interact with object database
 * through ODMG API.
 *
 * @version $Id$
 */
public interface ODMG extends Component {

    /** The ROLE */
    String ROLE = ODMG.class.getName();

    /**
     * Get a ODMG Instance with default settings.
     *
     * @return a ODMG Implementation Object
     * @throws ODMGException DOCUMENT ME!
     */
    Implementation getInstance()
    throws ODMGException;

    /**
     * Get a ODMG Instance with a specific connection definition.
     *
     * @param connection The connection name to be used (OJB specific connection name)
     * @return a ODMG Implementation Object
     * @throws ODMGException DOCUMENT ME!
     */
    Implementation getInstance(String connection)
    throws ODMGException;

    /**
     * Get a ODMG Instance with a specific connection definition and a Database operation mode.
     *
     * @param connection The connection name to be used (OJB specific connection name)
     * @param mode The Database operation mode
     * @return a ODMG Implementation Object
     * @throws ODMGException DOCUMENT ME!
     */
    Implementation getInstance(String connection, int mode)
    throws ODMGException;

    /**
     * Get a ODMG Instance with a default connection definition and a Database operation mode.
     *
     * @param mode The Database operation mode
     * @return a ODMG Implementation Object
     * @throws ODMGException DOCUMENT ME!
     */
    Implementation getInstance(int mode)
    throws ODMGException;
}
