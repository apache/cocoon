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

import java.util.Iterator;
import java.util.Map;

/** 
 * Meta module that obtains an Object from another module, assumes
 * that this Object implements the java.util.Map interface, and gives
 * access to the map contents. Possible use is to propagate data from
 * flow through request attributes to database actions.
 * The same can be achieved by using the {@link JXPathMetaModule}.
 *
 * <p>Configuration: "input-module", "object", "parameter"</p>
 *
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version CVS $Id: MapMetaModule.java,v 1.2 2004/02/15 21:30:00 haul Exp $
 */
public class MapMetaModule extends AbstractMetaModule implements ThreadSafe {

    protected String objectName = null;
    protected String parameter = null;


    public void configure(Configuration config) throws ConfigurationException {

        this.inputConf = config.getChild("input-module");
        this.defaultInput = this.inputConf.getAttribute("name", this.defaultInput);
        this.objectName = this.inputConf.getAttribute("object",this.objectName);
        this.parameter = this.inputConf.getAttribute("parameter",this.parameter);

        // preferred
        this.objectName = config.getChild("object").getValue(this.objectName);
        this.parameter = config.getChild("parameter").getValue(this.parameter);
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
        String inputName=null;
        String objectName = this.objectName;
        String parameter = this.parameter;
        if (modeConf != null) {
            inputName  = modeConf.getChild("input-module").getAttribute("name",null);
            objectName = modeConf.getAttribute("object",objectName);
            parameter  = modeConf.getAttribute("parameter",parameter);

            // preferred
            objectName = modeConf.getChild("object").getValue(objectName);
            parameter  = modeConf.getChild("parameter").getValue(parameter);
        }
        parameter = (parameter != null? parameter : name);

        Object value = getValue(objectName, objectModel, 
                                this.input, this.defaultInput, this.inputConf, 
                                null, inputName, modeConf);

        value = (value!=null? ((Map) value).get(parameter) : null);

        return value;        
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
        Configuration inputConfig = null;
        String inputName=null;
        String objectName = this.objectName;
        if (modeConf!=null) {
            inputName   = modeConf.getChild("input-module").getAttribute("name",null);
            objectName = modeConf.getAttribute("object",this.objectName);

            // preferred
            objectName = modeConf.getChild("object").getValue(objectName);
            if (inputName != null) {
                inputConfig = modeConf.getChild("input-module");
            }
        }

        Iterator keys = ((Map) getValue(objectName, objectModel, 
                                        this.input, this.defaultInput, this.inputConf,
                                        null, inputName, inputConfig)).keySet().iterator();

        return keys;        
   }




    public Object[] getAttributeValues( String name, Configuration modeConf, Map objectModel ) 
        throws ConfigurationException {

        Object[] values = new Object[1];
        values[0] = this.getAttribute(name, modeConf, objectModel);
        return (values[0]!=null?values:null);
    }

}
