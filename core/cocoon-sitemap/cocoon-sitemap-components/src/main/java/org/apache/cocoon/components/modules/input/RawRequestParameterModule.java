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

package org.apache.cocoon.components.modules.input;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.environment.ObjectModelHelper;

import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

/**
 * RawRequestParameterModule accesses request parameters without
 * decoding to the specified <code>form-encoding</code> or casting. It uses the
 * {@link org.apache.cocoon.environment.Request#get} method instead of the
 * {@link org.apache.cocoon.environment.Request#getParameter} method of the
 * {@link org.apache.cocoon.environment.Request Request} This is useful for example
 * in conjunction with uploads.
 *
 * <p>If <code>get()</code> returns a Vector, <code>getAttribute()</code> will return
 * the first element, otherwise it will return the same as <code>get()</code>.
 * <code>getAttributeValues()</code> will either convert the Vector to an array,
 * place the result in a new array, or return the array as is.</p>
 *
 * @version $Id$
 */
public class RawRequestParameterModule extends AbstractInputModule implements ThreadSafe {

    public Object getAttribute( String name, Configuration modeConf, Map objectModel ) throws ConfigurationException {

        String pname = (String) this.settings.get("parameter",name);
        if ( modeConf != null ) {
            pname = modeConf.getAttribute( "parameter", pname );
            // preferred
            pname = modeConf.getChild("parameter").getValue(pname);
        }
        Object obj = ObjectModelHelper.getRequest(objectModel).get( pname );
        if (obj instanceof Vector) {
            return ((Vector) obj).firstElement();
        } else {
            return obj;
        }

    }


    public Iterator getAttributeNames( Configuration modeConf, Map objectModel ) throws ConfigurationException {

        return new IteratorHelper(ObjectModelHelper.getRequest(objectModel).getParameterNames());
    }


    public Object[] getAttributeValues( String name, Configuration modeConf, Map objectModel )
        throws ConfigurationException {

        Object obj = getAttribute(name, modeConf, objectModel);
        if (obj instanceof Vector) {
           return ((Vector)obj).toArray();
        } else if (obj.getClass().isArray()) {
            return (Object[]) obj;
        } else {
            Object[] tmp = new Object[1];
            tmp[0] = obj;
            return tmp;
        }
    }

}
