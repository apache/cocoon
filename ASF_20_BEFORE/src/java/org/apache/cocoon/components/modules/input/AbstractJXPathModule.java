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

import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

/**
 * JXPathModule allows to access properties of any object in generic
 * way.  JXPath provides APIs for the traversal of graphs of
 * JavaBeans, DOM and other types of objects using the XPath
 * syntax. JXPathMetaModule is based on this class and duplicates
 * the code since multiple inheritance is not possible. Please keep both
 * classes in sync.
 *
 * <p>Configuration example:</p>
 * <table>
 * <tr><td><code>&lt;lenient&gt;false&lt;/lenient&gt;</td>
 * <td>When set to true, non-existing attributes return null, when set to false,
 *     an exception is thrown. Default is true.</td> 
 *</tr>
 * <tr><td><code>&lt;parameter&gt;foo&lt;/parameter&gt;</td>
 * <td>When set overrides attribute name passed to module.</td> 
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
 * @author <a href="mailto:kpiroumian@apache.org">Konstantin Piroumian</a>
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version CVS $Id: AbstractJXPathModule.java,v 1.3 2004/02/15 19:12:44 haul Exp $
 */
public abstract class AbstractJXPathModule extends AbstractInputModule {

    protected JXPathHelperConfiguration configuration = null;

    private static final boolean lenient = true; 

    /** override attribute name */
    protected String parameter = null;

    /**
     * Configure component. Preprocess list of packages and functions
     * to add to JXPath context later.
     *
     * @param config a <code>Configuration</code> value
     * @exception ConfigurationException if an error occurs
     */
    public void configure(Configuration config) throws ConfigurationException {

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
     * Returns the object which should be used as JXPath context.
     * Descendants should override this method to return a specific object
     * that is requried by the implementing class.
     * Examples are: request, session and application context objects.
     */
    protected abstract Object getContextObject(Configuration modeConf,
                                               Map objectModel) throws ConfigurationException;
}
