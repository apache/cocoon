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

import org.apache.cocoon.util.JDBCTypeConversions;

import java.util.ArrayList;
import java.util.Map;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Constructs an array of values suitable for a JDBC collection type
 * from parameters obtained from another input module. Application is
 * not limited to JDBC collections but can be used wherever similar
 * named attributes shall be collected to an array of a given
 * type. Currently, long, int, and string are known, more to come.
 *
 * <p><b>Global and local configuration</b></p>
 * <table border="1">
 * <tr><td><code>input-module</code></td><td>Name of the input module used to obtain the value and its configuration</td></tr>
 * <tr><td><code>member</code></td>      <td>Collection member <table
 *                                           <tr><td>Attribute</td><td></td></tr>
 *                                           <tr><td>name</td><td>Parameter name, "*" may distinguish multiple collections</td></tr>
 *                                           <tr><td>type</td><td>JDBC type name of members</td></tr>
 *                                           </table>                       </td></tr>
 * </table>
 *
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version CVS $Id: CollectionMetaModule.java,v 1.3 2004/03/05 13:01:55 bdelacretaz Exp $
 */
public class CollectionMetaModule extends AbstractMetaModule implements ThreadSafe {

    protected Configuration memberConf = null;


    public void configure(Configuration config) throws ConfigurationException {

        this.memberConf = config.getChild("member");
        this.inputConf = config.getChild("input-module");
        this.defaultInput = this.inputConf.getAttribute("name",this.defaultInput);
    }


    public Object getAttribute( String name, Configuration modeConf, Map objectModel ) 
        throws ConfigurationException {

        if (!this.initialized) {
            this.lazy_initialize();
        }
        if (this.defaultInput == null) {
            if (getLogger().isWarnEnabled()) 
                getLogger().warn("No input module given. FAILING");
            return null;
        }

        // obtain correct configuration objects
        // default vs dynamic
        Configuration mConf = this.memberConf;
        Configuration inputConfig = null;
        String inputName=null;
        if (modeConf!=null) {
            mConf       = modeConf.getChild("member");
            inputName   = modeConf.getChild("input-module").getAttribute("name",null);
            if (inputName != null) {
                inputConfig = modeConf.getChild("input-module");
            }
        }

        // read necessary parameters
        // name is used only if parameter name contains '*'
        // in that case it replaces '*' otherwise it is
        // ignored
        String jType = mConf.getAttribute("type","string");
        String pName = mConf.getAttribute("name");
        int index = pName.indexOf("*");
        if (index>-1) {
            String prefix = (index > 0 ? pName.substring(0,index) : null);
            String suffix = (index < (pName.length() -1) ? pName.substring(index+1,pName.length()) : null);
            pName = prefix+name+suffix;
        }

        getLogger().debug("jType "+jType);

        Object[] values = getValues(pName, objectModel,
                                    this.input, this.defaultInput, this.inputConf,
                                    null, inputName, inputConfig);
        Object[] objects = null;

        if (values != null) {
            
            objects = new Object[values.length];
            
            for (int i = 0; i<values.length; i++) {
                Object value = values[i];
                objects[i] = JDBCTypeConversions.convert(value, jType);
            }
        }

        return objects;
    }


    public Iterator getAttributeNames( Configuration modeConf, Map objectModel )
        throws ConfigurationException {

        if (!this.initialized) {
            this.lazy_initialize();
        }
        if (this.defaultInput == null) {
            if (getLogger().isWarnEnabled()) 
                getLogger().warn("No input module given. FAILING");
            return null;
        }

        // obtain correct configuration objects
        // default vs dynamic

        Configuration mConf = this.memberConf;
        Configuration inputConfig = null;
        String inputName=null;
        if (modeConf!=null) {
            mConf       = modeConf.getChild("member");
            inputName   = modeConf.getChild("input-module").getAttribute("name",null);
            if (inputName != null) {
                inputConfig = modeConf.getChild("input-module");
            }
        }

        Iterator names = getNames(objectModel,
                                  this.input, this.defaultInput, this.inputConf,
                                  null, inputName, inputConfig);

        if (names != null) {
            SortedSet matchset = new TreeSet();
            String pName = mConf.getAttribute("name");
            int index = pName.indexOf("*");

            if (index>-1) {
                // parameter name contains '*'
                // find all strings that match this '*'
                // return them in an enumeration

                String prefix = (index > 0 ? pName.substring(0,index) : null);
                String suffix = (index < (pName.length() -1) ? pName.substring(index+1,pName.length()) : null);
                
                while (names.hasNext()) {
                    String name = (String)names.next();
                    if (name.startsWith(prefix) && name.endsWith(suffix)) {
                        String wildcard = name.substring(prefix.length());
                        wildcard = wildcard.substring(0,wildcard.length()-suffix.length());
                        matchset.add(wildcard);
                    }
                }
            } else {
                // parameter name without wildcard
                // check that name is among available names
                // and return it in that case
                boolean done=false;
                while (!done && names.hasNext()) {
                    String name = (String)names.next();
                    if (name.equals(pName)) {
                        matchset.add(pName);
                        done = true;
                    }
                }
            }
            return matchset.iterator();
        } else {
            return null;
        }
    }


    public Object[] getAttributeValues( String name, Configuration modeConf, Map objectModel )
        throws ConfigurationException {

        Iterator names = this.getAttributeNames( modeConf, objectModel );
        ArrayList values = new ArrayList();
        while (names.hasNext()) {
            values.add(this.getAttribute((String) names.next(),modeConf,objectModel));
        }

        return values.toArray();

    }
}
