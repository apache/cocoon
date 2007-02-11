/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2004 The Apache Software Foundation. All rights reserved.

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

import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.thread.ThreadSafe;

/**
 * JXPathModule allows to access properties of any object in generic
 * way.  JXPath provides APIs for the traversal of graphs of
 * JavaBeans, DOM and other types of objects using the XPath
 * syntax. This is based on the AbstractJXPathModule and duplicates
 * the code since multiple inheritance is not possible. Please keep both
 * classes in sync.
 *
 * <p>Configuration example:</p>
 * <table>
 * <tr><td><code>&lt;lenient&gt;false&lt;/lenient&gt;</td>
 * <td>When set to true, non-existing attributes return null, when set to false,
 *     an exception is thrown. Default is true.</td> 
 *</tr>
 * <tr><td><code>&lt;parameter&gt;false&lt;/parameter&gt;</td>
 * <td>Attribute name to be used instead of passed attribute name.</td> 
 *</tr>
 * <tr><td><code>&lt;from-parameter&gt;false&lt;/from-parameter&gt;</td>
 * <td>Attribute name to pass to configured input module</td> 
 *</tr>
 * <tr><td><code>&lt;input-module name="request-attr"/&gt;</td>
 * <td>Uses the "request-attr" input module to obtain a value and 
 *     applies the given JXPath expression to it.</td> 
 *</tr>
 * <tr><td><code>&lt;function name="java.lang.String" prefix="str"/&gt;</td>
 * <td>Imports the class "String" as extension class to the JXPathContext using 
 * the prefix "str". Thus "str:length(xpath)" would apply the method "length" to 
 * the string object obtained from the xpath expression. Please note that the class
 * needs to be fully qualified.</td> 
 *</tr>
 * <tr><td><code>&lt;package name="java.util" prefix="util"/&gt;</td>
 * <td>Imports all classes in the package "java.util" as extension classes to the 
 * JXPathContext using the prefix "util". Thus "util:Date.new()" would create a 
 * new java.util.Date object.</td> 
 * </tr></table>
 *
 * <p>In addition, it accepts the attributes "parameter" to override
 * the attribute name and "from-parameter" to pass as attribute name
 * to the configured input module.</p>
 *
 * @author <a href="mailto:kpiroumian@apache.org">Konstantin Piroumian</a>
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version CVS $Id: JXPathMetaModule.java,v 1.6 2004/02/15 19:15:11 haul Exp $
 */
public class JXPathMetaModule extends AbstractMetaModule implements Configurable, ThreadSafe {

    /**
     * Contains all globally registered extension classes and
     * packages. Thus the lookup and loading of globally registered
     * extensions is done only once.
     *
     */
    protected JXPathHelperConfiguration configuration = null;

    /** set lenient mode for jxpath (i.e. throw an exception on
     * unsupported attributes) ? 
     */
    private static final boolean lenient = true;

    protected String parameter = "";


    public JXPathMetaModule() {
        // this value has a default in the super class
        this.defaultInput = "request-attr";
    }


    /**
     * Configure component. Preprocess list of packages and functions
     * to add to JXPath context later.
     *
     * @param config a <code>Configuration</code> value
     * @exception ConfigurationException if an error occurs
     */
    public void configure(Configuration config) throws ConfigurationException {

        this.inputConf = config.getChild("input-module");
        this.defaultInput = this.inputConf.getAttribute("name",this.defaultInput);
        this.parameter = config.getChild("parameter").getValue(this.parameter);

        this.configuration = JXPathHelper.setup(config, lenient);
    }


    public Object getAttribute(String name, Configuration modeConf, Map objectModel)
        throws ConfigurationException {

        Object contextObj = getContextObject(modeConf, objectModel);
        if (modeConf != null) { 
            name = modeConf.getChild("parameter").getValue(this.parameter != null ? this.parameter : name); 
        }
        return JXPathHelper.getAttribute(name, modeConf, this.configuration, contextObj);
    }


    public Iterator getAttributeNames(Configuration modeConf, Map objectModel)
        throws ConfigurationException {

        Object contextObj = getContextObject(modeConf, objectModel);
        return JXPathHelper.getAttributeNames(this.configuration, contextObj);
    }


    public Object[] getAttributeValues(String name, Configuration modeConf, Map objectModel)
        throws ConfigurationException {

        Object contextObj = getContextObject(modeConf, objectModel);
        if (modeConf != null) { 
            name = modeConf.getChild("parameter").getValue(this.parameter != null ? this.parameter : name); 
        }
        return JXPathHelper.getAttributeValues(name, modeConf, this.configuration, contextObj);
    }


    /**
     * Looks up object from configured InputModule. 
     *
     * @param modeConf a <code>Configuration</code> value
     * @param objectModel a <code>Map</code> value
     * @return an <code>Object</code> value
     */
    protected  Object getContextObject(Configuration modeConf, Map objectModel) throws ConfigurationException {

        if (!this.initialized) {
            this.lazy_initialize();
        }

        Configuration mConf = null;
        String inputName=null;
        String parameter = this.parameter;
        if (modeConf!=null) {
            mConf   = modeConf.getChild("input-module");
            inputName   = mConf.getAttribute("name",null);
            parameter   = modeConf.getChild("from-parameter").getValue(parameter);
        }

        if (getLogger().isDebugEnabled())
            getLogger().debug("modeConf is "+modeConf+" this.inputConf is "+this.inputConf
                              +" mConf is "+mConf+" this.input is "+this.input
                              +" this.defaultInput is "+this.defaultInput
                              +" inputName is "+inputName+" parameter is "+parameter);

        Object obj =  this.getValue(parameter, objectModel, 
                                    this.input, this.defaultInput, this.inputConf,
                                    null, inputName, mConf);
        
        if (getLogger().isDebugEnabled())
            getLogger().debug("returning an "+(obj == null ? "null" : obj.getClass().getName())+" as "+obj);

        return obj;
    }

}
