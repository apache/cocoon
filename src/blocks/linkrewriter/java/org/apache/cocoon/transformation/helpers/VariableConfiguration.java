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
 * @version CVS $Id: VariableConfiguration.java,v 1.4 2004/03/05 13:01:59 bdelacretaz Exp $
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
        InterpolatingConfigurationHandler handler = new InterpolatingConfigurationHandler(this.vars, this.conf.getLocation());
        DefaultConfigurationSerializer ser = new DefaultConfigurationSerializer();
        ser.serialize(handler, this.conf);
        return handler.getConfiguration();
    }

}
