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
package org.apache.cocoon.acting;

import org.apache.avalon.framework.logger.AbstractLogEnabled;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * AbstractAction gives you the infrastructure for easily deploying more
 * Actions.  In order to get at the Logger, use getLogger().
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version CVS $Id: AbstractAction.java,v 1.3 2004/03/05 13:02:42 bdelacretaz Exp $
 */
public abstract class AbstractAction extends AbstractLogEnabled
    implements Action {

    /**
     * Empty unmodifiable map. Replace with Collections.EMPTY_MAP when
     * pre-jdk1.3 support is dropped.
     */
    protected static final Map EMPTY_MAP = Collections.unmodifiableMap(new TreeMap());

}
