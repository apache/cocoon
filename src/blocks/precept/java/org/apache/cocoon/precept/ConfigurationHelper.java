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

package org.apache.cocoon.precept;

import org.apache.avalon.framework.configuration.Configuration;

/**
 * @author Torsten Curdt <tcurdt@dff.st>
 * @since Mar 26, 2002
 * @version CVS $Id: ConfigurationHelper.java,v 1.3 2004/03/05 13:02:18 bdelacretaz Exp $
 */
public class ConfigurationHelper {

    public final static String toString(Configuration conf) {
        String result = "<" + conf.getName();
        String[] attributes = conf.getAttributeNames();

        for (int i = 0; i < attributes.length; i++) {
            try {
                result += " " + attributes[i] + "=\"" + conf.getAttribute(attributes[i]) + "\"";
            }
            catch (Throwable t) {
            }
        }

        result += ">";

        try {
            result += conf.getValue();
        }
        catch (Throwable t) {
        }

        Configuration[] childs = conf.getChildren();
        for (int i = 0; i < childs.length; i++) {
            Configuration child = childs[i];
            result += toString(child);
        }

        return (result + "</" + conf.getName() + ">");
    }
}
