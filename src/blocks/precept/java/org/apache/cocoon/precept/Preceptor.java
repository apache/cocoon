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
package org.apache.cocoon.precept;

import java.util.Collection;

import org.apache.avalon.framework.component.Component;

/**
 * @author Torsten Curdt <tcurdt@dff.st>
 * @since Mar 14, 2002
 * @version CVS $Id: Preceptor.java,v 1.3 2004/03/05 13:02:19 bdelacretaz Exp $
 */
public interface Preceptor extends Component {

    public String ROLE = "org.apache.cocoon.precept.Preceptor";

    public Collection getConstraintsFor(String xpath) throws InvalidXPathSyntaxException, NoSuchNodeException;

    public boolean isValidNode(String xpath) throws InvalidXPathSyntaxException;

    public void buildInstance(Instance instance);

    public Collection validate(Instance instance, String xpath, Context context) throws InvalidXPathSyntaxException, NoSuchNodeException;

    public Collection validate(Instance instance, Context context) throws InvalidXPathSyntaxException;
}
