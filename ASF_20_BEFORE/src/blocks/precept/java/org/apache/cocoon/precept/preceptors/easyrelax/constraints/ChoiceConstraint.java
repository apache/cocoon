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
 * @version CVS $Id: ChoiceConstraint.java,v 1.3 2003/11/20 17:11:02 joerg Exp $
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
