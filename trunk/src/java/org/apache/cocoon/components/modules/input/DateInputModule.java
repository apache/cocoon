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

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * DateInputModule returns current date, optionally formated as
 * string. Format given through attribute "format" of configuration
 * root node or nested &lt;format/&gt; tag on module declaration.
 *
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version CVS $Id: DateInputModule.java,v 1.2 2004/03/08 13:58:30 cziegeler Exp $
 * @see java.text.SimpleDateFormat
 */
public class DateInputModule extends AbstractInputModule implements ThreadSafe {

    final static Vector returnNames;
    static {
        Vector tmp = new Vector();
        tmp.add("currentDate");
        returnNames = tmp;
    }

    public Object getAttribute( String name, Configuration modeConf, Map objectModel ) throws ConfigurationException {
        
        String format = (String) this.settings.get("format",null);
        if (modeConf != null) {
            format = modeConf.getAttribute("format", format);
            // this is preferred:
            format = modeConf.getChild("format").getValue(format);
        }

        if (format==null) {
            return new Date();
        } else {
            return new SimpleDateFormat(format).format(new Date());
        }
    }


    public Iterator getAttributeNames( Configuration modeConf, Map objectModel ) throws ConfigurationException {

        return DateInputModule.returnNames.iterator();
    }


    public Object[] getAttributeValues( String name, Configuration modeConf, Map objectModel )
        throws ConfigurationException {

            List values = new LinkedList();
            values.add( this.getAttribute(name, modeConf, objectModel) );

            return values.toArray();
            
    }

}
