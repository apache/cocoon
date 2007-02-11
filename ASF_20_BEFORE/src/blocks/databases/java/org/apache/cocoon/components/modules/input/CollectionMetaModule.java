/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

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
 * @version CVS $Id: CollectionMetaModule.java,v 1.2 2004/02/15 21:30:00 haul Exp $
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
