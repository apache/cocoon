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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/** Meta module that obtains values from an other module and by
 * replacing the requested attribute name with another name. This is
 * done first through a replacement table and may additionally prepend
 * or append a string. Replacement works in both ways, it is applied
 * to the returned attribute names as well.
 *
 * <p>Example configuration:<pre>
 * &lt;prefix&gt;cocoon.&lt;/prefix&gt;
 * &lt;suffix&gt;.attr&lt;/suffix&gt;
 * &lt;mapping in="foo" out="bar"/&gt;
 * &lt;mapping in="yuk" out="yeeha"/&gt;
 *</pre>
 *
 * Will map a parameter "foo" to the real one named
 * "cocoon.bar.attr". If parameters "coocoon.yeeha.attr" and
 * "shopping.cart" exist, the iterator will return
 * "yeeha". "shopping.cart" does not contain the pre-/ suffix and thus
 * is dropped.</p> 
 *
 * <p>Similarily, rm-prefix and rm-suffix will be removed from the
 * attribute name.</p>
 *
 * @author <a href="mailto:haul@informatik.tu-darmstadt.de">Christian Haul</a>
 * @version CVS $Id: SimpleMappingMetaModule.java,v 1.3 2004/02/15 19:09:38 haul Exp $
 */
public class SimpleMappingMetaModule extends AbstractMetaModule implements ThreadSafe {

    String prefix = null;
    String suffix = null;
    String rmPrefix = null;
    String rmSuffix = null;
    Mapping mapping = null;

    protected class Mapping {
        Map toMap = null;
        Map fromMap = null;

        public Mapping() {
        }       

        public Mapping(Map to, Map from) {
            this.toMap = to;
            this.fromMap = from;
        }

        public Mapping(Configuration config) throws ConfigurationException {
            Configuration[] mappings = config.getChildren("mapping");
            if (mappings!=null) {
                if (this.toMap == null) this.toMap = new HashMap();
                if (this.fromMap == null) this.fromMap = new HashMap();
                for (int i=0; i < mappings.length; i++){
                    String in = mappings[i].getAttribute("in",null);
                    String out = mappings[i].getAttribute("out",null);
                    if (in != null && out != null) {
                        this.toMap.put(in,out);
                        this.fromMap.put(out,in);
                    }
                }
            }
        }

        private String mapIt(Map map, String param) {
            Object newParam = param;
            if (map != null) {
                newParam = map.get(param);
                if (!map.containsKey(param) || newParam == null)
                    newParam = param;
            }
            return (String) newParam;
        }

        public String mapFrom(String param) {
            return this.mapIt(this.fromMap, param);
        }

        public String mapTo(String param) {
            return this.mapIt(this.toMap, param);
        }
    }


    public void configure(Configuration config) throws ConfigurationException {

        // It seems that even if there is no config, we'll get an empty
        // input-module element here, so it will never be null (JT)
        this.inputConf = config.getChild("input-module");
        this.defaultInput = this.inputConf.getAttribute("name", this.defaultInput);
        this.prefix = config.getChild("prefix").getValue(null);
        this.suffix = config.getChild("suffix").getValue(null);
        this.rmPrefix = config.getChild("rm-prefix").getValue(null);
        this.rmSuffix = config.getChild("rm-suffix").getValue(null);
        this.mapping = new Mapping(config);
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

        Configuration inputConfig = null;
        String inputName=null;
        Mapping mapping = this.mapping;
        String prefix = this.prefix;
        String suffix = this.suffix;
        String rmPrefix = this.rmPrefix;
        String rmSuffix = this.rmSuffix;

        if (modeConf!=null && modeConf.getChildren().length > 0) {
            inputName   = modeConf.getChild("input-module").getAttribute("name",null);
            if (inputName != null) {
                inputConfig = modeConf.getChild("input-module");
            }
            mapping = new Mapping(modeConf);
            prefix = modeConf.getChild("prefix").getValue(null);
            suffix = modeConf.getChild("suffix").getValue(null);
            rmPrefix = modeConf.getChild("rm-prefix").getValue(null);
            rmSuffix = modeConf.getChild("rm-suffix").getValue(null);
        }
        
        // remove rm-prefix and rm-suffix
        if (rmPrefix != null && name.startsWith(rmPrefix)) {
            name = name.substring(rmPrefix.length());
        }
        if (rmSuffix != null && name.endsWith(rmSuffix)) {
            name = name.substring(0,name.length() - rmSuffix.length());
        }
        // map
        String param = mapping.mapTo(name);
        // add prefix and suffix
        if (prefix != null) param = prefix + param;
        if (suffix != null) param = param + suffix;
        if (getLogger().isDebugEnabled())
            getLogger().debug("mapping ['"+name+"'] to ['"+param+"']");

        Object res = getValue(param, objectModel,
                              this.input, this.defaultInput, this.inputConf,
                              null, inputName, inputConfig);
        
        if (getLogger().isDebugEnabled())
            getLogger().debug("getting for real attribute ['"+param+"'] value: "+res);

        return res;
    }





