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

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.thread.SingleThreaded;

import org.apache.cocoon.precept.Context;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * @author Torsten Curdt <tcurdt@dff.st>
 * @since Mar 21, 2002
 * @version CVS $Id: RegexprConstraint.java,v 1.4 2004/03/05 13:02:19 bdelacretaz Exp $
 */
public class RegexprConstraint extends AbstractConstraint implements Configurable, SingleThreaded {
    private String expressionString;
    private RE expression;

    public void configure(Configuration configuration) throws ConfigurationException {
        id = configuration.getAttribute("name");
        expressionString = configuration.getValue();
        getLogger().debug("expression [" + String.valueOf(expressionString) + "]");
        try {
            expression = new RE(expressionString);
        }
        catch (RESyntaxException e) {
            throw new ConfigurationException("", e);
        }
    }

    public boolean isSatisfiedBy(Object value, Context context) {
        boolean isValid = expression.match(String.valueOf(value));
        getLogger().debug("checking regexpr [" + String.valueOf(value)
                          + "] matches [" + String.valueOf(expressionString)
                          + "] is " + isValid);
        return (isValid);
    }

    public String getId() {
        return (id);
    }

    public String getType() {
        return ("regexpr");
    }

    public String toString() {
        return (String.valueOf(getType()) + "[" + String.valueOf(getId())
                + "] -> [" + String.valueOf(expressionString) + "]");
    }

    public void toSAX(ContentHandler handler) throws SAXException {
    }
}
