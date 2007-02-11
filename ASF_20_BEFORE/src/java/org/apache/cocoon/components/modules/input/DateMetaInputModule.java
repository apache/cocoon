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
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version CVS $Id: DateMetaInputModule.java,v 1.3 2004/02/15 21:30:00 haul Exp $
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
