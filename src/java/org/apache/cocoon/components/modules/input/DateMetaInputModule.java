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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Map;

/**
 * Parses a date string according to a given format and returns a date
 * object. Configuration options: element "format" to hold a {@link
 * java.text.SimpleDateFormat} format string, child element
 * "input-module" holds InputModule to obtain the string from.
 *
 * @author <a href="mailto:haul@informatik.tu-darmstadt.de">Christian Haul</a>
 * @version CVS $Id: DateMetaInputModule.java,v 1.3 2004/03/08 13:58:30 cziegeler Exp $
 */
public class DateMetaInputModule extends AbstractMetaModule implements ThreadSafe {

    private String defaultFormat = "yyyy-MM-dd";
    private DateFormat defaultFormatter = null;

    
    public void configure(Configuration config) throws ConfigurationException {

        this.inputConf = config.getChild("input-module");
        this.defaultInput = this.inputConf.getAttribute("name",this.defaultInput);
        this.defaultFormat = this.inputConf.getAttribute("format",this.defaultFormat);
        if (this.defaultFormat != null) {
            this.defaultFormatter = new SimpleDateFormat(this.defaultFormat);
        }
    }


    public Object[] getAttributeValues( String name, Configuration modeConf, Map objectModel ) 
        throws ConfigurationException {

        if (!this.initialized) {
            this.lazy_initialize();
        }
        if (this.defaultInput == null) {
            if (getLogger().isWarnEnabled()) 
                getLogger().warn("No input module given");
        }

        // obtain correct configuration objects
        // default vs dynamic
        Configuration mConf = null;
        String inputName=null;
        String parameter=name;
        String format=this.defaultFormat;
        DateFormat formatter=null;
        if (modeConf!=null) {
            mConf       = modeConf.getChild("input-module");
            inputName   = modeConf.getChild("input-module").getAttribute("name",null);
            parameter   = modeConf.getAttribute("parameter",parameter);
            format      = modeConf.getAttribute("format",format);
            // preferred:
            parameter   = modeConf.getChild("parameter").getValue(parameter);
            format      = modeConf.getChild("format").getValue(format);
        }
        if (this.defaultFormat.equals(format)) {
            formatter = this.defaultFormatter;
        } else {
            formatter = new SimpleDateFormat(format);
        }
        
        Object[] values = getValues(parameter, objectModel, 
                                    this.input, this.defaultInput, this.inputConf,
                                    null, inputName, mConf);
        
        Object[] dates = null;
        if (values != null) {
            dates = new Object[values.length];
            for (int i=0; i<values.length; i++) 
                try {
                    dates[i] = formatter.parse(String.valueOf(values[i]));
                } catch (Exception e) {
                    if(getLogger().isWarnEnabled()) 
                        getLogger().warn("Problem: Aquired '"+values[i]+"' from '" + inputName + "' for '"
                                         +name+"' using format '"+format+"' : "+e.getMessage());
                }
        }
        return dates;
    }





    public Iterator getAttributeNames( Configuration modeConf, Map objectModel ) 
        throws ConfigurationException {

        if (!this.initialized) {
            this.lazy_initialize();
        }
        if (this.defaultInput == null) {
            if (getLogger().isWarnEnabled()) 
                getLogger().warn("No input module given");
        }

        // obtain correct configuration objects
        // default vs dynamic
        Configuration inputConfig = this.inputConf;
        Configuration mConf = null;
        String inputName=null;
        if (modeConf!=null) {
            mConf       = modeConf.getChild("input-module");
            inputName   = modeConf.getChild("input-module").getAttribute("name",null);
            if (inputName != null) {
                inputConfig = modeConf.getChild("input-module");
            }
        }

        // done reading configuration
        // setup modules and read values
        Iterator enum = getNames(objectModel,
                                 this.input, this.defaultInput, inputConfig,
                                 null, inputName, mConf);
        return enum;
     }




    public Object getAttribute( String name, Configuration modeConf, Map objectModel ) 
        throws ConfigurationException {

        Object[] values = this.getAttributeValues(name,modeConf,objectModel);
        return (values != null ? values[0] : null);
    }

}
