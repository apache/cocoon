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
package org.apache.cocoon.precept.preceptors.easyrelax.constraints;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.thread.SingleThreaded;

import org.apache.cocoon.precept.Context;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * @author Torsten Curdt <tcurdt@dff.st>
 * @since Mar 21, 2002
 * @version CVS $Id: ChoiceConstraint.java,v 1.4 2004/03/05 13:02:19 bdelacretaz Exp $
 */
public class ChoiceConstraint extends AbstractConstraint implements Configurable, SingleThreaded {

    public Collection validValues = new ArrayList();
    public Map validValuesDescription = new HashMap();

    public void configure(Configuration configuration) throws ConfigurationException {
        id = configuration.getAttribute("name");

        getLogger().debug("configuring constraint [" + String.valueOf(id) + "]");

        if (validValues.size() == 0) {
            Configuration[] choices = configuration.getChildren("choice");
            for (int i = 0; i < choices.length; i++) {
                Configuration choice = choices[i];
                String value = choice.getAttribute("value");
                String valueDescription = choice.getValue();

                getLogger().debug("registered choice [" + String.valueOf(value) + "] = [" + String.valueOf(valueDescription) + "]");

                validValues.add(value);
                validValuesDescription.put(value, valueDescription);
            }
        }
    }

    public boolean isSatisfiedBy(Object value, Context context) {
        boolean isValid = validValues.contains(value);
        getLogger().debug("checking choice [" + String.valueOf(value)
                          + "] contains [" + String.valueOf(validValues)
                          + "] is " + isValid);
        return (isValid);
    }

    public String getId() {
        return (id);
    }

    public String getType() {
        return ("choice");
    }

    public String toString() {
        return (String.valueOf(getType()) + "[" + String.valueOf(getId()) + "] -> [" + String.valueOf(validValues) + "]");
    }

    public void toSAX(ContentHandler handler) throws SAXException {
        AttributesImpl attributes = new AttributesImpl();
        attributes.addAttribute("", "type", "type", "CDATA", getType());
        attributes.addAttribute("", "name", "name", "CDATA", id);

        handler.startElement("", "constraint", "constraint", attributes);
        for (Iterator it = validValues.iterator(); it.hasNext();) {
            String value = (String) it.next();
            String description = (String) validValuesDescription.get(value);

            AttributesImpl choiceAttributes = new AttributesImpl();
            choiceAttributes.addAttribute("", "value", "value", "CDATA", value);

            handler.startElement("", "choice", "choice", choiceAttributes);
            handler.characters(description.toCharArray(), 0, description.length());
            handler.endElement("", "choice", "choice");
        }
        handler.endElement("", "constraint", "constraint");
    }

}
