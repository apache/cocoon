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
package org.apache.cocoon.transformation.helpers;

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfigurationSerializer;
import org.apache.avalon.framework.parameters.Parameters;
import org.xml.sax.SAXException;

/**
 * An Avalon <code>Configuration</code> factory that allows {variables} to be
 * replaced with values from a lookup table.
 *
 * @author <a href="jefft@apache.org">Jeff Turner</a>
 * @version CVS $Id: VariableConfiguration.java,v 1.2 2003/03/11 17:44:21 vgritsenko Exp $
 */
public class VariableConfiguration {
    public static final String UNSET_VAR="unset";
    private Configuration conf;
    private Map vars = new HashMap();

    /** Constructor.
     * @param conf Template Configuration with {variables} to marking where
     * values should be interpolated.  May be <code>null</code>.
     */
    public VariableConfiguration(Configuration conf) {
        this.conf = conf;
    }

    /** Add a name-value pair.
     */
    public void addVariable(String name, String value) {
        vars.put(name, value);
    }

    /** Add a set of name-value pairs.
     */
    public void addVariables(Parameters params) {
        String[] names = params.getNames();
        for (int i=0; i<names.length; i++) {
            String paramVal = params.getParameter(names[i], null);
            if (paramVal != null) {
                vars.put(names[i], paramVal);
            }
        }
    }

    /**
     * Get a generated Configuration with interpolated variable values.
     * @return The Configuration passed in the constructor, with {variable}
     * tokens in attributes and element bodies replaced with values (if
     * specified), or <code>null</code>.
     */
    public Configuration getConfiguration() throws SAXException, ConfigurationException {

        if (this.conf == null) return null;
        InterpolatingConfigurationHandler handler = new InterpolatingConfigurationHandler(this.vars);
        DefaultConfigurationSerializer ser = new DefaultConfigurationSerializer();
        ser.serialize(handler, this.conf);
        return handler.getConfiguration();
    }

}