    public Object[] getAttributeValues( String name, Configuration modeConf, Map objectModel ) 
        throws ConfigurationException {

        if (!this.initialized) {
            this.lazy_initialize();
        }
        if (this.defaultInput == null) {
            if (getLogger().isWarnEnabled()) 
                getLogger().warn("No input module given. FAILING");
            return null;
        }

        Configuration inputConfig = null;
        String inputName=null;
        Mapping mapping = this.mapping;
        String prefix = this.prefix;
        String suffix = this.suffix;
        String rmPrefix = this.rmPrefix;
        String rmSuffix = this.rmSuffix;

        if (modeConf!=null && modeConf.getChildren().length > 0) {
            inputName   = modeConf.getChild("input-module").getAttribute("name",null);
            if (inputName != null) {
                inputConfig = modeConf.getChild("input-module");
            }
            mapping = new Mapping(modeConf);
            prefix = modeConf.getChild("prefix").getValue(null);
            suffix = modeConf.getChild("suffix").getValue(null);
            rmPrefix = modeConf.getChild("rm-prefix").getValue(null);
            rmSuffix = modeConf.getChild("rm-suffix").getValue(null);
        }
        
        // remove rm-prefix and rm-suffix
        if (rmPrefix != null && name.startsWith(rmPrefix)) {
            name = name.substring(rmPrefix.length());
        }
        if (rmSuffix != null && name.endsWith(rmSuffix)) {
            name = name.substring(0,name.length() - rmSuffix.length());
        }
        // map
        String param = mapping.mapTo(name);
        // add prefix and suffix
        if (prefix != null) param = prefix + param;
        if (suffix != null) param = param + suffix;
        if (getLogger().isDebugEnabled())
            getLogger().debug("mapping ['"+name+"'] to ['"+param+"']");

        Object[] res = getValues(param, objectModel,
                                 this.input, this.defaultInput, this.inputConf,
                                 null, inputName, inputConfig);
        if (getLogger().isDebugEnabled())
            getLogger().debug("getting for real attribute ['"+param+"'] value: "+res);

        return res;
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

        Configuration inputConfig = null;
        String inputName=null;
        Mapping mapping = this.mapping;
        String prefix = this.prefix;
        String suffix = this.suffix;
        String rmPrefix = this.rmPrefix;
        String rmSuffix = this.rmSuffix;
        if (modeConf!=null && modeConf.getChildren().length > 0) {
            inputName   = modeConf.getChild("input-module").getAttribute("name",null);
            if (inputName != null) {
                inputConfig = modeConf.getChild("input-module");
            }
            mapping = new Mapping(modeConf);
            prefix = modeConf.getChild("prefix").getValue(null);
            suffix = modeConf.getChild("suffix").getValue(null);
            rmPrefix = modeConf.getChild("rm-prefix").getValue(null);
            rmSuffix = modeConf.getChild("rm-suffix").getValue(null);
        }
        
        Iterator names = getNames(objectModel, 
                                  this.input, this.defaultInput, this.inputConf, 
                                  null, inputName, inputConfig);

        Set set = new HashSet();
        while (names.hasNext()) {
            String param = (String) names.next();
            if (getLogger().isDebugEnabled())
                getLogger().debug("reverse mapping starts with ['"+param+"']");
            if (prefix != null) 
                if (param.startsWith(prefix))
                    param = param.substring(prefix.length());
                else 
                    continue; // prefix is set but parameter does not start with it.
            
            //if (getLogger().isDebugEnabled())
            //    getLogger().debug("reverse mapping after remove prefix ['"+param+"']");

            if (suffix != null)
                if (param.endsWith(suffix))
                    param = param.substring(0,param.length() - suffix.length());
                else 
                    continue; // suffix is set but parameter does not end with it.

            //if (getLogger().isDebugEnabled())
            //    getLogger().debug("reverse mapping after remove suffix ['"+param+"']");

            if (param.length() < 1)
                continue; // nothing left

            String newName = mapping.mapFrom(param);

            if (rmPrefix != null) newName = rmPrefix + newName;
            if (rmSuffix != null) newName = newName + rmSuffix;

            if (getLogger().isDebugEnabled())
                getLogger().debug("reverse mapping results in ['"+newName+"']");

            set.add(newName);
        }

        return set.iterator();

    }

}
