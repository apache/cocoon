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
 * decoding or casting. It uses the get() method instead of the getParameter() 
 * method of the {@link org.apache.cocoon.environment.Request Request} This is useful
 *  for example in conjunction with uploads.
 * If get() returns a Vector, getAttribute() will return the first element, otherwise it
 * will return the same as get(). getAttributeValues() will either convert the Vector to an array,
 * place the result in a new array, or return the array as is.
 *
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version CVS $Id: RawRequestParameterModule.java,v 1.2 2004/03/08 13:58:30 cziegeler Exp $
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
