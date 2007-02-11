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
package org.apache.cocoon.precept.preceptors.easyrelax.constraints;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.logger.AbstractLogEnabled;

import org.apache.cocoon.precept.Constraint;

/**
 * @author Torsten Curdt <tcurdt@dff.st>
 * @since Mar 21, 2002
 * @version CVS $Id: AbstractConstraint.java,v 1.4 2004/03/05 13:02:19 bdelacretaz Exp $
 */
public abstract class AbstractConstraint extends AbstractLogEnabled implements Constraint, Component {
    protected String id = null;
}

